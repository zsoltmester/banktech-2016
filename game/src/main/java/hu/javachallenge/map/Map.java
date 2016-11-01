package hu.javachallenge.map;

import hu.javachallenge.bean.Submarine;

import java.util.List;

public class Map {

    private static final Map INSTANCE = new Map();

    private Map() {
    }

    public static Map get() {
        return INSTANCE;
    }

    private List<Submarine> submarines;

    public void setSubmarines(List<Submarine> submarines) {
        this.submarines = submarines;
    }

    public List<Submarine> getSubmarines() {
        return submarines;
    }
}
