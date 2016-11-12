package hu.javachallenge.communication.offline;

import hu.javachallenge.bean.*;
import hu.javachallenge.map.IMap;
import hu.javachallenge.processor.Processor;
import hu.javachallenge.strategy.MoveUtil;
import hu.javachallenge.strategy.moving.IChangeMovableObject;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

class OfflineGame implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(OfflineGame.class.getName());

    private Game game = new Game();

    private Set<String> teams = new HashSet<>();
    private Map<String, Integer> scores = new HashMap<>();

    private List<Entity> entityList = new ArrayList<>();

    private Map<Long, MoveRequest> moves = new HashMap<>();
    private Map<Long, ShootRequest> shoots = new HashMap<>();
    private Set<Long> extendedSonar = new HashSet<>();

    private Thread tickerThread;

    private long idSeed = 0L;
    private Random random = new Random();

    @Override
    public void run() {
        for (int i = 1; i <= game.getMapConfiguration().getRounds(); ++i) {
            synchronized (this) {
                game.setStatus(Processor.GAME_STATUS.RUNNING.name());
                game.setRound(i);
            }

            try {
                Thread.sleep(game.getMapConfiguration().getRoundLength());
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }

            synchronized (this) {

                /////
                // moving and extend sonar for our submarines
                /////
                for (Submarine submarine : getOurSubmarines()) {
                    MoveRequest moveRequest = moves.get(submarine.getId());
                    Boolean extendSonar = extendedSonar.contains(submarine.getId());

                    // extendSonar
                    if (extendSonar) {
                        submarine.setSonarCooldown(game.getMapConfiguration().getExtendedSonarCooldown());
                        submarine.setSonarExtended(game.getMapConfiguration().getExtendedSonarRounds());
                    } else {
                        if (submarine.getSonarExtended() > 0) {
                            submarine.setSonarExtended(submarine.getSonarExtended() - 1);
                        }
                        if (submarine.getSonarCooldown() > 0) {
                            submarine.setSonarCooldown(submarine.getSonarCooldown() - 1);
                        }
                    }

                    // move
                    IChangeMovableObject<MovableObject> move = IChangeMovableObject.ZERO_MOVE;
                    if (moveRequest != null) {
                        move = new IChangeMovableObject.FixMove<>(moveRequest.getSpeed(), moveRequest.getTurn());
                    }
                    move.moveToNext(submarine);
                }
                extendedSonar.clear();
                moves.clear();

                // TODO extend sonar for enemy submarines

                /////
                // moving enemy submarines
                /////
                entityList.stream()
                        .filter(entity -> entity.getType().equals(Entity.SUBMARINE) && !entity.getOwner().getName().equals(IMap.OUR_NAME))
                        .forEach(entity -> {
                            // TODO enemy moving strategy

                            Submarine submarine = (Submarine) entity;

                            int maxSpeed = game.getMapConfiguration().getMaxSpeed();
                            int maxAccelerationPerRound = game.getMapConfiguration().getMaxAccelerationPerRound();
                            Integer acceleration = submarine.getVelocity() > maxSpeed / 2
                                    ? -maxAccelerationPerRound : maxAccelerationPerRound;

                            new IChangeMovableObject.FixMove(acceleration, (2 * random.nextDouble() - 1) * game.getMapConfiguration().getMaxSteeringPerRound()).moveToNext(submarine);
                            LOGGER.info("Enemy submarine " + submarine.getId() + " position: " + submarine.getPosition());
                        });

                /////
                // destroy submarines which are hit an island or went out from the map
                /////
                entityList = entityList.stream()
                        .filter(entity -> !entity.getType().equals(Entity.SUBMARINE) || (isValidPosition(entity.getPosition()) && game.getMapConfiguration().getIslandPositions().stream().noneMatch(island -> island.distance(entity.getPosition()) < game.getMapConfiguration().getIslandSize())))
                        .collect(Collectors.toList());

                /////
                // shooting with our submarines
                /////
                for (Submarine submarine : getOurSubmarines()) {
                    ShootRequest shootRequest = shoots.get(submarine.getId());

                    // shoot
                    if (shootRequest != null) {
                        Entity torpedo = new Entity();
                        torpedo.setId(++idSeed);
                        torpedo.setPosition(submarine.getPosition());
                        torpedo.setType(Entity.TORPEDO);
                        torpedo.setVelocity(game.getMapConfiguration().getTorpedoSpeed());
                        torpedo.setAngle(shootRequest.getAngle());
                        torpedo.setRoundsMoved(0);
                        torpedo.setOwner(new Owner(IMap.OUR_NAME));
                        entityList.add(torpedo);
                        submarine.setTorpedoCooldown(game.getMapConfiguration().getTorpedoCooldown() + 1);
                    } else if (submarine.getTorpedoCooldown() > 0) {
                        submarine.setTorpedoCooldown(submarine.getTorpedoCooldown() - 1);
                    }
                }
                shoots.clear();

                /////
                // shooting with enemy submarines
                /////
                entityList.stream()
                        .filter(entity -> entity.getType().equals(Entity.SUBMARINE) && !entity.getOwner().getName().equals(IMap.OUR_NAME))
                        .forEach(entity -> {
                            // TODO enemy shooting strategy

                            Submarine submarine = (Submarine) entity;

                            if (submarine.getTorpedoCooldown() > 0) {
                                submarine.setTorpedoCooldown(submarine.getTorpedoCooldown() - 1);
                                return;
                            }

                            Submarine submarineToShoot = findSubmarineToShoot(submarine);
                            if (submarineToShoot == null) {
                                return;
                            }

                            Position positionToShoot = MoveUtil.getPositionWhereShootMovingTarget(submarine.getPosition(), submarineToShoot, getEntityRadius(submarineToShoot));
                            if (positionToShoot == null) {
                                return;
                            }

                            Entity torpedo = new Entity();
                            torpedo.setId(++idSeed);
                            torpedo.setPosition(submarine.getPosition());
                            torpedo.setType(Entity.TORPEDO);
                            torpedo.setVelocity(game.getMapConfiguration().getTorpedoSpeed());
                            torpedo.setAngle(MoveUtil.getAngleForTargetPosition(submarine.getPosition(), positionToShoot));
                            torpedo.setRoundsMoved(0);
                            torpedo.setOwner(new Owner(submarine.getOwner().getName()));
                        });

                /////
                // move the torpedos
                /////
                entityList.stream()
                        .filter(entity -> entity.getType().equals(Entity.TORPEDO))
                        .forEach(torpedo -> {
                            torpedo.setRoundsMoved(torpedo.getRoundsMoved() - 1);
                            IChangeMovableObject.ZERO_MOVE.moveToNext(torpedo);
                        });

                /////
                // explode the torpedos, if necessary
                // destroy the torpedos, if exceeded the range
                // destroy the torpedos, which are hit an island or went out from the map
                /////
                entityList = entityList.stream()
                        .filter(torpedo -> {
                            if (torpedo.getType().equals(Entity.SUBMARINE)) {
                                return true;
                            }

                            // TODO first calculate where exploded the torpedo, then get the affected submarines
                            boolean exploded = entityList.stream()
                                    .filter(otherEntity -> otherEntity.getType().equals(Entity.SUBMARINE))
                                    .filter(submarine -> submarine.getPosition().distance(torpedo.getPosition())
                                            < getEntityRadius(submarine))
                                    .count() != 0;

                            if (exploded) {
                                Map.Entry<String, Integer> ownerPoint = game.getScores().getScores().entrySet().stream()
                                        .filter(entry -> entry.getKey().equals(torpedo.getOwner().getName()))
                                        .findFirst().get();

                                entityList.stream()
                                        .filter(otherEntity -> otherEntity.getType().equals(Entity.SUBMARINE))
                                        .filter(submarine -> submarine.getPosition().distance(torpedo.getPosition())
                                                < getEntityRadius(submarine) + game.getMapConfiguration().getTorpedoExplosionRadius()) // TODO temporary solution, because we don't know the hit point
                                        .map(submarine -> (Submarine) submarine)
                                        .forEach(submarine -> {
                                            submarine.setHp(submarine.getHp() - game.getMapConfiguration().getTorpedoDamage());

                                            if (!submarine.getOwner().getName().equals(torpedo.getOwner().getName())) {
                                                ownerPoint.setValue(ownerPoint.getValue() +
                                                        game.getMapConfiguration().getTorpedoHitScore());
                                                if (submarine.getHp() <= 0) {
                                                    ownerPoint.setValue(ownerPoint.getValue() +
                                                            game.getMapConfiguration().getTorpedoDestroyScore());
                                                }
                                            } else {
                                                ownerPoint.setValue(ownerPoint.getValue() +
                                                        game.getMapConfiguration().getTorpedoHitPenalty());
                                            }
                                        });
                            }

                            return !exploded && torpedo.getRoundsMoved() < game.getMapConfiguration().getTorpedoRange() && isValidPosition(torpedo.getPosition()) && game.getMapConfiguration().getIslandPositions().stream().noneMatch(island -> island.distance(torpedo.getPosition()) < game.getMapConfiguration().getIslandSize());
                        }).collect(Collectors.toList());

                /////
                // remove the destroyed submarines
                /////
                entityList = entityList.stream()
                        .filter(entity -> !entity.getType().equals(Entity.SUBMARINE) || (((Submarine) entity).getHp() > 0))
                        .collect(Collectors.toList());
            }
        }
        synchronized (this) {
            game.setStatus(Processor.GAME_STATUS.ENDED.name());
            game.setRound(game.getMapConfiguration().getRounds());
        }
        tickerThread = null;
    }

    private boolean isValidPosition(Position position) {
        return position != null &&
                0 <= position.getX() && position.getX() < game.getMapConfiguration().getWidth() &&
                0 <= position.getY() && position.getY() < game.getMapConfiguration().getHeight();
    }

    private Submarine findSubmarineToShoot(Submarine submarine) {
        List<Submarine> validTargets = entityList.stream()
                .filter(entity -> entity.getType().equals(Entity.SUBMARINE) && !entity.getOwner().getName().equals(submarine.getOwner().getName()))
                .filter(entity -> entity.getPosition().distance(submarine.getPosition()) <
                        (submarine.getSonarExtended() > 0 ? game.getMapConfiguration().getExtendedSonarRange() : game.getMapConfiguration().getSonarRange()))
                .map(entity -> (Submarine) entity)
                .collect(Collectors.toList());

        if (validTargets.size() == 0) {
            return null;
        }

        validTargets.sort((s1, s2) -> (int) s1.getPosition().distance(s2.getPosition()));
        return validTargets.get(0);
    }

    private double getEntityRadius(Entity e) {
        switch (e.getType()) {
            case Entity.SUBMARINE:
                return game.getMapConfiguration().getSubmarineSize();
            case Entity.TORPEDO:
                return 1;
            default:
                return 0;
        }
    }

    synchronized void join() {
        game.setStatus(Processor.GAME_STATUS.WAITING.name());
        game.setRound(0);

        // teams
        teams.add(IMap.OUR_NAME);
        teams.add("rabbit");
        teams.add("Just five more minutes Mom");
        teams.add("Thats No Moon");

        // map conf
        MapConfiguration configuration = new MapConfiguration();
        game.setMapConfiguration(configuration);
        configuration.setWidth(1700);
        configuration.setHeight(800);
        configuration.setIslandPositions(Collections.singletonList(new Position(850, 400)));
        configuration.setTeamCount(teams.size());
        configuration.setSubmarinesPerTeam(2);
        configuration.setTorpedoDamage(34);
        configuration.setTorpedoHitScore(100);
        configuration.setTorpedoDestroyScore(50);
        configuration.setTorpedoHitPenalty(0);
        configuration.setTorpedoCooldown(6);
        configuration.setSonarRange(150);
        configuration.setExtendedSonarRange(230);
        configuration.setExtendedSonarRounds(10);
        configuration.setExtendedSonarCooldown(20);
        configuration.setTorpedoSpeed(40.0);
        configuration.setTorpedoExplosionRadius(50);
        configuration.setRoundLength(1000); // faster
        configuration.setIslandSize(100);
        configuration.setSubmarineSize(15);
        configuration.setRounds(150);
        configuration.setMaxSteeringPerRound(20);
        configuration.setMaxAccelerationPerRound(5);
        configuration.setMaxSpeed(20);
        configuration.setTorpedoRange(10);
        configuration.setRateLimitedPenalty(10);

        // conn status
        Map<String, Boolean> connectionStatus = new HashMap<>();
        teams.forEach(team -> connectionStatus.put(team, true));
        game.setConnectionStatus(new ConnectionStatus());
        game.getConnectionStatus().setConnected(connectionStatus);

        // scores
        teams.forEach(team -> scores.put(team, 0));
        game.setScores(new Scores());
        game.getScores().setScores(scores);

        // submarines
        int[] i = {1};
        List<String> randomOrderedTeams = new ArrayList<>(teams);
        randomOrderedTeams.sort((s, t) -> random.nextInt(10) - 5);
        randomOrderedTeams.forEach(team -> {
            for (int j = 0; j < configuration.getSubmarinesPerTeam(); j++) {
                Submarine submarine = new Submarine();
                submarine.setType(Entity.SUBMARINE);
                submarine.setId(++idSeed);
                submarine.setPosition(new Position(
                        i[0] % 2 == 0 ? configuration.getWidth() / (5 - j) : configuration.getWidth() * (4 - j) / (5 - j),
                        i[0] > 2 ? configuration.getHeight() / (5 - j) : configuration.getHeight() * (4 - j) / (5 - j)));
                submarine.setOwner(new Owner(team));
                submarine.setVelocity(0.0);
                submarine.setAngle(random.nextDouble() * 360);
                submarine.setHp(100);
                submarine.setSonarCooldown(0);
                submarine.setTorpedoCooldown(0);
                submarine.setSonarExtended(0);
                entityList.add(submarine);
            }
            ++i[0];
        });


        // start the game
        tickerThread = new Thread(this);
        tickerThread.start();
    }

    synchronized Game getGame() {
        return game;
    }

    synchronized List<Submarine> getOurSubmarines() {
        return entityList.stream()
                .filter(e -> e.getOwner().getName().equals(IMap.OUR_NAME))
                .filter(e -> e.getType().equals(Entity.SUBMARINE))
                .map(e -> (Submarine) e)
                .collect(Collectors.toList());
    }

    private Submarine findOurSubmarine(Long id) {
        return getOurSubmarines().stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
    }

    synchronized MoveResponse moveSubmarine(Long submarineId, MoveRequest request) {
        Submarine submarine = findOurSubmarine(submarineId);

        if (submarine == null) {
            MoveResponse response = new MoveResponse();
            setStatus(response, 4);
            return response;
        }

        if (moves.containsKey(submarineId)) {
            MoveResponse response = new MoveResponse();
            setStatus(response, 50);
            return response;
        }

        // TODO check properly

        moves.put(submarineId, request);

        MoveResponse response = new MoveResponse();
        setStatus(response, 0);
        return response;
    }

    synchronized ShootResponse shoot(Long submarineId, ShootRequest request) {
        Submarine submarine = findOurSubmarine(submarineId);

        if (submarine == null) {
            ShootResponse response = new ShootResponse();
            setStatus(response, 4);
            return response;
        }

        if (shoots.containsKey(submarineId)) {
            ShootResponse response = new ShootResponse();
            setStatus(response, 50);
            return response;
        }

        shoots.put(submarineId, request);

        ShootResponse response = new ShootResponse();
        setStatus(response, 0);
        return response;
    }

    synchronized SonarResponse sonar(Long submarineId) {
        Submarine submarine = findOurSubmarine(submarineId);

        if (submarine == null) {
            SonarResponse response = new SonarResponse();
            setStatus(response, 4);
            return response;
        }

        // TODO check for multiple sonar attempt

        SonarResponse response = new SonarResponse();
        response.setEntities(
                entityList.stream()
                        .filter(e -> e.getPosition().distance(submarine.getPosition()) <= (submarine.getSonarExtended() > 0 ?
                                game.getMapConfiguration().getExtendedSonarRange() :
                                game.getMapConfiguration().getSonarRange()))
                        .filter(e -> !e.getId().equals(submarineId))
                        .collect(Collectors.toList())
        );
        setStatus(response, 0);
        return response;
    }

    synchronized ExtendSonarResponse extendSonar(Long submarineId) {
        Submarine submarine = findOurSubmarine(submarineId);

        if (submarine == null) {
            ExtendSonarResponse response = new ExtendSonarResponse();
            setStatus(response, 4);
            return response;
        }

        if (extendedSonar.contains(submarineId)) {
            ExtendSonarResponse response = new ExtendSonarResponse();
            setStatus(response, 50);
            return response;
        }

        extendedSonar.add(submarineId);

        ExtendSonarResponse response = new ExtendSonarResponse();
        setStatus(response, 0);
        return response;
    }

    private static void setStatus(StatusResponse response, int code) {
        response.setMessage(code == 0 ? "OK" : "ERROR");
        response.setCode(code);
    }
}
