package hu.javachallenge.map;

import hu.javachallenge.bean.*;
import hu.javachallenge.processor.Processor;

import java.util.*;
import java.util.stream.Collectors;

class DataMap implements IMap {

    private static final DataMap INSTANCE = new DataMap();

    DataMap() {
    }

    static DataMap get() {
        return INSTANCE;
    }

    protected MapConfiguration configuration;

    protected List<Submarine> ourSubmarines;

    protected HashMap<Long, List<Entity>> entities = new HashMap<>();

    protected NavigableMap<Integer, java.util.Map<Long, Entity>> entityHistory = new TreeMap<>();

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
    public List<Entity> getHistory(Long id, int count) {
        int round = Processor.game.getRound();

        List<Entity> idHistory = new ArrayList<>();

        for(int i = round - count + 1; i < 0; ++i) {
            idHistory.add(null);
        }

        for(int i = Math.max(0, round - count + 1); i <= round; ++i) {

            java.util.Map<Long, Entity> roundHistory = entityHistory.get(i);

            Entity entity = roundHistory == null ? null : roundHistory.get(id);
            idHistory.add(entity);
        }
        return idHistory;
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

        List<Long> destroyedSubmarines = entities.keySet().stream()
                .filter(id -> !this.ourSubmarines.stream().anyMatch(submarine -> submarine.getId().equals(id)))
                .collect(Collectors.toList());

        destroyedSubmarines.forEach(id -> entities.remove(id));
    }

    @Override
    public void submarineShoot(Long submarine, Double angle) {
        // TODO ez talán akkor lehet hasznos, ha követni akarjuk, hogy a torpedónk merre megy (és a következő sonar-ból már kimegy a rakéta, szóval msot látjuk utoljára)
        // sajnos nem hasznos, mert mindenképpen kapunk infót róla, hogy megjelent, és hogy merre mozog.
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

        entityHistory.put(Processor.game.getRound(), getEntities().stream().collect(Collectors.toMap(
                Entity::getId, e -> e, (e1, e2) -> e1
        )));
    }

    @Override
    public void print() {
        // no need for console map
    }

    @Override
    public void close() {

    }
}
