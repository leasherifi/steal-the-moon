import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.net.URL;

public class MinionMayhemGame extends JFrame {

    private JPanel topPanel;
    private JLayeredPane gameLayeredPane;
    private JLabel coinsLabel;
    private JLabel gruLabel;
    private JButton addMinionBtn, buyDrNefarioBtn, stealMoonBtn;

    private List<Minion> minions = new ArrayList<>();
    private DrNefario drNefario = null;
    private int coins = 20;
    private int nextMinionPrice = 10;
    private final String[] NEEDS = {"hungry", "sleepy", "play"};

    private ImageIcon bgIcon, minionIcon, gruIcon, happyGruIcon, meltiGruIcon, successBgIcon;

    private final int HELPER_SPEED = 12;
    private final int HELPER_REWARD = 15;
    private final int MANUAL_REWARD = 5;
    private final int PET_SPACING_X = 150;
    private final int PET_SPACING_Y = 150;
    private final int PETS_PER_ROW = 4;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MinionMayhemGame().setVisible(true));
    }

    public MinionMayhemGame() {
        setTitle("Minion Mayhem: Steal the Moon");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        bgIcon = loadIcon("lab.jpg", 800, 600);
        minionIcon = loadIcon("minion.png", 80, 80);
        gruIcon = loadIcon("gru.png", 100, 100);
        happyGruIcon = loadIcon("happygru.jpg", 100, 100);
        meltiGruIcon = loadIcon("meallti.jpg", 800, 600);
        successBgIcon = loadIcon("succes.jpg", 800, 600);

        topPanel = new JPanel();
        addMinionBtn = new JButton("Add Minion (" + nextMinionPrice + " coins)");
        buyDrNefarioBtn = new JButton("Buy Dr. Nefario (50 coins)");
        stealMoonBtn = new JButton("STEAL THE MOON (250 coins)");
        coinsLabel = new JLabel("Coins: " + coins);

        topPanel.add(addMinionBtn);
        topPanel.add(buyDrNefarioBtn);
        topPanel.add(stealMoonBtn);
        topPanel.add(coinsLabel);
        add(topPanel, BorderLayout.NORTH);

        gameLayeredPane = new JLayeredPane();
        gameLayeredPane.setPreferredSize(new Dimension(800, 520));
        add(gameLayeredPane, BorderLayout.CENTER);
        
        JLabel backgroundLabel = new JLabel(bgIcon);
        backgroundLabel.setBounds(0, 0, 800, 520);
        gameLayeredPane.add(backgroundLabel, JLayeredPane.DEFAULT_LAYER);

        gruLabel = new JLabel(gruIcon);
        gruLabel.setBounds(650, 400, 100, 100);
        gameLayeredPane.add(gruLabel, JLayeredPane.PALETTE_LAYER);

        addInitialMinion();

        addMinionBtn.addActionListener(e -> addMinion());
        buyDrNefarioBtn.addActionListener(e -> buyDrNefario());
        stealMoonBtn.addActionListener(e -> stealTheMoon());

        new java.util.Timer().scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    updateMinionsState();
                    if (drNefario != null) {
                        drNefario.step();
                    }
                });
            }
        }, 0, 50);

        new java.util.Timer().scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> triggerRandomNeeds());
            }
        }, 3000, 3000);
    }
    
    private ImageIcon loadIcon(String path, int width, int height) {
        URL imgURL = MinionMayhemGame.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(new ImageIcon(imgURL).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
        } else {
            System.err.println("Could not load image resource: " + path + ". Check file name and location.");
            ImageIcon fallback = new ImageIcon();
            Image image = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
            Graphics g = image.getGraphics();
            g.setColor(Color.RED);
            g.fillRect(0, 0, width, height);
            g.dispose();
            return new ImageIcon(image);
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private void gruReaction(int durationMillis) {
        gruLabel.setIcon(happyGruIcon);
        javax.swing.Timer timer = new javax.swing.Timer(durationMillis, e -> gruLabel.setIcon(gruIcon));
        timer.setRepeats(false);
        timer.start();
    }

    private class Minion {
        JLabel iconLabel;
        JLabel needLabel;
        JPanel buttonPanel;

        int needTimer = new Random().nextInt(4) + 1;
        
        boolean busy = false;
        String currentNeed = "none";
        int x, y;
        final int MINION_WIDTH = 80;

        Minion(int x, int y) {
            this.x = x;
            this.y = y;

            iconLabel = new JLabel(minionIcon);
            iconLabel.setBounds(x, y, MINION_WIDTH, MINION_WIDTH);

            needLabel = new JLabel("", SwingConstants.CENTER);
            needLabel.setBounds(x - 20, y - 40, MINION_WIDTH + 40, 30);
            needLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            needLabel.setBackground(Color.YELLOW);
            needLabel.setOpaque(true);
            needLabel.setFont(new Font("Arial", Font.BOLD, 12));
            needLabel.setVisible(false);

            buttonPanel = new JPanel(new GridLayout(1, 3, 2, 0));
            buttonPanel.setBounds(x - 10, y + MINION_WIDTH + 5, MINION_WIDTH + 20, 25);
            
            for (String need : NEEDS) {
                JButton btn = new JButton(capitalize(need));
                btn.setFont(new Font("Arial", Font.PLAIN, 10));
                btn.setMargin(new Insets(0, 2, 0, 2));
                
                btn.addActionListener(e -> {
                    if (currentNeed.equals(need)) {
                        serviceComplete();
                        coins += MANUAL_REWARD;
                        coinsLabel.setText("Coins: " + coins);
                    } else {
                        JOptionPane.showMessageDialog(MinionMayhemGame.this, "The minion needs " + capitalize(currentNeed) + "!");
                    }
                });
                buttonPanel.add(btn);
            }

            gameLayeredPane.add(iconLabel, JLayeredPane.PALETTE_LAYER);
            gameLayeredPane.add(needLabel, JLayeredPane.DRAG_LAYER);
            gameLayeredPane.add(buttonPanel, JLayeredPane.DRAG_LAYER);
            gameLayeredPane.repaint();
        }

        void setNeed(String need) {
            currentNeed = need;
            if (need.equals("none")) {
                needLabel.setText("");
                needLabel.setVisible(false);
            } else {
                needLabel.setText(capitalize(need));
                needLabel.setVisible(true);
            }
        }

        void decreaseTimer() {
            if (currentNeed.equals("none") && !busy) {
                needTimer--;
                if (needTimer <= 0) {
                    setNeed(NEEDS[new Random().nextInt(NEEDS.length)]);
                    needTimer = new Random().nextInt(4) + 1;
                }
            }
        }
        
        void serviceComplete() {
            if (drNefario != null && drNefario.targetMinion == this) {
                drNefario.targetMinion = null;
            }
            setNeed("none");
            busy = false;
            needTimer = new Random().nextInt(4) + 1;
        }

        void jump() {
            new javax.swing.Timer(100, new ActionListener() {
                int count = 0;
                int originalY = y;
                boolean goingUp = true;
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (count >= 10) {
                        ((javax.swing.Timer)e.getSource()).stop();
                        iconLabel.setLocation(x, originalY);
                        return;
                    }

                    if (goingUp) {
                        iconLabel.setLocation(x, iconLabel.getY() - 5);
                        if (iconLabel.getY() <= originalY - 20) goingUp = false;
                    } else {
                        iconLabel.setLocation(x, iconLabel.getY() + 5);
                        if (iconLabel.getY() >= originalY) {
                            goingUp = true;
                            count++;
                        }
                    }
                }
            }).start();
        }
    }

    private class DrNefario {
        JLabel iconLabel;
        int x, y;
        Minion targetMinion = null;
        ImageIcon drIcon = loadIcon("dr.jpg", 60, 60);

        DrNefario(int startX, int startY) {
            x = startX;
            y = startY;
            iconLabel = new JLabel(drIcon);
            iconLabel.setBounds(x, y, drIcon.getIconWidth(), drIcon.getIconHeight());
            
            gameLayeredPane.add(iconLabel, JLayeredPane.PALETTE_LAYER);
            gameLayeredPane.repaint();
        }

        void setTarget(Minion minion) {
            targetMinion = minion;
            minion.busy = true;
        }

        void step() {
            if (targetMinion == null || targetMinion.currentNeed.equals("none")) {
                targetMinion = null;
                x = (x > 0) ? x - HELPER_SPEED : 0;
                y = (y < 450) ? y + HELPER_SPEED : 450;
                iconLabel.setLocation(x, y);
                return;
            }

            int tx = targetMinion.x;
            int ty = targetMinion.y;

            if (Math.abs(x - tx) > HELPER_SPEED || Math.abs(y - ty) > HELPER_SPEED) {
                if (x < tx) x += HELPER_SPEED;
                else if (x > tx) x -= HELPER_SPEED;
                if (y < ty) y += HELPER_SPEED;
                else if (y > ty) y -= HELPER_SPEED;
                
                iconLabel.setLocation(x, y);
            } else {
                targetMinion.serviceComplete();
                coins += HELPER_REWARD;
                coinsLabel.setText("Coins: " + coins);
                targetMinion = null;
            }
        }
        
        void remove() {
             gameLayeredPane.remove(iconLabel);
             gameLayeredPane.repaint();
        }
    }

    private void addInitialMinion() {
        createMinionAtLayoutIndex(0);
    }
    
    private void createMinionAtLayoutIndex(int index) {
        int row = index / PETS_PER_ROW;
        int col = index % PETS_PER_ROW;
        
        int x = 50 + col * PET_SPACING_X;
        int y = 50 + row * PET_SPACING_Y;

        Minion m = new Minion(x, y);
        minions.add(m);
    }
    
    private void addMinion() {
        if (coins < nextMinionPrice) return;
        coins -= nextMinionPrice;
        nextMinionPrice += 5;
        coinsLabel.setText("Coins: " + coins);
        addMinionBtn.setText("Add Minion (" + nextMinionPrice + " coins)");

        gruReaction(1000);

        createMinionAtLayoutIndex(minions.size());
    }

    private void buyDrNefario() {
        if (drNefario != null) {
            JOptionPane.showMessageDialog(this, "Dr. Nefario is already on duty!");
            return;
        }
        if (coins < 50) {
            JOptionPane.showMessageDialog(this, "Not enough coins (Need 50)!");
            return;
        }
        
        coins -= 50;
        coinsLabel.setText("Coins: " + coins);
        buyDrNefarioBtn.setEnabled(false);

        gruReaction(1000);

        drNefario = new DrNefario(0, 450);
    }

    private void stealTheMoon() {
        if (coins < 250) {
            JOptionPane.showMessageDialog(this, "You need 250 coins to fund the mission!");
            return;
        }

        coins -= 250;
        coinsLabel.setText("Coins: " + coins);

        gruLabel.setIcon(meltiGruIcon);
        gruLabel.setBounds(0, 0, 800, 520);
        gruLabel.setVisible(true);

        topPanel.setEnabled(false);
        for (Component c : topPanel.getComponents()) c.setEnabled(false);

        for (Minion m : minions) {
            m.iconLabel.setVisible(false);
            m.needLabel.setVisible(false);
            m.buttonPanel.setVisible(false);
        }
        if (drNefario != null) drNefario.iconLabel.setVisible(false);

        javax.swing.Timer transitionTimer = new javax.swing.Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((javax.swing.Timer)e.getSource()).stop();
                missionSuccessScreen();
            }
        });
        transitionTimer.setRepeats(false);
        transitionTimer.start();
    }
    
    private void missionSuccessScreen() {
        gameLayeredPane.removeAll();
        
        JLabel successBackground = new JLabel(successBgIcon);
        successBackground.setBounds(0, 0, 800, 520);
        gameLayeredPane.add(successBackground, JLayeredPane.DEFAULT_LAYER);

        JLabel successText = new JLabel("Mission Successful! You Stole the Moon!", SwingConstants.CENTER);
        successText.setFont(new Font("Comic Sans MS", Font.BOLD, 30));
        successText.setForeground(Color.WHITE);
        successText.setBounds(0, 150, 800, 50);
        gameLayeredPane.add(successText, JLayeredPane.DRAG_LAYER);

        for (Minion m : minions) {
            m.iconLabel.setVisible(true);
            m.iconLabel.setBounds(m.x, m.y, m.MINION_WIDTH, m.MINION_WIDTH);
            gameLayeredPane.add(m.iconLabel, JLayeredPane.PALETTE_LAYER);
            m.jump();
        }
        
        if (drNefario != null) {
            drNefario.iconLabel.setVisible(true);
            drNefario.iconLabel.setBounds(drNefario.x, drNefario.y, drNefario.drIcon.getIconWidth(), drNefario.drIcon.getIconHeight());
            gameLayeredPane.add(drNefario.iconLabel, JLayeredPane.PALETTE_LAYER);
        }

        gameLayeredPane.revalidate();
        gameLayeredPane.repaint();

        javax.swing.Timer finalTimer = new javax.swing.Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 ((javax.swing.Timer)e.getSource()).stop();
                 JOptionPane.showMessageDialog(MinionMayhemGame.this, "Mission Complete! Game Over.");
                 System.exit(0);
            }
        });
        finalTimer.setRepeats(false);
        finalTimer.start();
    }
    
    private void triggerRandomNeeds() {
        for (Minion m : minions) {
            m.decreaseTimer();
        }
    }

    private void updateMinionsState() {
        if (drNefario != null && drNefario.targetMinion == null) {
            for (Minion m : minions) {
                if (!m.currentNeed.equals("none") && !m.busy) {
                    drNefario.setTarget(m);
                    break;
                }
            }
        }
    }
}
