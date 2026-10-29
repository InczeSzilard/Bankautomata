package com.mycompany.bankautomata.cash;

import com.mycompany.bankautomata.kivetel.NemKirakhatoOsszeg;
import com.mycompany.bankautomata.kivetel.NincsElegKeszpenz;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Holds the stock per denomination; leaves the "how to pay" to the strategy. */
public class CimletKazetta implements PenzKazetta {

    private final EnumMap<Cimlet, Integer> keszlet = new EnumMap<>(Cimlet.class);
    private final CimletezoStrategia strategia;

    public CimletKazetta(Map<Cimlet, Integer> kezdoKeszlet, CimletezoStrategia strategia) {
        this.strategia = Objects.requireNonNull(strategia, "strategia");
        for (Cimlet c : Cimlet.values()) {
            int db = kezdoKeszlet.getOrDefault(c, 0);
            if (db < 0) {
                throw new IllegalArgumentException("Negatív készlet: " + c);
            }
            keszlet.put(c, db);
        }
    }

    @Override
    public KifizetesiTerv tervez(long osszeg) throws NincsElegKeszpenz, NemKirakhatoOsszeg {
        if (osszeg <= 0) {
            throw new IllegalArgumentException("Az összegnek pozitívnak kell lennie, kapott: " + osszeg);
        }
        // 1) Is there enough cash in total?
        long osszes = osszesKeszpenz();
        if (osszeg > osszes) {
            throw new NincsElegKeszpenz(osszeg, osszes);
        }
        // 2) There is enough - but can it be made from the notes we have?
        return strategia.kirak(osszeg, keszlet())
                .orElseThrow(() -> new NemKirakhatoOsszeg(osszeg, elerhetoCimletek()));
    }

    @Override
    public void kiad(KifizetesiTerv terv) {
        // Check everything first, subtract only afterwards:
        // either every note goes out, or none of them.
        terv.darabok().forEach((c, db) -> {
            if (keszlet.get(c) < db) {
                throw new IllegalStateException("A terv mar nem teljesitheto, elfogyott: " + c.ertek() + " lej");
            }
        });
        terv.darabok().forEach((c, db) -> keszlet.merge(c, -db, Integer::sum));
    }

    @Override
    public long osszesKeszpenz() {
        return keszlet.entrySet().stream()
                .mapToLong(e -> (long) e.getKey().ertek() * e.getValue())
                .sum();
    }

    /** Read-only copy - the stock can't be changed from outside. */
    @Override
    public Map<Cimlet, Integer> keszlet() {
        return Collections.unmodifiableMap(new EnumMap<>(keszlet));
    }

    // Denominations that still have at least one note, largest first (for the error message).
    private List<Integer> elerhetoCimletek() {
        return Cimlet.csokkenoSorrendben().stream()
                .filter(c -> keszlet.get(c) > 0)
                .map(Cimlet::ertek)
                .toList();
    }
}