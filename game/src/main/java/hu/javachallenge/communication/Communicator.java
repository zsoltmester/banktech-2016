package hu.javachallenge.communication;

import hu.javachallenge.bean.*;

public interface Communicator {

    CreateGameResponse createGame();

    GamesResponse getGames();

    JoinGameResponse joinGame(Long id);

    GameResponse getGame(Long id);

    SubmarinesResponse getSubmarines(Long id);
}
