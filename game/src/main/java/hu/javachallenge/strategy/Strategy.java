package hu.javachallenge.strategy;

public interface Strategy {

    /**
     * Called when a new round is started.
     */
    void onNewRound();

    /**
     * Called when starts a game
     */
    default void init() {}
}
