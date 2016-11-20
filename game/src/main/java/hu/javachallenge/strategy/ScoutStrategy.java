package hu.javachallenge.strategy;

import hu.javachallenge.bean.Entity;
import hu.javachallenge.bean.Position;
import hu.javachallenge.bean.Submarine;
import hu.javachallenge.processor.Processor;
import hu.javachallenge.strategy.moving.CollissionDetector;
import hu.javachallenge.strategy.moving.IChangeMovableObject;
import hu.javachallenge.strategy.moving.MovingIsland;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ScoutStrategy extends MoveStrategy {
    private static final Logger LOGGER = Logger.getLogger(ScoutStrategy.class.getName());

    private static final int TARGET_REACHED_DISTANCE = 5;

    private final Deque<Position> targets;

    public ScoutStrategy(Long submarineId, List<Position> targets) {
        super(submarineId);
        this.targets = new ArrayDeque<>(targets);
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
            if (getSubmarine().getPosition().distance(targets.peek()) < TARGET_REACHED_DISTANCE) {
                targets.add(targets.pop());
            }
        }
    }

    public Deque<Position> getTargets() {
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
                List<Position> evadePosition = MoveUtil.getEvadePosition(getSubmarine(), this, map.getConfiguration().getSubmarineSize(), new MovingIsland(islandPosition), IChangeMovableObject.ZERO_MOVE, map.getConfiguration().getIslandSize(), 10);

                LOGGER.info("Detect collision with island in position: " + islandPosition);
                if (evadePosition == null) {
                    LOGGER.info("Cannot evade island: " + islandPosition + ". Wait for zero acceleration, then skip the target.");
                    if (getSubmarine().getVelocity() != 0) {
                        evadePosition = Collections.singletonList(getSubmarine().getPosition());
                    } else {
                        targets.add(targets.pop());
                        break;
                    }
                } else {
                    LOGGER.info("Evade position(s): " + evadePosition);
                }
                List<Position> finalEvadePosition = evadePosition;
                return new StrategySwitcher(this, new ScoutStrategy(getSubmarine().getId(), evadePosition),
                        () -> getSubmarine().getPosition().distance(finalEvadePosition.get(finalEvadePosition.size() - 1)) < TARGET_REACHED_DISTANCE);
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
                Strategy strategy = new ScoutStrategy(getSubmarine().getId(), Collections.singletonList(position));
                /*Strategy nextStrategy;
                while((nextStrategy = strategy.onChangeStrategy()) != null) {
                    strategy = nextStrategy;
                }*/

                return new StrategySwitcher(this, strategy,
                        () -> {
                            Entity entity1 = map.getEntities().stream()
                                    .filter(e -> e.getId().equals(entity.getId())).findFirst().orElse(null);
                            return entity1 == null ||
                                    CollissionDetector.submarineCollisionWithEntity(this, entity1, 10) == null;
                        });

            }
        }

        // return new StrategySwitcher(new ScoutStrategy(submarineId, /* evade control points */),
        //        this, () -> { return /* when not afraid to collosion */ });

        return null;
    }

    public boolean willCollusionOccur(int maxSteps) {
        for (Position islandPosition : map.getConfiguration().getIslandPositions()) {
            if (CollissionDetector.submarineCollisionWithIsland(this, islandPosition, maxSteps) != null) {
                return true;
            }
        }

        for (Entity entity : map.getEntities().stream().filter(e -> e.getType().equals(Entity.TORPEDO)).collect(Collectors.toList())) {
            if (CollissionDetector.submarineCollisionWithEntity(this, entity, maxSteps) != null) {
                return true;
            }
        }

        return false;
    }
}
