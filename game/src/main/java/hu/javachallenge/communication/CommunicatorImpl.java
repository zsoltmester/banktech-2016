package hu.javachallenge.communication;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.javachallenge.bean.CreateGameResponse;
import hu.javachallenge.bean.GameResponse;
import hu.javachallenge.bean.GamesResponse;
import hu.javachallenge.bean.JoinGameResponse;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;

public class CommunicatorImpl implements Communicator {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final String TEAM_TOKEN = "3120A9456D1FD8706C223980245717B7";

    private final String BASE_URL;

    private OkHttpClient client = new OkHttpClient();

    public CommunicatorImpl(String host, String port) {
        BASE_URL = "http://" + host + ":" + port + "/jc16-srv/";
    }

    private <T> T executeGetRequest(String relativeUrl, Class<T> responseClass) {
        Request request = new Request.Builder()
                .addHeader("TEAMTOKEN", TEAM_TOKEN)
                .url(BASE_URL + relativeUrl)
                .build();

        try {
            String response = client.newCall(request).execute().body().string();
            return new ObjectMapper().readValue(response, responseClass);
        } catch (IOException e) {
            // TODO logger
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public GamesResponse getGames() {
        return executeGetRequest("game", GamesResponse.class);
    }

    @Override
    public CreateGameResponse createGame() {
        Request request = new Request.Builder()
                .addHeader("TEAMTOKEN", TEAM_TOKEN)
                .url(BASE_URL + "game")
                .post(RequestBody.create(JSON, ""))
                .build();

        try {
            String response = client.newCall(request).execute().body().string();
            return new ObjectMapper().readValue(response, CreateGameResponse.class);
        } catch (IOException e) {
            // TODO logger
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public JoinGameResponse joinGame(Long id) {
        Request request = new Request.Builder()
                .addHeader("TEAMTOKEN", TEAM_TOKEN)
                .url(BASE_URL + "game/" + id)
                .post(RequestBody.create(JSON, ""))
                .build();

        try {
            String response = client.newCall(request).execute().body().string();
            return new ObjectMapper().readValue(response, JoinGameResponse.class);
        } catch (IOException e) {
            // TODO logger
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public GameResponse getGame(Long id) {
        return executeGetRequest("game/" + id, GameResponse.class);
    }
}