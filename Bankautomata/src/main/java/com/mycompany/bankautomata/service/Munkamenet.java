package com.mycompany.bankautomata.service;

import com.mycompany.bankautomata.model.Kartya;

/*
 * Proof of a successful login.
 * Only the Atm can create one (package-private constructor), so the UI
 * can't skip the PIN check. The UI also can't reach the Kartya itself -
 * only its masked number.
 */
public final class Munkamenet {

    private final Kartya kartya;

    Munkamenet(Kartya kartya) {
        this.kartya = kartya;
    }

    Kartya getKartya() {
        return kartya;
    }

    public String getMaszkoltKartyaSzam() {
        return kartya.getMaszkoltSzam();
    }
}