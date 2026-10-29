package com.mycompany.bankautomata.data;

import com.mycompany.bankautomata.data.KartyaTar;
import com.mycompany.bankautomata.model.Kartya;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Simple in-memory card storage (for the demo and the tests). */
public class MemoriaKartyaTar implements KartyaTar {

    private final Map<String, Kartya> kartyak = new HashMap<>();

    /* Returns this, so calls can be chained: tar.hozzaad(a).hozzaad(b) */
    public MemoriaKartyaTar hozzaad(Kartya kartya) {
        kartyak.put(normalizal(kartya.getKartyaSzam()), kartya);
        return this;
    }

    @Override
    public Optional<Kartya> keres(String kartyaSzam) {
        if (kartyaSzam == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(kartyak.get(normalizal(kartyaSzam)));
    }

    // Stored without spaces and dashes, so "1111-2222" and "1111 2222" are the same card.
    private static String normalizal(String szam) {
        return szam.replaceAll("[\\s-]", "");
    }
}