package com.mycompany.bankautomata.service;

import com.mycompany.bankautomata.data.KartyaTar;
import com.mycompany.bankautomata.cash.Cimlet;
import com.mycompany.bankautomata.cash.KifizetesiTerv;
import com.mycompany.bankautomata.cash.PenzKazetta;
import com.mycompany.bankautomata.kivetel.AtmKivetel;
import com.mycompany.bankautomata.kivetel.IsmeretlenKartya;
import com.mycompany.bankautomata.model.Kartya;
import com.mycompany.bankautomata.journal.Tranzakcio;
import com.mycompany.bankautomata.journal.TranzakcioNaplo;
import com.mycompany.bankautomata.journal.TranzakcioTipus;
import com.mycompany.bankautomata.rule.KivetSzabaly;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * The ATM itself. It doesn't do the work alone - it coordinates the parts:
 * card storage, cash cassette, rules and log. Every dependency comes in through
 * the constructor as an interface (Dependency Injection), so each one can be
 * replaced - e.g. with a fake in the tests.
 */
public class Atm implements AtmSzolgaltatas {

    private static final int LEGKISEBB_CIMLET = Cimlet.TIZ.ertek();

    private final KartyaTar kartyaTar;
    private final PenzKazetta kazetta;
    private final List<KivetSzabaly> szabalyok;
    private final TranzakcioNaplo naplo;
    private final Clock ora;
    private final Consumer<String> figyelmeztetes;

    /**
     * @param figyelmeztetes where non-fatal warnings go (e.g. "logging failed");
     *                       the console passes a method that prints them
     */
    public Atm(KartyaTar kartyaTar, PenzKazetta kazetta, List<KivetSzabaly> szabalyok,
               TranzakcioNaplo naplo, Clock ora, Consumer<String> figyelmeztetes) {
        this.kartyaTar = Objects.requireNonNull(kartyaTar, "kartyaTar");
        this.kazetta = Objects.requireNonNull(kazetta, "kazetta");
        this.szabalyok = List.copyOf(szabalyok);
        this.naplo = Objects.requireNonNull(naplo, "naplo");
        this.ora = Objects.requireNonNull(ora, "ora");
        this.figyelmeztetes = Objects.requireNonNull(figyelmeztetes, "figyelmeztetes");
    }

    @Override
    public Munkamenet bejelentkezik(String kartyaSzam, String pin) throws AtmKivetel {
        String maszk = Kartya.maszkol(kartyaSzam);
        try {
            Kartya kartya = kartyaTar.keres(kartyaSzam)
                    .orElseThrow(() -> new IsmeretlenKartya(maszk));
            kartya.ellenorizPin(pin);
            naplozBiztonsagosan(maszk, TranzakcioTipus.BEJELENTKEZES, 0, true, "");
            return new Munkamenet(kartya);
        } catch (AtmKivetel e) {
            naplozBiztonsagosan(maszk, TranzakcioTipus.BEJELENTKEZES, 0, false, e.getMessage());
            throw e; // log it, then pass it on to the UI
        }
    }

    @Override
    public long egyenleg(Munkamenet munkamenet) {
        Kartya kartya = kartya(munkamenet);
        long egyenleg = kartya.getSzamla().getEgyenleg();
        naplozBiztonsagosan(kartya.getMaszkoltSzam(), TranzakcioTipus.EGYENLEG, 0, true, "");
        return egyenleg;
    }

    @Override
    public void befizet(Munkamenet munkamenet, long osszeg) {
        Kartya kartya = kartya(munkamenet);
        try {
            if (osszeg % LEGKISEBB_CIMLET != 0) {
                throw new IllegalArgumentException(
                        "Csak " + LEGKISEBB_CIMLET + " lej tobbszorose fizetheto be.");
            }
            // Szamla.befizet() rejects zero / negative amounts by itself.
            // (Deposited notes go to a separate deposit box, not into the payout cassette.)
            kartya.getSzamla().befizet(osszeg);
            naplozBiztonsagosan(kartya.getMaszkoltSzam(), TranzakcioTipus.BEFIZETES, osszeg, true, "");
        } catch (IllegalArgumentException e) {
            naplozBiztonsagosan(kartya.getMaszkoltSzam(), TranzakcioTipus.BEFIZETES, osszeg, false, e.getMessage());
            throw e;
        }
    }

    /**
     * The order matters: everything that can fail is checked BEFORE any money moves.
     *   1. rules (daily limit)        - only checks
     *   2. account balance            - only checks
     *   3. cash plan from the cassette - only checks
     *   4. charge the account         - from here on nothing business-related can fail
     *   5. hand out the notes
     *   6. record it in the rules (e.g. daily total)
     *   7. log - a logging failure does NOT undo or block the payout
     */
    @Override
    public KifizetesiTerv kivesz(Munkamenet munkamenet, long osszeg) throws AtmKivetel {
        Kartya kartya = kartya(munkamenet);
        String maszk = kartya.getMaszkoltSzam();
        try {
            if (osszeg <= 0) {
                throw new IllegalArgumentException("Az osszegnek pozitivnak kell lennie.");
            }
            for (KivetSzabaly szabaly : szabalyok) {
                szabaly.ellenoriz(kartya, osszeg);
            }
            kartya.getSzamla().ellenorizFedezet(osszeg);
            KifizetesiTerv terv = kazetta.tervez(osszeg);

            kartya.getSzamla().kivesz(osszeg);
            kazetta.kiad(terv);
            for (KivetSzabaly szabaly : szabalyok) {
                szabaly.rogzit(kartya, osszeg);
            }

            naplozBiztonsagosan(maszk, TranzakcioTipus.KIFIZETES, osszeg, true, terv.toString());
            return terv;
        } catch (AtmKivetel | IllegalArgumentException e) {
            naplozBiztonsagosan(maszk, TranzakcioTipus.KIFIZETES, osszeg, false, e.getMessage());
            throw e;
        }
    }

    private static Kartya kartya(Munkamenet munkamenet) {
        return Objects.requireNonNull(munkamenet, "munkamenet").getKartya();
    }

    /**
     * Logs the transaction, but never throws: if logging fails, only a warning
     * is shown. This is what keeps a broken log file from blocking a payout.
     */
    private void naplozBiztonsagosan(String kartya, TranzakcioTipus tipus, long osszeg,
                                     boolean sikeres, String megjegyzes) {
        Tranzakcio tranzakcio = new Tranzakcio(LocalDateTime.now(ora), kartya, tipus,
                osszeg, sikeres, megjegyzes);
        try {
            naplo.naploz(tranzakcio);
        } catch (IOException | RuntimeException e) {
            figyelmeztetes.accept("Figyelem: a tranzakciot nem sikerult naplozni ("
                    + e.getMessage() + ").");
        }
    }
}