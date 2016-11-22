# Infinite Ringbuffer - BankTech Java Challange 2016

**A döntő helyszíne:**
Tesla Budapest - Kazinczy utca 21./C, Budapest 1075

**A döntő időpontja:**
2016. november 22. kedd, 9:30 – 18:15

**Időzítés:**

- 9:30 – 10:00: Check in (regisztráció és gépek összerakása)
- 10:00 – 10:20: Megnyitó
- 10:20 – 12:20: Programozás
- 12:20 – 13:00: Ebéd
- 13:00 – 17:00: Programozás
- 17:00 – 17:30: Döntő
- 17:30 – 18:00: Eredményhirdetés
- 18:15: Zárás

**Infók:**

- Lesz sok tesztelési lehetőség.
- Délután lesznek mért eedmények.
- A legjobb 8 jut a döntőbe.
- Párhuzamosan akár 6 játék is mehet.

## TODO

- (1) Megjavítani a torpedoRange-es hibákat. Egy konstansba szervezzük ki, hogy mekkorának tekintsünk egy torpedót
- (1) Több hajót formációba mozgassunk. Háromszögformáció. Várják be egymást
- (2) Pánik stratégia
- (2) Kígyó mozgás
- (3) History move okosítás
- (3) Majd esetleg ezeket a felismerteket cachelni egy adott csapathoz / submarine ID-hoz
- (5) History alapján is lőhetnénk, ha jobb

## Maven project fordítása és futtatása

1. Legyen feltelepítve egy *Java 8* és a *JAVA_HOME* környezeti változótok annak rootjába mutasson.
2. Telepítsetek fel egy *3.3.9-es Maven*-t.
3. Fordítani a *pom.xml* mappájából tudjátok a következő parancsal: `mvn clean package`. A `clean` törli a *target*-et, a `package` meg lefordítja az appot és megcsinálja a *jar*-t. Az utóbbi a unit teszteket is lefordítja és lefuttatja. A target mappába került egy *infinite_ringbuffer.jar*.
4. Futtatni így tudjátok: `java -jar infinite_ringbuffer.jar`.

## IntelliJ IDEA integráció

1. Telepítsétek fel az IDEA legutolsó stabil verzóját [innen](https://www.jetbrains.com/idea/download/).
2. Importáljátok a projektet: *Import project*, majd válasszátok ki a *pom.xml*-t. A megjelenő ablakban alul van egy *Environment settings...*, itt válasszátok ki, hogy az előre csomagolt helyett a saját 3.3.9-es Maven-eteket használja. Utána tovább addig, amíg az SDK kiválasztó ablakhoz nem kerültök. Itt adjátok hozzá / válasszátok ki a 8-as JDK-t. A végén még egy next és egy finish.
3. Ha betöltötte a projektet, a jobb alsó sarokban kiírja, hogy *Unregistered VCS root detected*. Válasszátok azt, hogy *Add root*.
4. Zárjátok be az IDEA-t, másoljátok be a *runConfigurations* mappát a *.idea* mappába, majd indítsátok el az IDEA-t. Ezután lesz egy *clean package* nevű conf, ami futtat egy `mvn clean package`-t és egy *compile and run*, ami lefuttatja az előző confot majd elindítja a jar-t.

## Code Style

Használjuk az IDEA default kódformázóját, amit a következővel lehet futtatni: **ctrl + alt + l**.

## Architecture

Hogy a stratégiát magas szinten tudjuk megfogalmazni és leprogramozni, nem törődve az adattok előfeldogozásával, a kommunikációval és a térkép frissítgetésével, ahhoz egy megfelelő architektúrára van szükség. Az alábbi rétegek segítik ezt elérni (lentről fölfele felsorolva). Mindegyikkel egy interface-en keresztül lehet kommunikálni.

### Kommunikációs

- Az előfeldolgozó réteg hívhatja meg.

Request beant kaphat és mindig egy response beant ad vissza. Ha hiba történt a kommunikáció során, akkor null-al tér vissza.

### Térkép

- Csak az előfeldolgozó réteg frissítheti.
- A stratégiának csak kiszolgálni tudja az adatokat.

Frissíti a térképet az előfeldolgozó rétegtől kapott adatok alapján. Ezekkel az adatokkal képes kiszolgálni a stratégiát. Meg is tudja a térképet jeleníteni az std out-on. Vagy ha van kedvünk, azt is megcsinálhatjuk, hogy egy grafikus felületen is megjeleníti az adatokat (mondjuk egy egyszerű AWT/Spring-es alkalmazás).

### Előfeldolgozó

- A stratégia hívhatja meg.
- A kommunikációs réteget hívogatja és a térképet frissíti.

Ez a kért művelet alpján hívja meg a kommunikációs réteget. A response-ok alapján frissíti a térképet, és küldi tovább egy esetleges feldolgozás utána response-t a stratégiának. Ez kezeli a hibakódokat is.

### Stratégia

- Az előfeldolgozó réteget hívogatva hajtja végre a műveleteket (adatlekérés, mozgás, stb).
- A térkép állapotának lekérdezése a rendelkezésére áll.

Ez már nem törődik az adatok előfeldolgozásával, a térkép frissítésével és a műveletek végrehajtásával, csak a stratégiát fogalmazza meg.

Ezt tovább lehetne úgy vinni, hogy a stratégia dinamikus legyen, azaz egyes stratégiák válthatják egymást (mondjuk ha meglátunk egy ellenséges hajót, stb). Ez azért is tehető meg, mert a térkép és az előfeldolgozás teljesen független a stratégiától.
