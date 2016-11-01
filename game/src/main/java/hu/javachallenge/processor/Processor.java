package hu.javachallenge.processor;

public interface Processor {

    /**
     * It creates a game, validates that the server created it and then joins to it.
     * <p>
     * TODO at the finals, it's logic should be more sophisticated. There maybe we don't have to create a game, just join to one with a given game id.
     *
     * @return if the game is successfully started or not.
     */
    boolean joinGame();
}
