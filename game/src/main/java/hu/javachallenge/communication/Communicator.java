package hu.javachallenge.communication;

import hu.javachallenge.bean.CreateGameResponse;
import hu.javachallenge.bean.GameListResponse;
import hu.javachallenge.bean.JoinGameResponse;

public interface Communicator {

    CreateGameResponse createGame();

    GameListResponse getGames();

    JoinGameResponse joinGame(Long id);
}
