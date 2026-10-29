package com.mycompany.bankautomata.rule;

import com.mycompany.bankautomata.kivetel.AtmKivetel;
import com.mycompany.bankautomata.model.Kartya;

/**
 * A business rule checked before every withdrawal.
 * Open/Closed: a new rule (e.g. max amount per transaction, night limit)
 * is a new class - the Atm code stays unchanged.
 */
public interface KivetSzabaly {

    /** Throws if the withdrawal is not allowed. Must not change any state. */
    void ellenoriz(Kartya kartya, long osszeg) throws AtmKivetel;

    /**
     * Called after a successful withdrawal (e.g. to add it to today's total).
     * Default: does nothing - rules that don't need it simply don't override it.
     */
    default void rogzit(Kartya kartya, long osszeg) {
    }
}