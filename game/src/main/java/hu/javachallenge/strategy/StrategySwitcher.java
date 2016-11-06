package hu.javachallenge.strategy;

import java.util.function.BooleanSupplier;

/**
 * Created by qqcs on 06/11/16.
 */
public class StrategySwitcher implements Strategy {
    Strategy callerStrategy;
    Strategy replacedByStrategy;
    BooleanSupplier backToPrevious;

    public StrategySwitcher(Strategy callerStrategy, Strategy replacedByStrategy, BooleanSupplier backToPrevious) {
        this.callerStrategy = callerStrategy;
        this.replacedByStrategy = replacedByStrategy;
        this.backToPrevious = backToPrevious;
    }

    @Override
    public void init() {
        replacedByStrategy.init();
    }

    @Override
    public void onStartRound() {
        replacedByStrategy.onStartRound();
    }

    @Override
    public void onRound() {
        replacedByStrategy.onRound();
    }

    @Override
    public Strategy onChangeStrategy() {
        if(backToPrevious.getAsBoolean()) {
            return callerStrategy;
        }

        Strategy replaceStrategy = replacedByStrategy.onChangeStrategy();
        if(replaceStrategy != null) {
            replaceStrategy.init();
            replacedByStrategy = replaceStrategy;
        }

        return null;
    }
}
