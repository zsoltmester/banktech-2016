package hu.javachallenge.strategy;

import hu.javachallenge.map.IMap;
import hu.javachallenge.processor.Processor;

import java.util.HashMap;
import java.util.Map;

public class IndividualStrategy implements Strategy {

    private IMap map = IMap.MapConfig.getMap();

    private Map<Long, Strategy> strategies = new HashMap<>();

    @Override
    public void init() {
        Processor.updateOurSubmarines();
        map.getOurSubmarines().forEach(submarine -> {
            Strategy strategy = new ScoutStrategy();
            strategy.init();
            strategies.put(submarine.getId(), strategy);
        });
    }

    @Override
    public void onStartRound() {
        map.getOurSubmarines().forEach(submarine -> strategies.get(submarine).onStartRound());
    }

    @Override
    public void onRound() {
        map.getOurSubmarines().forEach(submarine -> strategies.get(submarine).onRound());
    }

    @Override
    public Strategy onChangeStrategy() {
        map.getOurSubmarines().forEach(submarine -> {
            Strategy newStrategy = strategies.get(submarine).onChangeStrategy();
            if (newStrategy != null) {
                newStrategy.init();
                strategies.put(submarine.getId(), newStrategy);
            }
        });
        return null;
    }
}
