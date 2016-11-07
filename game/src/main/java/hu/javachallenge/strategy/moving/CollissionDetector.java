package hu.javachallenge.strategy.moving;

import hu.javachallenge.bean.Entity;
import hu.javachallenge.bean.MovableObject;
import hu.javachallenge.bean.Position;
import hu.javachallenge.map.IMap;
import hu.javachallenge.strategy.MoveStrategy;

import java.util.List;

/**
 * Created by qqcs on 07/11/16.
 */
public class CollissionDetector {
    private static IMap map = IMap.MapConfig.getMap();

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
        MovingIsland island = new MovingIsland(islandPos);
        return submarineCollision(strategy,
                island, island, map.getConfiguration().getIslandSize(), step);
    }

    public static Integer submarineCollisionWithEntity(MoveStrategy strategy, Entity entity, int step) {
        IChangeMovableObject<Entity> entityMover = new IChangeMovableObject.ZeroMove<>();

        double radius = entity.getType().equals(Entity.SUBMARINE) ?
                map.getConfiguration().getSubmarineSize() :
                map.getConfiguration().getTorpedoRange();

        return submarineCollision(strategy, entity, entityMover, radius, step);
    }

    public static Integer entityCollisionWithEntityHistory(Entity entity, Entity historicalEntity, int step) {
        List<Entity> entityHistory = map.getHistory(historicalEntity.getId(), 2);

        double radius1 = entity.getType().equals(Entity.SUBMARINE) ?
                map.getConfiguration().getSubmarineSize() :
                map.getConfiguration().getTorpedoRange();

        double radius2 = historicalEntity.getType().equals(Entity.SUBMARINE) ?
                map.getConfiguration().getSubmarineSize() :
                map.getConfiguration().getTorpedoRange();

        IChangeMovableObject<Entity> entityMover = new IChangeMovableObject.HistoryMove(entityHistory);

        return collisionWith(entity, new IChangeMovableObject.ZeroMove<>(), radius1,
                historicalEntity, entityMover, radius2, step);
    }
}
