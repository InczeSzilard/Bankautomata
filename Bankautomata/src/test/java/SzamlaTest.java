package com.mycompany.bankautomata.model;

import com.mycompany.bankautomata.kivetel.NincsFedezet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SzamlaTest {

    @Test
    void nullaOsszegBefizetese_IllegalArgumentException() {
        Szamla szamla = new Szamla("RO01", 1_000);
        assertThrows(IllegalArgumentException.class, () -> szamla.befizet(0));
    }

    @Test
    void negativOsszegFelvetele_IllegalArgumentException() {
        Szamla szamla = new Szamla("RO01", 1_000);
        assertThrows(IllegalArgumentException.class, () -> szamla.kivesz(-50));
    }

    @Test
    void negativNyitoegyenleg_IllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Szamla("RO01", -1));
    }

    @Test
    void tobbFelvetele_mintAzEgyenleg_NincsFedezetKivetel() {
        Szamla szamla = new Szamla("RO01", 300);

        NincsFedezet e = assertThrows(NincsFedezet.class, () -> szamla.kivesz(500));

        assertEquals(200, e.getHiany());         // the exception carries the missing amount
        assertEquals(300, szamla.getEgyenleg()); // a failed withdrawal doesn't change the balance
    }

    @Test
    void befizetesEsFelvetel_helyesenValtoztatjaAzEgyenleget() throws Exception {
        Szamla szamla = new Szamla("RO01", 1_000);

        szamla.befizet(500);
        szamla.kivesz(300);

        assertEquals(1_200, szamla.getEgyenleg());
    }
}