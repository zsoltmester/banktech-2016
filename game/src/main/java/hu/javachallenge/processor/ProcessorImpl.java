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

    private static final Integer SLEEP_TIME_AT_WAITING = 100;

    private Communicator communicator = new CommunicatorImpl(App.serverAddress);

    private enum GAME_STATUS {
        WAITING, RUNNING, ENDED
    }

    private Long gameId;

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
        String status;

        do {
            GameResponse gameResponse = communicator.getGame(gameId);

            if (gameResponse.getCode() == 3) {
                LOGGER.severe("We get back from to server, that the game id doesn't exists.");
                System.exit(1);
            }

            status = gameResponse.getGame().getStatus();

            try {
                Thread.sleep(SLEEP_TIME_AT_WAITING);
            } catch (InterruptedException e) {
                LOGGER.warning("Exception at Thread.sleep...");
                e.printStackTrace();
            }
        } while (GAME_STATUS.WAITING.toString().equals(status));
    }

}
