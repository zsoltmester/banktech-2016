package hu.javachallenge.strategy;

import hu.javachallenge.map.IMap;
import hu.javachallenge.processor.Processor;

public class Player {

    private Player(Strategy strategy, IMap map) {
        this.strategy = strategy;
        this.map = map;
    }

    private final IMap map;

    private Strategy strategy;

    private void play() {

        Processor.joinGame();
        Processor.waitForStart();

        strategy.init();

        while (Processor.isGameRunning()) {
            Processor.updateOurSubmarines();
            strategy.onStartRound();

            Strategy newStrategy = strategy.onChangeStrategy();
            if (newStrategy != null) {
                newStrategy.init();
                strategy = newStrategy;
            }

            strategy.onRound();

            map.tick();
            Processor.waitForNextRound();
        }
    }

    public static void play(Strategy strategy, IMap map) {
        new Player(strategy, map).play();
    }
}
