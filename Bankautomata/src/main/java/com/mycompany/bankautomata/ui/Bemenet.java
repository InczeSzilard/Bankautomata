package com.mycompany.bankautomata.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * Reads the user's input safely.
 * The input stream is passed in (not System.in directly), so the menu can be
 * tested with prepared input.
 */
public class Bemenet {

    /** Thrown when there is no more input (e.g. end of stream): the program exits. */
    public static class VegeKivetel extends RuntimeException {
        public VegeKivetel() {
            super("Nincs tobb bemenet.");
        }
    }

    private final BufferedReader olvaso;
    private final PrintStream ki;

    public Bemenet(InputStream be, PrintStream ki) {
        this.olvaso = new BufferedReader(new InputStreamReader(be));
        this.ki = ki;
    }

    /** Prints the question and returns the trimmed answer. */
    public String sor(String kerdes) {
        ki.print(kerdes);
        ki.flush();
        try {
            String sor = olvaso.readLine();
            if (sor == null) {
                throw new VegeKivetel();
            }
            return sor.trim();
        } catch (IOException e) {
            throw new VegeKivetel();
        }
    }

    /* Like sor(), but expects a whole number. Text like "abc" -> IllegalArgumentException. */
    public long szam(String kerdes) {
        String sor = sor(kerdes);
        try {
            return Long.parseLong(sor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("\"" + sor + "\" nem ervenyes egesz szam.");
        }
    }
}