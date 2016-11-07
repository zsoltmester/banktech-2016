package hu.javachallenge.strategy;

import hu.javachallenge.bean.Entity;
import hu.javachallenge.bean.Position;
import hu.javachallenge.bean.Submarine;
import hu.javachallenge.map.IMap;
import hu.javachallenge.processor.Processor;
import hu.javachallenge.strategy.moving.CollissionDetector;
import hu.javachallenge.strategy.moving.IChangeMovableObject;
import hu.javachallenge.strategy.moving.MovingIsland;

/**
 * Created by qqcs on 06/11/16.
 */
public class AttackerStrategy extends SubmarineStrategy {
    protected AttackerStrategy(Long submarineId) {
        super(submarineId);
    }

    @Override
    public void init() {

    }

    @Override
    public void onRound() {
        Submarine submarine = getSubmarine();

        // TODO following someone if we want.

        if (submarine.getTorpedoCooldown() == 0) {
            Position toShoot = map.getEntities().stream()
                    .filter(e -> !e.getOwner().getName().equals(IMap.OUR_NAME))
                    .filter(e -> e.getType().equals(Entity.SUBMARINE))
                    .map(e -> MoveUtil.getPositionWhereShootMovingTarget(submarine.getPosition(), e))
                    .filter(map::isValidPosition)
                    .filter(p -> p.distance(submarine.getPosition()) >
                            map.getConfiguration().getTorpedoExplosionRadius())
                    .filter(p -> {
                        Entity torpedo = new Entity();
                        torpedo.setPosition(submarine.getPosition());
                        torpedo.setVelocity(map.getConfiguration().getTorpedoSpeed());
                        torpedo.setAngle(MoveUtil.getAngleForTargetPosition(submarine.getPosition(), p));
                        torpedo.setType(Entity.TORPEDO);
                        torpedo.setId(-1L);

                        // not shoot islands
                        return map.getConfiguration().getIslandPositions().stream()
                                .map(MovingIsland::new).allMatch(island ->
                                        CollissionDetector.collisionWith(
                                        torpedo, new IChangeMovableObject.ZeroMove<>(),
                                        map.getConfiguration().getTorpedoRange(),
                                        island, island,
                                        map.getConfiguration().getIslandSize(),
                                        (int) Math.ceil(submarine.getPosition().distance(p) /
                                                map.getConfiguration().getTorpedoSpeed())) == null)

                                &&
                        // not shoot anyone else
                            map.getEntities().stream().filter(e -> e.getType().equals(Entity.SUBMARINE))
                                    .filter(e -> !e.getOwner().getName().equals(map.OUR_NAME))
                                .allMatch(e -> {
                                    Integer time = CollissionDetector.entityCollisionWithEntityHistory(torpedo,
                                            e, 100);
                                    if(time != null) {
                                        Position otherexplosion =
                                                IChangeMovableObject.getSteppedPositions(new IChangeMovableObject.ZeroMove<>(),
                                                torpedo, time).getLast();
                                        boolean result = otherexplosion.distance(submarine.getPosition())
                                                 > map.getConfiguration().getTorpedoExplosionRadius();
                                        return result;
                                    }
                                    return true;
                                });

                    })
                    .sorted((p1, p2) ->
                            Double.compare(p1.distance(submarine.getPosition()),
                                    p2.distance(submarine.getPosition())))
                    .findFirst().orElse(null);

            if (toShoot != null) {
                double direction = MoveUtil.getAngleForTargetPosition(submarine.getPosition(), toShoot);
                Processor.shoot(submarine.getId(), direction);
            }
        }
    }

    @Override
    public Strategy onChangeStrategy() {
        return null;
    }
}
