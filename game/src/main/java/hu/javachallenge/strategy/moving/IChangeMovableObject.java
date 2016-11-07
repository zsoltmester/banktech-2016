package hu.javachallenge.strategy.moving;

import hu.javachallenge.bean.Entity;
import hu.javachallenge.bean.IMovableObject;
import hu.javachallenge.bean.Position;

import java.util.ArrayDeque;
import java.util.List;

/**
 * Created by qqcs on 07/11/16.
 */
public interface IChangeMovableObject<T extends IMovableObject> {

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
        if(0 <= nextSpeed) {
            object.setVelocity(nextSpeed);
        }

        // TODO max speed handling

        object.setPosition(new Position(
                object.getPosition().getX() + Math.cos(Math.toRadians(object.getAngle())) * object.getVelocity(),
                object.getPosition().getY() + Math.sin(Math.toRadians(object.getAngle())) * object.getVelocity()
        ));
    }

    static <T extends IMovableObject> ArrayDeque<Position> getSteppedPositions(IChangeMovableObject<T> moving, T object, int count) {
        ArrayDeque<Position> result = new ArrayDeque<>();
        result.add(object.getPosition());

        for(int i = 0; i < count; ++i) {
            moving.moveToNext(object);
            result.add(object.getPosition());
        }

        return result;
    }

    class FixMove<T extends IMovableObject> implements IChangeMovableObject<T> {
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

    class ZeroMove<T extends IMovableObject> extends FixMove<T> {
        public ZeroMove() {
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

            double angle = history.get(history.size() - 1).getAngle() - history.get(history.size() - 2).getAngle();

            if(angle > 180.0) angle = angle - 360;
            if(angle < -180.0) angle = angle + 360;

            return angle;
        }

        private static double getAccelerationDelta(List<Entity> history) {
            if(history.size() < 2 || history.get(history.size() - 2) == null)
                return 0;

            return history.get(history.size() - 1).getVelocity() - history.get(history.size() - 2).getVelocity();
        }
    }
}
