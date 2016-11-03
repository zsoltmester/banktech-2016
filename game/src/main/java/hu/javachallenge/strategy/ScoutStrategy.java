package hu.javachallenge.strategy;

import hu.javachallenge.map.Map;
import hu.javachallenge.processor.Processor;

public class ScoutStrategy implements Strategy {

    private Map map = Map.MapConfig.getMap();

    /*private List<Position> targets = Arrays.asList(
            new Position(map.getConfiguration().getWidth() * 2 / 3, map.getConfiguration().getHeight() * 2 / 3),
            new Position(map.getConfiguration().getWidth() * 2 / 3, map.getConfiguration().getHeight() * 1 / 3),
            new Position(map.getConfiguration().getWidth() * 1 / 3, map.getConfiguration().getHeight() * 1 / 3),
            new Position(map.getConfiguration().getWidth() * 1 / 3, map.getConfiguration().getHeight() * 2 / 3));*/

    @Override
    public void onNewRound() {
        map.getOurSubmarines().forEach(submarine -> {
            Processor.move(submarine.getId(),
                    MoveUtil.getAccelerationForTargetSpeed(submarine, map.getConfiguration().getMaxSpeed()), 10d);
        });
    }
}
