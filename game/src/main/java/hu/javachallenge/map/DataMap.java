package hu.javachallenge.map;

import hu.javachallenge.bean.Entity;
import hu.javachallenge.bean.Game;
import hu.javachallenge.bean.MapConfiguration;
import hu.javachallenge.bean.Submarine;

import java.util.List;

class DataMap implements Map {

    private static final DataMap INSTANCE = new DataMap();

    DataMap() {
    }

    static DataMap get() {
        return INSTANCE;
    }

    protected MapConfiguration configuration;

    protected List<Submarine> ourSubmarines;

    @Override
    public void initialize(Game game) {
        this.configuration = game.getMapConfiguration();
    }

    @Override
    public MapConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public List<Submarine> getOurSubmarines() {
        return ourSubmarines;
    }

    @Override
    public void updateOurSubmarines(List<Submarine> submarines) {
        this.ourSubmarines = submarines;
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
