package hu.javachallenge;

import hu.javachallenge.bean.GameListResponse;
import hu.javachallenge.communication.Communicator;
import hu.javachallenge.communication.CommunicatorImpl;

import java.util.Arrays;

public class App {
    public static void main(String[] args) {
        Communicator communicator = new CommunicatorImpl("195.228.45.100", "8080");
        GameListResponse gameListResponse = communicator.getGameList();
        System.out.println(Arrays.toString(gameListResponse.getGames().toArray()));
        System.out.println(gameListResponse.getMessage());
        System.out.println(gameListResponse.getCode());
    }
}
