package hu.javachallenge.map;

import hu.javachallenge.bean.*;

import java.util.List;
import java.util.stream.Stream;

public interface Map {
    String ourName = "Infinite Ringbuffer";

    void initialize(Game game);

    MapConfiguration getConfiguration();

    List<Submarine> getOurSubmarines();

    Stream<Entity> getEntities();
    List<Entity> getEntities(Long submarine);

    boolean isValidPosition(Position position);

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
