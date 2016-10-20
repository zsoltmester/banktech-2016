import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;

public class Task30 {
    public static void main(String[] args) {
        Properties prop1 = new Properties();
        Properties prop2 = new Properties();

        try {
            prop1.load(new ByteArrayInputStream(" foo=true ".getBytes(Charset.forName("UTF-8"))));
            prop2.load(new ByteArrayInputStream("foo = true".getBytes(Charset.forName("UTF-8"))));
        } catch (IOException e) {
        }

        String value1 = (String) prop1.get("foo");
        String value2 = (String) prop2.get("foo");
        System.out.println((Boolean.getBoolean(value1) ==
                Boolean.valueOf(value1)) + " " + (Boolean.getBoolean(value2) == Boolean.valueOf(value2)));
    }
}
