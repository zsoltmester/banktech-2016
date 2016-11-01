package hu.javachallenge.strategy;

import hu.javachallenge.processor.Processor;

public class Player {

    public static void play() {

        Processor.joinGame();
        Processor.waitForStart();

        while (Processor.isGameRunning()) {

            Processor.updateSubmarines();

            /*///// TEST communication

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

            communicator.extendSonar(gameResponse.getGame().getId(), submarine.getId());*/

            Processor.waitForNextRound();
        }
    }
}
