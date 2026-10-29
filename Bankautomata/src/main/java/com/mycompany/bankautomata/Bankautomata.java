package com.mycompany.bankautomata;

import com.mycompany.bankautomata.data.MemoriaKartyaTar;
import com.mycompany.bankautomata.cash.Cimlet;
import com.mycompany.bankautomata.cash.CimletKazetta;
import com.mycompany.bankautomata.cash.PenzKazetta;
import com.mycompany.bankautomata.cash.VisszalepesesCimletezo;
import com.mycompany.bankautomata.model.Kartya;
import com.mycompany.bankautomata.model.Szamla;
import com.mycompany.bankautomata.journal.FajlNaplo;
import com.mycompany.bankautomata.journal.NaploSorFormatum;
import com.mycompany.bankautomata.journal.TranzakcioNaplo;
import com.mycompany.bankautomata.rule.KivetSzabaly;
import com.mycompany.bankautomata.rule.NapiLimitSzabaly;
import com.mycompany.bankautomata.service.Atm;
import com.mycompany.bankautomata.service.AtmSzolgaltatas;
import com.mycompany.bankautomata.ui.Bemenet;
import com.mycompany.bankautomata.ui.HibaUzenetFordito;
import com.mycompany.bankautomata.ui.KonzolMenu;

import java.nio.file.Path;
import java.time.Clock;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Entry point and "composition root": the only place where concrete classes
 * are created and wired together. Everything else depends on interfaces.
 */
public class Bankautomata {

    private static final long NAPI_LIMIT = 2_000;
    private static final Path NAPLO_FAJL = Path.of("tranzakciok.log");

    public static void main(String[] args) {
        try {
            Clock ora = Clock.systemDefaultZone();

            MemoriaKartyaTar kartyak = new MemoriaKartyaTar()
                    .hozzaad(new Kartya("1111-2222-3333-4444", "1234", new Szamla("RO01-0001", 5_000)))
                    .hozzaad(new Kartya("5555-6666-7777-8888", "4321", new Szamla("RO01-0002", 300)));

            // 5 x 200 + 5 x 100 + 4 x 50 + 10 x 10 = 1800 lei in the machine
            Map<Cimlet, Integer> keszlet = new EnumMap<>(Cimlet.class);
            keszlet.put(Cimlet.KETSZAZ, 5);
            keszlet.put(Cimlet.SZAZ, 5);
            keszlet.put(Cimlet.OTVEN, 4);
            keszlet.put(Cimlet.TIZ, 10);
            PenzKazetta kazetta = new CimletKazetta(keszlet, new VisszalepesesCimletezo());

            List<KivetSzabaly> szabalyok = List.of(new NapiLimitSzabaly(NAPI_LIMIT, ora));
            TranzakcioNaplo naplo = new FajlNaplo(NAPLO_FAJL, new NaploSorFormatum());

            AtmSzolgaltatas atm = new Atm(kartyak, kazetta, szabalyok, naplo, ora,
                    uzenet -> System.out.println("! " + uzenet));

            System.out.println("Demo kartyak:");
            System.out.println("  1111-2222-3333-4444  PIN: 1234  (egyenleg: 5000 lej)");
            System.out.println("  5555-6666-7777-8888  PIN: 4321  (egyenleg: 300 lej)");

            new KonzolMenu(atm, new Bemenet(System.in, System.out), System.out,
                    new HibaUzenetFordito()).futtat();
        } catch (RuntimeException e) {
            // last safety net: even a startup error never shows a stack trace
            System.out.println("Az automata nem tudott elindulni: " + new HibaUzenetFordito().uzenet(e));
        }
    }
}