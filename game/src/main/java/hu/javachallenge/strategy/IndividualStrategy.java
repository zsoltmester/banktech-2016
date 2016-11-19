package hu.javachallenge.strategy;

import hu.javachallenge.bean.Position;
import hu.javachallenge.bean.Submarine;
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

        int submarinesCount = map.getConfiguration().getSubmarinesPerTeam();
        double part = map.getConfiguration().getWidth() / submarinesCount;
        double radarDistance = map.getConfiguration().getExtendedSonarRange();

        int i = 0;
        for(Submarine submarine : map.getOurSubmarines()) {
            Position[] positions = new Position[] {
                    new Position(i * part + radarDistance, map.getConfiguration().getHeight() - radarDistance),
                    new Position((i + 1) * part - radarDistance, map.getConfiguration().getHeight() - radarDistance),
                    new Position((i + 1) * part - radarDistance, radarDistance),
                    new Position(i * part + radarDistance, radarDistance),
            };

            Strategy strategy = new ScoutStrategy(submarine.getId(), positions);
            strategy.init();
            strategies.put(submarine.getId(), strategy);
            ++i;
        }
    }

    @Override
    public void onStartRound() {
        map.getOurSubmarines().forEach(submarine -> strategies.get(submarine.getId()).onStartRound());
    }

    @Override
    public void onRound() {
        map.getOurSubmarines().forEach(submarine -> strategies.get(submarine.getId()).onRound());
    }

    @Override
    public Strategy onChangeStrategy() {
        map.getOurSubmarines().forEach(submarine -> {
            Strategy newStrategy = strategies.get(submarine.getId()).onChangeStrategy();
            if (newStrategy != null) {
                newStrategy.init();
                strategies.put(submarine.getId(), newStrategy);
            }
        });

        // TODO if we want to switch the global state
        return null;
    }

    public Map<Long, Strategy> getStrategies() {
        return strategies;
    }
}
