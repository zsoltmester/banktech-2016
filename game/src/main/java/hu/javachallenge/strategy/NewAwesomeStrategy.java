package hu.javachallenge.strategy;

import hu.javachallenge.bean.Position;
import hu.javachallenge.bean.Submarine;
import hu.javachallenge.map.IMap;
import hu.javachallenge.processor.Processor;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by qqcs on 22/11/16.
 */
public class NewAwesomeStrategy implements Strategy {

    protected IMap map = IMap.MapConfig.getMap();
    protected Map<Long, Strategy> strategies = new HashMap<>();

    private ArrayDeque<Position> targets;
    private double distance;
    private int plus = 0;
    private int maxDistanceToTarget;

    class MyMoveStrategy extends MoveStrategy {
        private int myId;

        protected MyMoveStrategy(Long submarineId, int myId) {
            super(submarineId);
            this.myId = myId;
        }

        @Override
        public void init() {
        }

        private Position getFinalPosition() {
            return MoveUtil.getPositionToAngle(targets.getFirst(), (120 * myId + plus) % 360, distance);
        }

        @Override
        public double getNextAcceleration(Submarine submarine) {
            return MoveUtil.getAccelerationToCloseThereWhenOnRightDirection(submarine, getFinalPosition());
        }

        @Override
        public double getNextSteering(Submarine submarine) {
            return MoveUtil.getTurnForTargetPosition(submarine, getFinalPosition());
        }

        @Override
        public Strategy onChangeStrategy() {
            return super.onChangeStrategy();
        }

        public boolean closeToTarget() {
            Submarine sm = getSubmarine();
            if(sm == null) return true;

            return sm.getPosition().distance(getFinalPosition()) <= maxDistanceToTarget;
        }
    }

    @Override
    public void init() {
        Processor.updateOurSubmarines();

        int submarinesCount = map.getConfiguration().getSubmarinesPerTeam();
        double part = map.getConfiguration().getWidth() / submarinesCount;
        double radarDistance = map.getConfiguration().getExtendedSonarRange();

        int i = 0;
        for (Submarine submarine : map.getOurSubmarines()) {
            strategies.put(submarine.getId(), new MyMoveStrategy(submarine.getId(), i));
            ++i;
        }
        distance = map.getConfiguration().getTorpedoExplosionRadius() + map.getConfiguration().getSubmarineSize() * 2;
        maxDistanceToTarget = map.getConfiguration().getSubmarineSize() * 2;

        targets = new ArrayDeque<>();
        targets.push(new Position(450, 200));
        targets.push(new Position(850, 400));
        targets.push(new Position(1275, 600));
        targets.push(new Position(1475, 400));
        targets.push(new Position(1275, 200));
        targets.push(new Position(850, 400));
        targets.push(new Position(450, 600));
        targets.push(new Position(250, 400));

        Position p = map.getOurSubmarines().get(0).getPosition();
        int minindex = 0;
        double minDist = Double.POSITIVE_INFINITY;
        int ind = 0;
        for(Position tar : targets) {
            if(tar.distance(p) < minDist) {
                minindex = ind;
                minDist = tar.distance(p);
            }
            ++ind;
        }
        for(ind = 0; ind < minindex; ++ind) {
            targets.addLast(targets.getFirst());
            targets.removeFirst();
        }
    }

    @Override
    public void onStartRound() {
        boolean closeTo = true;
        for(Strategy strategy : strategies.values()) {
            strategy.onStartRound();
            if(strategy instanceof MyMoveStrategy) {
                closeTo &= ((MyMoveStrategy) strategy).closeToTarget();
            } else {
                closeTo = false;
            }
        }
        if(closeTo) {
            targets.addLast(targets.getFirst());
            targets.removeFirst();
        }
    }

    @Override
    public void onRound() {
        for(Strategy strategy : strategies.values()) {
            strategy.onRound();
        }
        plus+= 5;
    }

    @Override
    public Strategy onChangeStrategy() {
        for(Map.Entry<Long, Strategy> strategy : strategies.entrySet()) {
            Strategy str  = strategy.getValue().onChangeStrategy();
            if(str != null) {
                strategy.setValue(str);
            }
        }
        return null;
    }
}
