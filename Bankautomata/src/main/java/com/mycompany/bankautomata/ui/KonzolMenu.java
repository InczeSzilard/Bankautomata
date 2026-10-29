package com.mycompany.bankautomata.ui;

import com.mycompany.bankautomata.cash.KifizetesiTerv;
import com.mycompany.bankautomata.kivetel.AtmKivetel;
import com.mycompany.bankautomata.kivetel.HibasPin;
import com.mycompany.bankautomata.service.AtmSzolgaltatas;
import com.mycompany.bankautomata.service.Munkamenet;

import java.io.PrintStream;
import java.util.Objects;

/**
 * Console menu. It only talks to the user: reads input, calls the ATM,
 * prints the result. Every error is caught here and shown as a human message.
 * Depends on the AtmSzolgaltatas interface, not on the Atm class (DIP).
 */
public class KonzolMenu {

    private final AtmSzolgaltatas atm;
    private final Bemenet bemenet;
    private final PrintStream ki;
    private final HibaUzenetFordito fordito;

    public KonzolMenu(AtmSzolgaltatas atm, Bemenet bemenet, PrintStream ki, HibaUzenetFordito fordito) {
        this.atm = Objects.requireNonNull(atm, "atm");
        this.bemenet = Objects.requireNonNull(bemenet, "bemenet");
        this.ki = Objects.requireNonNull(ki, "ki");
        this.fordito = Objects.requireNonNull(fordito, "fordito");
    }

    public void futtat() {
        ki.println("==============================");
        ki.println("        BANKAUTOMATA");
        ki.println("==============================");
        boolean fut = true;
        while (fut) {
            try {
                fut = fomenu();
            } catch (Bemenet.VegeKivetel e) {
                fut = false;                 // end of input: exit quietly
            } catch (AtmKivetel | RuntimeException e) {
                hiba(e);                     // e.g. blocked or unknown card -> back to the main menu
            }
        }
        ki.println("Viszontlatasra!");
    }

    /** @return false if the user wants to exit */
    private boolean fomenu() throws AtmKivetel {
        ki.println();
        ki.println("1 - Kartya behelyezese");
        ki.println("0 - Kilepes");
        switch (bemenet.sor("Valasztas: ")) {
            case "1" -> kartyaMunkamenet();
            case "0" -> {
                return false;
            }
            default -> ki.println("Ismeretlen menupont.");
        }
        return true;
    }

    private void kartyaMunkamenet() throws AtmKivetel {
        String kartyaSzam = bemenet.sor("Kartyaszam: ");
        Munkamenet munkamenet = bejelentkeztet(kartyaSzam);
        kartyaMenu(munkamenet);
    }

    /*
     * Asks for the PIN until it's correct. A wrong PIN only shows a message
     * and asks again; a blocked or unknown card ends the attempt (the
     * exception goes up to futtat(), which prints it).
     */
    private Munkamenet bejelentkeztet(String kartyaSzam) throws AtmKivetel {
        while (true) {
            String pin = bemenet.sor("PIN-kod: ");
            try {
                return atm.bejelentkezik(kartyaSzam, pin);
            } catch (HibasPin e) {
                hiba(e);
            }
        }
    }

    private void kartyaMenu(Munkamenet munkamenet) {
        ki.println("Sikeres bejelentkezes. Kartya: " + munkamenet.getMaszkoltKartyaSzam());
        while (true) {
            ki.println();
            ki.println("1 - Egyenleg lekerdezese");
            ki.println("2 - Befizetes");
            ki.println("3 - Keszpenzfelvetel");
            ki.println("0 - Kartya visszavetele");
            String valasztas = bemenet.sor("Valasztas: ");
            if (valasztas.equals("0")) {
                ki.println("Kerjuk, vegye el a kartyajat.");
                return;
            }
            try {
                switch (valasztas) {
                    case "1" -> ki.println("Egyenleg: " + atm.egyenleg(munkamenet) + " lej");
                    case "2" -> {
                        long osszeg = bemenet.szam("Befizetendo osszeg (lej): ");
                        atm.befizet(munkamenet, osszeg);
                        ki.println("Sikeres befizetes: " + osszeg + " lej.");
                    }
                    case "3" -> {
                        long osszeg = bemenet.szam("Felvenni kivant osszeg (lej): ");
                        KifizetesiTerv terv = atm.kivesz(munkamenet, osszeg);
                        ki.println("Kerjuk, vegye el a penzet: " + terv);
                    }
                    default -> ki.println("Ismeretlen menupont.");
                }
            } catch (Bemenet.VegeKivetel e) {
                throw e;                     // must not be swallowed by the catch below
            } catch (AtmKivetel | RuntimeException e) {
                hiba(e);                     // show the message, stay in the card menu
            }
        }
    }

    private void hiba(Exception e) {
        ki.println("! " + fordito.uzenet(e));
    }
}