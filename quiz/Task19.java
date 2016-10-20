import java.lang.reflect.Field;

public class Task19 {

    static {
        try {
            Field f = Integer.class.getDeclaredClasses()[0].getDeclaredField("cache");
            f.setAccessible(true);
            Integer[] arr = (Integer[])f.get(null);
            
            arr[131] = null; // ide bármit lehet írni: new Integer(/* szam */);
            
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // :(
        }
    }

    public static void main(String[] args) {
        Integer a = 2;
        Integer b = 1;
        Integer c = a + b;

        System.out.println(c);
    }
}

