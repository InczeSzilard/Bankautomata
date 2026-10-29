package com.mycompany.bankautomata.service;

import com.mycompany.bankautomata.cash.KifizetesiTerv;
import com.mycompany.bankautomata.kivetel.AtmKivetel;

/**
 * The operations of the ATM.
 * Dependency Inversion: the console menu depends on this interface,
 * not on the concrete Atm class (so a Swing UI could use it too).
 */
public interface AtmSzolgaltatas {

    /** Card + PIN check. Returns a session used for the further operations. */
    Munkamenet bejelentkezik(String kartyaSzam, String pin) throws AtmKivetel;

    long egyenleg(Munkamenet munkamenet);

    /** Invalid amount -> IllegalArgumentException. */
    void befizet(Munkamenet munkamenet, long osszeg);

    /** Returns which notes were paid out. */
    KifizetesiTerv kivesz(Munkamenet munkamenet, long osszeg) throws AtmKivetel;
}