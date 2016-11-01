package hu.javachallenge.processor;

import hu.javachallenge.bean.Submarine;

import java.util.List;

public interface Processor {

    /**
     * It creates a game, validates that the server created it and then joins to it.
     * <p>
     * TODO at the finals, it's logic should be more sophisticated. There maybe we don't have to create a game, just join to one with a given game id.
     */
    void joinToGame();

    /**
     * Wait until the game is not ready.
     */
    void waitForStart();

    /**
     * Wait for the next round.
     */
    void waitForNextRound();

    /**
     * @return is the game is running or not.
     */
    boolean isGameRunning();
}
