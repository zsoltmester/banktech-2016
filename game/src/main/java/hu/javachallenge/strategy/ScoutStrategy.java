package hu.javachallenge.strategy;

import hu.javachallenge.bean.Position;
import hu.javachallenge.bean.Submarine;
import hu.javachallenge.processor.Processor;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public class ScoutStrategy extends SubmarineStrategy {

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

        Processor.move(submarine.getId(),
                MoveUtil.getAccelerationToCloseThere(submarine, targets.peek()),
                MoveUtil.getTurnForTargetPosition(submarine, targets.peek()));

        // TODO temporary
        new AttackerStrategy(submarine.getId()).onRound();
    }

    @Override
    public Strategy onChangeStrategy() {

        // TODO collosion detection
        // return new StrategySwitcher(new ScoutStrategy(submarineId, /* evade control points */),
        //        this, () -> { return /* when not afraid to collosion */ });


        return null;
    }
}
