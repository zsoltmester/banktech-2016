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

            Submarine submarine = submarinesResponse.getSubmarines().get(round % 2);

            Double speed = (randomGenerator.nextDouble() * 2 - 1) * gameResponse.getGame().getMapConfiguration().getMaxAccelerationPerRound();
            Double turn = (randomGenerator.nextDouble() * 2 - 1) * gameResponse.getGame().getMapConfiguration().getMaxSteeringPerRound();
            MoveRequest moveRequest = new MoveRequest(speed, turn);
            MoveResponse moveResponse = communicator.move(gameResponse.getGame().getId(), submarine.getId(), moveRequest);
            System.out.println(moveResponse.toString());

            Double angle = randomGenerator.nextDouble() *  360;
            ShootRequest shootRequest = new ShootRequest(angle);
            ShootResponse shootResponse = communicator.shoot(gameResponse.getGame().getId(), submarine.getId(), shootRequest);
            System.out.println(shootResponse.toString());

            submarine = submarinesResponse.getSubmarines().get(1 - (round % 2));

            SonarResponse sonarResponse = communicator.sonar(gameResponse.getGame().getId(), submarine.getId());
            System.out.println(sonarResponse.toString());

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
