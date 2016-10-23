import java.net.URL;
import java.io.*;

public class Task18 {

    public static void main(String[] args) {

        for (Long i = Long.MIN_VALUE; i < Long.MAX_VALUE; ++i) {
            new Thread(() -> {

                //new EgyOsztaly(); // jól értem ezt? zsolt: szerintem ez:
                try {
                    // fontos, hogy itt a class loader tetszőleges lehet, nem csak ez
                    Class eoClass = new MyClassLoader(ClassLoader.getSystemClassLoader()).loadClass("EgyOsztaly");
                    // az előzőnél nem hívódik meg a statikus iniciálizáció

                    // ezt nem írta a feladat, hogy megtörténik:
                    //EgyOsztaly eoInstance = (EgyOsztaly) eoClass.newInstance();
                    // itt meghívódna a statikus inicializáció

                    //Class.forName("EgyOsztaly");
                    //Class.forName("EgyOsztaly", true, ClassLoader.getSystemClassLoader());
                    // ezeknél meghívódna a statikus inicializáció
                    // de a feladat itt szerintem a classloader-en keresztüli inicializációra gondolt, nem erre

                    // szóval az egész osztályról nem kell tudnunk semmit, ez megkönnyíti a feladatot: nem inicializálódik ott semmi, csak felkerül a classpathra
                } catch (Exception e) {
                    System.out.println(e);
                }
            }).start();
        };
    }

    static class MyClassLoader extends ClassLoader{

        public MyClassLoader(ClassLoader parent) {
            super(parent);
        }

        public Class loadClass(String name) throws ClassNotFoundException {
            if(!"EgyOsztaly".equals(name)) return super.loadClass(name);

            try {
                InputStream input = new URL("file:"+name+".class").openConnection().getInputStream();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int data = input.read();

                while(data != -1){
                    buffer.write(data);
                    data = input.read();
                }

                input.close();

                byte[] classData = buffer.toByteArray();

                return defineClass(name, classData, 0, classData.length);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
