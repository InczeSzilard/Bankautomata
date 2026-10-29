package com.mycompany.bankautomata.cash;

import com.mycompany.bankautomata.kivetel.NemKirakhatoOsszeg;
import com.mycompany.bankautomata.kivetel.NincsElegKeszpenz;

import java.util.Map;

/*
 * The ATM's cash cassette. Paying out is two steps:
 * first tervez() - changes nothing - then kiad().
 * So the account is only charged when the cash can surely be handed out.
 */
public interface PenzKazetta {

    KifizetesiTerv tervez(long osszeg) throws NincsElegKeszpenz, NemKirakhatoOsszeg;

    void kiad(KifizetesiTerv terv);

    long osszesKeszpenz();

    Map<Cimlet, Integer> keszlet();
}