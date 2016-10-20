public class Task26 {
    public static void main(String[] args) {
        Double a = Double.NaN;
        Double b = Double.NaN;
        Double c = -0.0d;
        Double d = 1.0d;
        Double e = 0d;
        Double f = null;
        Double g = null;

        System.out.println("A: " + (a == b));
        System.out.println("B: " + (f == g));
        System.out.println("C: " + (a.equals(b)));
        System.out.println("D: " + (c.equals(e)));
        System.out.println("E: " + Double.isNaN(d/a/b) + " - " + (d/a/b));
        System.out.println("F: " + "false" + " - " + d/c*-1);
        System.out.println("G: " + "false" + " - " + d/c*-1);
        System.out.println("H: " + Double.isNaN(e/c) + " - " + e/c);
        System.out.println("I: " + Double.isInfinite(e/c) + " - " + e/c);
        try {
            System.out.println("J: " + "false" + " - " + e / g);
        } catch (NullPointerException ex) {
            System.out.println("J: " + "true" + " - " + ex);
        }
    }
}
