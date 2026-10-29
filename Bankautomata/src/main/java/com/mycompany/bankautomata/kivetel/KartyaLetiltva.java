package com.mycompany.bankautomata.kivetel;

/** The card is blocked (e.g. after 3 wrong PINs). */
public class KartyaLetiltva extends AtmKivetel {

    // only the masked number (****-4444), never the full card number
    private final String maszkoltKartyaSzam;

    public KartyaLetiltva(String maszkoltKartyaSzam) {
        super("A(z) " + maszkoltKartyaSzam
                + " kartya le van tiltva. Kerjuk, forduljon a bankjahoz.");
        this.maszkoltKartyaSzam = maszkoltKartyaSzam;
    }

    public String getMaszkoltKartyaSzam() {
        return maszkoltKartyaSzam;
    }
}