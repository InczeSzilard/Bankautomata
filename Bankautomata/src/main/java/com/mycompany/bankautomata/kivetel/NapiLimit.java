package com.mycompany.bankautomata.kivetel;

/** The withdrawal would exceed the card's daily cash limit. */
public class NapiLimit extends AtmKivetel {

    private final long napiLimit;
    private final long maradekKeret;

    public NapiLimit(long napiLimit, long maradekKeret) {
        super("A kifizetes tullepne a napi " + napiLimit + " lejes limitet. Ma meg legfeljebb "
                + maradekKeret + " lej veheto fel.");
        this.napiLimit = napiLimit;
        this.maradekKeret = maradekKeret;
    }

    public long getNapiLimit() { return napiLimit; }

    public long getMaradekKeret() { return maradekKeret; }
}