package hu.javachallenge.strategy;

import hu.javachallenge.bean.Position;
import hu.javachallenge.bean.Submarine;
import hu.javachallenge.processor.Processor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.logging.Logger;

public class ScoutStrategy extends CollisionDetectorStrategy {
    private static final Logger LOGGER = Logger.getLogger(ScoutStrategy.class.getName());

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
    protected void goToNextTarget() {
        targets.add(targets.pop());
    }
}
