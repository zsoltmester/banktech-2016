public class EgyOsztaly {

    static byte[] ojjektum;
    static {
        ojjektum = new byte[100];
        System.out.println("static init block executed");
    }

    ThreadLocal<EgyOsztaly> en = new ThreadLocal<>();

    public EgyOsztaly() {
        en.set(this);
    }
}
