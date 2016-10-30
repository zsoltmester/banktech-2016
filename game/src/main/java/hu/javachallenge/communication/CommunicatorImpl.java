package hu.javachallenge.communication;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.javachallenge.bean.CreateGameResponse;
import hu.javachallenge.bean.GameListResponse;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;

public class CommunicatorImpl implements Communicator {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final String BASE_URL;

    private OkHttpClient client = new OkHttpClient();

    public CommunicatorImpl(String host, String port) {
        BASE_URL = "http://" + host + ":" + port + "/jc16-srv/";
    }

    @Override
    public GameListResponse getGames() {
        Request request = new Request.Builder()
                .addHeader("TEAMTOKEN", "3120A9456D1FD8706C223980245717B7")
                .url(BASE_URL + "game")
                .build();

        String response = null;
        try {
            response = client.newCall(request).execute().body().string();
        } catch (IOException e) {
            e.printStackTrace();
            // TODO logger
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(response, GameListResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
            // TODO logger
        }

        return null;
    }

    @Override
    public CreateGameResponse createGame() {
        Request request = new Request.Builder()
                .addHeader("TEAMTOKEN", "3120A9456D1FD8706C223980245717B7")
                .url(BASE_URL + "game")
                .post(RequestBody.create(JSON, ""))
                .build();

        String response = null;
        try {
            response = client.newCall(request).execute().body().string();
        } catch (IOException e) {
            e.printStackTrace();
            // TODO logger
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(response, CreateGameResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
            // TODO logger
        }

        return null;
    }
}
