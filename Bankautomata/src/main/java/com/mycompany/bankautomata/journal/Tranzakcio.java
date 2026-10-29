package com.mycompany.bankautomata.journal;

import java.time.LocalDateTime;
import java.util.Objects;

/*
 * One immutable log entry.
 * kartya always holds the MASKED card number (****-4444), never the full one.
 */
public record Tranzakcio(LocalDateTime idopont,
                         String kartya,
                         TranzakcioTipus tipus,
                         long osszeg,
                         boolean sikeres,
                         String megjegyzes) {

    public Tranzakcio {
        Objects.requireNonNull(idopont, "idopont");
        Objects.requireNonNull(kartya, "kartya");
        Objects.requireNonNull(tipus, "tipus");
        megjegyzes = (megjegyzes == null) ? "" : megjegyzes;
    }
}