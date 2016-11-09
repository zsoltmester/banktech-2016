package hu.javachallenge.strategy.moving;

import hu.javachallenge.bean.Entity;
import hu.javachallenge.bean.MovableObject;
import hu.javachallenge.bean.Position;
import hu.javachallenge.strategy.MoveUtil;

import java.util.ArrayDeque;
import java.util.List;

/**
 * Created by qqcs on 07/11/16.
 */
public interface IChangeMovableObject<T extends MovableObject> {

    IChangeMovableObject<MovableObject> ZERO_MOVE = new ZeroMove<>();

    double getNextAcceleration(T object);
    double getNextSteering(T object);

    default void moveToNext(T object) {

        Double nextAngle = object.getAngle() + getNextSteering(object);
        Double nextSpeed = object.getVelocity() + getNextAcceleration(object);

        if(nextAngle < 0) {
            nextAngle += 360;
        }
        if(nextAngle >= 360) {
            nextAngle -= 360;
        }

        object.setAngle(nextAngle);
        if(0 <= nextSpeed && nextSpeed <= MoveUtil.map.getConfiguration().getMaxSpeed()) {
            object.setVelocity(nextSpeed);
        }

        object.setPosition(new Position(
                object.getPosition().getX() + Math.cos(Math.toRadians(object.getAngle())) * object.getVelocity(),
                object.getPosition().getY() + Math.sin(Math.toRadians(object.getAngle())) * object.getVelocity()
        ));
    }

    static <T extends MovableObject> ArrayDeque<Position> getSteppedPositions(IChangeMovableObject<T> moving, T object, int count) {

        T objectCopy;
        try {
            objectCopy = (T) object.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return new ArrayDeque<>();
        }

        ArrayDeque<Position> result = new ArrayDeque<>();
        result.add(objectCopy.getPosition());

        for(int i = 0; i < count; ++i) {
            moving.moveToNext(objectCopy);
            result.add(objectCopy.getPosition());
        }

        return result;
    }

    class FixMove<T extends MovableObject> implements IChangeMovableObject<T> {
        private final double nextAcceleration;
        private final double nextSteering;

        public FixMove(double nextAcceleration, double nextSteering) {
            this.nextAcceleration = nextAcceleration;
            this.nextSteering = nextSteering;
        }

        @Override
        public double getNextAcceleration(T object) {
            return nextAcceleration;
        }

        @Override
        public double getNextSteering(T object) {
            return nextSteering;
        }
    }

    class ZeroMove<T extends MovableObject> extends FixMove<T> {
        private ZeroMove() {
            super(0.0, 0.0);
        }
    }

    class HistoryMove extends FixMove<Entity> {
        public HistoryMove(List<Entity> history) {
            super(getAccelerationDelta(history), getSteeringDelta(history));
        }

        private static double getSteeringDelta(List<Entity> history) {
            if(history.size() < 2 || history.get(history.size() - 2) == null)
                return 0;

            return MoveUtil.velocityDistance(history.get(history.size() - 2).getAngle(), history.get(history.size() - 1).getAngle());
        }

        private static double getAccelerationDelta(List<Entity> history) {
            //if(history.size() < 2 || history.get(history.size() - 2) == null)
            //    return 0;

            // TODO it is a hack!!!
            // remove comment if it is final
            return MoveUtil.map.getConfiguration().getMaxAccelerationPerRound();

            //return history.get(history.size() - 1).getVelocity() - history.get(history.size() - 2).getVelocity();
        }
    }
}
