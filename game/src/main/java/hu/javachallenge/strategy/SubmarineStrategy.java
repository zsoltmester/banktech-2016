package hu.javachallenge.strategy;

import hu.javachallenge.bean.Submarine;
import hu.javachallenge.map.IMap;
import hu.javachallenge.processor.Processor;

/**
 * Created by qqcs on 06/11/16.
 */
public abstract class SubmarineStrategy implements Strategy {
    protected IMap map = IMap.MapConfig.getMap();

    private final Long submarineId;

    protected SubmarineStrategy(Long submarineId) {
        this.submarineId = submarineId;
    }

    public Submarine getSubmarine() {
        return map.getOurSubmarines().stream().filter(s -> s.getId().equals(submarineId))
                .findFirst().orElse(null);
    }

    @Override
    public final void onStartRound() {
        Submarine submarine = getSubmarine();

        if (submarine.getSonarCooldown() == 0) {
            Processor.extendSonar(submarine.getId());
            submarine.setSonarExtended(map.getConfiguration().getExtendedSonarRounds());
            submarine.setSonarCooldown(map.getConfiguration().getExtendedSonarCooldown());
        }
        Processor.sonar(submarine.getId());
    }
}
