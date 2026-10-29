package com.mycompany.bankautomata.service;

import com.mycompany.bankautomata.data.MemoriaKartyaTar;
import com.mycompany.bankautomata.cash.Cimlet;
import com.mycompany.bankautomata.cash.CimletKazetta;
import com.mycompany.bankautomata.cash.KifizetesiTerv;
import com.mycompany.bankautomata.cash.VisszalepesesCimletezo;
import com.mycompany.bankautomata.kivetel.IsmeretlenKartya;
import com.mycompany.bankautomata.kivetel.NapiLimit;
import com.mycompany.bankautomata.kivetel.NincsElegKeszpenz;
import com.mycompany.bankautomata.kivetel.NincsFedezet;
import com.mycompany.bankautomata.model.Kartya;
import com.mycompany.bankautomata.model.Szamla;
import com.mycompany.bankautomata.journal.Tranzakcio;
import com.mycompany.bankautomata.journal.TranzakcioNaplo;
import com.mycompany.bankautomata.journal.TranzakcioTipus;
import com.mycompany.bankautomata.rule.NapiLimitSzabaly;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the Atm together with its real parts, but with a fake log
 * (a list in memory, or one that always fails).
 */
class AtmTest {

    private static final String GAZDAG = "1111-2222-3333-4444"; // 5000 lei
    private static final String SZEGENY = "5555-6666-7777-8888"; // 300 lei

    private final Clock ora = Clock.fixed(Instant.parse("2026-10-29T10:00:00Z"), ZoneId.of("UTC"));
    private final List<Tranzakcio> naplozott = new ArrayList<>();
    private final List<String> figyelmeztetesek = new ArrayList<>();
    private CimletKazetta kazetta;

    /** Builds an ATM with 1800 lei in cash, a 2000 lei daily limit and the given log. */
    private Atm atm(TranzakcioNaplo naplo) {
        MemoriaKartyaTar kartyak = new MemoriaKartyaTar()
                .hozzaad(new Kartya(GAZDAG, "1234", new Szamla("RO01", 5_000)))
                .hozzaad(new Kartya(SZEGENY, "4321", new Szamla("RO02", 300)));
        Map<Cimlet, Integer> keszlet = new EnumMap<>(Cimlet.class);
        keszlet.put(Cimlet.KETSZAZ, 5);
        keszlet.put(Cimlet.SZAZ, 5);
        keszlet.put(Cimlet.OTVEN, 4);
        keszlet.put(Cimlet.TIZ, 10);
        kazetta = new CimletKazetta(keszlet, new VisszalepesesCimletezo());
        return new Atm(kartyak, kazetta, List.of(new NapiLimitSzabaly(2_000, ora)),
                naplo, ora, figyelmeztetesek::add);
    }

    private Atm atm() {
        return atm(naplozott::add); // fake log: just collects the entries in a list
    }

    @Test
    void ismeretlenKartya_IsmeretlenKartyaKivetel() {
        Atm atm = atm();

        assertThrows(IsmeretlenKartya.class, () -> atm.bejelentkezik("9999-9999-9999-9999", "1234"));
    }

    @Test
    void sikeresKifizetes_terheliASzamlat_esNaplozodik() throws Exception {
        Atm atm = atm();
        Munkamenet m = atm.bejelentkezik(GAZDAG, "1234");

        KifizetesiTerv terv = atm.kivesz(m, 450);

        assertEquals(450, terv.osszeg());
        assertEquals(4_550, atm.egyenleg(m));
        assertTrue(naplozott.stream().anyMatch(t ->
                t.tipus() == TranzakcioTipus.KIFIZETES && t.sikeres() && t.osszeg() == 450));
    }

    @Test
    void naplozasHibaja_nemAkasztjaMegAKifizetest() throws Exception {
        TranzakcioNaplo rosszNaplo = t -> {
            throw new IOException("A lemez megtelt");
        };
        Atm atm = atm(rosszNaplo);
        Munkamenet m = atm.bejelentkezik(GAZDAG, "1234");

        KifizetesiTerv terv = assertDoesNotThrow(() -> atm.kivesz(m, 450));

        assertEquals(450, terv.osszeg());        // the money was paid out
        assertEquals(4_550, atm.egyenleg(m));    // and the account was charged
        assertFalse(figyelmeztetesek.isEmpty()); // only a warning about the log
    }

    @Test
    void nincsElegKeszpenz_aSzamlatNemTerheli() throws Exception {
        Atm atm = atm();
        Munkamenet m = atm.bejelentkezik(GAZDAG, "1234");

        // 1900 is within the daily limit and the balance, but the ATM only has 1800
        assertThrows(NincsElegKeszpenz.class, () -> atm.kivesz(m, 1_900));

        assertEquals(5_000, atm.egyenleg(m));
        assertEquals(1_800, kazetta.osszesKeszpenz());
    }

    @Test
    void nincsFedezet_aKazettaValtozatlan() throws Exception {
        Atm atm = atm();
        Munkamenet m = atm.bejelentkezik(SZEGENY, "4321");

        NincsFedezet e = assertThrows(NincsFedezet.class, () -> atm.kivesz(m, 500));

        assertEquals(200, e.getHiany());
        assertEquals(1_800, kazetta.osszesKeszpenz());
    }

    @Test
    void napiLimit_azAtmenKeresztulIsErvenyes() throws Exception {
        Atm atm = atm();
        Munkamenet m = atm.bejelentkezik(GAZDAG, "1234");
        atm.kivesz(m, 1_500);

        assertThrows(NapiLimit.class, () -> atm.kivesz(m, 600));
        assertEquals(3_500, atm.egyenleg(m)); // the rejected 600 was not charged
    }

    @Test
    void sikertelenKifizetesIsNaplozodik() throws Exception {
        Atm atm = atm();
        Munkamenet m = atm.bejelentkezik(SZEGENY, "4321");

        assertThrows(NincsFedezet.class, () -> atm.kivesz(m, 500));

        assertTrue(naplozott.stream().anyMatch(t ->
                t.tipus() == TranzakcioTipus.KIFIZETES && !t.sikeres()));
    }
}