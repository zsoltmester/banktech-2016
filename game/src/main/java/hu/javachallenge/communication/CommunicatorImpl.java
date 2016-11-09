package hu.javachallenge.communication;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.javachallenge.bean.*;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.util.logging.Logger;

public class CommunicatorImpl implements Communicator {

    private static final Logger LOGGER = Logger.getLogger(CommunicatorImpl.class.getName());

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final String TEAM_TOKEN = "3120A9456D1FD8706C223980245717B7";

    private final String BASE_URL;

    private OkHttpClient client = new OkHttpClient();

    public CommunicatorImpl(String serverAddress) {
        BASE_URL = "http://" + serverAddress + "/jc16-srv/";
    }

    private <T> T executeGetRequest(String relativeUrl, Class<T> responseClass) {
        Request request = new Request.Builder()
                .addHeader("TEAMTOKEN", TEAM_TOKEN)
                .url(BASE_URL + relativeUrl)
                .build();

        LOGGER.finest("Request '" + request + "'");
        try {
            String response = client.newCall(request).execute().body().string();
            T responseBean = new ObjectMapper().readValue(response, responseClass);
            LOGGER.finest("Response for GET request to '" + relativeUrl + "\': " + responseBean);
            return responseBean;
        } catch (IOException e) {
            LOGGER.severe("Failed to execute GET request with URL: '" + relativeUrl + '\'');
            e.printStackTrace();
            return null;
        }
    }

    private <T> T executePostRequest(String relativeUrl, Object requestBean, Class<T> responseClass) {
        try {
            Request request = new Request.Builder()
                    .addHeader("TEAMTOKEN", TEAM_TOKEN)
                    .url(BASE_URL + relativeUrl)
                    .post(RequestBody.create(JSON, requestBean == null ? "" : new ObjectMapper().writeValueAsString(requestBean)))
                    .build();

            LOGGER.finest("Request '" + request + "'" + ", body: " + (requestBean == null ? "" : new ObjectMapper().writeValueAsString(requestBean)));
            String response = client.newCall(request).execute().body().string();
            T responseBean = new ObjectMapper().readValue(response, responseClass);
            LOGGER.finest("Response for POST request to '" + relativeUrl + "\': " + responseBean);
            return responseBean;
        } catch (IOException e) {
            LOGGER.severe("Failed to execute POST request with URL: '" + relativeUrl + '\'');
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
        return executePostRequest("game", null, CreateGameResponse.class);
    }

    @Override
    public JoinGameResponse joinGame(Long id) {
        return executePostRequest("game/" + id, null, JoinGameResponse.class);
    }

    @Override
    public GameResponse getGame(Long id) {
        return executeGetRequest("game/" + id, GameResponse.class);
    }

    @Override
    public SubmarinesResponse getSubmarines(Long id) {
        return executeGetRequest("game/" + id + "/submarine", SubmarinesResponse.class);
    }

    @Override
    public MoveResponse move(Long gameId, Long submarineId, MoveRequest moveRequest) {
        return executePostRequest("game/" + gameId + "/submarine/" + submarineId + "/move", moveRequest, MoveResponse.class);
    }

    @Override
    public ShootResponse shoot(Long gameId, Long submarineId, ShootRequest shootRequest) {
        return executePostRequest("game/" + gameId + "/submarine/" + submarineId + "/shoot", shootRequest, ShootResponse.class);
    }

    @Override
    public SonarResponse sonar(Long gameId, Long submarineId) {
        return executeGetRequest("game/" + gameId + "/submarine/" + submarineId + "/sonar", SonarResponse.class);
    }

    @Override
    public ExtendSonarResponse extendSonar(Long gameId, Long submarineId) {
        return executePostRequest("game/" + gameId + "/submarine/" + submarineId + "/sonar", null, ExtendSonarResponse.class);
    }
}
