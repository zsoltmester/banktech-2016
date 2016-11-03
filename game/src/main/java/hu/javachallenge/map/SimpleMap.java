package hu.javachallenge.map;

import hu.javachallenge.bean.Entity;
import hu.javachallenge.bean.Game;
import hu.javachallenge.bean.Submarine;

import java.util.List;

public class SimpleMap implements Map {

    private static final SimpleMap INSTANCE = new SimpleMap();

    SimpleMap() {
    }

    static SimpleMap get() {
        return INSTANCE;
    }

    private List<Submarine> submarines;

    @Override
    public void initialize(Game game) {
        // TODO
    }

    @Override
    public List<Submarine> getOurSubmarines() {
        return submarines;
    }

    @Override
    public void updateOurSubmarines(List<Submarine> submarines) {
        this.submarines = submarines;
    }

    @Override
    public void submarineShoot(Long submarine, Double angle) {
        // TODO
    }

    @Override
    public void processSonarResult(List<Entity> entities) {
        // TODO
    }

    @Override
    public void print() {
        // TODO should print it to the console
    }
}
