package com.mycompany.bankautomata.cash;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** Banknote denominations stored in the ATM (in lei). */
public enum Cimlet {
    TIZ(10),
    OTVEN(50),
    SZAZ(100),
    KETSZAZ(200);

    private final int ertek;

    Cimlet(int ertek) {
        this.ertek = ertek;
    }

    public int ertek() {
        return ertek;
    }

    /** Largest first: 200, 100, 50, 10 - the order the payout algorithm tries them in. */
    public static List<Cimlet> csokkenoSorrendben() {
        return Arrays.stream(values())
                .sorted(Comparator.comparingInt(Cimlet::ertek).reversed())
                .toList();
    }
}