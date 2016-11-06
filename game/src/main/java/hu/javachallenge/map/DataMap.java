package hu.javachallenge.map;

import hu.javachallenge.bean.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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
    public List<Entity> getEntities() {
        return entities.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public List<Entity> getEntitiesForSubmarine(Long submarine) {
        return entities.get(submarine);
    }

    @Override
    public boolean isValidPosition(Position position) {
        return position != null &&
                0 <= position.getX() && position.getX() < configuration.getWidth() &&
                0 <= position.getY() && position.getY() < configuration.getHeight();
    }

    @Override
    public void updateOurSubmarines(List<Submarine> submarines) {
        this.ourSubmarines = submarines;

        List<Long> submarinesToRemove = entities.keySet().stream()
                .filter(id -> this.ourSubmarines.stream().anyMatch(submarine -> submarine.getId().equals(id)))
                .collect(Collectors.toList());

        submarinesToRemove.forEach(id -> entities.remove(id));
    }

    @Override
    public void submarineShoot(Long submarine, Double angle) {
        // TODO ez talán akkor lehet hasznos, ha követni akarjuk, hogy a torpedónk merre megy (és a következő sonar-ból már kimegy a rakéta, szóval msot látjuk utoljára)
        /*
        Entity entity = new Entity();
        entity.setPosition(ourSubmarines.stream().filter(s -> s.getId().equals(submarine)).findFirst().orElse(null)
            .getPosition());
        entity.setAngle(angle);
        entity.setOwner(new Owner(OUR_NAME));
        entity.setRoundsMoved(0);
        entity.setVelocity(configuration.getTorpedoSpeed());
        entity.setType("Torpedo");

        this.entities.put(-1L, Collections.singletonList(entity));
        */
    }

    @Override
    public void processSonarResult(Long submarine, List<Entity> entities) {
        this.entities.put(submarine, entities);
    }

    @Override
    public void print() {
        // no need for console map
    }
}
