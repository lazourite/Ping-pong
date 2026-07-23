import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PingPongGame extends JPanel implements ActionListener, KeyListener {

    // Constants
    final int W = 800, H = 500;
    final int BALL = 20, PADDLE_H = 80;

    // Ball
    int bx, by, bdx = 3, bdy = 3;

    // Paddles
    int p1y = 150, p2y = 150;

    // Score
    int s1 = 0, s2 = 0;

    enum State { MENU, MODE, PLAY, OVER }
    State state = State.MENU;

    enum Difficulty { EASY, NORMAL, HARD, IMPOSSIBLE_FUNNI }
    Difficulty difficulty = Difficulty.NORMAL;

    Timer timer = new Timer(10, this);
    long lastAI = 0;

    public PingPongGame() {
        setFocusable(true);
        addKeyListener(this);
        timer.start();
        centerBall();
    }

    // ---------- CORE ----------

    void centerBall() {
        bx = W / 2 - BALL / 2;
        by = H / 2 - BALL / 2;
    }

    int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    boolean hit(int px, int py) {
        return bx <= px + 10 && bx + BALL >= px &&
                by <= py + PADDLE_H && by + BALL >= py;
    }

    // ---------- GAME LOOP ----------

    public void actionPerformed(ActionEvent e) {
        if (state != State.PLAY) {
            repaint();
            return;
        }

        bx += bdx;
        by += bdy;

        // Wall bounce
        if (by <= 0 || by >= H - BALL) bdy *= -1;

        // Paddle collision
        if (hit(40, p1y)) bdx = Math.abs(bdx);
        if (hit(740, p2y)) bdx = -Math.abs(bdx);

        // Score
        if (bx < 0) { s2++; resetBall(); }
        if (bx > W) { s1++; resetBall(); }

        aiMove();

        if (s1 >= 15 || s2 >= 15) state = State.OVER;

        repaint();
    }

    void resetBall() {
        centerBall();
        bdx *= -1;
    }

    // ---------- AI ----------

    void aiMove() {
        int speed = switch (difficulty) {
            case EASY -> 6;
            case NORMAL -> 10;
            case HARD -> 16;
            case IMPOSSIBLE_FUNNI -> 22;
        };

        double mistake = switch (difficulty) {
            case EASY -> 0.2;
            case NORMAL -> 0.1;
            case HARD -> 0.05;
            case IMPOSSIBLE_FUNNI -> 0.15;
        };

        int delay = switch (difficulty) {
            case EASY -> 300;
            case NORMAL -> 200;
            case HARD -> 100;
            case IMPOSSIBLE_FUNNI -> 30;
        };

        if (System.currentTimeMillis() - lastAI < delay) return;
        lastAI = System.currentTimeMillis();

        if (difficulty == Difficulty.IMPOSSIBLE_FUNNI && Math.random() < mistake) {
            p2y += (Math.random() > 0.5 ? -speed : speed);
        } else {
            if (by < p2y + 40) p2y -= speed;
            if (by > p2y + 40) p2y += speed;
        }

        if (difficulty == Difficulty.IMPOSSIBLE_FUNNI) {
            p2y += (Math.random() - 0.5) * 6;
        }

        p2y = clamp(p2y, 0, H - PADDLE_H);
    }

    // ---------- RENDER ----------

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, W, H);
        g.setColor(Color.WHITE);

        if (state == State.MENU) {
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("PING PONG", 260, 150);

            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("ENTER - Start", 300, 220);
            g.drawString("M - Modes", 320, 260);
        }

        else if (state == State.MODE) {
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("Modes", 330, 120);

            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("1 Easy", 350, 200);
            g.drawString("2 Normal", 350, 240);
            g.drawString("3 Hard", 350, 280);
            g.drawString("4 Impossible Funni", 270, 320);
        }

        else if (state == State.PLAY) {
            g.fillOval(bx, by, BALL, BALL);
            g.fillRect(30, p1y, 10, PADDLE_H);
            g.fillRect(760, p2y, 10, PADDLE_H);

            g.drawString("Left: " + s1, 100, 50);
            g.drawString("Right: " + s2, 600, 50);
        }

        else if (state == State.OVER) {
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("GAME OVER", 220, 220);

            g.setFont(new Font("Arial", Font.PLAIN, 25));
            g.drawString("Press R to Restart", 260, 300);
        }
    }

    // ---------- INPUT ----------

    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();

        if (state == State.MENU) {
            if (k == KeyEvent.VK_ENTER) {
                centerBall();
                state = State.PLAY;
            }
            if (k == KeyEvent.VK_M) state = State.MODE;
        }

        else if (state == State.MODE) {
            if (k == KeyEvent.VK_1) difficulty = Difficulty.EASY;
            if (k == KeyEvent.VK_2) difficulty = Difficulty.NORMAL;
            if (k == KeyEvent.VK_3) difficulty = Difficulty.HARD;
            if (k == KeyEvent.VK_4) difficulty = Difficulty.IMPOSSIBLE_FUNNI;
            state = State.MENU;
        }

        else if (state == State.PLAY) {
            if (k == KeyEvent.VK_W) p1y -= 20;
            if (k == KeyEvent.VK_S) p1y += 20;
            p1y = clamp(p1y, 0, H - PADDLE_H);
        }

        else if (state == State.OVER && k == KeyEvent.VK_R) {
            s1 = s2 = 0;
            p1y = p2y = 150;
            centerBall();
            state = State.PLAY;
        }
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame f = new JFrame("Ping Pong");
        f.add(new PingPongGame());
        f.setSize(800, 500);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
}
