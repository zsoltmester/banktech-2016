# Infinite Ringbuffer - BankTech Java Challange 2016

## Maven project fordítása és futtatása

1. Legyen feltelepítve egy *Java 8* és a *JAVA_HOME* környezeti változótok annak rootjába mutasson.
2. Telepítsetek fel egy *3.3.9-es Maven*-t.
3. Fordítani a *pom.xml* mappájából tudjátok a következő parancsal: `mvn clean package`. A `clean` törli a *target*-et, a `package` meg lefordítja az appot és megcsinálja a *jar*-t. Az utóbbi a unit teszteket is lefordítja és lefuttatja. A target mappába került egy *infinite_ringbuffer.jar*.
4. Futtatni így tudjátok: `java -jar infinite_ringbuffer.jar`.
