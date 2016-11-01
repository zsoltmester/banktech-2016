package hu.javachallenge.processor;

import hu.javachallenge.App;
import hu.javachallenge.bean.*;
import hu.javachallenge.communication.Communicator;
import hu.javachallenge.communication.CommunicatorImpl;
import hu.javachallenge.map.Map;

import java.util.List;
import java.util.logging.Logger;

public class Processor {

    private static final Logger LOGGER = Logger.getLogger(Processor.class.getName());

    private static final Integer SLEEP_TIME_AT_WAITING_FOR_START = 100;
    private static final Integer SLEEP_TIME_AT_WAITING_FOR_NEXT_ROUND = 100;

    private static Communicator communicator = new CommunicatorImpl(App.serverAddress);
    private static Map map = Map.get();

    private enum GAME_STATUS {
        WAITING, RUNNING, ENDED
    }

    private static Long gameId;
    public static Game game;
    private static Integer lastKnownRound;

    private static boolean isValidResponse(Integer code, String message) {
        switch (code) {
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
        if (!isValidResponse(gameResponse.getCode(), gameResponse.getMessage())) {
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
        isValidResponse(joinGameResponse.getCode(), joinGameResponse.getMessage());
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

        // TODO game started, initialize the map based on the 'game' instance
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

            // TODO update the map based on the 'game' instance

        } while (game.getStatus().equals(GAME_STATUS.RUNNING.name()) && game.getRound().equals(lastKnownRound));

        lastKnownRound = game.getRound();

        if (!game.getStatus().equals(GAME_STATUS.RUNNING.name())) {
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
    public static void updateSubmarines() {
        SubmarinesResponse submarinesResponse = communicator.getSubmarines(gameId);
        if (!isValidResponse(submarinesResponse.getCode(), submarinesResponse.getMessage())) {
            return;
        }

        List<Submarine> submarines = submarinesResponse.getSubmarines();
        map.setSubmarines(submarines);
        // TODO update the map based on the 'submarines' instance

        LOGGER.info("Submarines' status updated.");
    }

    /**
     * Moves the given submarine with the given speed and turn.
     */
    public static void move(Long submarine, Double speed, Double turn) {
        // TODO safe check that the action is valid

        MoveRequest moveRequest = new MoveRequest(speed, turn);
        MoveResponse moveResponse = communicator.move(gameId, submarine, moveRequest);
        if (!isValidResponse(moveResponse.getCode(), moveResponse.getMessage())) {
            return;
        }

        LOGGER.info(submarine + " submarine moved successfully with speed: " + speed + " and turn: " + turn);
        // TODO update the map based on the move action
    }

    /**
     * Shoot with the given submarine and angle.
     */
    public static void shoot(Long submarine, Double angle) {
        // TODO safe check that the action is valid

        ShootRequest shootRequest = new ShootRequest(angle);
        ShootResponse shootResponse = communicator.shoot(gameId, submarine, shootRequest);
        if (!isValidResponse(shootResponse.getCode(), shootResponse.getMessage())) {
            return;
        }

        LOGGER.info(submarine + " submarine shoot successfully with angle: " + angle);
        // TODO update the map based on the shoot action
    }

    /**
     * Use the sonar of the given submarine.
     */
    public static void sonar(Long submarine) {
        // TODO safe check that the action is valid

        SonarResponse sonarResponse = communicator.sonar(gameId, submarine);
        if (!isValidResponse(sonarResponse.getCode(), sonarResponse.getMessage())) {
            return;
        }

        LOGGER.info(submarine + " submarine used sonar successfully.");
        // TODO update the map based on the sonar data
    }

    /**
     * Extend the sonar of the given submarine.
     */
    public static void extendSonar(Long submarine) {
        // TODO safe check that the action is valid

        ExtendSonarResponse extendSonarResponse = communicator.extendSonar(gameId, submarine);
        if (!isValidResponse(extendSonarResponse.getCode(), extendSonarResponse.getMessage())) {
            return;
        }

        LOGGER.info(submarine + " submarine extended it's sonar successfully.");
        // TODO update the map based on the sonar data
    }
}