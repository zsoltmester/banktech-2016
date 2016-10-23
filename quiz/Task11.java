public class Task11 {

    public static void main(String[] args) {
        String str = new String("foo");

        System.out.println("A: " + (str == "foo"));
        System.out.println("B: nem. Hatékonyabb a String str = \"foo\"");
        System.out.println("C: nem. Az értékadásban csak 1 objektumot hoz létre. \n" +
            "  A string poolban létezhet már, de azt nem biztos hogy itt keletkezik. \n" +
            "  --- Esetleg a konstruktor egy másik (típusú) objektumot létrehozhat \n" +
            "  --- ami miatt a számosságuk 2 lesz?"); // zsolt: szerintem ez implementáció (openjdk vs oracle jdk) és java verzió függű is lehet, úgyhogy nem hiszem
        System.out.println("D: Az elozo magyarazat miatt igen, ez az egyetlen valid valasz");
    }
}
