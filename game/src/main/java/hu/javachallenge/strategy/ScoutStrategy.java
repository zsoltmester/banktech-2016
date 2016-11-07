package hu.javachallenge.strategy;

import hu.javachallenge.bean.Position;
import hu.javachallenge.bean.Submarine;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public class ScoutStrategy extends MoveStrategy {

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

        // TODO collision detection
        // return new StrategySwitcher(new ScoutStrategy(submarineId, /* evade control points */),
        //        this, () -> { return /* when not afraid to collosion */ });


        return null;
    }
}
