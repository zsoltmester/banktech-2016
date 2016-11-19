package hu.javachallenge.strategy.moving;

import hu.javachallenge.bean.Entity;
import hu.javachallenge.bean.Position;

/**
 * Created by qqcs on 07/11/16.
 */
public final class MovingIsland extends Entity implements IChangeMovableObject<MovingIsland> {

    public static final String ISLAND = "Island";

    public MovingIsland(Position position) {
        setPosition(position);
        setVelocity(0.0);
        setAngle(0.0);
        setType(ISLAND);
        setId(-1L);
    }

    @Override
    public double getNextAcceleration(MovingIsland object) {
        return 0;
    }

    @Override
    public double getNextSteering(MovingIsland object) {
        return 0;
    }

    @Override
    public void moveToNext(MovingIsland object) {

    }
}
