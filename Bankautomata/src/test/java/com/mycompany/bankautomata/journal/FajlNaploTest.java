package com.mycompany.bankautomata.journal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FajlNaploTest {

    @TempDir
    Path ideiglenesMappa; // JUnit creates an empty folder for every test and deletes it afterwards

    private final NaploSorFormatum formatum = new NaploSorFormatum();

    private Tranzakcio kifizetes(long osszeg) {
        return new Tranzakcio(LocalDateTime.of(2026, 10, 29, 18, 30), "****-4444",
                TranzakcioTipus.KIFIZETES, osszeg, true, "2 x 200 lej, 1 x 50 lej");
    }

    @Test
    void mindenTranzakcioUjSorbaKerul() throws IOException {
        Path fajl = ideiglenesMappa.resolve("tranzakciok.log");
        FajlNaplo naplo = new FajlNaplo(fajl, formatum);

        naplo.naploz(kifizetes(450));
        naplo.naploz(kifizetes(100));

        List<String> sorok = Files.readAllLines(fajl, StandardCharsets.UTF_8);
        assertEquals(2, sorok.size());   // appended, not overwritten
        assertTrue(sorok.get(0).contains("KIFIZETES;450;SIKERES"));
    }

    @Test
    void nemIrhatoFajl_IOExceptiontDob() {
        // a folder can't be opened as a file -> IOException is passed on to the caller
        FajlNaplo naplo = new FajlNaplo(ideiglenesMappa, formatum);

        assertThrows(IOException.class, () -> naplo.naploz(kifizetes(450)));
    }

    @Test
    void formazasEsVisszaolvasas_ugyanaztAdja() {
        Tranzakcio eredeti = kifizetes(450);

        Tranzakcio visszaolvasott = formatum.feldolgoz(formatum.formaz(eredeti));

        assertEquals(eredeti, visszaolvasott);
    }

    @Test
    void hibasNaplosor_IllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> formatum.feldolgoz("ez nem naplosor"));
    }
}