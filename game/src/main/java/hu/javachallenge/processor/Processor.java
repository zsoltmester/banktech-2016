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
    private static Game game;
    private static Integer lastKnownRound;

    private static void preprocessGameResponse(GameResponse gameResponse) {
        if (gameResponse.getCode() == 3) {
            LOGGER.severe("We get back from to server, that the game id doesn't exists.");
            System.exit(1);
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
        switch (joinGameResponse.getCode()) {
            case 0:
                LOGGER.info("Successfully joined to the created game.");
                break;
            case 1:
                LOGGER.severe("Cannot join to game, because we not invited.");
                System.exit(1);
            case 2:
                LOGGER.warning("Game is already in progress.");
                break;
            case 3:
                LOGGER.severe("Cannot join to game, the game id doesn't exists.");
                System.exit(1);
            default:
                LOGGER.warning("Unspecified response code: " + joinGameResponse.getCode() + ", with message: " + joinGameResponse.getMessage());
                break;
        }
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

        if (submarinesResponse.getCode() == 3) {
            LOGGER.severe("We get back from to server, that the game id doesn't exists.");
            System.exit(1);
        }

        List<Submarine> submarines = submarinesResponse.getSubmarines();
        map.setSubmarines(submarines);
        // TODO update the map based on the 'submarines' instance

        LOGGER.info("Submarines' status updated.");
    }
}
