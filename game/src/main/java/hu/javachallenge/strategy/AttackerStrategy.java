package hu.javachallenge.strategy;

import hu.javachallenge.bean.Entity;
import hu.javachallenge.bean.Position;
import hu.javachallenge.bean.Submarine;
import hu.javachallenge.map.IMap;
import hu.javachallenge.processor.Processor;

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
                    .map(e -> MoveUtil.getPositionWhereShootTarget(submarine, e))
                    .filter(map::isValidPosition)
                    .filter(p -> p.distance(submarine.getPosition()) >
                            map.getConfiguration().getTorpedoExplosionRadius())
                    .sorted((p1, p2) ->
                            Double.compare(p1.distance(submarine.getPosition()),
                                    p2.distance(submarine.getPosition())))
                    .findFirst().orElse(null);

            if (toShoot != null) {
                double direction = MoveUtil.getAngleForTargetPosition(submarine, toShoot);
                Processor.shoot(submarine.getId(), direction);
            }
        }
    }

    @Override
    public Strategy onChangeStrategy() {
        return null;
    }
}
