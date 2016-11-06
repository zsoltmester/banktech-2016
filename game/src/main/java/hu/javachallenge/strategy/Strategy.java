package hu.javachallenge.strategy;

public interface Strategy {

    void init();

    void onStartRound();

    void onRound();

    Strategy onChangeStrategy();
}
