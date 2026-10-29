package com.mycompany.bankautomata.kivetel;

import java.util.List;

/**
 * The ATM has enough cash in total, but the amount can't be made from the
 * available notes (e.g. 30 lei when only 50s are left, or 15 lei).
 */
public class NemKirakhatoOsszeg extends AtmKivetel {

    private final long kertOsszeg;
    private final List<Integer> elerhetoCimletek;

    public NemKirakhatoOsszeg(long kertOsszeg, List<Integer> elerhetoCimletek) {
        super("A kert " + kertOsszeg + " lej nem fizetheto ki a jelenleg elerheto cimletekbol "
                + elerhetoCimletek + ". Kerjuk, valasszon mas osszeget.");
        this.kertOsszeg = kertOsszeg;
        this.elerhetoCimletek = List.copyOf(elerhetoCimletek); // defensive copy
    }

    public long getKertOsszeg() { return kertOsszeg; }

    public List<Integer> getElerhetoCimletek() { return elerhetoCimletek; }
}