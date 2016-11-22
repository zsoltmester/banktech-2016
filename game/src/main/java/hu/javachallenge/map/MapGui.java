package hu.javachallenge.map;

import hu.javachallenge.bean.*;
import hu.javachallenge.processor.Processor;
import hu.javachallenge.strategy.*;

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

    private Map<String, Integer> previousScores = new HashMap<>();
    private Map<String, Position> possiblyScores = new HashMap<>();

    private MapGui() {
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
        Map<String, Integer> scores = Processor.game.getScores().getScores();
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            Integer previousScore = previousScores.get(entry.getKey());
            if (previousScore == null) {
                possiblyScores.put(entry.getKey(), new Position(0, 0));
                continue;
            }
            Position possiblyScore = possiblyScores.get(entry.getKey());

            Integer gotScore = entry.getValue() - previousScore;

            /*
            // Sometimes the following bad data comes from the server:
            LOGGER.severe("previousScore: " + previousScore); // 350
            LOGGER.severe("entry.getValue(): " + entry.getValue()); // 340
            LOGGER.severe("gotScore: " + gotScore); // -10
            // So that's why we do the following hack:
            */
            if (gotScore < 0) {
                gotScore = 0;
            }

            int hitScore = getConfiguration().getTorpedoHitScore();
            int destroyScore = getConfiguration().getTorpedoDestroyScore();

            if (gotScore % hitScore != 0) {
                possiblyScore.setX(possiblyScore.getX() + 1);
                possiblyScore.setY(possiblyScore.getY() + 1);
                gotScore -= destroyScore;
                gotScore -= hitScore;
            }
            possiblyScore.setX(possiblyScore.getX() + gotScore / hitScore);

        }
        previousScores = scores;

        new Thread(() -> {
            mapPanel.invalidate();
            mapPanel.repaint();
        }).start();
    }

    private class MapPanel extends JPanel implements KeyListener {

        private final float SIZE_MULTIPLIER = 0.5f;
        private final int TORPEDO_DISPLAY_SIZE = 4;
        private final int TARGET_DISPLAY_SIZE = 6;
        private final List<Color> AVAILABLE_TEAM_COLORS =
                Arrays.asList(Color.GREEN, Color.RED, Color.CYAN, Color.YELLOW, Color.GRAY, Color.PINK, Color.MAGENTA);

        private boolean displayWithHistory = false;
        private boolean displayWithRadius = true;
        private boolean displayWithTeamColor = true;
        private boolean displayWithTargets = true;
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
                        fillDirectionCircle(graphics, entity.getPosition(), configuration.getTorpedoExplosionRadius(), entity.getAngle());
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
                                        fillDirectionCircle(graphics, e.getPosition(), configuration.getSubmarineSize(), e.getAngle());
                                    }
                                }
                            }

                            graphics.setColor(displayWithTeamColor ? playersColor.get(entity.getOwner().getName()) : Color.RED);
                            fillDirectionCircle(graphics, entity.getPosition(), configuration.getSubmarineSize(), entity.getAngle());
                        }
                    } else if (entity.getType().equals(Entity.TORPEDO)) {

                        if (displayWithHistory) {
                            List<Entity> history = getHistory(entity.getId(), 3);
                            for(int i = 0; i < history.size() - 1; ++i) {
                                Entity e = history.get(i);
                                if(e != null) {

                                    graphics.setColor(displayWithTeamColor ? playersColor.get(e.getOwner().getName()).brighter() : Color.CYAN.brighter());
                                    fillDirectionCircle(graphics, e.getPosition(), TORPEDO_DISPLAY_SIZE, e.getAngle());
                                }
                            }
                        }

                        graphics.setColor(displayWithTeamColor ? playersColor.get(entity.getOwner().getName()) : Color.CYAN);
                        fillDirectionCircle(graphics, entity.getPosition(), TORPEDO_DISPLAY_SIZE, entity.getAngle());
                    }
                });
            }

            // paint our submarines
            if (ourSubmarines != null) {
                ourSubmarines.forEach(submarine -> {
                    graphics.setColor(displayWithTeamColor ? playersColor.get(submarine.getOwner().getName()) : Color.GREEN);
                    fillDirectionCircle(graphics, submarine.getPosition(), configuration.getSubmarineSize(), submarine.getAngle());
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

            // paint the targets
            if (displayWithTargets && ourSubmarines != null) {
                IndividualStrategy individualStrategy;
                if ((individualStrategy = Player.strategy instanceof IndividualStrategy ? (IndividualStrategy) Player.strategy : null) != null && individualStrategy.getStrategies() != null && !individualStrategy.getStrategies().isEmpty()) {
                    Map<Long, Strategy> strategies = individualStrategy.getStrategies();
                    ourSubmarines.stream().filter(submarine -> strategies.keySet().contains(submarine.getId())).forEach(submarine ->
                    {
                        Strategy strategy = strategies.get(submarine.getId());
                        if (strategy == null) return;
                        ScoutStrategy scoutStrategy = null;
                        while (strategy instanceof StrategySwitcher) {
                            strategy = ((StrategySwitcher) strategy).getCurrent();
                        }
                        if (strategy instanceof ScoutStrategy) scoutStrategy = (ScoutStrategy) strategy;
                        if (scoutStrategy == null) return;
                        graphics.setColor(Color.BLACK);
                        scoutStrategy.getTargets().stream().forEachOrdered(target -> {
                            fillCircle(graphics, target, TARGET_DISPLAY_SIZE);
                            graphics.setColor(Color.WHITE);
                        });
                    });
                }
                NewAwesomeStrategy newAwesomeStrategy;
                if ((newAwesomeStrategy = Player.strategy instanceof NewAwesomeStrategy ? (NewAwesomeStrategy) Player.strategy : null) != null) {
                    ArrayList<Position> targetsArray = new ArrayList<>(newAwesomeStrategy.getTargets());
                    for (int i = 0; i < targetsArray.size(); ++i) {
                        float c = (float)i / targetsArray.size();
                        graphics.setColor(new Color(c, c, c));
                        fillCircle(graphics, targetsArray.get(i), TARGET_DISPLAY_SIZE);
                    }
                }
            }

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
            for (Map.Entry<String, Integer> entry : Processor.game.getScores().getScores().entrySet()) {
                String key = entry.getKey();
                graphics.setColor(connected.get(key) ? displayWithTeamColor ? playersColor.get(key) : Color.white : Color.RED);
                graphics.drawString(key.substring(0, Math.min(10, key.length())) + ": " + entry.getValue(),
                        (int) getPreferredSize().getWidth() - 150, (i+1) * 14);

                if (possiblyScores != null && !possiblyScores.isEmpty()) {
                    graphics.drawString("Hit: " + possiblyScores.get(entry.getKey()).getX().intValue() + ", Des: " +
                                    possiblyScores.get(entry.getKey()).getY().intValue(),
                            (int) getPreferredSize().getWidth() - 320, (i + 1) * 14);
                }

                ++i;
            }

        }

        private void fillCircle(Graphics graphics, Position position, int radius) {
            graphics.fillOval((int) ((position.getX() - radius) * SIZE_MULTIPLIER),
                    (int) ((position.getY() - radius) * SIZE_MULTIPLIER),
                    (int) (radius * 2 * SIZE_MULTIPLIER),
                    (int) (radius * 2 * SIZE_MULTIPLIER));

        }

        private void fillDirectionCircle(Graphics graphics, Position position, int radius, double angle) {
            fillCircle(graphics, position, radius);

            double rad = angle * Math.PI / 180.0;

            int smallRadius = 3;
            int directionPosition = (int)(0.8 * radius);

            graphics.setColor(Color.WHITE);
            Position angleUnit = new Position(directionPosition * Math.cos(rad), directionPosition * Math.sin(rad));
            graphics.fillOval(
                (int) ((position.getX() + angleUnit.getX() - smallRadius) * SIZE_MULTIPLIER),
                (int) ((position.getY() + angleUnit.getY() - smallRadius) * SIZE_MULTIPLIER),
                (int) (smallRadius * 2 * SIZE_MULTIPLIER),
                (int) (smallRadius * 2 * SIZE_MULTIPLIER));
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
                case 't':
                    displayWithTargets = !displayWithTargets;
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
