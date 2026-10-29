package com.mycompany.bankautomata.cash;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.StringJoiner;

/*
 * Immutable payout plan: how many notes of each denomination the ATM hands out.
 * Example: 450 lei -> 2 x 200, 1 x 50
 */
public record KifizetesiTerv(Map<Cimlet, Integer> darabok) {

    // Compact constructor: validates and makes a defensive, unmodifiable copy,
    // so nobody can change the plan after it was created.
    public KifizetesiTerv {
        EnumMap<Cimlet, Integer> masolat = new EnumMap<>(Cimlet.class);
        darabok.forEach((cimlet, db) -> {
            if (db < 0) {
                throw new IllegalArgumentException("Negativ darabszam: " + cimlet);
            }
            if (db > 0) { // zero counts are left out
                masolat.put(cimlet, db);
            }
        });
        darabok = Collections.unmodifiableMap(masolat);
    }

    /* Total amount of the plan in lei. */
    public long osszeg() {
        return darabok.entrySet().stream()
                .mapToLong(e -> (long) e.getKey().ertek() * e.getValue())
                .sum();
    }

    /** How many notes of the given denomination are in the plan (0 if none). */
    public int darab(Cimlet cimlet) {
        return darabok.getOrDefault(cimlet, 0);
    }

    /* Human-readable, e.g. "2 x 200 lej, 1 x 50 lej" - this text also goes into the log. */
    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ");
        for (Cimlet c : Cimlet.csokkenoSorrendben()) {
            int db = darab(c);
            if (db > 0) {
                sj.add(db + " x " + c.ertek() + " lej");
            }
        }
        return sj.toString();
    }
}