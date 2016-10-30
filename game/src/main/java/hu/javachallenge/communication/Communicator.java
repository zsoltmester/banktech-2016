package hu.javachallenge.communication;

import hu.javachallenge.bean.GameListResponse;

public interface Communicator {

    GameListResponse getGameList();
}
