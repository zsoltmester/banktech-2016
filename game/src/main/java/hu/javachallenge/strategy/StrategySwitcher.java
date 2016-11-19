package hu.javachallenge.strategy;

import java.util.function.BooleanSupplier;

public class StrategySwitcher implements Strategy {

    private Strategy caller;
    private Strategy current;
    private BooleanSupplier backToCaller;

    public StrategySwitcher(Strategy caller, Strategy current, BooleanSupplier backToCaller) {
        this.caller = caller;
        this.current = current;
        this.backToCaller = backToCaller;
    }

    @Override
    public void init() {
        current.init();
    }

    @Override
    public void onStartRound() {
        current.onStartRound();
    }

    @Override
    public void onRound() {
        current.onRound();
    }

    @Override
    public Strategy onChangeStrategy() {
        if (backToCaller.getAsBoolean()) {
            // TODO itt elfelejtettem azt, hogy
            // Strategy changerNewStrategy = caller.onChangeStrategy();
            // return changerNewStrategy == null ? caller : changerNewStrategy ;
            return caller;
        }

        Strategy next = current.onChangeStrategy();
        if (next != null) {
            next.init();
            current = next;
            // TODO why we shouldn't change the caller to the previous current?
        }

        return null;
    }

    public Strategy getCurrent() {
        return current;
    }
}
