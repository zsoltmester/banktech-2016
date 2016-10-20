public class Task10 {

    public static Integer getSomeNumber(final boolean foo,
                                        final boolean bar,
                                        final int num,
                                        final Integer num1,
                                        final Integer num2) {

        return foo ? num : bar ? num1 : num2;

    }

    public static void main(String[] args) {
    
        try {
            System.out.println(getSomeNumber(false, true, 0, null, null));
        } catch(NullPointerException e) {
            System.out.println("A - true -> cached a " + e);
            System.out.println("B - false");
        }
        
        // System.out.println((false ? 5 : new Integer(5)).toString());  C -> error: int cannot be dereferenced
        System.out.println("C - false -> (false ? 5 : new Integer(5)).toString() - error: int cannot be dereferenced");
        System.out.println("D - true -> working toString method: " + (false ? new Integer(5) : new Integer(5)).toString());
        
    }
}
