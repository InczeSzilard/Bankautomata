package com.mycompany.bankautomata.model;

import com.mycompany.bankautomata.kivetel.HibasPin;
import com.mycompany.bankautomata.kivetel.KartyaLetiltva;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KartyaTest {

    private static final String JO_PIN = "1234";

    private Kartya ujKartya() {
        return new Kartya("1111-2222-3333-4444", JO_PIN, new Szamla("RO01", 1_000));
    }

    @Test
    void hibasPin_HibasPinKivetel_maradekProbalkozassal() {
        Kartya kartya = ujKartya();

        HibasPin e = assertThrows(HibasPin.class, () -> kartya.ellenorizPin("0000"));

        assertEquals(2, e.getMaradekProbalkozas());
        assertFalse(kartya.isLetiltva());
    }

    @Test
    void harmadikHibasPin_letiltjaAKartyat() {
        Kartya kartya = ujKartya();

        assertThrows(HibasPin.class, () -> kartya.ellenorizPin("0000"));
        assertThrows(HibasPin.class, () -> kartya.ellenorizPin("1111"));
        assertThrows(KartyaLetiltva.class, () -> kartya.ellenorizPin("2222"));

        assertTrue(kartya.isLetiltva());
    }

    @Test
    void letiltottKartya_helyesPinnelSemHasznalhato() {
        Kartya kartya = ujKartya();
        for (int i = 0; i < Kartya.MAX_HIBAS_PIN; i++) {
            assertThrows(Exception.class, () -> kartya.ellenorizPin("0000"));
        }

        assertThrows(KartyaLetiltva.class, () -> kartya.ellenorizPin(JO_PIN));
    }

    @Test
    void helyesPin_nullazzaAHibaszamlalot() throws Exception {
        Kartya kartya = ujKartya();
        assertThrows(HibasPin.class, () -> kartya.ellenorizPin("0000"));
        assertThrows(HibasPin.class, () -> kartya.ellenorizPin("0000"));

        kartya.ellenorizPin(JO_PIN); // correct PIN -> counter back to 0

        HibasPin e = assertThrows(HibasPin.class, () -> kartya.ellenorizPin("0000"));
        assertEquals(2, e.getMaradekProbalkozas());
        assertFalse(kartya.isLetiltva());
    }

    @Test
    void maszkolas_csakAzUtolso4SzamjegyetMutatja() {
        assertEquals("****-4444", Kartya.maszkol("1111-2222-3333-4444"));
    }
}