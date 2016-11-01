package hu.javachallenge.processor;

import hu.javachallenge.App;
import hu.javachallenge.bean.CreateGameResponse;
import hu.javachallenge.bean.GameResponse;
import hu.javachallenge.bean.GamesResponse;
import hu.javachallenge.bean.JoinGameResponse;
import hu.javachallenge.communication.Communicator;
import hu.javachallenge.communication.CommunicatorImpl;

import java.util.logging.Logger;

public class ProcessorImpl implements Processor {

    private static final Logger LOGGER = Logger.getLogger(ProcessorImpl.class.getName());

    private static final Integer SLEEP_TIME_AT_WAITING_FOR_START = 100;
    private static final Integer SLEEP_TIME_AT_WAITING_FOR_NEXT_ROUND = 100;

    private Communicator communicator = new CommunicatorImpl(App.serverAddress);

    private enum GAME_STATUS {
        WAITING, RUNNING, ENDED
    }

    private Long gameId;
    private GAME_STATUS gameStatus;
    private Integer lastKnownRound;

    @Override
    public void joinToGame() {
        CreateGameResponse createGameResponse = communicator.createGame();
        gameId = createGameResponse.getId();

        GamesResponse gamesResponse = communicator.getGames();
        if (!gamesResponse.getGames().stream().anyMatch(id -> id.equals(gameId))) {
            LOGGER.warning("Created a game, but the created game id is missing from the available games list.");
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
                LOGGER.warning("Game is in progress.");
                break;
            case 3:
                LOGGER.severe("Cannot join to game, the game id doesn't exists.");
                System.exit(1);
            default:
                LOGGER.warning("Unspecified response code: " + joinGameResponse.getCode() + ", with message: " + joinGameResponse.getMessage());
                break;
        }
    }

    @Override
    public void waitForStart() {
        GameResponse gameResponse;
        do {
            try {
                Thread.sleep(SLEEP_TIME_AT_WAITING_FOR_START);
            } catch (InterruptedException e) {
                LOGGER.warning("Exception at Thread.sleep...");
                e.printStackTrace();
            }

            gameResponse = communicator.getGame(gameId);

            if (gameResponse.getCode() == 3) {
                LOGGER.severe("We get back from to server, that the game id doesn't exists.");
                System.exit(1);
            }

            gameStatus = GAME_STATUS.valueOf(gameResponse.getGame().getStatus());
            lastKnownRound = gameResponse.getGame().getRound();
        } while (gameStatus == GAME_STATUS.WAITING);

        // TODO game started, initialize the map, the stats, etc based on the gameResponse
    }

    @Override
    public void waitForNextRound() {
        Integer round;
        do {
            try {
                Thread.sleep(SLEEP_TIME_AT_WAITING_FOR_NEXT_ROUND);
            } catch (InterruptedException e) {
                LOGGER.warning("Exception at Thread.sleep...");
                e.printStackTrace();
            }

            GameResponse gameResponse = communicator.getGame(gameId);

            if (gameResponse.getCode() == 3) {
                LOGGER.severe("We get back from to server, that the game id doesn't exists.");
                System.exit(1);
            }

            gameStatus = GAME_STATUS.valueOf(gameResponse.getGame().getStatus());
            round = gameResponse.getGame().getRound();

            // TODO update the map, the stats, etc based on the gameResponse

        } while (round.equals(lastKnownRound));

        lastKnownRound = round;
    }

    @Override
    public boolean isGameRunning() {
        switch (gameStatus) {
            case WAITING:
                LOGGER.warning("The game is in waiting status, but it is already started once.");
                return false;
            case ENDED:
                LOGGER.info("The game is ended.");
                return false;
            case RUNNING:
                return true;
            default:
                LOGGER.severe("Invalid game status: " + gameStatus);
                System.exit(1);
        }
        return false; // unreachable code
    }
}
