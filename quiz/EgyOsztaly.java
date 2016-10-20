public class EgyOsztaly {

    static byte[] ojjektum;
    static {
        ojjektum = new byte[100];
    }
    
    ThreadLocal<EgyOsztaly> en = new ThreadLocal<>();
    
    public EgyOsztaly() {
        en.set(this);
    }
} 
