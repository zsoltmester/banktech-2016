package hu.javachallenge.communication;

import hu.javachallenge.bean.CreateGameResponse;
import hu.javachallenge.bean.GameResponse;
import hu.javachallenge.bean.GamesResponse;
import hu.javachallenge.bean.JoinGameResponse;

public interface Communicator {

    CreateGameResponse createGame();

    GamesResponse getGames();

    JoinGameResponse joinGame(Long id);

    GameResponse getGame(Long id);
}
