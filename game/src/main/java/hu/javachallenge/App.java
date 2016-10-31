package hu.javachallenge;

import hu.javachallenge.bean.*;
import hu.javachallenge.communication.Communicator;
import hu.javachallenge.communication.CommunicatorImpl;

public class App {
    public static void main(String[] args) {
        Communicator communicator = new CommunicatorImpl("195.228.45.100", "8080");

        CreateGameResponse createGameResponse = communicator.createGame();
        System.out.println(createGameResponse.toString());

        GamesResponse gamesResponse = communicator.getGames();
        System.out.println(gamesResponse.toString());

        JoinGameResponse joinGameResponse = communicator.joinGame(createGameResponse.getId());
        System.out.println(joinGameResponse.toString());

        GameResponse gameResponse = communicator.getGame(createGameResponse.getId());
        System.out.println(gameResponse.toString());

        SubmarinesResponse submarinesResponse = communicator.getSubmarines(createGameResponse.getId());
        System.out.println(submarinesResponse.toString());
    }
}
