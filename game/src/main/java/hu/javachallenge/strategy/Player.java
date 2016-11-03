package hu.javachallenge.strategy;

import hu.javachallenge.map.Map;
import hu.javachallenge.processor.Processor;

public class Player {

    private static final Strategy STRATEGY = new ScoutStrategy();

    private static final Map MAP = Map.MapConfig.getMap();

    public static void play() {

        Processor.joinGame();
        Processor.waitForStart();

        while (Processor.isGameRunning()) {
            Processor.updateOurSubmarines();
            MAP.print();
            STRATEGY.onNewRound();
            Processor.waitForNextRound();
        }
    }
}
