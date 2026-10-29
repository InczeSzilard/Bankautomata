# Bankautomata saját kivételekkel (b-07)

Egy ATM konzolos szimulációja Java nyelven. Minden lehetséges hibának saját,
beszédes kivétele van, és a felhasználó semmilyen bemenetre nem kap stack trace-t:
minden hiba emberi nyelvű üzenetként jelenik meg.

## Futtatás

- **NetBeans:** Run Project (F6), a bemenetet az Output ablakba kell gépelni.
- **Tesztek:** Test Project (Alt+F6), vagy parancssorból `mvn test`.
- Követelmény: JDK 17 vagy újabb, Maven.

Demó kártyák:

| Kártyaszám | PIN | Egyenleg |
|---|---|---|
| 1111-2222-3333-4444 | 1234 | 5000 lej |
| 5555-6666-7777-8888 | 4321 | 300 lej |

Az automatában induláskor 1800 lej van (5 × 200, 5 × 100, 4 × 50, 10 × 10),
a napi limit kártyánként 2000 lej. Így minden hibaeset kézzel is kipróbálható.
A tranzakciók a `tranzakciok.log` fájlba kerülnek.

## Checked és unchecked kivételek

| Kivétel | Fajta |
|---|---|
| `AtmKivetel` és minden alosztálya: `HibasPinKivetel`, `KartyaLetiltvaKivetel`, `NincsFedezetKivetel`, `NincsElegKeszpenzKivetel`, `NapiLimitKivetel`, `NemKirakhatoOsszegKivetel`, `IsmeretlenKartyaKivetel` | **checked** (`Exception` leszármazottja) |
| `IllegalArgumentException` (pl. nulla vagy negatív összeg a `Szamla`-nál) | **unchecked** (`RuntimeException` leszármazottja) |

**Miért?** Az `AtmKivetel` családja olyan *várható* üzleti helyzeteket jelez,
amelyeket a felhasználó idéz elő (rossz PIN, kevés pénz a számlán, kifogyott automata),
ezért checked: a fordító kikényszeríti, hogy a hívó kezelje őket, egy ATM-nél
ezt nem lehet „elfelejteni”. A nem pozitív összeg ezzel szemben *programozási hiba*:
a hívónak kellett volna validálnia a bemenetet, mielőtt a `Szamla`-hoz fordul.
Ha ezt is checked kivétel jelezné, minden egyes hívásnál értelmetlen `try/catch`
blokkok kellenének egy olyan hibára, amely helyes kódban elő sem fordul.
Ezért unchecked `IllegalArgumentException` – a konzolos menü a biztonság kedvéért
ezt is elkapja és emberi nyelven írja ki.

Minden saját kivétel hasznos adatot is hordoz (pl. `NincsFedezetKivetel.getHiany()`,
`HibasPinKivetel.getMaradekProbalkozas()`, `NapiLimitKivetel.getMaradekKeret()`).


## Fontos tervezési döntések

- **Kifizetés sorrendje:** előbb minden csak *ellenőriz* (napi limit → fedezet →
  kifizethetőség címletekből), és csak ezután mozdul a pénz (számla terhelése →
  bankjegyek kiadása). Így nem fordulhat elő, hogy a számla terhelődik, de pénz nem jön ki.
- **Naplózás hibatűrése:** a `FajlNaplo` továbbdobja az `IOException`-t, az `Atm` pedig
  a kifizetés *után* elkapja, és csak figyelmeztetést ír ki. A napló hibája nem akasztja
  meg a kifizetést.
- **Címletezés:** visszalépéses keresés a legnagyobb címlettől indulva – ha az összeg
  kirakható a készletből, biztosan talál megoldást, és kevés bankjegyet ad ki.
  Ha nincs elég pénz: `NincsElegKeszpenzKivetel`; ha van, de nem rakható ki
  (pl. 15 lej, vagy 30 lej csak 50-esekkel): `NemKirakhatoOsszegKivetel`.
- **Biztonság:** a naplóba és az üzenetekbe csak a maszkolt kártyaszám kerül (`****-4444`),
  a PIN-hez nincs getter.

## Tesztek

30 JUnit 5 teszt, ebből 25 `assertThrows`. Minden saját kivételre legalább egy:

| Kivétel | Tesztosztály |
|---|---|
| `HibasPinKivetel` | `KartyaTest` |
| `KartyaLetiltvaKivetel` | `KartyaTest` |
| `NincsFedezetKivetel` | `SzamlaTest`, `AtmTest` |
| `NincsElegKeszpenzKivetel` | `CimletKazettaTest`, `AtmTest` |
| `NapiLimitKivetel` | `NapiLimitSzabalyTest`, `AtmTest` |
| `NemKirakhatoOsszegKivetel` | `CimletKazettaTest` |
| `IsmeretlenKartyaKivetel` | `AtmTest` |

Az `AtmTest` azt is ellenőrzi, hogy a naplózás hibája nem akasztja meg a kifizetést,
és hogy sikertelen kifizetésnél sem a számla, sem a pénzkazetta nem változik.
