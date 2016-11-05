package hu.javachallenge.strategy;

import hu.javachallenge.bean.Entity;
import hu.javachallenge.bean.Position;
import hu.javachallenge.bean.Submarine;
import hu.javachallenge.map.Map;

import java.util.OptionalDouble;
import java.util.stream.DoubleStream;

public class MoveUtil {

    private static Map map = Map.MapConfig.getMap();

    public static double getAccelerationForTargetSpeed(Submarine submarine, double targetSpeed) {
        int maxSpeed = map.getConfiguration().getMaxSpeed();
        double maxAccelerationPerRound = map.getConfiguration().getMaxAccelerationPerRound();
        double currentSpeed = submarine.getVelocity();

        double targetAcceleration = Math.abs(targetSpeed - currentSpeed) > maxAccelerationPerRound
                ? (targetSpeed > currentSpeed ? maxAccelerationPerRound : -maxAccelerationPerRound)
                : targetSpeed - currentSpeed;

        if (targetAcceleration + currentSpeed > maxSpeed) {
            targetAcceleration = maxSpeed - currentSpeed;
        }

        return targetAcceleration;
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

    public static double getAccelerationToGoThere(Submarine submarine, Position targetPosition) {
        // TODO

        return 0.0;
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
        System.out.println(t1 + " " + t2);
        OptionalDouble first = DoubleStream.of(t1, t2)
                .filter(d -> d > 0)
                .sorted().findFirst();

        if(first.isPresent()) {
            double time = first.getAsDouble();

            Position toTarget = new Position(time * targetVelocity.getX() + target.getX(),
                    time * targetVelocity.getY() + target.getY());

            System.out.println(toTarget);

            return toTarget;
        }

        return null;
    }
}
