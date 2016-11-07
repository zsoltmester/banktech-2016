package hu.javachallenge.strategy;

import hu.javachallenge.bean.Position;
import hu.javachallenge.bean.Submarine;
import hu.javachallenge.strategy.moving.CollissionDetector;
import hu.javachallenge.strategy.moving.MovingIsland;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.logging.Logger;

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
        // TODO collision detection
        // return new StrategySwitcher(new ScoutStrategy(submarineId, /* evade control points */),
        //        this, () -> { return /* when not afraid to collosion */ });


        return null;
    }
}
