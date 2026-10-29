package com.mycompany.bankautomata.journal;

import java.io.IOException;

/**
 * Where transactions are logged (Dependency Inversion: the Atm only knows this interface).
 * It may throw IOException - the Atm is responsible for making sure a logging
 * failure never blocks a payout.
 */
public interface TranzakcioNaplo {

    void naploz(Tranzakcio tranzakcio) throws IOException;
}