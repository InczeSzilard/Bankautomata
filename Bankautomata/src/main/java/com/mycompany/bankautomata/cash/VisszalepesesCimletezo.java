package com.mycompany.bankautomata.cash;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Backtracking search, starting from the largest note.
 *
 * A plain greedy algorithm can fail when the stock is limited (it may take
 * "too many" big notes). This one tries every possibility, so if the amount
 * can be made, it always finds a way - and because it starts with the big
 * notes, it hands out few banknotes.
 */
public class VisszalepesesCimletezo implements CimletezoStrategia {

    @Override
    public Optional<KifizetesiTerv> kirak(long osszeg, Map<Cimlet, Integer> keszlet) {
        if (osszeg <= 0) {
            return Optional.empty();
        }
        List<Cimlet> cimletek = Cimlet.csokkenoSorrendben();
        EnumMap<Cimlet, Integer> terv = new EnumMap<>(Cimlet.class);
        if (keres(osszeg, 0, cimletek, keszlet, terv)) {
            return Optional.of(new KifizetesiTerv(terv));
        }
        return Optional.empty();
    }

    /*
     * Tries to pay "maradek" using the denominations from "index" onwards.
     * For the current note it tries the most pieces first, then fewer and fewer.
     */
    private boolean keres(long maradek, int index, List<Cimlet> cimletek,
                          Map<Cimlet, Integer> keszlet, EnumMap<Cimlet, Integer> terv) {
        if (maradek == 0) {
            return true;  // done: the amount is fully covered
        }
        if (index == cimletek.size()) {
            return false; // no more note types, but money is still missing
        }
        Cimlet cimlet = cimletek.get(index);
        long maxDb = Math.min(keszlet.getOrDefault(cimlet, 0), maradek / cimlet.ertek());
        for (long db = maxDb; db >= 0; db--) {
            terv.put(cimlet, (int) db);
            if (keres(maradek - db * cimlet.ertek(), index + 1, cimletek, keszlet, terv)) {
                return true;
            }
        }
        terv.remove(cimlet); // this branch failed - undo
        return false;
    }
}