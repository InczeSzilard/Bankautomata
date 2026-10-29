package com.mycompany.bankautomata.cash;

import java.util.Map;
import java.util.Optional;

/**
 * How to make an amount from the available notes (Strategy pattern).
 * Open/Closed: a new algorithm is a new class - the cassette doesn't change.
 */
public interface CimletezoStrategia {

    /**
     * @return the payout plan, or an empty Optional if the amount
     *         can't be made from the given stock
     */
    Optional<KifizetesiTerv> kirak(long osszeg, Map<Cimlet, Integer> keszlet);
}