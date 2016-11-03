package hu.javachallenge.map;

import hu.javachallenge.bean.Entity;
import hu.javachallenge.bean.Game;
import hu.javachallenge.bean.MapConfiguration;
import hu.javachallenge.bean.Submarine;

import java.util.List;

public interface Map {

    void initialize(Game game);

    MapConfiguration getConfiguration();

    List<Submarine> getOurSubmarines();

    void updateOurSubmarines(List<Submarine> submarines);

    void submarineShoot(Long submarine, Double angle);

    void processSonarResult(List<Entity> entities);

    void print();

    class MapConfig {

        private MapConfig() {
        }

        public static Map getMap() {
            return MapGui.get();
        }
    }
}
