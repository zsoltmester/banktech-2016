package hu.javachallenge.map;

import hu.javachallenge.bean.*;
import hu.javachallenge.processor.Processor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;
import java.util.List;

class MapGui extends DataMap {

    private static final MapGui INSTANCE = new MapGui();

    static MapGui get() {
        return INSTANCE;
    }

    private JFrame frame;
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
        frame.addKeyListener(mapPanel);
        frame.add(mapPanel);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void tick() {
        super.tick();
        new Thread(() -> {
            mapPanel.invalidate();
            mapPanel.repaint();
        }).start();
    }

    private class MapPanel extends JPanel implements KeyListener {

        private final float SIZE_MULTIPLIER = 0.5f;
        private final int TORPEDO_DISPLAY_SIZE = 4;
        private final List<Color> AVAILABLE_TEAM_COLORS =
                Arrays.asList(Color.GREEN, Color.RED, Color.CYAN, Color.YELLOW, Color.GRAY, Color.PINK, Color.MAGENTA);

        private boolean displayWithHistory = false;
        private boolean displayWithRadius = true;
        private boolean displayWithTeamColor = true;
        private Map<String, Color> playersColor = new HashMap<>();
        private Random random = new Random();
        private MapConfiguration configuration;

        private MapPanel(MapConfiguration configuration) {
            this.configuration = configuration;

            Set<String> names = Processor.game.getScores().getScores().keySet();
            int i = 0;
            for(String name : names) {
                playersColor.put(name, AVAILABLE_TEAM_COLORS.get(i));
                ++i;
            }
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

            // paint working radius
            if (ourSubmarines != null && displayWithRadius) {

                ourSubmarines.forEach(submarine -> {
                    graphics.setColor(new Color(0, 0, 200));
                    int maxTorpedoRange = (int) (configuration.getTorpedoRange() * configuration.getTorpedoSpeed());
                    fillCircle(graphics, submarine.getPosition(), maxTorpedoRange);
                });
            }

            // paint our sonars
            if (ourSubmarines != null && displayWithRadius) {

                ourSubmarines.forEach(submarine -> {
                    graphics.setColor(new Color(189,183,107));
                    boolean hasExtendedSonar = submarine.getSonarExtended() > 0;
                    int sonarRange = hasExtendedSonar ? configuration.getExtendedSonarRange() : configuration.getSonarRange();
                    fillCircle(graphics, submarine.getPosition(), sonarRange);
                });
            }

            if (entities != null) {
                // paint torpedos explosion ranges
                getEntities().forEach(entity -> {
                    if (entity.getType().equals(Entity.TORPEDO) && displayWithRadius) {
                        graphics.setColor(Color.ORANGE);
                        fillCircle(graphics, entity.getPosition(), configuration.getTorpedoExplosionRadius());
                    }
                });

                // paint entities
                getEntities().forEach(entity -> {
                    if (entity.getType().equals(Entity.SUBMARINE)) {
                        if (!entity.getOwner().getName().equals(OUR_NAME)) {
                            if (displayWithHistory) {
                                List<Entity> history = getHistory(entity.getId(), 5);
                                for(int i = 0; i < history.size() - 1; ++i) {
                                    Entity e = history.get(i);
                                    if(e != null) {

                                        graphics.setColor(displayWithTeamColor ? playersColor.get(e.getOwner().getName()).brighter() : Color.RED.brighter());
                                        fillCircle(graphics, e.getPosition(), configuration.getSubmarineSize());
                                    }
                                }
                            }

                            graphics.setColor(displayWithTeamColor ? playersColor.get(entity.getOwner().getName()) : Color.RED);
                            fillCircle(graphics, entity.getPosition(), configuration.getSubmarineSize());
                        }
                    } else if (entity.getType().equals(Entity.TORPEDO)) {

                        if (displayWithHistory) {
                            List<Entity> history = getHistory(entity.getId(), 3);
                            for(int i = 0; i < history.size() - 1; ++i) {
                                Entity e = history.get(i);
                                if(e != null) {

                                    graphics.setColor(displayWithTeamColor ? playersColor.get(e.getOwner().getName()).brighter() : Color.CYAN.brighter());
                                    fillCircle(graphics, e.getPosition(), TORPEDO_DISPLAY_SIZE);
                                }
                            }
                        }

                        graphics.setColor(displayWithTeamColor ? playersColor.get(entity.getOwner().getName()) : Color.CYAN);
                        fillCircle(graphics, entity.getPosition(), TORPEDO_DISPLAY_SIZE);
                    }
                });
            }

            // paint our submarines
            if (ourSubmarines != null) {
                ourSubmarines.forEach(submarine -> {
                    graphics.setColor(displayWithTeamColor ? playersColor.get(submarine.getOwner().getName()) : Color.GREEN);
                    fillCircle(graphics, submarine.getPosition(), configuration.getSubmarineSize());
                });
            }

            int height = (int) getPreferredSize().getHeight();

            // paint the HP-s
            if (ourSubmarines != null) {
                for (int i = 0; i < ourSubmarines.size(); ++i) {
                    Submarine submarine = ourSubmarines.get(i);

                    int hp = submarine.getHp();

                    graphics.setColor(Color.GREEN);
                    graphics.fillRect(0, height - i*5 - 5, hp*2, 5);
                    if (hp != 100) {
                        graphics.setColor(Color.RED);
                        graphics.fillRect(hp * 2, height - i*5 - 5, (100 - hp) * 2, 5);
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
            graphics.drawString("Round " + Processor.game.getRound() + " / " + configuration.getRounds(),
                    2, (int) getPreferredSize().getHeight() - configuration.getSubmarinesPerTeam() * 5 - 7);
            int i = 0;

            int roundPercent = (int) (Processor.game.getRound() * 100.0 / configuration.getRounds());

            graphics.setColor(Color.cyan);
            graphics.fillRect(0, height - configuration.getSubmarinesPerTeam() * 5 - 5, roundPercent * 2, 5);
            if (roundPercent != 100) {
                graphics.setColor(Color.DARK_GRAY);
                graphics.fillRect(roundPercent * 2, height - configuration.getSubmarinesPerTeam() * 5 - 5, (100 - roundPercent) * 2, 5);
            }

            Map<String, Boolean> connected = Processor.game.getConnectionStatus().getConnected();
            graphics.setColor(Color.WHITE);
            graphics.drawString("Possible torpedo hit: " + possiblyScores.getX().intValue() + ", destroy: " +
                possiblyScores.getY().intValue(),
                    (int) getPreferredSize().getWidth() - 320, (int) getPreferredSize().getHeight() - 2);
            for (Map.Entry<String, Integer> entry : Processor.game.getScores().getScores().entrySet()) {
                String key = entry.getKey();
                graphics.setColor(connected.get(key) ? displayWithTeamColor ? playersColor.get(key) : Color.white : Color.RED);
                graphics.drawString(key.substring(0, Math.min(10, key.length())) + ": " + entry.getValue(),
                        (int) getPreferredSize().getWidth() - 150, (i+1) * 14);
                ++i;
            }

        }

        private void fillCircle(Graphics graphics, Position position, int radius) {
            graphics.fillOval((int) ((position.getX() - radius) * SIZE_MULTIPLIER),
                    (int) ((position.getY() - radius) * SIZE_MULTIPLIER),
                    (int) (radius * 2 * SIZE_MULTIPLIER),
                    (int) (radius * 2 * SIZE_MULTIPLIER));

        }

        @Override
        public void keyTyped(KeyEvent e) {
            switch (e.getKeyChar()) {
                case 'h':
                    displayWithHistory = !displayWithHistory;
                    break;
                case 'r':
                    displayWithRadius = !displayWithRadius;
                    break;
                case 'c':
                    displayWithTeamColor = !displayWithTeamColor;
                    break;
                case 'g':
                    for(Map.Entry<String, Color> entry : playersColor.entrySet()) {
                        final float hue = random.nextFloat();
                        final float saturation = 0.9f;
                        final float luminance = 1.0f;
                        final Color color = Color.getHSBColor(hue, saturation, luminance);
                        entry.setValue(color);
                    }
                    break;
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {

        }

        @Override
        public void keyReleased(KeyEvent e) {

        }
    }
}
