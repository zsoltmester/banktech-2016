package hu.javachallenge.strategy;

import hu.javachallenge.bean.Entity;
import hu.javachallenge.bean.Position;
import hu.javachallenge.bean.Submarine;
import hu.javachallenge.map.IMap;
import hu.javachallenge.processor.Processor;
import hu.javachallenge.strategy.moving.CollissionDetector;
import hu.javachallenge.strategy.moving.IChangeMovableObject;
import hu.javachallenge.strategy.moving.MovingIsland;

import java.util.logging.Logger;

/**
 * Created by qqcs on 06/11/16.
 */
public class AttackerStrategy extends SubmarineStrategy {

    private static final Logger LOGGER = Logger.getLogger(AttackerStrategy.class.getName());
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
                    .peek(e -> LOGGER.fine("Entity to shoot: " + e))
                    .map(e -> MoveUtil.getPositionWhereShootMovingTarget(submarine.getPosition(), e,
                            map.getConfiguration().getSubmarineSize()))
                    .peek(e -> LOGGER.fine("Position where: " + e))
                    .filter(map::isValidPosition)
                    .filter(p -> p.distance(submarine.getPosition()) >
                            map.getConfiguration().getTorpedoExplosionRadius())
                    .peek(e -> LOGGER.fine("Bigger distance then torpedo explosion"))
                    .filter(p -> p.distance(
                            IChangeMovableObject.getSteppedPositions(
                                    IChangeMovableObject.ZERO_MOVE, submarine, 2).getLast()) >
                            map.getConfiguration().getTorpedoExplosionRadius())
                    .peek(e -> LOGGER.fine("Bigger distance then torpedo explosion 2 tick later"))
                    .filter(p -> p.distance(submarine.getPosition()) <
                            map.getConfiguration().getTorpedoRange() *
                                    (1 + map.getConfiguration().getTorpedoSpeed()))
                    .peek(e -> LOGGER.fine("Distance is good for us"))
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
                                        torpedo, IChangeMovableObject.ZERO_MOVE, 1,
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
                                                IChangeMovableObject.getSteppedPositions(IChangeMovableObject.ZERO_MOVE,
                                                torpedo, time).getLast();
                                        boolean result = otherexplosion.distance(submarine.getPosition())
                                                 > map.getConfiguration().getTorpedoExplosionRadius();

                                        result &= otherexplosion.distance(IChangeMovableObject.getSteppedPositions(
                                                        IChangeMovableObject.ZERO_MOVE, submarine, 2).getLast()) >
                                                map.getConfiguration().getTorpedoExplosionRadius();

                                        return result;
                                    }
                                    return true;
                                });

                    })
                    .peek(e -> LOGGER.fine("Not shoot island or anyone else close to us"))
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
