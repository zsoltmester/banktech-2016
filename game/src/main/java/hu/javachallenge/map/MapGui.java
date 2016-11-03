package hu.javachallenge.map;

public class MapGui extends SimpleMap {

    private static final MapGui INSTANCE = new MapGui();

    MapGui() {
    }

    static MapGui get() {
        return INSTANCE;
    }

    @Override
    public void print() {
        super.print();
        // TODO should also update our GUI
    }
}
