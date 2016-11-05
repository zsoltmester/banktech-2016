package hu.javachallenge.map;

import hu.javachallenge.bean.Entity;
import hu.javachallenge.bean.Game;
import hu.javachallenge.bean.MapConfiguration;
import hu.javachallenge.bean.Submarine;

import java.util.List;
import java.util.stream.Stream;

public interface Map {
    String ourName = "Infinite Ringbuffer";

    void initialize(Game game);

    MapConfiguration getConfiguration();

    List<Submarine> getOurSubmarines();

    Stream<Entity> getEntities();
    List<Entity> getEntities(Long submarine);

    void updateOurSubmarines(List<Submarine> submarines);

    void submarineShoot(Long submarine, Double angle);

    void processSonarResult(Long submarine, List<Entity> entities);

    void print();

    class MapConfig {

        private MapConfig() {
        }

        public static Map getMap() {
            return MapGui.get();
        }
    }
}
