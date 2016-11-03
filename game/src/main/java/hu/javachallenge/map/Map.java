package hu.javachallenge.map;

import hu.javachallenge.bean.Submarine;

import java.util.List;

public interface Map {

    List<Submarine> getOurSubmarines();

    void updateOurSubmarines(List<Submarine> submarines);

    void print();

    class MapConfig {

        private MapConfig() {
        }

        public static Map getMap() {
            return SimpleMap.get();
        }
    }
}
