package com.mycompany.bankautomata.model;

import com.mycompany.bankautomata.kivetel.HibasPin;
import com.mycompany.bankautomata.kivetel.KartyaLetiltva;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;

/**
 * Bank card: responsible for checking the PIN and blocking the card.
 * The PIN has no getter - from outside it can only be checked, never read.
 */
public class Kartya {

    public static final int MAX_HIBAS_PIN = 3;

    private final String kartyaSzam;
    private final byte[] pin;
    private final Szamla szamla;

    private int hibasProbalkozasok;
    private boolean letiltva;

    public Kartya(String kartyaSzam, String pin, Szamla szamla) {
        if (kartyaSzam == null || kartyaSzam.isBlank()) {
            throw new IllegalArgumentException("A kartyaszam nem lehet ures.");
        }
        if (pin == null || !pin.matches("\\d{4}")) {
            throw new IllegalArgumentException("A PIN-kodnak pontosan 4 szamjegybol kell allnia.");
        }
        this.kartyaSzam = kartyaSzam;
        this.pin = pin.getBytes(StandardCharsets.UTF_8);
        this.szamla = Objects.requireNonNull(szamla, "szamla");
    }

    /**
     * Checks the PIN.
     * - card already blocked      -> KartyaLetiltvaKivetel (even with the correct PIN)
     * - correct PIN               -> resets the wrong-attempt counter
     * - wrong PIN, attempts left  -> HibasPinKivetel (carries attempts left)
     * - 3rd wrong PIN             -> blocks the card, KartyaLetiltvaKivetel
     */
    public void ellenorizPin(String megadottPin) throws HibasPin, KartyaLetiltva {
        if (letiltva) {
            throw new KartyaLetiltva(getMaszkoltSzam());
        }
        if (pinEgyezik(megadottPin)) {
            hibasProbalkozasok = 0;
            return;
        }
        hibasProbalkozasok++;
        if (hibasProbalkozasok >= MAX_HIBAS_PIN) {
            letiltva = true;
            throw new KartyaLetiltva(getMaszkoltSzam());
        }
        throw new HibasPin(MAX_HIBAS_PIN - hibasProbalkozasok);
    }

    private boolean pinEgyezik(String megadottPin) {
        if (megadottPin == null) {
            return false;
        }
        // Constant-time comparison: the response time doesn't reveal
        // how many digits were correct.
        return MessageDigest.isEqual(pin, megadottPin.getBytes(StandardCharsets.UTF_8));
    }

    public boolean isLetiltva() {
        return letiltva;
    }

    public String getKartyaSzam() {
        return kartyaSzam;
    }

    public String getMaszkoltSzam() {
        return maszkol(kartyaSzam);
    }

    public Szamla getSzamla() {
        return szamla;
    }

    /** Only the last 4 digits go into messages and the log, e.g. "****-4444". */
    public static String maszkol(String kartyaSzam) {
        if (kartyaSzam == null) {
            return "****";
        }
        String csakSzamjegyek = kartyaSzam.replaceAll("[^0-9]", "");
        if (csakSzamjegyek.length() <= 4) {
            return "****";
        }
        return "****-" + csakSzamjegyek.substring(csakSzamjegyek.length() - 4);
    }
}