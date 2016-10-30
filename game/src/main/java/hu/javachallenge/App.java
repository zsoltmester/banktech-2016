package hu.javachallenge;

import hu.javachallenge.bean.CreateGameResponse;
import hu.javachallenge.bean.GameListResponse;
import hu.javachallenge.bean.JoinGameResponse;
import hu.javachallenge.communication.Communicator;
import hu.javachallenge.communication.CommunicatorImpl;

public class App {
    public static void main(String[] args) {
        Communicator communicator = new CommunicatorImpl("195.228.45.100", "8080");

        CreateGameResponse createGameResponse = communicator.createGame();
        System.out.println(createGameResponse.toString());

        GameListResponse gameListResponse = communicator.getGames();
        System.out.println(gameListResponse.toString());

        JoinGameResponse joinGameResponse = communicator.joinGame(createGameResponse.getId());
        System.out.println(joinGameResponse.toString());
    }
}
