package hu.javachallenge.strategy;

import hu.javachallenge.map.IMap;
import hu.javachallenge.processor.Processor;

public class Player {

    private Player(Strategy strategy, IMap map) {
        this.strategy = strategy;
        this.map = map;
    }

    private final Strategy strategy;
    private final IMap map;

    private void play() {

        Processor.joinGame();
        Processor.waitForStart();

        strategy.init();

        while (Processor.isGameRunning()) {
            Processor.updateOurSubmarines();
            strategy.onNewRound();
            map.print();
            Processor.waitForNextRound();
        }
    }

    public static void play(Strategy strategy, IMap map) {
        new Player(strategy, map).play();
    }
}
