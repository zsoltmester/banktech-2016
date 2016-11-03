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
        double sx = submarinePosition.getX();
        double sy = submarinePosition.getY();
        double tx = targetPosition.getX();
        double ty = targetPosition.getY();

        // TODO

        return 0d;
    }
}
