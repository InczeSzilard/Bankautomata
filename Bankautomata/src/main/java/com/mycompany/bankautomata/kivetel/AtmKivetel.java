package com.mycompany.bankautomata.kivetel;

/**
 * Common parent of every ATM business error.
 * Checked exception: these are expected situations caused by user input
 * (wrong PIN, not enough money...), so the compiler forces callers to handle them.
 * Abstract: we never throw it directly, always a specific subclass.
 */
public abstract class AtmKivetel extends Exception {

    protected AtmKivetel(String uzenet) {
        super(uzenet);
    }
}