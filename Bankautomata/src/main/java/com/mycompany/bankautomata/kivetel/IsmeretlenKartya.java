package com.mycompany.bankautomata.kivetel;

/* The ATM doesn't recognise the inserted card number. */
public class IsmeretlenKartya extends AtmKivetel {

    private final String maszkoltKartyaSzam;

    public IsmeretlenKartya(String maszkoltKartyaSzam) {
        super("A(z) " + maszkoltKartyaSzam + " kartyat az automata nem ismeri fel.");
        this.maszkoltKartyaSzam = maszkoltKartyaSzam;
    }

    public String getMaszkoltKartyaSzam() { return maszkoltKartyaSzam; }
}