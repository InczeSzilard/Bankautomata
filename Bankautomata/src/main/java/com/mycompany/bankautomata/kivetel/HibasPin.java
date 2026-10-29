package com.mycompany.bankautomata.kivetel;

/** Wrong PIN; carries how many attempts are left before the card is blocked. */
public class HibasPin extends AtmKivetel {

    private final int maradekProbalkozas;

    public HibasPin(int maradekProbalkozas) {
        super("Hibas PIN-kod. Meg " + maradekProbalkozas
                + " probalkozasa maradt, utana a kartya letiltasra kerul.");
        this.maradekProbalkozas = maradekProbalkozas;
    }

    public int getMaradekProbalkozas() {
        return maradekProbalkozas;
    }
}