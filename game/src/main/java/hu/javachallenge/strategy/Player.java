package hu.javachallenge.strategy;

import hu.javachallenge.processor.Processor;
import hu.javachallenge.processor.ProcessorImpl;

public class Player {

    private Processor processor = new ProcessorImpl();

    public void play() {

        processor.joinToGame();

        processor.waitForStart();

        /*///// TEST communication

        GameResponse gameResponse;
        String status;
        Integer round;

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
        }*/
    }
}
