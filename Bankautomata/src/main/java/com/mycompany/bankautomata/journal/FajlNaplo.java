package com.mycompany.bankautomata.journal;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/** Appends every transaction as one line to a text file. */
public class FajlNaplo implements TranzakcioNaplo {

    private final Path fajl;
    private final NaploSorFormatum formatum;

    public FajlNaplo(Path fajl, NaploSorFormatum formatum) {
        this.fajl = Objects.requireNonNull(fajl, "fajl");
        this.formatum = Objects.requireNonNull(formatum, "formatum");
    }

    @Override
    public void naploz(Tranzakcio tranzakcio) throws IOException {
        // try-with-resources: the writer is ALWAYS closed (and flushed),
        // even if write() throws an exception halfway through.
        try (BufferedWriter iro = Files.newBufferedWriter(fajl, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            iro.write(formatum.formaz(tranzakcio));
            iro.newLine();
        }
    }
}