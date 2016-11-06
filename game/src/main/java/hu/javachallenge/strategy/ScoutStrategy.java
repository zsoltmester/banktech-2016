package hu.javachallenge.strategy;

import hu.javachallenge.bean.Entity;
import hu.javachallenge.bean.Position;
import hu.javachallenge.bean.Submarine;
import hu.javachallenge.map.IMap;
import hu.javachallenge.processor.Processor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

public class ScoutStrategy implements Strategy {

    private IMap map = IMap.MapConfig.getMap();

    private ArrayList<Deque<Position>> submarinesDestinations = new ArrayList<>();

    @Override
    public void init() {
        int submarinesCount= map.getConfiguration().getSubmarinesPerTeam();
        double part = map.getConfiguration().getWidth() / submarinesCount;
        double radarDistance = map.getConfiguration().getSonarRange();
        for(int i = 0; i < submarinesCount; ++i) {
            Deque<Position> positions = new ArrayDeque<>();

            positions.push(new Position(i * part + radarDistance, radarDistance));
            positions.push(new Position((i+1) * part - radarDistance, radarDistance));
            positions.push(new Position((i+1) * part - radarDistance, map.getConfiguration().getHeight() - radarDistance));
            positions.push(new Position(i * part + radarDistance, map.getConfiguration().getHeight() - radarDistance));

            submarinesDestinations.add(positions);
        }
    }

    @Override
    public void onNewRound() {
        for(Submarine submarine : map.getOurSubmarines()) {
            if (submarine.getSonarCooldown() == 0) {
                Processor.extendSonar(submarine.getId());
                submarine.setSonarExtended(map.getConfiguration().getExtendedSonarRounds());
                submarine.setSonarCooldown(map.getConfiguration().getExtendedSonarCooldown());
            }
            Processor.sonar(submarine.getId());
        }

        int submarineIndex = 0;
        for(Submarine submarine : map.getOurSubmarines()) {
            // TODO calculate position
            // move
            Deque<Position> targets = submarinesDestinations.get(submarineIndex);

            if(submarine.getPosition().distance(targets.peek()) < 10) {
                targets.add(targets.pop());
            }

            Processor.move(submarine.getId(),
                    MoveUtil.getAccelerationToCloseThere(submarine, targets.peek()),
                    MoveUtil.getTurnForTargetPosition(submarine, targets.peek()));

            // shooting
            if(submarine.getTorpedoCooldown() == 0) {
                Position toShoot = map.getEntities().stream().filter(e -> !e.getOwner().getName().equals(IMap.OUR_NAME))
                        .filter(e -> e.getType().equals(Entity.SUBMARINE))
                        .map(e -> MoveUtil.getPositionWhereShootTarget(submarine, e))
                        .filter(map::isValidPosition)
                        .filter(p -> p.distance(submarine.getPosition()) >
                                map.getConfiguration().getTorpedoExplosionRadius())
                        .sorted((p1, p2) ->
                                Double.compare(p1.distance(submarine.getPosition()),
                                        p2.distance(submarine.getPosition()) ))
                        .findFirst().orElse(null);

                if(toShoot != null) {
                    double direction = MoveUtil.getAngleForTargetPosition(submarine, toShoot);
                    Processor.shoot(submarine.getId(), direction);
                }
            }
            ++submarineIndex;
        }
    }
}
