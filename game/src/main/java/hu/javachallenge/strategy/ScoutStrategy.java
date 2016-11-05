package hu.javachallenge.strategy;

import hu.javachallenge.bean.Entity;
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
                    MoveUtil.getAccelerationForTargetSpeed(submarine, map.getConfiguration().getMaxSpeed()),
                    MoveUtil.getTurnForTargetPosition(submarine, map.getConfiguration().getIslandPositions().get(0)));

            if(submarine.getTorpedoCooldown() == 0) {
                Entity entity = map.getEntities().filter(e -> !e.getOwner().getName().equals(Map.ourName))
                        .filter(e -> e.getType().equals(Entity.SUBMARINE))
                        .findFirst().orElse(null);

                Double direction = 270.0;

                if(entity != null) {
                    direction = MoveUtil.getAngleForTargetPosition(submarine, entity.getPosition());
                }
                Processor.shoot(submarine.getId(), direction);
            }
            if(submarine.getSonarCooldown() == 0) {
                Processor.sonar(submarine.getId());
            }
        });
    }
}
