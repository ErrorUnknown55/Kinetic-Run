import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.Color;
import java.awt.geom.Line2D;
import javax.swing.JPanel;
import java.util.Random;
import java.awt.Image;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
public class BluePillPowerUp extends PowerUp {
    //insert extra fields here;
    private Image blueP;
    private int x,y,width,height,dx;
    private JPanel jp;
    private GamePanel gp;
    private Player player;

    private long freezeDuration = 5000; // 5 seconds freeze
    private long freezeStartTime;

    public BluePillPowerUp(String effect, float duration,JPanel p, int x, int y) {
        super(effect, duration);
        //initialize extra fields here;
        this.x = x;
        this.y=y;
        width = 22;
        height = 21;
        dx = 6;
        jp=p;
        this.gp = (GamePanel) p;
        

        blueP = ImageManager.loadImage("images/powerups/pill_blue.png");
    }

    public void setPlayer(Player p){
        this.player = p;
    }

    public void applyEffect() {
        // implement function;
        freezeStartTime = System.currentTimeMillis();
        
        // Freeze all enemies
        for (Enemy enemy : gp.getEnemies()) {
            enemy.setFrozen(true);
        }
        
        // Schedule unfreeze after duration
        new java.util.Timer().schedule(
            new java.util.TimerTask() {
                @Override
                public void run() {
                    removeEffect(player, gp);
                }
            },
            freezeDuration
        );

    }

    public void removeEffect(Player player, GamePanel gamePanel) {
        // Unfreeze all enemies
        for (Enemy enemy : gamePanel.getEnemies()) {
            enemy.setFrozen(false);
        }
    }

    public void move(){
        if (!jp.isVisible()) return;

        x = x - dx;
    }
    
    public void update(){
        if (!jp.isVisible()) return;

        x = x - dx;
    }
    public void draw(Graphics g2d){
        g2d.drawImage(blueP, x, y, width, height, null);
        if (player.isInWater()) {
            // Darker outline for underwater (e.g., navy blue or dark gray)
            g2d.setColor(new Color(0, 0, 100, 150)); // Semi-transparent dark blue
        } else {
            // Normal outline for above water
            g2d.setColor(new Color(255, 255, 255, 150)); // Semi-transparent white
        }
        // g2d.setColor(new Color(255, 255, 255, 100));
        g2d.fillOval(x-5, y-5, width+10, height+10);
    }
    public Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(x, y, width, height);
    }
    public int getX(){
        return this.x;
    }
    public int getY(){
        return this.y;
    }
    public int getWidth(){
        return this.width;
    }
    public int getHeight(){
        return this.height;
    }
}