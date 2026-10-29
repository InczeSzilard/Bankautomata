package com.mycompany.bankautomata.journal;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/*
 * Serialization of a transaction to one line of text, and back.
 * Single Responsibility: the file writer doesn't need to know the format.
 *
 * Format: time;card;type;amount;SIKERES|SIKERTELEN;note
 * Example: 2026-10-29T18:49:12;****-4444;KIFIZETES;450;SIKERES;2 x 200 lej, 1 x 50 lej
 */
public class NaploSorFormatum {

    private static final String ELVALASZTO = ";";
    private static final int MEZOK_SZAMA = 6;

    public String formaz(Tranzakcio t) {
        return String.join(ELVALASZTO,
                t.idopont().toString(),
                t.kartya(),
                t.tipus().name(),
                Long.toString(t.osszeg()),
                t.sikeres() ? "SIKERES" : "SIKERTELEN",
                tisztit(t.megjegyzes()));
    }

    /** Parses one log line. A broken line throws IllegalArgumentException. */
    public Tranzakcio feldolgoz(String sor) {
        String[] r = sor.split(ELVALASZTO, MEZOK_SZAMA); // limit: the note may be empty
        if (r.length != MEZOK_SZAMA) {
            throw new IllegalArgumentException("Hibas naplosor: " + sor);
        }
        try {
            return new Tranzakcio(
                    LocalDateTime.parse(r[0]),
                    r[1],
                    TranzakcioTipus.valueOf(r[2]),
                    Long.parseLong(r[3]),
                    "SIKERES".equals(r[4]),
                    r[5]);
        } catch (DateTimeParseException | NumberFormatException e) {
            throw new IllegalArgumentException("Hibás naplosor: " + sor, e);
        }
    }

    // The note must not break the line format: no separator, no line breaks.
    private static String tisztit(String szoveg) {
        return szoveg.replace(ELVALASZTO, ",")
                .replace("\r", " ")
                .replace("\n", " ");
    }
}