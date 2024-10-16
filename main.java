import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class main extends JFrame {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int DUCK_WIDTH = 40;
    private static final int DUCK_HEIGHT = 30;
    private static final int INITIAL_SPAWN_DELAY = 2000; // Initial delay in milliseconds
    private static final int MIN_SPAWN_DELAY = 500; // Minimum delay in milliseconds
    private static final int MAX_MISSED_DUCKS = 10; // Maximum missed ducks before game over
    private static final int TREE_COUNT = 5; // Number of trees to generate

    private JPanel gamePanel;
    private ArrayList<Duck> ducks;
    private ArrayList<Tree> trees;
    private int score;
    private int missedDucks;
    private JLabel scoreLabel;
    private JLabel missedLabel;
    private Timer spawnTimer;
    private Random random;
    private boolean gameRunning;

    public main() {
        setTitle("Duck Shooting Game");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        ducks = new ArrayList<>();
        trees = new ArrayList<>();
        score = 0;
        missedDucks = 0;
        random = new Random();
        gameRunning = false;

        generateTrees();
        setupGamePanel();
        setupControlPanel();

        spawnTimer = new Timer(INITIAL_SPAWN_DELAY, e -> spawnDuck());
    }

    private void generateTrees() {
        for (int i = 0; i < TREE_COUNT; i++) {
            int x = random.nextInt(WIDTH - 60) + 30; // Random x position with some margin
            int y = HEIGHT - random.nextInt(100) - 50; // Random height
            trees.add(new Tree(x, y));
        }
    }

    private void setupGamePanel() {
        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.BLUE); // Set background color to blue
                g.fillRect(0, 0, WIDTH, HEIGHT); // Fill the background

                for (Tree tree : trees) {
                    tree.draw(g); // Draw trees
                }
                for (Duck duck : ducks) {
                    duck.draw(g); // Draw ducks
                }
            }
        };
        gamePanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameRunning) {
                    checkShot(e.getX(), e.getY());
                }
            }
        });
        add(gamePanel, BorderLayout.CENTER);
    }

    private void setupControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(new Color(150, 150, 150));
        controlPanel.setLayout(new FlowLayout());

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
        controlPanel.add(scoreLabel);

        missedLabel = new JLabel("Missed: 0");
        missedLabel.setFont(new Font("Arial", Font.BOLD, 20));
        controlPanel.add(missedLabel);

        JButton startButton = createStyledButton("Start Game");
        startButton.addActionListener(e -> startGame());
        controlPanel.add(startButton);

        JButton replayButton = createStyledButton("Replay");
        replayButton.addActionListener(e -> startGame());
        replayButton.setEnabled(false); // Initially disabled
        controlPanel.add(replayButton);

        add(controlPanel, BorderLayout.NORTH);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(new Color(0, 100, 0)); // Dark green
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(120, 40));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Hand cursor on hover
        return button;
    }

    private void startGame() {
        score = 0;
        missedDucks = 0;
        scoreLabel.setText("Score: 0");
        missedLabel.setText("Missed: 0");
        ducks.clear();
        gameRunning = true;
        spawnTimer.start();

        Timer gameTimer = new Timer(16, e -> {
            if (gameRunning) {
                updateGame();
                gamePanel.repaint();
            }
        });
        gameTimer.start();
    }

    private void updateGame() {
        for (int i = ducks.size() - 1; i >= 0; i--) {
            Duck duck = ducks.get(i);
            duck.move();
            if (duck.y + DUCK_HEIGHT < 0) {
                ducks.remove(i);
                missedDucks++;
                missedLabel.setText("Missed: " + missedDucks);
                if (missedDucks >= MAX_MISSED_DUCKS) {
                    gameOver();
                }
            }
        }
        adjustDifficulty();
    }

    private void adjustDifficulty() {
        // Adjust spawn delay and duck speed based on score
        int newDelay = Math.max(MIN_SPAWN_DELAY, INITIAL_SPAWN_DELAY - (score * 100)); // Decrease delay
        spawnTimer.setDelay(newDelay);

        for (Duck duck : ducks) {
            duck.speed = Math.min(duck.speed + (score / 5), 8); // Limit max speed
        }
    }

    private void gameOver() {
        gameRunning = false;
        spawnTimer.stop();
        JOptionPane.showMessageDialog(this, "Game Over! Your score: " + score, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }

    private void spawnDuck() {
        int x = random.nextInt(WIDTH - DUCK_WIDTH);
        int y = HEIGHT;
        int speed = random.nextInt(3) + 1; // Initial speed between 1 and 3
        ducks.add(new Duck(x, y, speed));
    }

    private void checkShot(int x, int y) {
        for (int i = ducks.size() - 1; i >= 0; i--) {
            Duck duck = ducks.get(i);
            if (duck.contains(x, y)) {
                ducks.remove(i);
                score++;
                scoreLabel.setText("Score: " + score);
                break;
            }
        }
    }

    private class Duck {
        int x, y, speed;
        Color color;

        Duck(int x, int y, int speed) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        }

        void draw(Graphics g) {
            g.setColor(color);
            g.fillOval(x, y, DUCK_WIDTH, DUCK_HEIGHT);
            g.setColor(Color.BLACK);
            g.fillOval(x + DUCK_WIDTH - 10, y + 5, 5, 5); // Eye
            g.setColor(Color.ORANGE);
            g.fillRect(x + DUCK_WIDTH - 5, y + DUCK_HEIGHT / 2, 10, 5); // Beak
        }

        boolean contains(int px, int py) {
            return px >= x && px <= x + DUCK_WIDTH && py >= y && py <= y + DUCK_HEIGHT;
        }

        void move() {
            y -= speed;
        }
    }

    private class Tree {
        int x, y;

        Tree(int x, int y) {
            this.x = x;
            this.y = y;
        }

        void draw(Graphics g) {
            g.setColor(new Color(139, 69, 19)); // Brown trunk
            g.fillRect(x - 10, y, 20, 50); // Trunk
            g.setColor(new Color(34, 139, 34)); // Green leaves
            g.fillOval(x - 40, y - 40, 80, 80); // Leaves
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            main game = new main();
            game.setVisible(true);
        });
    }
}
