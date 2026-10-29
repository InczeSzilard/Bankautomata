package com.mycompany.bankautomata.kivetel.data;

import com.mycompany.bankautomata.model.Kartya;

import java.util.Optional;

/**
 * Source of the cards.
 * The ATM depends on this interface, not on a concrete storage (Dependency
 * Inversion). Today it's in memory; later it could be a file or a database
 * without changing the ATM.
 */
public interface KartyaTar {

    /** Returns the card, or an empty Optional if the number is unknown. */
    Optional<Kartya> keres(String kartyaSzam);
}