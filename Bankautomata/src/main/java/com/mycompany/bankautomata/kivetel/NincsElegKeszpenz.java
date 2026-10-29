package com.mycompany.bankautomata.kivetel;

/** The ATM holds less cash in total than the requested amount. */
public class NincsElegKeszpenz extends AtmKivetel {

    private final long kertOsszeg;
    private final long elerhetoOsszeg;

    public NincsElegKeszpenz(long kertOsszeg, long elerhetoOsszeg) {
        super("Az automataban jelenleg nincs eleg keszpenz (kert: " + kertOsszeg
                + " lej, elerheto: " + elerhetoOsszeg + " lej). Kerjuk, valasszon kisebb osszeget.");
        this.kertOsszeg = kertOsszeg;
        this.elerhetoOsszeg = elerhetoOsszeg;
    }

    public long getKertOsszeg() { return kertOsszeg; }

    public long getElerhetoOsszeg() { return elerhetoOsszeg; }

    public long getHiany() { return kertOsszeg - elerhetoOsszeg; }
}