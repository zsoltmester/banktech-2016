package hu.javachallenge.strategy;

import hu.javachallenge.bean.Entity;
import hu.javachallenge.bean.Position;
import hu.javachallenge.bean.Submarine;
import hu.javachallenge.processor.Processor;
import hu.javachallenge.strategy.moving.CollissionDetector;
import hu.javachallenge.strategy.moving.IChangeMovableObject;
import hu.javachallenge.strategy.moving.MovingIsland;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ScoutStrategy extends MoveStrategy {
    private static final Logger LOGGER = Logger.getLogger(ScoutStrategy.class.getName());

    private final Deque<Position> targets;

    public ScoutStrategy(Long submarineId, Position... targets) {
        super(submarineId);

        // order the targets to start to scout the farthest point

        int firstTargetIndex = 0;
        for (int i = 1; i < targets.length; ++i) {
            if (targets[firstTargetIndex].distance(getSubmarine().getPosition()) < targets[i].distance(getSubmarine().getPosition())) {
                targets[firstTargetIndex] = targets[i];
            }
        }

        List<Position> orderedTargets = new ArrayList<>(targets.length);
        for (int i = firstTargetIndex; i < firstTargetIndex + targets.length; i++) {
            orderedTargets.add(targets[i % targets.length]);
        }

        LOGGER.finest("Position for " + submarineId + ": " + getSubmarine().getPosition());
        LOGGER.finest("Scout targets for " + submarineId + ": " + Arrays.toString(targets));
        LOGGER.finest("First target index for " + submarineId + ": " + firstTargetIndex);
        LOGGER.finest("Ordered scout targets for " + submarineId + ": " + orderedTargets);

        this.targets = new ArrayDeque<>(orderedTargets);
    }

    @Override
    public void init() {
    }

    @Override
    public void onRound() {
        Submarine submarine = getSubmarine();

        changeTargetIfNeed();

        if (targets.peek() != null) {
            LOGGER.info("Submarine " + submarine.getId() + " next position: " + targets.peek());
            // moves to the next position
            super.onRound();
        } else {
            LOGGER.info("Submarine " + submarine.getId() + " has no next position");
            // stay calm
            Processor.move(submarine.getId(), -1, 0);
        }

        // TODO temporary
        new AttackerStrategy(submarine.getId()).onRound();
    }

    protected void changeTargetIfNeed() {
        if (targets.peek() != null) {
            if (getSubmarine().getPosition().distance(targets.peek()) < 10) {
                targets.add(targets.pop());
            }
        }
    }

    protected Deque<Position> getTargets() {
        return targets;
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
            if(CollissionDetector.submarineCollisionWithIsland(this, islandPosition, 10) != null) {
                Position pos =
                        MoveUtil.evadeThis(getSubmarine(), targets.peek(),
                                new MovingIsland(islandPosition), map.getConfiguration().getIslandSize());

                LOGGER.info("Detect collision with Island in position: " + islandPosition);
                LOGGER.info("Set new pos: " + pos);
                Strategy strategy = new ScoutStrategy(getSubmarine().getId(), pos);
                Strategy nextStrategy = strategy;
                while((nextStrategy = strategy.onChangeStrategy()) != null) {
                    strategy = nextStrategy;
                }

                return new StrategySwitcher(this, strategy,
                        () -> CollissionDetector.submarineCollisionWithIsland(this, islandPosition, 10) == null);
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
                                    map.getConfiguration().getTorpedoExplosionRadius() * 2);
                } else {
                    position = getSubmarine().getPosition();
                }
                LOGGER.info("Detect collision with a torpedo in position: " + where);
                LOGGER.info("Set new pos: " + position);
                Strategy strategy = new ScoutStrategy(getSubmarine().getId(), position);
                Strategy nextStrategy;
                while((nextStrategy = strategy.onChangeStrategy()) != null) {
                    strategy = nextStrategy;
                }

                return new StrategySwitcher(this, strategy,
                        () -> {
                            Entity entity1 = map.getEntities().stream()
                                    .filter(e -> e.getId().equals(entity.getId())).findFirst().orElse(null);
                            return entity1 == null ||
                                    CollissionDetector.submarineCollisionWithEntity(this, entity1, 10) == null;
                        });

            }
        }
        // TODO collision detection
        // return new StrategySwitcher(new ScoutStrategy(submarineId, /* evade control points */),
        //        this, () -> { return /* when not afraid to collosion */ });


        return null;
    }
}
