package hu.javachallenge.map;

import hu.javachallenge.bean.*;
import hu.javachallenge.processor.Processor;

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
        JFrame frame = new JFrame(OUR_NAME);
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

            if (ourSubmarines != null) {
                // paint sonars
                graphics.setColor(Color.YELLOW);
                ourSubmarines.forEach(submarine -> {

                    boolean hasExtendedSonar = submarine.getSonarExtended() > 0;
                    int sonarRange = hasExtendedSonar ? configuration.getExtendedSonarRange() : configuration.getSonarRange();

                    fillCircle(graphics, submarine.getPosition(), sonarRange);
                });
            }

            if (entities != null) {
                // paint torpedos explosion ranges
                getEntities().forEach(entity -> {
                    if (entity.getType().equals(Entity.TORPEDO)) {
                        graphics.setColor(Color.ORANGE);
                        fillCircle(graphics, entity.getPosition(), configuration.getTorpedoExplosionRadius());
                    }
                });
                // paint entities
                getEntities().forEach(entity -> {
                    if (entity.getType().equals(Entity.SUBMARINE)) {
                        if (!entity.getOwner().getName().equals(OUR_NAME)) { // just for safe check, we already removed them
                            graphics.setColor(Color.RED); // TODO calculate color from name, so at the finals each team will have a unique color
                            fillCircle(graphics, entity.getPosition(), configuration.getSubmarineSize());
                        }
                    } else if (entity.getType().equals(Entity.TORPEDO)) {
                        graphics.setColor(Color.CYAN);
                        fillCircle(graphics, entity.getPosition(), configuration.getTorpedoRange());
                    }
                });
            }

            // paint our submarines
            if (ourSubmarines != null) {
                graphics.setColor(Color.GREEN); // TODO calculate color from name, so at the finals each team will have a unique color
                ourSubmarines.forEach(submarine -> {
                    fillCircle(graphics, submarine.getPosition(), configuration.getSubmarineSize());
                });
            }

            // paint the HP-s
            if (ourSubmarines != null) {
                int height = (int) getPreferredSize().getHeight();
                for (int i = 0; i < ourSubmarines.size(); ++i) {
                    Submarine submarine = ourSubmarines.get(i);

                    int hp = submarine.getHp();

                    graphics.setColor(Color.GREEN);
                    graphics.fillRect(0, (int) (height - i * 10 * SIZE_MULTIPLIER), hp * 2, (int) (10 * SIZE_MULTIPLIER));
                    if (hp != 100) {
                        graphics.setColor(Color.RED);
                        graphics.fillRect(hp * 2, (int) (height - i * 10 * SIZE_MULTIPLIER), (100 - hp) * 2, (int) (10 * SIZE_MULTIPLIER));
                    }
                }
            }

            // paint the islands
            graphics.setColor(Color.BLACK);
            configuration.getIslandPositions().forEach(islandPosition -> {
                fillCircle(graphics, islandPosition, configuration.getIslandSize());
            });

            // paint the round number
            Font usedFont = new Font("Dialog", Font.BOLD, 16);
            graphics.setFont(usedFont);
            graphics.setColor(Color.WHITE);
            graphics.drawString("Round " + Processor.game.getRound(),
                    2, (int) getPreferredSize().getHeight() - 12);
        }

        private void fillCircle(Graphics graphics, Position position, int radius) {
            graphics.fillOval((int) ((position.getX() - radius) * SIZE_MULTIPLIER),
                    (int) ((position.getY() - radius) * SIZE_MULTIPLIER),
                    (int) (radius * 2 * SIZE_MULTIPLIER),
                    (int) (radius * 2 * SIZE_MULTIPLIER));

        }
    }
}
