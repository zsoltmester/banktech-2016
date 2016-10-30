package hu.javachallenge;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .addHeader("TEAMTOKEN", "3120A9456D1FD8706C223980245717B7")
                .url("http://195.228.45.100:8080/jc16-srv/game")
                .build();

        Response response = client.newCall(request).execute();
        System.out.println(response.body().string());
    }
}
