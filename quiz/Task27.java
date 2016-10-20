import java.util.function.Predicate;

public class Task27 {
    private static boolean test(Predicate<Integer> p) {
        return p.test(5);
    }

    public static void main(String[] args) {
        System.out.println("A: " + test(i -> i == 5));
        System.out.println("B: " + "compile error" /* test(i -> {i == 5;}) */ );
        System.out.println("C: " + test((i) -> i == 5));
        System.out.println("D: " + "compile error" /* test((int i) -> i == 5) */ );
        System.out.println("E: " + "compile error" /* test((int) -> {return  i == 5}) */ );
        System.out.println("F: " + test((i) -> {return  i == 5;}));
        System.out.println("G: " + "compile error" /* test(((i)) -> i == 5) */ );
    }
}
