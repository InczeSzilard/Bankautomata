package com.mycompany.bankautomata.cash;

import com.mycompany.bankautomata.kivetel.NemKirakhatoOsszeg;
import com.mycompany.bankautomata.kivetel.NincsElegKeszpenz;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CimletKazettaTest {

    private CimletKazetta kazetta(int db200, int db100, int db50, int db10) {
        Map<Cimlet, Integer> keszlet = new EnumMap<>(Cimlet.class);
        keszlet.put(Cimlet.KETSZAZ, db200);
        keszlet.put(Cimlet.SZAZ, db100);
        keszlet.put(Cimlet.OTVEN, db50);
        keszlet.put(Cimlet.TIZ, db10);
        return new CimletKazetta(keszlet, new VisszalepesesCimletezo());
    }

    @Test
    void tobbetKerunk_mintAmennyiPenzVan_NincsElegKeszpenzKivetel() {
        CimletKazetta kazetta = kazetta(1, 1, 0, 0); // 300 lei in total

        NincsElegKeszpenz e = assertThrows(NincsElegKeszpenz.class,
                () -> kazetta.tervez(500));

        assertEquals(500, e.getKertOsszeg());
        assertEquals(300, e.getElerhetoOsszeg());
        assertEquals(200, e.getHiany());
    }

    @Test
    void csakOtvenesekVannak_30Lej_NemKirakhatoOsszegKivetel() {
        CimletKazetta kazetta = kazetta(0, 0, 10, 0); // 500 lei, only 50s

        assertThrows(NemKirakhatoOsszeg.class, () -> kazetta.tervez(30));
    }

    @Test
    void nemTizzelOszthatoOsszeg_NemKirakhatoOsszegKivetel() {
        CimletKazetta kazetta = kazetta(5, 5, 5, 5);

        NemKirakhatoOsszeg e = assertThrows(NemKirakhatoOsszeg.class,
                () -> kazetta.tervez(15));

        assertEquals(15, e.getKertOsszeg());
    }

    @Test
    void legnagyobbCimletekkelFizetKi_esCsakKiadaskorCsokkenAKeszlet() throws Exception {
        CimletKazetta kazetta = kazetta(5, 5, 5, 5);

        KifizetesiTerv terv = kazetta.tervez(450);

        assertEquals(2, terv.darab(Cimlet.KETSZAZ));
        assertEquals(1, terv.darab(Cimlet.OTVEN));
        assertEquals(450, terv.osszeg());
        assertEquals(5, kazetta.keszlet().get(Cimlet.KETSZAZ)); // planning alone changes nothing

        kazetta.kiad(terv);

        assertEquals(3, kazetta.keszlet().get(Cimlet.KETSZAZ));
        assertEquals(4, kazetta.keszlet().get(Cimlet.OTVEN));
    }

    @Test
    void haANagyCimletNemJo_kisebbekbolRakjaKi() throws Exception {
        CimletKazetta kazetta = kazetta(1, 0, 3, 0); // one 200, three 50s

        KifizetesiTerv terv = kazetta.tervez(150);  // the 200 is too big -> 3 x 50

        assertEquals(3, terv.darab(Cimlet.OTVEN));
        assertEquals(0, terv.darab(Cimlet.KETSZAZ));
    }
}