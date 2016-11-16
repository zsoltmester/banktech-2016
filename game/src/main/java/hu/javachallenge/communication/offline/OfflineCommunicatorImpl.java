package hu.javachallenge.communication.offline;

import hu.javachallenge.bean.*;
import hu.javachallenge.communication.Communicator;

import java.util.Date;
import java.util.HashMap;
import java.util.stream.Collectors;

public class OfflineCommunicatorImpl implements Communicator {

    private HashMap<Long, OfflineGame> games = new HashMap<>();

    private static void setStatus(StatusResponse response, int code) {
        response.setMessage(code == 0 ? "OK" : "ERROR");
        response.setCode(code);
    }

    @Override
    public CreateGameResponse createGame() {
        CreateGameResponse response = new CreateGameResponse();

        OfflineGame offlineGame = new OfflineGame();
        offlineGame.getGame().setId((long) games.size());
        offlineGame.getGame().setCreatedTime(new Date());

        games.put((long) games.size(), offlineGame);

        setStatus(response, 0);

        return response;
    }

    @Override
    public GamesResponse getGames() {
        GamesResponse response = new GamesResponse();
        response.setGames(games.keySet().stream().collect(Collectors.toList()));
        setStatus(response, 0);
        return response;
    }

    @Override
    public JoinGameResponse joinGame(Long id) {
        JoinGameResponse response = new JoinGameResponse();
        OfflineGame game = games.get(id);

        if (game != null) {
            game.join();
        }
        setStatus(response, game != null ? 0 : 3);
        return response;
    }

    @Override
    public GameResponse getGame(Long id) {
        GameResponse response = new GameResponse();
        OfflineGame game = games.get(id);

        if (game != null) {
            response.setGame(game.getGame());
        }
        setStatus(response, game != null ? 0 : 3);
        return response;
    }

    @Override
    public SubmarinesResponse getSubmarines(Long id) {
        SubmarinesResponse response = new SubmarinesResponse();
        OfflineGame game = games.get(id);

        if (game != null) {
            response.setSubmarines(game.getOurSubmarines());
        }
        setStatus(response, game != null ? 0 : 3);
        return response;
    }

    @Override
    public MoveResponse move(Long gameId, Long submarineId, MoveRequest request) {
        MoveResponse response;
        OfflineGame game = games.get(gameId);

        if (game != null) {
            response = game.moveSubmarine(submarineId, request);
        } else {
            response = new MoveResponse();
            setStatus(response, 3);
        }
        return response;
    }

    @Override
    public ShootResponse shoot(Long gameId, Long submarineId, ShootRequest request) {
        ShootResponse response;
        OfflineGame game = games.get(gameId);

        if (game != null) {
            response = game.shoot(submarineId, request);
        } else {
            response = new ShootResponse();
            setStatus(response, 3);
        }
        return response;
    }

    @Override
    public SonarResponse sonar(Long gameId, Long submarineId) {
        SonarResponse response;
        OfflineGame game = games.get(gameId);

        if (game != null) {
            response = game.sonar(submarineId);
        } else {
            response = new SonarResponse();
            setStatus(response, 3);
        }
        return response;
    }

    @Override
    public ExtendSonarResponse extendSonar(Long gameId, Long submarineId) {
        ExtendSonarResponse response;
        OfflineGame game = games.get(gameId);

        if (game != null) {
            response = game.extendSonar(submarineId);
        } else {
            response = new ExtendSonarResponse();
            setStatus(response, 3);
        }
        return response;
    }
}
