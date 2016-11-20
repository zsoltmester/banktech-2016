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
    private static final int STEPS_TO_CHECK_FOR_COLLISION = 10;

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
            if (CollissionDetector.submarineCollisionWithIsland(this, islandPosition, STEPS_TO_CHECK_FOR_COLLISION) != null) {
                List<Position> evadePosition = MoveUtil.getEvadePosition(getSubmarine(), this, map.getConfiguration().getSubmarineSize(), new MovingIsland(islandPosition), IChangeMovableObject.ZERO_MOVE, map.getConfiguration().getIslandSize(), STEPS_TO_CHECK_FOR_COLLISION);

                LOGGER.info("Detect collision with ISLAND in position: " + islandPosition);
                if (evadePosition == null) {
                    LOGGER.info("Cannot evade ISLAND: " + islandPosition + ". Wait for zero velocity, then skip the target.");
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

        for (Entity torpedo : map.getEntities().stream().filter(e -> e.getType().equals(Entity.TORPEDO)).collect(Collectors.toList())) {
            if (CollissionDetector.submarineCollisionWithEntity(this, torpedo, STEPS_TO_CHECK_FOR_COLLISION) != null) {
                List<Position> evadePosition = MoveUtil.getEvadePosition(getSubmarine(), this, map.getConfiguration().getSubmarineSize(), torpedo, IChangeMovableObject.ZERO_MOVE, 1, STEPS_TO_CHECK_FOR_COLLISION);

                LOGGER.info("Detect collision with TORPEDO in position: " + torpedo);
                if (evadePosition == null) {
                    LOGGER.info("Cannot evade TORPEDO: " + torpedo + ". Wait...");
                    evadePosition = Collections.singletonList(getSubmarine().getPosition());
                } else {
                    LOGGER.info("Evade position(s): " + evadePosition);
                }
                List<Position> finalEvadePosition = evadePosition;
                return new StrategySwitcher(this, new ScoutStrategy(getSubmarine().getId(), evadePosition),
                        () -> map.getEntities().stream().noneMatch(entity -> entity.getId().equals(torpedo.getId()))
                                || getSubmarine().getPosition().distance(finalEvadePosition.get(finalEvadePosition.size() - 1)) < TARGET_REACHED_DISTANCE);
            }
        }

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
