package hu.javachallenge.processor;

import hu.javachallenge.App;
import hu.javachallenge.bean.*;
import hu.javachallenge.communication.Communicator;
import hu.javachallenge.communication.CommunicatorImpl;
import hu.javachallenge.map.IMap;

import java.util.List;
import java.util.logging.Logger;

public class Processor {

    private static final Logger LOGGER = Logger.getLogger(Processor.class.getName());

    private static final Integer SLEEP_TIME_AT_WAITING_FOR_START = 100;
    private static final Integer SLEEP_TIME_AT_WAITING_FOR_NEXT_ROUND = 100;

    private static Communicator communicator = new CommunicatorImpl(App.serverAddress);
    private static IMap map = IMap.MapConfig.getMap();

    private enum GAME_STATUS {
        WAITING, RUNNING, ENDED
    }

    private static Long gameId;
    public static Game game;
    private static Integer lastKnownRound;

    private static boolean isValidResponse(StatusResponse response) {
        if(response == null) {
            LOGGER.severe("Response is null. Maybe we lost the connection?");
            return false;
        }
        String message = response.getMessage();
        switch (response.getCode()) {
            case 1:
                LOGGER.severe("Cannot join to game, because we not invited. Server message: " + message);
                System.exit(1);
            case 2:
                LOGGER.warning("Game is already in progress. Server message: " + message);
                break;
            case 3:
                LOGGER.severe("The game id doesn't exists. Server message: " + message);
                System.exit(1);
                break;
            case 4:
                LOGGER.warning("Cannot use the submarine. Server message: " + message);
                break;
            case 7:
                LOGGER.warning("Torpedo is on cooldown. Server message: " + message);
                break;
            case 8:
                LOGGER.warning("Extended sonar is on cooldown. Server message: " + message);
                break;
            case 9:
                LOGGER.severe("The game is not in progress. Server message: " + message);
                System.exit(1);
                break;
            case 10:
                LOGGER.warning("The submarine already moved. Server message: " + message);
                break;
            case 11:
                LOGGER.warning("Too big acceleration. Server message: " + message);
                break;
            case 12:
                LOGGER.warning("Too big turn. Server message: " + message);
                break;
            case 50:
                LOGGER.warning("Too many action with this submarine this turn. Server message: " + message);
                break;
            default:
                return true;
        }
        return false;
    }

    private static void preprocessGameResponse(GameResponse gameResponse) {
        if (!isValidResponse(gameResponse)) {
            return;
        }
        game = gameResponse.getGame();
    }

    /**
     * It creates a game, validates that the server created it and then joins to it.
     * <p>
     * TODO at the finals, it's logic should be more sophisticated. There maybe we don't have to create a game, just join to one with a given game id.
     */
    public static void joinGame() {
        CreateGameResponse createGameResponse = communicator.createGame();
        gameId = createGameResponse.getId();

        GamesResponse gamesResponse = communicator.getGames();
        if (!gamesResponse.getGames().stream().anyMatch(id -> id.equals(gameId))) {
            LOGGER.warning("Created a game, but the created game id is missing from the available games list. Maybe we already joined to it.");
        }

        JoinGameResponse joinGameResponse = communicator.joinGame(gameId);
        isValidResponse(joinGameResponse);
    }

    /**
     * Wait until the game is not ready.
     */
    public static void waitForStart() {
        do {
            try {
                Thread.sleep(SLEEP_TIME_AT_WAITING_FOR_START);
            } catch (InterruptedException e) {
                LOGGER.warning("Exception at Thread.sleep...");
                e.printStackTrace();
            }

            GameResponse gameResponse = communicator.getGame(gameId);
            preprocessGameResponse(gameResponse);

            lastKnownRound = game.getRound();

        } while (game.getStatus().equals(GAME_STATUS.WAITING.name()));

        LOGGER.info("Game is stating now.");

        map.initialize(game);
    }

    /**
     * Wait for the next round.
     */
    public static void waitForNextRound() {
        do {
            try {
                Thread.sleep(SLEEP_TIME_AT_WAITING_FOR_NEXT_ROUND);
            } catch (InterruptedException e) {
                LOGGER.warning("Exception at Thread.sleep...");
                e.printStackTrace();
            }

            GameResponse gameResponse = communicator.getGame(gameId);
            preprocessGameResponse(gameResponse);

        } while (game.getStatus().equals(GAME_STATUS.RUNNING.name()) && game.getRound().equals(lastKnownRound));

        lastKnownRound = game.getRound();

        if (game.getStatus().equals(GAME_STATUS.RUNNING.name())) {
            LOGGER.info("Next round started: " + lastKnownRound);
        }
    }

