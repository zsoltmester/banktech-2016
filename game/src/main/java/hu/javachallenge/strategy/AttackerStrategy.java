package hu.javachallenge.strategy;

import hu.javachallenge.bean.Entity;
import hu.javachallenge.bean.Position;
import hu.javachallenge.bean.Submarine;
import hu.javachallenge.map.IMap;
import hu.javachallenge.processor.Processor;
import hu.javachallenge.strategy.moving.CollissionDetector;
import hu.javachallenge.strategy.moving.IChangeMovableObject;
import hu.javachallenge.strategy.moving.MovingIsland;

import java.util.logging.Logger;
import java.util.stream.Stream;

public class AttackerStrategy extends SubmarineStrategy {

    private static final Logger LOGGER = Logger.getLogger(AttackerStrategy.class.getName());

    private MoveStrategy parent;

    protected AttackerStrategy(Long submarineId, MoveStrategy parent) {
        super(submarineId);
        this.parent = parent;
    }

    @Override
    public void init() {

    }

    // Need to be Stream -s
    Stream<Position> filterPointsToShoot(Stream<Position> possiblePositions) {
        Submarine submarine = getSubmarine();
        double neeedDistanceFromTorpedo = map.getConfiguration().getTorpedoExplosionRadius()
                + map.getConfiguration().getSubmarineSize();

        return possiblePositions.peek(e -> LOGGER.fine("Position where: " + e))
                .filter(map::isValidPosition)
                .filter(p -> p.distance(submarine.getPosition()) >
                        neeedDistanceFromTorpedo)
                .peek(e -> LOGGER.fine("Bigger distance then torpedo explosion"))
                .filter(p -> IChangeMovableObject.getSteppedPositions(
                        IChangeMovableObject.ZERO_MOVE, submarine, 2).stream().allMatch(stp ->
                        p.distance(stp) >= neeedDistanceFromTorpedo))
                .peek(e -> LOGGER.fine("Bigger distance then torpedo explosion 2 tick later"))
                .filter(p -> p.distance(submarine.getPosition()) <
                        map.getConfiguration().getTorpedoRange() *
                                (1 + map.getConfiguration().getTorpedoSpeed()))
                .peek(e -> LOGGER.fine("Distance is good for us"))
                .filter(p -> {
                    Entity torpedo = new Entity();
                    torpedo.setPosition(submarine.getPosition());
                    torpedo.setVelocity(map.getConfiguration().getTorpedoSpeed());
                    torpedo.setAngle(MoveUtil.getAngleForTargetPosition(submarine.getPosition(), p));
                    torpedo.setType(Entity.TORPEDO);
                    torpedo.setId(-1L);

                    // not shoot islands
                    return map.getConfiguration().getIslandPositions().stream()
                            .map(MovingIsland::new).allMatch(island ->
                                    CollissionDetector.collisionWith(
                                            torpedo, IChangeMovableObject.ZERO_MOVE, 1,
                                            island, island,
                                            map.getConfiguration().getIslandSize(),
                                            (int) Math.ceil(submarine.getPosition().distance(p) /
                                                    map.getConfiguration().getTorpedoSpeed())) == null)

                            &&
                            // not shoot anyone else
                            map.getEntities().stream().filter(e -> e.getType().equals(Entity.SUBMARINE))
                                    .allMatch(e -> {
                                        Integer time = CollissionDetector.entityCollisionWithEntityHistory(torpedo,
                                                e, 100);
                                        if(time != null) {
                                            Position otherExplosion =
                                                    IChangeMovableObject.getSteppedPositions(IChangeMovableObject.ZERO_MOVE,
                                                            torpedo, time).getLast();
                                            boolean result = otherExplosion.distance(submarine.getPosition())
                                                    > neeedDistanceFromTorpedo;

                                            result &= IChangeMovableObject.getSteppedPositions(
                                                    IChangeMovableObject.ZERO_MOVE, submarine, 2).stream()
                                                    .allMatch(stp -> otherExplosion.distance(stp) >
                                                            neeedDistanceFromTorpedo);

                                            return result;
                                        }
                                        return true;
                                    });

                })
                .peek(e -> LOGGER.fine("Not shoot island or anyone else close to us"));
    }

    @Override
    public void onRound() {
        Submarine submarine = getSubmarine();

        // TODO following someone if we want.

        if (submarine.getTorpedoCooldown() == 0) {
            Position toShoot = filterPointsToShoot(
                    map.getEntities().stream()
                        .filter(e -> !e.getOwner().getName().equals(IMap.OUR_NAME))
                        .filter(e -> e.getType().equals(Entity.SUBMARINE))
                        .peek(e -> LOGGER.fine("Entity to shoot: " + e))
                        .map(e -> MoveUtil.getPositionWhereShootMovingTarget(submarine.getPosition(), e,
                                map.getConfiguration().getSubmarineSize()))
                    )
                    .sorted((p1, p2) ->
                            Double.compare(p1.distance(submarine.getPosition()),
                                    p2.distance(submarine.getPosition())))
                    .findFirst().orElse(null);
            /*
            if (toShoot == null) { // historical shoot
                List<Long> targetIds = map.getAllHistory().values().stream()
                        .flatMap(e -> e.entrySet().stream())
                        .filter(p -> p.getValue().getType().equals(Entity.SUBMARINE))
                        .filter(p -> !p.getValue().getOwner().getName().equals(IMap.OUR_NAME))
                        .map(Map.Entry::getKey)
                        .sorted()
                        .distinct()
                        .collect(Collectors.toList());

                int rounds = map.getConfiguration().getRounds();
                toShoot = filterPointsToShoot(
                    targetIds.stream()
                        .map(id -> map.getHistory(id, rounds))
                        .map(list -> {
                            Entity lastSaw = null;
                            int lastSawTime = 0;
                            for(int i = list.size() - 1; i >= 0; --i) {
                                if(list.get(i) != null) {
                                    lastSaw = list.get(i);
                                    lastSawTime = i;
                                    break;
                                }
                            }
                            LOGGER.fine("Last saw entity " + lastSaw + " in tick: " + (rounds - lastSawTime - 1));
                            // where is now?
                            Position nowPossiblePos =
                                    IChangeMovableObject.getSteppedPositions(IChangeMovableObject.ZERO_MOVE, lastSaw,
                                    rounds - lastSawTime - 1).getLast();

                            if(nowPossiblePos == null) {
                                return null;
                            }
                            LOGGER.fine("Possible position now: " + nowPossiblePos);
                            try {
                                Entity nowPossible = (Entity) lastSaw.clone();
                                nowPossible.setPosition(nowPossiblePos);
                                return MoveUtil.getPositionWhereShootTarget(submarine.getPosition(), nowPossible);
                            } catch (CloneNotSupportedException e) {
                                e.printStackTrace();
                                return null;
                            }
                        })
                    )
                    .sorted((p1, p2) ->
                            Double.compare(p1.distance(submarine.getPosition()),
                                    p2.distance(submarine.getPosition())))
                    .findFirst().orElse(null);
            }
            */
            if (toShoot != null) {
                double direction = MoveUtil.getAngleForTargetPosition(submarine.getPosition(), toShoot);
                Processor.shoot(submarine.getId(), direction);
            }
        }
    }

    @Override
    public Strategy onChangeStrategy() {
        // TODO we should change to something else it if there is nothing to attack
        return null;
    }
}
