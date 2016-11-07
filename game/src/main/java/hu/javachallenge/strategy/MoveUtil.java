package hu.javachallenge.strategy;

import hu.javachallenge.bean.Entity;
import hu.javachallenge.bean.Position;
import hu.javachallenge.bean.Submarine;
import hu.javachallenge.map.IMap;
import hu.javachallenge.strategy.moving.IChangeMovableObject;

import java.util.ArrayDeque;
import java.util.OptionalDouble;
import java.util.stream.DoubleStream;

public class MoveUtil {

    private static IMap map = IMap.MapConfig.getMap();

    public static ArrayDeque<Position> stepForward(Position from, Double direction, Double speed,
                                                   Double deltaDirection, Double deltaSpeed, int count) {
        Entity entity = new Entity();
        entity.setPosition(from);
        entity.setAngle(direction);
        entity.setVelocity(speed);

        IChangeMovableObject<Entity> movingObject = new IChangeMovableObject.FixMove<>(deltaSpeed, deltaDirection);

        return IChangeMovableObject.getSteppedPositions(movingObject, entity, count);
    }

    public static double distanceToTurnNDegreesOnMaxSpeed(double degree) {
        Double maxSteering = map.getConfiguration().getMaxSteeringPerRound().doubleValue();
        Double maxSpeed = map.getConfiguration().getMaxSpeed().doubleValue();

        degree = Math.abs(degree);

        int countOfStep = (int) Math.ceil(degree / maxSteering);

        Position position = stepForward(new Position(0, 0), 0.0, maxSpeed, maxSteering, 0.0, countOfStep).getLast();

        return position.distance(new Position(0, 0));
    }

    public static double getAngleForTargetPosition(Submarine submarine, Position targetPosition) {
        Position submarinePosition = submarine.getPosition();

        double sx = submarinePosition.getX();
        double sy = submarinePosition.getY();
        double tx = targetPosition.getX();
        double ty = targetPosition.getY();

        double vecX = tx - sx;
        double vecY = ty - sy;

        double angleRad = Math.atan2(vecY, vecX);
        double toAngleInDegree = Math.toDegrees(angleRad);

        if(toAngleInDegree < 0) // positive correction -> 0 - 360
            toAngleInDegree = 360.0 + toAngleInDegree;

        return toAngleInDegree;
    }

    public static double getTurnForTargetPosition(Submarine submarine, Position targetPosition) {
        double currentDirection = submarine.getAngle();
        double toAngleInDegree = getAngleForTargetPosition(submarine, targetPosition);

        Integer maxSteering = map.getConfiguration().getMaxSteeringPerRound();

        double targetTurn = toAngleInDegree - currentDirection;
        if(targetTurn > 180.0) targetTurn = targetTurn - 360;
        if(targetTurn < -180.0) targetTurn = targetTurn + 360;

        if(Math.abs(targetTurn) > maxSteering) {
            if(targetTurn > 0)
                targetTurn = maxSteering;
            if(targetTurn < 0)
                targetTurn = -maxSteering;
        }

        return targetTurn;
    }

    public static double getAccelerationToCloseThere(Submarine submarine, Position targetPosition) {
        double distance = submarine.getPosition().distance(targetPosition);
        double speed = submarine.getVelocity();

        double maxAcceleration = map.getConfiguration().getMaxAccelerationPerRound();
        double maxSpeed = map.getConfiguration().getMaxSpeed();

        // solution of 5 * i*(i+1) / 2 = x + 2.5 equality rounded down
        // the sum of 5th multiples to distance (2.5 distance proximitly)
        double expectedAcceleration = Math.floor((Math.sqrt((2 * maxAcceleration * 4) * (distance + (maxAcceleration / 2)) + maxAcceleration * maxAcceleration) - maxAcceleration) / (2 * maxAcceleration)) * maxAcceleration - speed;

        expectedAcceleration = Math.max(-maxAcceleration, Math.min(maxAcceleration, expectedAcceleration));

        if(expectedAcceleration + speed > maxSpeed) {
            expectedAcceleration = 0;
        }
        if(expectedAcceleration + speed < 0) {
            expectedAcceleration = 0;
        }

        return expectedAcceleration;
    }

    public static double getAccelerationToCloseThereWhenOnRightDirection(Submarine submarine, Position targetPosition) {
        double currentDirection = submarine.getAngle();
        double toAngleInDegree = getAngleForTargetPosition(submarine, targetPosition);

        double targetTurn = toAngleInDegree - currentDirection;
        if(targetTurn > 180.0) targetTurn = targetTurn - 360;
        if(targetTurn < -180.0) targetTurn = targetTurn + 360;

        double maxAcceleration = map.getConfiguration().getMaxAccelerationPerRound();
        double currentSpeed = submarine.getVelocity();

        double acceleration = getAccelerationToCloseThere(submarine, targetPosition);

        boolean badAngle = Math.abs(targetTurn) > 90.0 || (Math.abs(targetTurn) > 30.0 &&
                submarine.getPosition().distance(targetPosition) < distanceToTurnNDegreesOnMaxSpeed(targetTurn));

        if(badAngle) {
            // WRONG WAY
            return currentSpeed == 0.0 ? 0.0 : -maxAcceleration;
        }

        return acceleration;
    }

    public static Position getPositionWhereShootTarget(Submarine submarine, Entity targetEntity) {
        // http://stackoverflow.com/a/2249237

        Position from = submarine.getPosition();
        Position target = targetEntity.getPosition();
        Position targetVelocity =
                new Position(targetEntity.getVelocity() * Math.cos(Math.toRadians(targetEntity.getAngle())),
                        targetEntity.getVelocity() * Math.sin(Math.toRadians(targetEntity.getAngle())));

        double speed = map.getConfiguration().getTorpedoSpeed();

        // a * x^2 + b * x + c = 0

        double a = targetVelocity.getX() * targetVelocity.getX() +
                targetVelocity.getY() * targetVelocity.getY() -
                speed * speed;

        double b = 2 * (targetVelocity.getX() * (target.getX() - from.getX()) +
                targetVelocity.getY() * (target.getY() - from.getY()));

        double c = (target.getX() - from.getX()) * (target.getX() - from.getX()) +
                (target.getY() - from.getY()) * (target.getY() - from.getY());

        double discriminant = b * b - 4 * a * c;

        if(discriminant < 0) return null;

        double t1 = (-b + Math.sqrt(discriminant)) / (2 * a);
        double t2 = (-b - Math.sqrt(discriminant)) / (2 * a);

        OptionalDouble first = DoubleStream.of(t1, t2)
                .filter(d -> d > 0)
                .sorted().findFirst();

        if(first.isPresent()) {
            double time = first.getAsDouble();

            return new Position(time * targetVelocity.getX() + target.getX(),
                    time * targetVelocity.getY() + target.getY());
        }

        return null;
    }
}
