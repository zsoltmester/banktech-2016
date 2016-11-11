package hu.javachallenge.communication;

import hu.javachallenge.bean.*;
import hu.javachallenge.map.IMap;
import hu.javachallenge.processor.Processor;
import hu.javachallenge.strategy.ScoutStrategy;
import hu.javachallenge.strategy.moving.IChangeMovableObject;

import java.util.*;
import java.util.stream.Collectors;

public class OfflineCommunicatorImpl implements Communicator {

    private static class ServerGame implements Runnable {

        private Game game = new Game();

        private Set<String> teams = new HashSet<>();
        private Map<String, Integer> scores = new HashMap<>();

        private List<Entity> entityList = new ArrayList<>();

        private Map<Long, MoveRequest> moves = new HashMap<>();
        private Map<Long, ShootRequest> shoots = new HashMap<>();
        private Set<Long> extSonar = new HashSet<>();

        private Thread tickerThread;

        private long idSeed = 0L;
        private Random random = new Random();

        synchronized Game getGame() {
            return game;
        }

        synchronized List<Submarine> getSubmarineList() {
            return entityList.stream()
                    .filter(e -> e.getOwner().getName().equals(IMap.OUR_NAME))
                    .filter(e -> (e instanceof Submarine))
                    .map(e -> (Submarine) e)
                    .collect(Collectors.toList());
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
            final int[] i = {1};
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

        public boolean isValidPosition(Position position) {
            return position != null &&
                    0 <= position.getX() && position.getX() < game.getMapConfiguration().getWidth() &&
                    0 <= position.getY() && position.getY() < game.getMapConfiguration().getHeight();
        }

        private class MyStrategy extends ScoutStrategy {
            public MyStrategy(Long submarineId, Position... targets) {
                super(submarineId, targets);
            }

            @Override
            public Submarine getSubmarine() {
                return entityList.stream()
                        .filter(e -> e.getId().equals(submarineId))
                        .map(e -> (Submarine) e)
                        .findFirst().orElse(null);
            }
        }

        private double entityRadius(Entity e) {
            if (e.getType().equals(Entity.SUBMARINE)) {
                return game.getMapConfiguration().getSubmarineSize();
            } else {
                return 1;
            }
        }

        @Override
        public void run() {
            try {
                Thread.sleep(200);
                for (int i = 1; i <= game.getMapConfiguration().getRounds(); ++i) {
                    synchronized (this) {
                        game.setStatus(Processor.GAME_STATUS.RUNNING.name());
                        game.setRound(i);
                    }
                    Thread.sleep(game.getMapConfiguration().getRoundLength());
                    synchronized (this) {
                        for (Submarine submarine : getSubmarineList()) {
                            MoveRequest moveRequest = moves.get(submarine.getId());
                            ShootRequest shootRequest = shoots.get(submarine.getId());
                            Boolean extendSonar = extSonar.contains(submarine.getId());

                            // extSonar
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

                            // shoot
                            if (shootRequest != null) {
                                Entity entity = new Entity();
                                entity.setId(++idSeed);
                                entity.setPosition(submarine.getPosition());
                                entity.setType(Entity.TORPEDO);
                                entity.setVelocity(game.getMapConfiguration().getTorpedoSpeed());
                                entity.setAngle(shootRequest.getAngle());
                                entity.setRoundsMoved(1);
                                entity.setOwner(new Owner(IMap.OUR_NAME));

                                IChangeMovableObject.ZERO_MOVE.moveToNext(entity);

                                entityList.add(entity);

                                submarine.setTorpedoCooldown(game.getMapConfiguration().getTorpedoCooldown());
                            } else if (submarine.getTorpedoCooldown() > 0) {
                                submarine.setTorpedoCooldown(submarine.getTorpedoCooldown() - 1);
                            }
                        }

                        // torpedos
                        entityList = entityList.stream()
                                .filter(e -> {
                                    if (e.getType().equals(Entity.SUBMARINE)) {
                                        return true;
                                    }

                                    boolean hasExplosion = entityList.stream()
                                            .filter(o -> o.getType().equals(Entity.SUBMARINE))
                                            .filter(o -> o.getPosition().distance(e.getPosition()) <
                                                    entityRadius(e) + entityRadius(o))
                                            .count() != 0;

                                    if (hasExplosion) {
                                        Map.Entry<String, Integer> ownerPoint = game.getScores().getScores().entrySet().stream()
                                                .filter(entry -> entry.getKey().equals(e.getOwner().getName()))
                                                .findFirst().get();

                                        entityList.stream()
                                                .filter(o -> o.getType().equals(Entity.SUBMARINE))
                                                .filter(o -> o.getPosition().distance(e.getPosition()) <
                                                        entityRadius(o) +
                                                                game.getMapConfiguration().getTorpedoExplosionRadius())
                                                .map(o -> (Submarine) o)
                                                .forEach(o -> {
                                                    o.setHp(o.getHp() - game.getMapConfiguration().getTorpedoDamage());

                                                    if (!o.getOwner().getName().equals(e.getOwner().getName())) {
                                                        ownerPoint.setValue(ownerPoint.getValue() +
                                                                game.getMapConfiguration().getTorpedoHitScore());
                                                        if (o.getHp() <= 0) {
                                                            ownerPoint.setValue(ownerPoint.getValue() +
                                                                    game.getMapConfiguration().getTorpedoDestroyScore());
                                                        }
                                                    } else {
                                                        ownerPoint.setValue(ownerPoint.getValue() +
                                                                game.getMapConfiguration().getTorpedoHitPenalty());
                                                    }
                                                });
                                    }

                                    return !hasExplosion;
                                }).collect(Collectors.toList());


                        // + moving others
                        entityList = entityList.stream()
                                .filter(e -> {
                                    if (e.getType().equals(Entity.SUBMARINE) &&
                                            !e.getOwner().getName().equals(IMap.OUR_NAME)) {
                                        new MyStrategy(e.getId(), new Position(850, 400)).moveToNext((Submarine) e);
                                        if (((Submarine) e).getHp() < 0) {
                                            return false;
                                        }
                                    } else if (e.getType().equals(Entity.TORPEDO)) {
                                        IChangeMovableObject.ZERO_MOVE.moveToNext(e);
                                        if (e.getRoundsMoved() >= game.getMapConfiguration().getTorpedoRange()) {
                                            return false;
                                        }
                                        e.setRoundsMoved(e.getRoundsMoved());
                                    }
                                    return isValidPosition(e.getPosition());
                                })
                                .filter(e ->
                                        game.getMapConfiguration().getIslandPositions().stream()
                                                .allMatch(is ->
                                                        e.getPosition().distance(is) >
                                                                entityRadius(e) +
                                                                        game.getMapConfiguration().getIslandSize())
                                )
                                .collect(Collectors.toList());

                        // remove messages
                        moves.clear();
                        shoots.clear();
                        extSonar.clear();
                    }
                }
                synchronized (this) {
                    game.setStatus(Processor.GAME_STATUS.ENDED.name());
                    game.setRound(game.getMapConfiguration().getRounds());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            tickerThread = null;
        }

        private synchronized Submarine findPlayerSubmarine(Long id) {
            return getSubmarineList().stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
        }

        public synchronized MoveResponse moveSubmarine(Long submarineId, MoveRequest request) {
            Submarine submarine = findPlayerSubmarine(submarineId);

            if (submarine == null) {
                MoveResponse response = new MoveResponse();
                setStatus(response, 4);
                return response;
            }
            // move submarine

            if (moves.containsKey(submarineId)) {
                MoveResponse response = new MoveResponse();
                setStatus(response, 10);
                return response;
            }

            // TODO check properly

            moves.put(submarineId, request);
            MoveResponse response = new MoveResponse();
            setStatus(response, 0);
            return response;
        }

        public synchronized ShootResponse shoot(Long submarineId, ShootRequest request) {
            Submarine submarine = findPlayerSubmarine(submarineId);

            if (submarine == null) {
                ShootResponse response = new ShootResponse();
                setStatus(response, 4);
                return response;
            }

            // shoot
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

        public synchronized SonarResponse sonar(Long submarineId) {
            Submarine submarine = findPlayerSubmarine(submarineId);

            if (submarine == null) {
                SonarResponse response = new SonarResponse();
                setStatus(response, 4);
                return response;
            }

            SonarResponse response = new SonarResponse();
            response.setEntities(
                    entityList.stream().filter(e ->
                            e.getPosition().distance(submarine.getPosition()) <=
                                    (submarine.getSonarExtended() > 0 ?
                                            game.getMapConfiguration().getExtendedSonarRange() :
                                            game.getMapConfiguration().getSonarRange()))
                            .filter(e -> !e.getId().equals(submarineId))
                            .collect(Collectors.toList())
            );
            setStatus(response, 0);
            return response;
        }

        public synchronized ExtendSonarResponse extSonar(Long submarineId) {
            Submarine submarine = findPlayerSubmarine(submarineId);

            if (submarine == null) {
                ExtendSonarResponse response = new ExtendSonarResponse();
                setStatus(response, 4);
                return response;
            }

            if (extSonar.contains(submarineId)) {
                ExtendSonarResponse response = new ExtendSonarResponse();
                setStatus(response, 50);
                return response;
            }

            extSonar.add(submarineId);

            ExtendSonarResponse response = new ExtendSonarResponse();
            setStatus(response, 0);
            return response;
        }
    }

    HashMap<Long, ServerGame> games = new HashMap<>();


    public static void setStatus(StatusResponse response, int code) {
        response.setMessage(code == 0 ? "" : "---");
        response.setCode(code);
    }

    @Override
    public CreateGameResponse createGame() {
        CreateGameResponse response = new CreateGameResponse();
        response.setId((long) games.size());

        ServerGame serverGame = new ServerGame();
        serverGame.getGame().setId((long) games.size());
        serverGame.getGame().setCreatedTime(new Date());

        games.put((long) games.size(), serverGame);

        setStatus(response, 0);

        return response;
    }

    @Override
    public GamesResponse getGames() {
        GamesResponse response = new GamesResponse();
        response.setGames(games.keySet().stream().collect(Collectors.toList()));
        setStatus(response, 0);
        return response;
    }

    @Override
    public JoinGameResponse joinGame(Long id) {
        JoinGameResponse response = new JoinGameResponse();
        ServerGame game = games.get(id);

        if (game != null) {
            game.join();
        }
        setStatus(response, game != null ? 0 : 3);

        return response;
    }

    @Override
    public GameResponse getGame(Long id) {
        GameResponse response = new GameResponse();
        ServerGame game = games.get(id);

        if (game != null) {
            response.setGame(game.getGame());
        }
        setStatus(response, game != null ? 0 : 3);
        return response;
    }

    @Override
    public SubmarinesResponse getSubmarines(Long id) {
        SubmarinesResponse response = new SubmarinesResponse();
        ServerGame game = games.get(id);

        if (game != null) {
            response.setSubmarines(game.getSubmarineList());
        }
        setStatus(response, game != null ? 0 : 3);
        return response;
    }

    @Override
    public MoveResponse move(Long gameId, Long submarineId, MoveRequest request) {
        MoveResponse response;
        ServerGame game = games.get(gameId);

        if (game != null) {
            response = game.moveSubmarine(submarineId, request);
        } else {
            response = new MoveResponse();
            setStatus(response, 3);
        }
        return response;
    }

    @Override
    public ShootResponse shoot(Long gameId, Long submarineId, ShootRequest request) {
        ShootResponse response;
        ServerGame game = games.get(gameId);

        if (game != null) {
            response = game.shoot(submarineId, request);
        } else {
            response = new ShootResponse();
            setStatus(response, 3);
        }
        return response;
    }

    @Override
    public SonarResponse sonar(Long gameId, Long submarineId) {
        SonarResponse response;
        ServerGame game = games.get(gameId);

        if (game != null) {
            response = game.sonar(submarineId);
        } else {
            response = new SonarResponse();
            setStatus(response, 3);
        }
        return response;
    }

    @Override
    public ExtendSonarResponse extendSonar(Long gameId, Long submarineId) {
        ExtendSonarResponse response;
        ServerGame game = games.get(gameId);

        if (game != null) {
            response = game.extSonar(submarineId);
        } else {
            response = new ExtendSonarResponse();
            setStatus(response, 3);
        }
        return response;
    }
}
