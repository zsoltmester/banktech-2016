public class Lazy {

    private Lazy lazy;
    
    public Lazy getLazy() {
        if (lazy == null) {
            synchronized(this) {
                if (lazy == null) {
                    lazy = new Lazy();
                }
            }
        }
        return lazy;
    }

    public static void main(String[] args) {
        Lazy lazy1 = new Lazy();
        Lazy lazy2 = lazy1.getLazy();
        
        System.out.println("2 lazy object is the same? : " + (lazy1 == lazy2));
    }
}


