package hu.javachallenge.strategy;

import hu.javachallenge.bean.Submarine;
import hu.javachallenge.map.IMap;
import hu.javachallenge.processor.Processor;

public abstract class SubmarineStrategy implements Strategy {

    protected IMap map = IMap.MapConfig.getMap();

    protected final Long submarineId;

    protected SubmarineStrategy(Long submarineId) {
        this.submarineId = submarineId;
    }

    public Submarine getSubmarine() {
        return map.getOurSubmarines().stream().filter(s -> s.getId().equals(submarineId)).findFirst().orElse(null);
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

    @Override
    public Strategy onChangeStrategy() {
        // TODO maybe we should change here to the panic strategy, because it's a common thing and independent from the current strategy
        return null;
    }
}
