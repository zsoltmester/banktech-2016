package hu.javachallenge.strategy;

import hu.javachallenge.bean.Entity;
import hu.javachallenge.bean.Position;
import hu.javachallenge.bean.Submarine;
import hu.javachallenge.strategy.moving.CollissionDetector;
import hu.javachallenge.strategy.moving.IChangeMovableObject;
import hu.javachallenge.strategy.moving.MovingIsland;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ScoutStrategy extends MoveStrategy {
    private static final Logger LOGGER = Logger.getLogger(ScoutStrategy.class.getName());

    private final Deque<Position> targets;

    public ScoutStrategy(Long submarineId, Position... targets) {
        super(submarineId);
        this.targets = new ArrayDeque<>(Arrays.asList(targets));
    }

    @Override
    public void init() {
    }

    @Override
    public void onRound() {
        Submarine submarine = getSubmarine();

        if (submarine.getPosition().distance(targets.peek()) < 10) {
            targets.add(targets.pop());
        }
        LOGGER.info("Submarine " + submarine.getId() + " next position: " + targets.peek());

        // moves to the next position
        super.onRound();

        // TODO temporary
        new AttackerStrategy(submarine.getId()).onRound();
    }

    @Override
    public double getNextAcceleration(Submarine submarine) {
        return MoveUtil.getAccelerationToCloseThereWhenOnRightDirection(submarine, targets.peek());
    }

    @Override
    public double getNextSteering(Submarine submarine) {
        return MoveUtil.getTurnForTargetPosition(submarine, targets.peek());
    }

    @Override
    public Strategy onChangeStrategy() {

        for(Position islandPosition : map.getConfiguration().getIslandPositions()) {
            if(CollissionDetector.submarineCollisionWithIsland(this, islandPosition, 5) != null) {
                Position pos =
                        MoveUtil.evadeThis(getSubmarine(), targets.peek(),
                                new MovingIsland(islandPosition), map.getConfiguration().getIslandSize());

                LOGGER.info("Detect collision with Island in position: " + islandPosition);
                LOGGER.info("Set new pos: " + pos);
                return new StrategySwitcher(this, new ScoutStrategy(getSubmarine().getId(), pos),
                        () -> CollissionDetector.submarineCollisionWithIsland(this, islandPosition, 5) == null);
            }
        }

        for(Entity entity : map.getEntities().stream().filter(e -> e.getType().equals(Entity.TORPEDO)).collect(Collectors.toList())) {
            Integer time;
            if((time = CollissionDetector.submarineCollisionWithEntity(this, entity, 10)) != null) {
                Position where = IChangeMovableObject.getSteppedPositions(this, getSubmarine(), time).getLast();

                // párhuzamos vagy merőleges?
                double distance = MoveUtil.velocityDistance(entity.getVelocity(), getSubmarine().getVelocity());

                Position position;
                if(Math.abs(distance) < 10 || Math.abs(distance) > 170) {
                    // TFH párhuzamos
                    position = MoveUtil.evadeThis(getSubmarine(), where, entity,
                                    map.getConfiguration().getTorpedoExplosionRadius());
                } else {
                    position = getSubmarine().getPosition();
                }
                LOGGER.info("Detect collision with a torpedo in position: " + where);
                LOGGER.info("Set new pos: " + position);

                return new StrategySwitcher(this, new ScoutStrategy(getSubmarine().getId(), position),
                        () -> {
                            Entity entity1 = map.getEntities().stream()
                                    .filter(e -> e.getId().equals(entity.getId())).findFirst().orElse(null);
                            return entity1 == null || CollissionDetector.submarineCollisionWithEntity(this, entity1, 10) == null;
                        });

            }
        }
        // TODO collision detection
        // return new StrategySwitcher(new ScoutStrategy(submarineId, /* evade control points */),
        //        this, () -> { return /* when not afraid to collosion */ });


        return null;
    }
}
