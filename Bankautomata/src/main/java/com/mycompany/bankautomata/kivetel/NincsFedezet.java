package com.mycompany.bankautomata.kivetel;

/** Not enough money on the account; carries the missing amount. */
public class NincsFedezet extends AtmKivetel {

    private final long hiany;

    public NincsFedezet(long hiany) {
        super("Nincs eleg fedezet a szamlan, hianyzik: " + hiany + " lej.");
        this.hiany = hiany;
    }

    public long getHiany() {
        return hiany;
    }
}