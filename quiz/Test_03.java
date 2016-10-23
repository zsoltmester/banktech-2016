public class Test_03 {

    public static void main(String[] args) {
        int i = 1;
        int j = 2;
        System.out.println(sum(++i, j++, i = j));
    }

    public static int sum(final int i, final int j, final int k) {
        return i + j + k;
    }
}
