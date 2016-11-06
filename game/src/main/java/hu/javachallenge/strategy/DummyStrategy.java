package hu.javachallenge.strategy;

import hu.javachallenge.bean.Submarine;
import hu.javachallenge.map.IMap;
import hu.javachallenge.processor.Processor;

import java.util.Random;

/**
 * Just for test.
 */
public class DummyStrategy implements Strategy {

    private IMap map = IMap.MapConfig.getMap();

    @Override
    public void init() {
    }

    @Override
    public void onStartRound() {
    }

    @Override
    public void onRound() {
        Random randomGenerator = new Random();

        Submarine submarine = map.getOurSubmarines().get(Processor.game.getRound() % 2);

        Double speed = (double) (randomGenerator.nextInt(3) - 1) * Processor.game.getMapConfiguration().getMaxAccelerationPerRound();
        Double turn = (randomGenerator.nextDouble() * 2 - 1) * Processor.game.getMapConfiguration().getMaxSteeringPerRound();
        Processor.move(submarine.getId(), speed, turn);

        Double angle = randomGenerator.nextDouble() * 360;
        Processor.shoot(submarine.getId(), angle);

        submarine = map.getOurSubmarines().get(1 - (Processor.game.getRound() % 2));
        Processor.sonar(submarine.getId());
        Processor.extendSonar(submarine.getId());
    }

    @Override
    public Strategy onChangeStrategy() {
        return null;
    }
}
