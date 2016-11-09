package hu.javachallenge.map;

import hu.javachallenge.bean.*;

import java.util.List;
import java.util.Map;

public interface IMap extends AutoCloseable {

    String OUR_NAME = "Infinite Ringbuffer";

    void initialize(Game game);

    MapConfiguration getConfiguration();

    List<Submarine> getOurSubmarines();

    List<Entity> getEntities();

    List<Entity> getEntitiesForSubmarine(Long submarine);

    List<Entity> getHistory(Long id, int count);

    Map<Integer, Map<Long, Entity>> getAllHistory();

    boolean isValidPosition(Position position);

    void updateOurSubmarines(List<Submarine> submarines);

    void submarineShoot(Long submarine, Double angle);

    void processSonarResult(Long submarine, List<Entity> entities);

    void tick();

    class MapConfig {

        private MapConfig() {
        }

        public static IMap getMap() {
            return MapGui.get();
        }
    }
}
