package com.mycompany.bankautomata.rule;

import com.mycompany.bankautomata.kivetel.NapiLimit;
import com.mycompany.bankautomata.model.Kartya;
import com.mycompany.bankautomata.model.Szamla;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NapiLimitSzabalyTest {

    private final AllithatoOra ora = new AllithatoOra(LocalDateTime.of(2026, 10, 29, 10, 0));
    private final NapiLimitSzabaly szabaly = new NapiLimitSzabaly(1_000, ora);
    private final Kartya kartya = new Kartya("1111-2222-3333-4444", "1234", new Szamla("RO01", 10_000));

    @Test
    void limitTullepese_NapiLimitKivetel_maradekKerettel() {
        szabaly.rogzit(kartya, 800);

        NapiLimit e = assertThrows(NapiLimit.class, () -> szabaly.ellenoriz(kartya, 300));

        assertEquals(1_000, e.getNapiLimit());
        assertEquals(200, e.getMaradekKeret());
    }

    @Test
    void pontosanALimitigEngedi() {
        szabaly.rogzit(kartya, 800);

        assertDoesNotThrow(() -> szabaly.ellenoriz(kartya, 200));
    }

    @Test
    void masnapUjraLehetFelvenni() {
        szabaly.rogzit(kartya, 1_000);
        assertThrows(NapiLimit.class, () -> szabaly.ellenoriz(kartya, 10));

        ora.elore(Duration.ofDays(1));

        assertDoesNotThrow(() -> szabaly.ellenoriz(kartya, 1_000));
    }

    @Test
    void kulonKartyaknakKulonLimitjukVan() {
        Kartya masik = new Kartya("5555-6666-7777-8888", "4321", new Szamla("RO02", 10_000));
        szabaly.rogzit(kartya, 1_000);

        assertDoesNotThrow(() -> szabaly.ellenoriz(masik, 1_000));
    }
}