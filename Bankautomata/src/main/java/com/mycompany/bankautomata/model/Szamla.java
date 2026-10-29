package com.mycompany.bankautomata.model;

import com.mycompany.bankautomata.kivetel.NincsFedezet;

/*
 * Bank account, fully encapsulated: the balance field is private and can only
 * change through befizet() and kivesz(), which always validate the amount.
 * A zero or negative amount throws IllegalArgumentException (unchecked):
 * that is a programming error - the caller should have validated the input -
 * not a normal business situation the user has to be told about.
 */
public class Szamla {

    private final String szamlaszam;
    private long egyenleg;

    public Szamla(String szamlaszam, long nyitoEgyenleg) {
        if (szamlaszam == null || szamlaszam.isBlank()) {
            throw new IllegalArgumentException("A szamlaszam nem lehet ures.");
        }
        if (nyitoEgyenleg < 0) {
            throw new IllegalArgumentException("A nyitoegyenleg nem lehet negativ: " + nyitoEgyenleg);
        }
        this.szamlaszam = szamlaszam;
        this.egyenleg = nyitoEgyenleg;
    }

    public String getSzamlaszam() {
        return szamlaszam;
    }

    // Only a getter, no setter: the balance can't be overwritten from outside.
    public long getEgyenleg() {
        return egyenleg;
    }

    public void befizet(long osszeg) {
        ellenorizPozitiv(osszeg);
        egyenleg = Math.addExact(egyenleg, osszeg); // throws instead of silently overflowing
    }

    /**
     * Only checks, does not change anything.
     * The ATM uses it to check the balance BEFORE it prepares the cash.
     */
    public void ellenorizFedezet(long osszeg) throws NincsFedezet {
        ellenorizPozitiv(osszeg);
        if (osszeg > egyenleg) {
            throw new NincsFedezet(osszeg - egyenleg);
        }
    }

    public void kivesz(long osszeg) throws NincsFedezet {
        ellenorizFedezet(osszeg);
        egyenleg -= osszeg;
    }

    private static void ellenorizPozitiv(long osszeg) {
        if (osszeg <= 0) {
            throw new IllegalArgumentException("Az osszegnek pozitivnak kell lennie, kapott: " + osszeg);
        }
    }
}