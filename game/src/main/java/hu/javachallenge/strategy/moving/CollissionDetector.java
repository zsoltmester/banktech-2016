package hu.javachallenge.strategy.moving;

import hu.javachallenge.bean.Entity;
import hu.javachallenge.bean.MovableObject;
import hu.javachallenge.bean.Position;
import hu.javachallenge.map.IMap;
import hu.javachallenge.strategy.MoveStrategy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by qqcs on 07/11/16.
 */
public class CollissionDetector {
    private static IMap map = IMap.MapConfig.getMap();

    public static int getEntitySize(Entity entity) {
        switch (entity.getType())
        {
            case Entity.SUBMARINE:
                return map.getConfiguration().getSubmarineSize();
            case Entity.TORPEDO:
                return 5;
            case MovingIsland.ISLAND:
                return map.getConfiguration().getIslandSize();
            default:
                assert false;
                return 0;
        }
    }

    public static <T1 extends MovableObject, T2 extends MovableObject> Integer collisionWith(
            T1 object1, IChangeMovableObject<T1> object1Moves, double object1radius,
            T2 object2, IChangeMovableObject<T2> object2Moves, double object2radius, int step) {


        T1 object1Clone;
        T2 object2Clone;
        try {
            object1Clone = (T1) object1.clone();
            object2Clone = (T2) object2.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }

        for(int i = 1; i <= step; ++i) {
            object1Moves.moveToNext(object1Clone);
            object2Moves.moveToNext(object2Clone);

            if(!map.isValidPosition(object1Clone.getPosition()) ||
                    !map.isValidPosition(object2Clone.getPosition())) {
                break;
            }

            if(object1Clone.getPosition().distance(object2Clone.getPosition()) < object1radius + object2radius) {
                return i;
            }
        }
        return null;
    }

    public static <T extends MovableObject> Integer submarineCollision(MoveStrategy strategy,
                                                                       T object, IChangeMovableObject<T> objectMoving, double radius, int step) {
        return collisionWith(strategy.getSubmarine(), strategy, map.getConfiguration().getSubmarineSize(),
                object, objectMoving, radius, step);
    }

    public static Integer submarineCollisionWithIsland(MoveStrategy strategy, Position islandPos, int step) {
        return submarineCollisionWithEntity(strategy, new MovingIsland(islandPos), step);
    }

    public static Integer submarineCollisionWithEntity(MoveStrategy strategy, Entity entity, int step) {
        return submarineCollision(strategy, entity, IChangeMovableObject.ZERO_MOVE, getEntitySize(entity), step);
    }

    public static Integer entityCollisionWithEntityHistory(Entity entity, Entity historicalEntity, int step) {
        IChangeMovableObject<Entity> entityMover = new IChangeMovableObject.HistoryMove(
                map.getHistory(historicalEntity.getId(), 2));

        return collisionWith(entity, IChangeMovableObject.ZERO_MOVE, getEntitySize(entity),
                historicalEntity, entityMover, getEntitySize(historicalEntity), step);
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
