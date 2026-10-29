package com.mycompany.bankautomata.ui;

import com.mycompany.bankautomata.kivetel.AtmKivetel;

/**
 * Turns any exception into a short, human-readable message.
 * Single Responsibility: the menu doesn't decide how errors are worded.
 * The user never sees a stack trace or an exception class name.
 */
public class HibaUzenetFordito {

    public String uzenet(Throwable hiba) {
        if (hiba instanceof AtmKivetel) {
            // our own exceptions already carry a message written for the user
            return hiba.getMessage();
        }
        if (hiba instanceof IllegalArgumentException) {
            return hiba.getMessage() == null
                    ? "Ervenytelen adat."
                    : "Ervenytelen adat: " + hiba.getMessage();
        }
        // anything unexpected: hide the technical details
        return "Varatlan hiba tortent, a muvelet megszakadt. Kerjuk, probalja ujra.";
    }
}