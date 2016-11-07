package hu.javachallenge.strategy;

import hu.javachallenge.bean.Submarine;
import hu.javachallenge.processor.Processor;
import hu.javachallenge.strategy.moving.IChangeMovableObject;

/**
 * Created by qqcs on 07/11/16.
 */
public abstract class MoveStrategy extends SubmarineStrategy implements IChangeMovableObject<Submarine> {

    protected MoveStrategy(Long submarineId) {
        super(submarineId);
    }

    @Override
    public void onRound() {
        Submarine submarine = getSubmarine();
        Processor.move(submarine.getId(), getNextAcceleration(submarine),
                getNextSteering(submarine));

        // moveToNext(submarine); // sets the variables to the next round
    }

    @Override
    public abstract double getNextAcceleration(Submarine submarine);

    @Override
    public abstract double getNextSteering(Submarine submarine);
}
