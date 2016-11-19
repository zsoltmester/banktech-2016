package hu.javachallenge.strategy;

import hu.javachallenge.bean.Entity;
import hu.javachallenge.bean.MovableObject;
import hu.javachallenge.bean.Position;
import hu.javachallenge.bean.Submarine;
import hu.javachallenge.map.IMap;
import hu.javachallenge.strategy.moving.IChangeMovableObject;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class MoveUtil {

    private static final int DISTANCE_FROM_ENTITY_WHEN_EVADE = 75;

    public static IMap map = IMap.MapConfig.getMap();

    public static double velocityDistance(double first, double second) {
        double targetTurn = second - first;
        if(targetTurn > 180.0) targetTurn = targetTurn - 360;
        if(targetTurn < -180.0) targetTurn = targetTurn + 360;

        return targetTurn;
    }

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

        return position.length();
    }

    public static double getAngleForTargetPosition(Position from, Position targetPosition) {
        double sx = from.getX();
        double sy = from.getY();
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
        double toAngleInDegree = getAngleForTargetPosition(submarine.getPosition(), targetPosition);

        Integer maxSteering = map.getConfiguration().getMaxSteeringPerRound();

        double targetTurn = velocityDistance(currentDirection, toAngleInDegree);

        if(targetTurn > maxSteering) {
            targetTurn = maxSteering;
        } else if(targetTurn < -maxSteering) {
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
        double toAngleInDegree = getAngleForTargetPosition(submarine.getPosition(), targetPosition);

        double targetTurn = velocityDistance(currentDirection, toAngleInDegree);

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

    public static Position getPositionWhereShootTarget(Position from, Entity targetEntity) {
        // http://stackoverflow.com/a/2249237

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

    public static Position getPositionWhereShootMovingTarget(Position from, Entity entity,
                                                             double entitySize) {
        List<Entity> history = map.getHistory(entity.getId(), 2);
        IChangeMovableObject<Entity> entityMover = new IChangeMovableObject.HistoryMove(history);

        double torpedoSpeed = map.getConfiguration().getTorpedoSpeed();

        Entity entityClone;
        try {
            entityClone = (Entity) entity.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }

        int maxStep = (int) Math.ceil(new Position(map.getConfiguration().getWidth(),
                map.getConfiguration().getHeight()).length() / torpedoSpeed); // upper bound of steps

        for(int i = 1; i <= maxStep; ++i) {
            entityMover.moveToNext(entityClone);
            if(!map.isValidPosition(entityClone.getPosition())) {
                return null;
            }
            if(Math.abs(entityClone.getPosition().distance(from) - torpedoSpeed * i) <= entitySize) {
                return entityClone.getPosition();
            }
        }
        return null;
    }

    public static Position evadeThis(Submarine submarine, Position destination,
                                     MovableObject object, double radius) {

        // need some big changes
        Position result = new Position(destination.getX(), destination.getY());
        result.translate(submarine.getPosition());

        result = new Position(-result.getY(), result.getX());

        result.normalize();

        Position p1 = new Position(
                object.getPosition().getX() + result.getX() * (radius + map.getConfiguration().getSubmarineSize() * 2),
                object.getPosition().getY() + result.getY() * (radius + map.getConfiguration().getSubmarineSize() * 2));
        Position p2 = new Position(
                object.getPosition().getX() - result.getX() * (radius + map.getConfiguration().getSubmarineSize() * 2),
                object.getPosition().getY() - result.getY() * (radius + map.getConfiguration().getSubmarineSize() * 2));

        double distance1 = p1.distance(submarine.getPosition());
        double distance2 = p2.distance(submarine.getPosition());
        return distance1 < distance2 ? p1 : p2;
    }

    /**
     * Evade a non moving object.
     */
    public static Position evadeIsland(Submarine submarine, Position destination, Position entityToEvade, double radius) {
        Double objectX = entityToEvade.getX();
        Double objectY = entityToEvade.getY();
        List<Position> possibleEvadePositions = Arrays.asList(
                new Position(objectX + radius + DISTANCE_FROM_ENTITY_WHEN_EVADE, objectY),
                new Position(objectX - radius - DISTANCE_FROM_ENTITY_WHEN_EVADE, objectY),
                new Position(objectX, objectY + radius + DISTANCE_FROM_ENTITY_WHEN_EVADE),
                new Position(objectX, objectY - radius - DISTANCE_FROM_ENTITY_WHEN_EVADE));

        possibleEvadePositions = possibleEvadePositions.stream()
                .filter(evadePoint -> getCircleLineIntersectionPoint(submarine.getPosition(), evadePoint, entityToEvade, radius).isEmpty()
                        && getCircleLineIntersectionPoint(evadePoint, destination, entityToEvade, radius).isEmpty())
                .filter(evadePoint -> map.isValidPosition(evadePoint))
                .collect(Collectors.toList());

        if (possibleEvadePositions.isEmpty()) {
            // TODO if can't evade with 2 line
            return submarine.getPosition(); // stay
        }

        return possibleEvadePositions.get(0);
    }

    // http://stackoverflow.com/a/13055116
    public static List<Position> getCircleLineIntersectionPoint(Position pointA, Position pointB, Position center, double radius) {
        double baX = pointB.getX() - pointA.getX();
        double baY = pointB.getY() - pointA.getY();
        double caX = center.getX() - pointA.getX();
        double caY = center.getY() - pointA.getY();

        double a = baX * baX + baY * baY;
        double bBy2 = baX * caX + baY * caY;
        double c = caX * caX + caY * caY - radius * radius;

        double pBy2 = bBy2 / a;
        double q = c / a;

        double disc = pBy2 * pBy2 - q;
        if (disc < 0) {
            return Collections.emptyList();
        }
        // if disc == 0 ... dealt with later
        double tmpSqrt = Math.sqrt(disc);
        double abScalingFactor1 = -pBy2 + tmpSqrt;
        double abScalingFactor2 = -pBy2 - tmpSqrt;

        Position p1 = new Position(pointA.getX() - baX * abScalingFactor1, pointA.getY()
                - baY * abScalingFactor1);
        if (disc == 0) { // abScalingFactor1 == abScalingFactor2
            return Collections.singletonList(p1);
        }
        Position p2 = new Position(pointA.getX() - baX * abScalingFactor2, pointA.getY()
                - baY * abScalingFactor2);
        return Arrays.asList(p1, p2);
    }
}
