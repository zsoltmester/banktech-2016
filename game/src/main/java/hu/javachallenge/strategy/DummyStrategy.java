package hu.javachallenge.strategy;

import hu.javachallenge.bean.Submarine;
import hu.javachallenge.map.Map;
import hu.javachallenge.processor.Processor;

import java.util.Random;

/**
 * Just for test.
 */
public class DummyStrategy implements Strategy {

    private Map map = Map.get();

    @Override
    public void onNewRound() {
        Random randomGenerator = new Random();

        Submarine submarine = map.getSubmarines().get(Processor.game.getRound() % 2);

        Double speed = (randomGenerator.nextDouble() * 2 - 1) * Processor.game.getMapConfiguration().getMaxAccelerationPerRound();
        Double turn = (randomGenerator.nextDouble() * 2 - 1) * Processor.game.getMapConfiguration().getMaxSteeringPerRound();
        Processor.move(submarine.getId(), speed, turn);

        Double angle = randomGenerator.nextDouble() * 360;
        Processor.shoot(submarine.getId(), angle);

        submarine = map.getSubmarines().get(1 - (Processor.game.getRound() % 2));
        Processor.sonar(submarine.getId());
        Processor.extendSonar(submarine.getId());
    }
}
