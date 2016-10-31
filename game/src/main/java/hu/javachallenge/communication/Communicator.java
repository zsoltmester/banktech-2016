package hu.javachallenge.communication;

import hu.javachallenge.bean.*;

public interface Communicator {

    CreateGameResponse createGame();

    GamesResponse getGames();

    JoinGameResponse joinGame(Long id);

    GameResponse getGame(Long id);

    SubmarinesResponse getSubmarines(Long id);

    MoveResponse move(Long gameId, Long submarineId, MoveRequest request);

    ShootResponse shoot(Long gameId, Long submarineId, ShootRequest request);

    SonarResponse sonar(Long gameId, Long submarineId);

    ExtendSonarResponse extendSonar(Long gameId, Long submarineId);
}
