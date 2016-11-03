package hu.javachallenge.map;

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
    public void updateOurSubmarines(List<Submarine> submarines) {
        this.submarines = submarines;
    }

    @Override
    public List<Submarine> getOurSubmarines() {
        return submarines;
    }

    @Override
    public void print() {
        // TODO should print it to the console
    }
}
