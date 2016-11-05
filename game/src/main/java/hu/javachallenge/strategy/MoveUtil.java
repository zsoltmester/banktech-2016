package hu.javachallenge.strategy;

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

    public static double getTurnForTargetPosition(Submarine submarine, Position targetPosition) {
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

        Integer maxSteering = map.getConfiguration().getMaxSteeringPerRound();

        double steering = Math.abs(toAngleInDegree - currentDirection);
        if(steering > 180.0) steering = 360 - steering;

        if(steering > maxSteering) {
            // + or - direction?
            double diff = toAngleInDegree - currentDirection;
            if(diff < 0) diff = 360.0 + diff;

            if(diff > 180.0) {
                toAngleInDegree = currentDirection - maxSteering;
            } else {
                toAngleInDegree = currentDirection + maxSteering;
            }
        }

        return toAngleInDegree;
    }
}
