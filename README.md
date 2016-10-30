# Infinite Ringbuffer - BankTech Java Challange 2016

2\. forduló vége: **november 9. éjfél**

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
