package hu.javachallenge.processor;

import hu.javachallenge.App;
import hu.javachallenge.bean.CreateGameResponse;
import hu.javachallenge.bean.GamesResponse;
import hu.javachallenge.bean.JoinGameResponse;
import hu.javachallenge.communication.Communicator;
import hu.javachallenge.communication.CommunicatorImpl;

import java.util.logging.Logger;

public class ProcessorImpl implements Processor {

    private static final Logger LOGGER = Logger.getLogger(ProcessorImpl.class.getName());

    private Communicator communicator = new CommunicatorImpl(App.serverAddress);

    private Long gameId;

    @Override
    public boolean joinGame() {
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
                return true;
            case 1:
                LOGGER.severe("Cannot join to game, because we not invited.");
                return false;
            case 2:
                LOGGER.warning("Game is in progress.");
                return true;
            case 3:
                LOGGER.severe("Cannot join to game, the game id doesn't exists.");
                return false;
            default:
                LOGGER.warning("Unspecified response code: " + joinGameResponse.getCode() + ", with message: " + joinGameResponse.getMessage());
                return true;
        }
    }

}
