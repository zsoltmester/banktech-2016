package hu.javachallenge.strategy;

import hu.javachallenge.bean.Entity;
import hu.javachallenge.bean.Position;
import hu.javachallenge.map.IMap;
import hu.javachallenge.strategy.moving.CollissionDetector;
import hu.javachallenge.strategy.moving.IChangeMovableObject;
import hu.javachallenge.strategy.moving.MovingIsland;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class CollisionDetectorStrategy extends MoveStrategy {

    private static final Logger LOGGER = Logger.getLogger(CollisionDetectorStrategy.class.getName());

    protected static final int STEPS_TO_CHECK_FOR_COLLISION = 10;

    protected CollisionDetectorStrategy(Long submarineId) {
        super(submarineId);
    }

    protected abstract void goToNextTarget();

    @Override
    public Strategy onChangeStrategy() {

        for (Position islandPosition : map.getConfiguration().getIslandPositions()) {
            if (CollissionDetector.submarineCollisionWithIsland(this, islandPosition, STEPS_TO_CHECK_FOR_COLLISION) != null) {
                List<Position> evadePosition = MoveUtil.getEvadePosition(getSubmarine(), this, map.getConfiguration().getSubmarineSize(), new MovingIsland(islandPosition), IChangeMovableObject.ZERO_MOVE, map.getConfiguration().getIslandSize(), STEPS_TO_CHECK_FOR_COLLISION);

                LOGGER.info("Detect collision with ISLAND in position: " + islandPosition);
                if (evadePosition == null) {
                    LOGGER.info("Cannot evade ISLAND: " + islandPosition + ". Wait for zero velocity, then skip the target.");
                    if (getSubmarine().getVelocity() != 0) {
                        evadePosition = Collections.singletonList(getSubmarine().getPosition());
                    } else {
                        goToNextTarget();
                        break;
                    }
                } else {
                    LOGGER.info("Evade position(s): " + evadePosition);
                }
                return new StrategySwitcher(this, new ScoutStrategy(getSubmarine().getId(), evadePosition),
                        () -> !willCollusionOccur(STEPS_TO_CHECK_FOR_COLLISION));
            }
        }

        for (Entity torpedo : map.getEntities().stream().filter(e -> e.getType().equals(Entity.TORPEDO)).collect(Collectors.toList())) {
            if (CollissionDetector.submarineCollisionWithEntity(this, torpedo, STEPS_TO_CHECK_FOR_COLLISION) != null) {

                List<Position> evadePosition = MoveUtil.getEvadePosition(getSubmarine(), this, map.getConfiguration().getSubmarineSize(), torpedo, IChangeMovableObject.ZERO_MOVE, MoveUtil.TORPEDO_SIZE, STEPS_TO_CHECK_FOR_COLLISION);

                LOGGER.info("Detect collision with TORPEDO in position: " + torpedo);
                if (evadePosition == null) {
                    LOGGER.info("Cannot evade TORPEDO: " + torpedo + ". Wait...");
                    break;
                } else {
                    LOGGER.info("Evade position(s): " + evadePosition);
                }
                return new StrategySwitcher(this, new ScoutStrategy(getSubmarine().getId(), evadePosition),
                        () -> !willCollusionOccur(STEPS_TO_CHECK_FOR_COLLISION));
            }
        }

        for (Entity submarine : map.getEntities().stream().filter(e -> e.getType().equals(Entity.SUBMARINE) && !e.getOwner().getName().equals(IMap.OUR_NAME)).collect(Collectors.toList())) {
            if (CollissionDetector.submarineCollisionWithSubmarine(this, submarine, STEPS_TO_CHECK_FOR_COLLISION) != null) {

                List<Position> evadePosition = MoveUtil.getEvadePosition(getSubmarine(), this, map.getConfiguration().getSubmarineSize(), submarine, new IChangeMovableObject.HistoryMove(map.getHistory(submarine.getId(), 2)), map.getConfiguration().getSubmarineSize(), STEPS_TO_CHECK_FOR_COLLISION);

                LOGGER.info("Detect collision with SUBMARINE in position: " + submarine);
                if (evadePosition == null) {
                    LOGGER.info("Cannot evade SUBMARINE: " + submarine + ". Wait...");
                    continue;
                } else {
                    LOGGER.info("Evade position(s): " + evadePosition);
                }
                return new StrategySwitcher(this, new ScoutStrategy(getSubmarine().getId(), evadePosition),
                        () -> map.getEntities().stream().noneMatch(entity -> entity.getId().equals(submarine.getId()))
                                || evadePosition.get(evadePosition.size() - 1).distance(getSubmarine().getPosition()) > MoveUtil.MAX_DISTANCE_FROM_SUBMARINE_WHEN_EVADE);
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
