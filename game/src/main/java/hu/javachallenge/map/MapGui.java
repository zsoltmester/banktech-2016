package hu.javachallenge.map;

import hu.javachallenge.bean.Game;
import hu.javachallenge.bean.MapConfiguration;
import hu.javachallenge.bean.Position;

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
            graphics.fillRect(0, 0, getPreferredSize().width, getPreferredSize().height);


            // paint our submarines
            if (ourSubmarines != null) {
                // paint sonars
                graphics.setColor(Color.YELLOW); // TODO calculate color from name
                ourSubmarines.forEach(submarine -> {

                    boolean hasExtendedSonar = submarine.getSonarExtended() > 0;
                    int sonarRange = (int)( (hasExtendedSonar ? configuration.getExtendedSonarRange() : configuration.getSonarRange()) * SIZE_MULTIPLIER / 2 );

                    fillCircle(graphics, submarine.getPosition(), sonarRange);
                });

                // paint submarine
                graphics.setColor(Color.GREEN); // TODO calculate color from name
                ourSubmarines.forEach(submarine -> {

                    fillCircle(graphics, submarine.getPosition(), configuration.getSubmarineSize());
                });
            }
            // TODO paint the other objects


            // paint the islands
            graphics.setColor(Color.BLACK);
            configuration.getIslandPositions().forEach(islandPosition -> {
                fillCircle(graphics, islandPosition, configuration.getIslandSize());
            });
        }

        private void fillCircle(Graphics graphics, Position position, int radius) {
            graphics.fillOval((int) ((position.getX() - radius) * SIZE_MULTIPLIER),
                    (int) ((position.getY() - radius) * SIZE_MULTIPLIER),
                    (int) (radius * 2 * SIZE_MULTIPLIER),
                    (int) (radius * 2 * SIZE_MULTIPLIER));

        }
    }
}
