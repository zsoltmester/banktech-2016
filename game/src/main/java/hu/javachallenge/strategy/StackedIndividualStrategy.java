package hu.javachallenge.strategy;

import hu.javachallenge.bean.Position;
import hu.javachallenge.bean.Submarine;
import hu.javachallenge.processor.Processor;

import java.util.ArrayList;
import java.util.List;

public class StackedIndividualStrategy extends IndividualStrategy {

    private static final int GAP_WHEN_MOVING_IN_GROUP = 60;
    private static final int SHIFT_WHEN_MOVING_IN_GROUP = 1;

    @Override
    public void init() {
        Processor.updateOurSubmarines();

        double radarDistance = map.getConfiguration().getExtendedSonarRange();

        int i = 0;
        for (Submarine submarine : map.getOurSubmarines()) {

            /*
            ///// same points for all
            Position[] positions = new Position[] {
                    new Position(radarDistance, map.getConfiguration().getHeight() - radarDistance),
                    new Position(map.getConfiguration().getWidth() - radarDistance, map.getConfiguration().getHeight() - radarDistance),
                    new Position(map.getConfiguration().getWidth() - radarDistance, radarDistance),
                    new Position(radarDistance, radarDistance),
            };
            */

            /*
            ///// moving in a group, with a gap between each other
            int k = i - SHIFT_WHEN_MOVING_IN_GROUP;
            Position[] positions = new Position[]{
                    new Position(radarDistance + k * GAP_WHEN_MOVING_IN_GROUP, map.getConfiguration().getHeight() - radarDistance - k * GAP_WHEN_MOVING_IN_GROUP),
                    new Position(map.getConfiguration().getWidth() - radarDistance - k * GAP_WHEN_MOVING_IN_GROUP, map.getConfiguration().getHeight() - radarDistance - k * GAP_WHEN_MOVING_IN_GROUP),
                    new Position(map.getConfiguration().getWidth() - radarDistance - k * GAP_WHEN_MOVING_IN_GROUP, radarDistance + k * GAP_WHEN_MOVING_IN_GROUP),
                    new Position(radarDistance + k * GAP_WHEN_MOVING_IN_GROUP, radarDistance + k * GAP_WHEN_MOVING_IN_GROUP),
            };
            */

            ///// moving in a group and form a triangle
            double x = submarine.getPosition().getX() + Math.cos(Math.toRadians(120)) * submarine.getVelocity();
            double y = submarine.getPosition().getY() + Math.sin(Math.toRadians(120)) * submarine.getVelocity();
            Position[] positions = new Position[]{
                    MoveUtil.getPositionToAngle(new Position(radarDistance, map.getConfiguration().getHeight() - radarDistance), i * 120, GAP_WHEN_MOVING_IN_GROUP),
                    MoveUtil.getPositionToAngle(new Position(map.getConfiguration().getWidth() - radarDistance, map.getConfiguration().getHeight() - radarDistance), i * 120, GAP_WHEN_MOVING_IN_GROUP),
                    MoveUtil.getPositionToAngle(new Position(map.getConfiguration().getWidth() - radarDistance, radarDistance), i * 120, GAP_WHEN_MOVING_IN_GROUP),
                    MoveUtil.getPositionToAngle(new Position(radarDistance, radarDistance), i * 120, GAP_WHEN_MOVING_IN_GROUP),
            };

            // order the targets to start to scout the (nearest/farthest) point
            int firstTargetIndex = 0;
            for (int j = 1; j < positions.length; ++j) {
                if (positions[firstTargetIndex].distance(submarine.getPosition()) > positions[j].distance(submarine.getPosition())) { // nearest
                    //if (positions[firstTargetIndex].distance(submarine.getPosition()) < positions[j].distance(submarine.getPosition())) { // farthest
                    firstTargetIndex = j;
                }
            }

            List<Position> orderedTargets = new ArrayList<>(positions.length);
            for (int j = firstTargetIndex; j < firstTargetIndex + positions.length; j++) {
                orderedTargets.add(positions[j % positions.length]);
            }

            Strategy strategy = new ScoutStrategy(submarine.getId(), orderedTargets);
            strategy.init();
            strategies.put(submarine.getId(), strategy);
            ++i;
        }
    }
}
