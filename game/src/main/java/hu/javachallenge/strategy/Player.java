package hu.javachallenge.strategy;

import hu.javachallenge.processor.Processor;

public class Player {

    private static final Strategy STRATEGY = new DummyStrategy();

    public static void play() {

        Processor.joinGame();
        Processor.waitForStart();

        while (Processor.isGameRunning()) {
            Processor.updateOurSubmarines();
            STRATEGY.onNewRound();
            Processor.waitForNextRound();
        }
    }
}
