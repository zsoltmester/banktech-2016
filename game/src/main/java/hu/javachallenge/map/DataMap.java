package hu.javachallenge.map;

import hu.javachallenge.bean.*;

import java.util.*;
import java.util.stream.Stream;

class DataMap implements Map {

    private static final DataMap INSTANCE = new DataMap();

    DataMap() {
    }

    static DataMap get() {
        return INSTANCE;
    }

    protected MapConfiguration configuration;

    protected List<Submarine> ourSubmarines;

    protected HashMap<Long, List<Entity>> entities = new HashMap<>();

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
    public Stream<Entity> getEntities() {
        return entities.values().stream().flatMap(Collection::stream);
    }

    @Override
    public List<Entity> getEntities(Long submarine) {
        return entities.get(submarine);
    }

    @Override
    public void updateOurSubmarines(List<Submarine> submarines) {
        this.ourSubmarines = submarines;
    }

    @Override
    public void submarineShoot(Long submarine, Double angle) {
        /*
        Entity entity = new Entity();
        entity.setPosition(ourSubmarines.stream().filter(s -> s.getId().equals(submarine)).findFirst().orElse(null)
            .getPosition());
        entity.setAngle(angle);
        entity.setOwner(new Owner(ourName));
        entity.setRoundsMoved(0);
        entity.setVelocity(configuration.getTorpedoSpeed());
        entity.setType("Torpedo");

        this.entities.put(-1L, Collections.singletonList(entity));
        */
        // TODO
        // but nothing to do
    }

    @Override
    public void processSonarResult(Long submarine, List<Entity> entities) {
        this.entities.put(submarine, entities);
    }

    @Override
    public void print() {
        // TODO should print it to the console
    }
}
