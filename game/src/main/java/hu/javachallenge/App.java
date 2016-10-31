package hu.javachallenge;

import hu.javachallenge.bean.*;
import hu.javachallenge.communication.Communicator;
import hu.javachallenge.communication.CommunicatorImpl;

import java.util.Random;
import java.util.logging.Logger;

public class App {

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {

        try {
            Class.forName(LoggerConfig.class.getName());
        } catch (ClassNotFoundException e) {
            LOGGER.warning("Cannot load logger configuration. Default configuration will be used.");
            e.printStackTrace();
        }

        String serverAddress;
        if (args.length == 0) {
            serverAddress = "195.228.45.100:8080";
            LOGGER.warning("No server address given, using the default: " + serverAddress);
        } else {
            serverAddress = args[0];
        }

        ///// TEST communication

        Communicator communicator = new CommunicatorImpl(serverAddress);

        CreateGameResponse createGameResponse = communicator.createGame();

        communicator.getGames();

        communicator.joinGame(createGameResponse.getId());

        GameResponse gameResponse;
        String status;
        Integer round;

        do {
            gameResponse = communicator.getGame(createGameResponse.getId());

            status = gameResponse.getGame().getStatus();
            round = gameResponse.getGame().getRound();

            try {
                Thread.sleep(333);
            } catch (InterruptedException e) {
            }

        } while ("WAITING".equals(status));

        while ("RUNNING".equals(status)) {

            SubmarinesResponse submarinesResponse = communicator.getSubmarines(gameResponse.getGame().getId());

            Random randomGenerator = new Random();

            Submarine submarine = submarinesResponse.getSubmarines().get(round % 2);

            Double speed = (randomGenerator.nextDouble() * 2 - 1) * gameResponse.getGame().getMapConfiguration().getMaxAccelerationPerRound();
            Double turn = (randomGenerator.nextDouble() * 2 - 1) * gameResponse.getGame().getMapConfiguration().getMaxSteeringPerRound();
            MoveRequest moveRequest = new MoveRequest(speed, turn);
            communicator.move(gameResponse.getGame().getId(), submarine.getId(), moveRequest);

            Double angle = randomGenerator.nextDouble() * 360;
            ShootRequest shootRequest = new ShootRequest(angle);
            communicator.shoot(gameResponse.getGame().getId(), submarine.getId(), shootRequest);

            submarine = submarinesResponse.getSubmarines().get(1 - (round % 2));

            communicator.sonar(gameResponse.getGame().getId(), submarine.getId());

            communicator.extendSonar(gameResponse.getGame().getId(), submarine.getId());

            Integer prevRound;
            do {
                gameResponse = communicator.getGame(createGameResponse.getId());

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
