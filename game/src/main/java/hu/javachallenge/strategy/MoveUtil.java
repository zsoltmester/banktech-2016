package hu.javachallenge.strategy;

import hu.javachallenge.bean.Entity;
import hu.javachallenge.bean.Position;
import hu.javachallenge.bean.Submarine;
import hu.javachallenge.map.Map;

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
        double currentDirection = submarine.getAngle();
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

    public static double getAngleForShootTarget(Submarine submarine, Entity targetEntity) {
        // TODO

        return 0.0;
    }
}
