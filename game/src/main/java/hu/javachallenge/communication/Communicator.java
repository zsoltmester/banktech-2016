package hu.javachallenge.communication;

import hu.javachallenge.bean.CreateGameResponse;
import hu.javachallenge.bean.GameListResponse;

public interface Communicator {

    CreateGameResponse createGame();

    GameListResponse getGames();
}
