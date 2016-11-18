package hu.javachallenge.strategy;

import hu.javachallenge.map.IMap;
import hu.javachallenge.processor.Processor;

import java.util.logging.Logger;

public class Player {

    private static final Logger LOGGER = Logger.getLogger(Player.class.getName());

    private Player(Strategy strategy) {
        this.strategy = strategy;
    }

    private final IMap map = IMap.MapConfig.getMap();

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

        LOGGER.info("Final scores: " + Processor.game.getScores());
    }

    public static void play(Strategy strategy) {
        new Player(strategy).play();
    }
}
