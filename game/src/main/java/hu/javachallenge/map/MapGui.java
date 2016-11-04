package hu.javachallenge.map;

import hu.javachallenge.bean.Game;
import hu.javachallenge.bean.MapConfiguration;

import javax.swing.*;
import java.awt.*;

class MapGui extends DataMap {

    private static final MapGui INSTANCE = new MapGui();

    static MapGui get() {
        return INSTANCE;
    }

    private MapPanel mapPanel;

    MapGui() {
    }

    @Override
    public void initialize(Game game) {
        super.initialize(game);
        mapPanel = new MapPanel(super.configuration);
        JFrame frame = new JFrame("Infinite Ringbuffer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(mapPanel);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void print() {
        super.print();
        new Thread(() -> {
            mapPanel.invalidate();
            mapPanel.repaint();
        }).start();
    }

    private class MapPanel extends JPanel {

        private static final float SIZE_MULTIPLIER = 0.5f;

        private MapConfiguration configuration;

        private MapPanel(MapConfiguration configuration) {
            this.configuration = configuration;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension((int) (configuration.getWidth() * SIZE_MULTIPLIER), (int) (configuration.getHeight() * SIZE_MULTIPLIER));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            // paint the background
            graphics.setColor(Color.BLUE);
            graphics.fillRect(0, 0, (int) (configuration.getWidth() * SIZE_MULTIPLIER), (int) (configuration.getHeight() * SIZE_MULTIPLIER));

            // paint the islands
            graphics.setColor(Color.BLACK);
            configuration.getIslandPositions().forEach(islandPosition -> {
                graphics.fillOval((int) (islandPosition.getX() * SIZE_MULTIPLIER), (int) (islandPosition.getY() * SIZE_MULTIPLIER),
                        (int) ((configuration.getIslandSize() * SIZE_MULTIPLIER) / 2), (int) (configuration.getIslandSize() * SIZE_MULTIPLIER) / 2);
            });

            // paint our submarines
            graphics.setColor(Color.GREEN); // TODO calculate color from name
            if (ourSubmarines != null) {
                ourSubmarines.forEach(submarine -> {
                    graphics.fillOval((int) (submarine.getPosition().getX() * SIZE_MULTIPLIER), (int) (submarine.getPosition().getY() * SIZE_MULTIPLIER),
                            (int) (configuration.getSubmarineSize() * SIZE_MULTIPLIER / 2), (int) (configuration.getSubmarineSize() * SIZE_MULTIPLIER / 2));
                });
            }
            // TODO paint the other objects
        }
    }
}
