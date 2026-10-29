package com.mycompany.bankautomata.rule;

import com.mycompany.bankautomata.kivetel.NapiLimit;
import com.mycompany.bankautomata.model.Kartya;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Limits how much cash one card can withdraw per calendar day. */
public class NapiLimitSzabaly implements KivetSzabaly {

    // How much a card has withdrawn on which day.
    private record NapiForgalom(LocalDate nap, long osszeg) {
    }

    private final long napiLimit;
    private final Clock ora;
    private final Map<String, NapiForgalom> forgalom = new HashMap<>();

    /**
     * @param ora the clock is injected (not LocalDate.now() directly), so tests
     *            can control the date - e.g. check that the limit resets the next day
     */
    public NapiLimitSzabaly(long napiLimit, Clock ora) {
        if (napiLimit <= 0) {
            throw new IllegalArgumentException("A napi limitnek pozitivnak kell lennie: " + napiLimit);
        }
        this.napiLimit = napiLimit;
        this.ora = Objects.requireNonNull(ora, "ora");
    }

    @Override
    public void ellenoriz(Kartya kartya, long osszeg) throws NapiLimit {
        long maradek = napiLimit - maiForgalom(kartya);
        if (osszeg > maradek) {
            throw new NapiLimit(napiLimit, Math.max(0, maradek));
        }
    }

    @Override
    public void rogzit(Kartya kartya, long osszeg) {
        LocalDate ma = LocalDate.now(ora);
        long eddigi = maiForgalom(kartya);
        forgalom.put(kartya.getKartyaSzam(), new NapiForgalom(ma, eddigi + osszeg));
    }

    // Today's total for this card; 0 if there is no entry or it's from an earlier day.
    private long maiForgalom(Kartya kartya) {
        NapiForgalom f = forgalom.get(kartya.getKartyaSzam());
        if (f == null || !f.nap().equals(LocalDate.now(ora))) {
            return 0;
        }
        return f.osszeg();
    }
}