    /**
     * @return is the game is running or not.
     */
    public static boolean isGameRunning() {
        switch (GAME_STATUS.valueOf(game.getStatus())) {
            case WAITING:
                LOGGER.warning("The game is in waiting status, but it is already started once.");
                return false;
            case ENDED:
                LOGGER.info("The game is ended.");
                return false;
            case RUNNING:
                return true;
            default:
                LOGGER.severe("Invalid game status: " + game.getStatus());
                System.exit(1);
        }
        return false; // unreachable code
    }

    /**
     * Updates our submarines' status.
     */
    public static void updateOurSubmarines() {
        SubmarinesResponse submarinesResponse = communicator.getSubmarines(gameId);
        if (!isValidResponse(submarinesResponse)) {
            return;
        }

        List<Submarine> submarines = submarinesResponse.getSubmarines();
        map.updateOurSubmarines(submarines);

        LOGGER.fine("Our submarines' status updated.");
    }

    /**
     * Moves the given submarine with the given acceleration and turn.
     */
    public static void move(Long submarineId, double acceleration, double turn) {
        Submarine submarine = map.getOurSubmarines().stream()
                .filter(otherSubmarine -> otherSubmarine.getId().equals(submarineId)).findAny().orElseGet(() -> null);
        if (submarine == null) {
            LOGGER.warning("Safe check triggered: invalid submarine ID.");
            return;
        }
        int maxAccelerationPerRound = map.getConfiguration().getMaxAccelerationPerRound();
        if (Math.abs(acceleration) > maxAccelerationPerRound) {
            acceleration = acceleration > 0 ? maxAccelerationPerRound : -maxAccelerationPerRound;
            LOGGER.warning("Safe check triggered: decreased the acceleration to the maximum per round.");
        }
        double currentSpeed = submarine.getVelocity();
        int maxSpeed = map.getConfiguration().getMaxSpeed();
        if (acceleration + currentSpeed > maxSpeed) {
            acceleration = maxSpeed - currentSpeed;
            LOGGER.warning("Safe check triggered: decreased the acceleration to not to exceed the max speed.");
        }

        if(acceleration + currentSpeed < 0) {
            acceleration = -currentSpeed;
            LOGGER.warning("Safe check triggered: increased the acceleration to not to exceed the zero speed.");
        }

        int maxSteeringPerRound = map.getConfiguration().getMaxSteeringPerRound();
        if (Math.abs(turn) > maxSteeringPerRound) {
            turn = turn > 0 ? maxSteeringPerRound : -maxSteeringPerRound;
            LOGGER.warning("Safe check triggered: decreased the turn to the maximum per round.");
        }

        MoveRequest moveRequest = new MoveRequest(acceleration, turn);
        MoveResponse moveResponse = communicator.move(gameId, submarineId, moveRequest);
        if (!isValidResponse(moveResponse)) {
            return;
        }

        LOGGER.info(submarineId + " submarine moved successfully with acceleration: " + acceleration + " and turn: " + turn);
    }

    /**
     * Shoot with the given submarine and angle.
     */
    public static void shoot(Long submarine, Double angle) {
        // TODO safe check that the action is valid

        ShootRequest shootRequest = new ShootRequest(angle);
        ShootResponse shootResponse = communicator.shoot(gameId, submarine, shootRequest);
        if (!isValidResponse(shootResponse)) {
            return;
        }

        LOGGER.info(submarine + " submarine shoot successfully with angle: " + angle);

        map.submarineShoot(submarine, angle);
    }

    /**
     * Use the sonar of the given submarine.
     */
    public static void sonar(Long submarine) {
        // TODO safe check that the action is valid

        SonarResponse sonarResponse = communicator.sonar(gameId, submarine);
        if (!isValidResponse(sonarResponse)) {
            return;
        }

        LOGGER.info(submarine + " submarine used sonar successfully.");

        map.processSonarResult(submarine, sonarResponse.getEntities());
    }

    /**
     * Extend the sonar of the given submarine.
     */
    public static void extendSonar(Long submarine) {
        // TODO safe check that the action is valid

        ExtendSonarResponse extendSonarResponse = communicator.extendSonar(gameId, submarine);
        if (!isValidResponse(extendSonarResponse)) {
            return;
        }

        LOGGER.info(submarine + " submarine extended it's sonar successfully.");
    }
}
