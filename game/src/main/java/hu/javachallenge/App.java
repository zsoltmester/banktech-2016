package hu.javachallenge;

import hu.javachallenge.bean.*;
import hu.javachallenge.communication.Communicator;
import hu.javachallenge.communication.CommunicatorImpl;

import java.util.Random;

public class App {
    public static void main(String[] args) {
        Communicator communicator = new CommunicatorImpl("195.228.45.100", "8080");

        CreateGameResponse createGameResponse = communicator.createGame();
        System.out.println(createGameResponse.toString());

        GamesResponse gamesResponse = communicator.getGames();
        System.out.println(gamesResponse.toString());

        JoinGameResponse joinGameResponse = communicator.joinGame(createGameResponse.getId());
        System.out.println(joinGameResponse.toString());

        GameResponse gameResponse;
        String status;
        Integer round;

        do {
            gameResponse = communicator.getGame(createGameResponse.getId());
            System.out.println(gameResponse.toString());

            status = gameResponse.getGame().getStatus();
            round = gameResponse.getGame().getRound();

            try {
                Thread.sleep(333);
            } catch (InterruptedException e) {
            }

        } while ("WAITING".equals(status));

        while ("RUNNING".equals(status)) {

            SubmarinesResponse submarinesResponse = communicator.getSubmarines(gameResponse.getGame().getId());
            System.out.println(submarinesResponse.toString());

            Random randomGenerator = new Random();

            Integer submarineIndex = randomGenerator.nextInt(submarinesResponse.getSubmarines().size());
            Submarine submarine = submarinesResponse.getSubmarines().get(submarineIndex);

            Double speed = (randomGenerator.nextDouble() * 2 - 1) * gameResponse.getGame().getMapConfiguration().getMaxAccelerationPerRound();
            Double turn = (randomGenerator.nextDouble() * 2 - 1) * gameResponse.getGame().getMapConfiguration().getMaxSteeringPerRound();
            MoveRequest moveRequest = new MoveRequest(speed, turn);
            MoveResponse moveResponse = communicator.move(createGameResponse.getId(), submarine.getId(), moveRequest);
            System.out.println(moveResponse.toString());

            Integer prevRound;
            do {
                gameResponse = communicator.getGame(createGameResponse.getId());
                System.out.println(gameResponse.toString());

                status = gameResponse.getGame().getStatus();
                prevRound = round;
                round = gameResponse.getGame().getRound();

                try {
                    Thread.sleep(333);
                } catch (InterruptedException e) {
                }

            } while (prevRound.equals(round));
        }
    }
}
