package hu.javachallenge.communication;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.javachallenge.bean.*;
import hu.javachallenge.map.IMap;
import hu.javachallenge.processor.Processor;
import hu.javachallenge.strategy.ScoutStrategy;
import hu.javachallenge.strategy.moving.IChangeMovableObject;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by qqcs on 08/11/16.
 */
public class OfflineCommunicatorImpl implements Communicator {
    static class ServerGame implements Runnable {
        private Game game = new Game();
        private List<Entity> entityList = new ArrayList<>();
        private HashMap<Long, MoveRequest> moves = new HashMap<>();
        private HashMap<Long, ShootRequest> shoots = new HashMap<>();
        private HashSet<Long> extSonar = new HashSet<>();
        private Thread tickerThread;
        private Long maxId = 0L;

        public synchronized Game getGame() {
            return game;
        }

        public synchronized List<Submarine> getSubmarineList() {
            return entityList.stream()
                    .filter(e -> e.getOwner().getName().equals(IMap.OUR_NAME))
                    .filter(e -> (e instanceof Submarine))
                    .map(e -> (Submarine) e)
                    .collect(Collectors.toList());
        }

        public synchronized void join() {
            game.setStatus(Processor.GAME_STATUS.WAITING.name());
            game.setRound(0);

            // mapConf
            try {
                game.setMapConfiguration(new ObjectMapper().readValue("{TODO}", MapConfiguration.class));
            } catch (IOException e) {
                e.printStackTrace();
            }

            // conn status + scores
            HashMap<String, Boolean> map = new HashMap<>();
            map.put(IMap.OUR_NAME, true);
            map.put("BOT", true);

            game.setConnectionStatus(new ConnectionStatus());
            game.getConnectionStatus().setConnected(map);

            HashMap<String, Integer> scores = new HashMap<>();
            scores.put(IMap.OUR_NAME, 0);
            scores.put("BOT", 0);

            game.setScores(new Scores());
            game.getScores().setScores(scores);

            // TODO create entities


            tickerThread = new Thread(this);
            tickerThread.start();
            // & start the game
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

            @Override
            protected void changeTargetIfNeed() {
                // TODO
                getTargets().add(new Position(2, 2));

                super.changeTargetIfNeed();
            }
        }

        @Override
        public void run() {
            try {
                Thread.sleep(200);
                for(int i = 1; i < game.getMapConfiguration().getRounds() + 1; ++i) {
                    synchronized (this) {
                        game.setStatus(Processor.GAME_STATUS.RUNNING.name());
                        game.setRound(i);
                    }
                    Thread.sleep(1000);
                    synchronized (this) {
                        for(Submarine submarine : getSubmarineList()) {
                            MoveRequest moveRequest = moves.get(submarine.getId());
                            ShootRequest shootRequest = shoots.get(submarine.getId());
                            Boolean extendSonar = extSonar.contains(submarine.getId());

                            // extSonar
                            if (extendSonar) {
                                submarine.setSonarCooldown(game.getMapConfiguration().getExtendedSonarCooldown());
                                submarine.setSonarExtended(game.getMapConfiguration().getExtendedSonarRounds());
                            } else {
                                if(submarine.getSonarExtended() > 0) {
                                    submarine.setSonarExtended(submarine.getSonarExtended() - 1);
                                }
                                if(submarine.getSonarCooldown() > 0) {
                                    submarine.setSonarCooldown(submarine.getSonarCooldown() - 1);
                                }
                            }

                            // shoot
                            if (shootRequest != null) {
                                Entity entity = new Entity();
                                entity.setId(++maxId);
                                entity.setPosition(submarine.getPosition());
                                entity.setType(Entity.TORPEDO);
                                entity.setVelocity(game.getMapConfiguration().getTorpedoSpeed());
                                entity.setAngle(shootRequest.getAngle());
                                entity.setRoundsMoved(1);
                                entity.setOwner(new Owner(IMap.OUR_NAME));

                                IChangeMovableObject.ZERO_MOVE.moveToNext(entity);
                            }

                            // move
                            IChangeMovableObject<MovableObject> move = IChangeMovableObject.ZERO_MOVE;
                            if(moveRequest != null) {
                                move = new IChangeMovableObject.FixMove<>(moveRequest.getSpeed(), moveRequest.getTurn());
                            }
                            move.moveToNext(submarine);
                        }


                        // + moving others
                        entityList = entityList.stream()
                                .filter(e -> {
                                    if(e.getType().equals(Entity.SUBMARINE) &&
                                            !e.getOwner().getName().equals(IMap.OUR_NAME)) {
                                        new MyStrategy(e.getId()).moveToNext((Submarine) e);
                                    } else if (e.getType().equals(Entity.TORPEDO)) {
                                        IChangeMovableObject.ZERO_MOVE.moveToNext(e);
                                        if(e.getRoundsMoved() >= game.getMapConfiguration().getTorpedoRange()) {
                                            return false;
                                        }
                                        e.setRoundsMoved(e.getRoundsMoved());
                                    }
                                    return isValidPosition(e.getPosition());
                                })
                                .filter(e -> true) // TODO nem-e ment neki torpedónak
                                .filter(e -> true) // TODO nem-e ment neki szigetnek
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

            if(submarine == null) {
                MoveResponse response = new MoveResponse();
                setStatus(response, 4);
                return response;
            }
            // move submarine

            if(moves.containsKey(submarineId)) {
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

            if(submarine == null) {
                ShootResponse response = new ShootResponse();
                setStatus(response, 4);
                return response;
            }

            // shoot
            if(shoots.containsKey(submarineId)) {
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

            if(submarine == null) {
                SonarResponse response = new SonarResponse();
                setStatus(response, 4);
                return response;
            }

            SonarResponse response = new SonarResponse();
            response.setEntities(
            entityList.stream().filter(e ->
                e.getPosition().distance(submarine.getPosition()) <=
                        (submarine.getSonarExtended() > 0 ?
                        game.getMapConfiguration().getExtendedSonarRange():
                        game.getMapConfiguration().getSonarRange()))
                    .filter(e -> !e.getId().equals(submarineId))
                    .collect(Collectors.toList())
            );
            setStatus(response, 0);
            return response;
        }

        public synchronized ExtendSonarResponse extSonar(Long submarineId) {
            Submarine submarine = findPlayerSubmarine(submarineId);

            if(submarine == null) {
                ExtendSonarResponse response = new ExtendSonarResponse();
                setStatus(response, 4);
                return response;
            }

            if(extSonar.contains(submarineId)) {
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

        if(game != null) {
            game.join();
        }
        setStatus(response, game != null ? 0 : 3);

        return response;
    }

    @Override
    public GameResponse getGame(Long id) {
        GameResponse response = new GameResponse();
        ServerGame game = games.get(id);

        if(game != null) {
            response.setGame(game.getGame());
        }
        setStatus(response, game != null ? 0 : 3);
        return response;
    }

    @Override
    public SubmarinesResponse getSubmarines(Long id) {
        SubmarinesResponse response = new SubmarinesResponse();
        ServerGame game = games.get(id);

        if(game != null) {
            response.setSubmarines(game.getSubmarineList());
        }
        setStatus(response, game != null ? 0 : 3);
        return response;
    }

    @Override
    public MoveResponse move(Long gameId, Long submarineId, MoveRequest request) {
        MoveResponse response;
        ServerGame game = games.get(gameId);

        if(game != null) {
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

        if(game != null) {
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

        if(game != null) {
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

        if(game != null) {
            response = game.extSonar(submarineId);
        } else {
            response = new ExtendSonarResponse();
            setStatus(response, 3);
        }
        return response;
    }
}
