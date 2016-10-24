public class Task16 {

    public static final int N_ITERATIONS = 1000000;

    public static String testFinal(final String a, final String b) {
        return a + b;
    }

    public static String testNonFinal(String a, String b) {
        return a + b;
    }

    public static void main(String[] args) {
        long tStart, tElapsed;
        // create strings to string pool
        final String a = "a";
        final String b = "b";
        final String c = "ab";

        tStart = System.currentTimeMillis();
        for (int i = 0; i < N_ITERATIONS; i++)
            testFinal("a", "b");
        tElapsed = System.currentTimeMillis() - tStart;
        System.out.println("Method with finals took " + tElapsed + " ms");

        tStart = System.currentTimeMillis();
        for (int i = 0; i < N_ITERATIONS; i++)
            testNonFinal("a", "b");
        tElapsed = System.currentTimeMillis() - tStart;
        System.out.println("Method without finals took " + tElapsed + " ms");

        tStart = System.currentTimeMillis();
        for (int i = 0; i < N_ITERATIONS; i++)
            testFinal("a", "b");
        tElapsed = System.currentTimeMillis() - tStart;
        System.out.println("Method with finals took " + tElapsed + " ms");
        
        tStart = System.currentTimeMillis();
        for (int i = 0; i < N_ITERATIONS; i++)
            testNonFinal("a", "b");
        tElapsed = System.currentTimeMillis() - tStart;
        System.out.println("Method without finals took " + tElapsed + " ms");
    }

}
