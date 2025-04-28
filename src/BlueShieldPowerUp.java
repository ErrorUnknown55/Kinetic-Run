import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import javax.swing.JPanel;
import java.util.Random;
import java.awt.Image;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
public class BlueShieldPowerUp extends PowerUp{
    
    private Image blueS;
    private int x,y,width,height,dx;
    private JPanel jp;
    private GamePanel gp;
    private Player player;

    private long shieldDuration = 8000; // 8 seconds of protection
    private long shieldStartTime;
    

    public BlueShieldPowerUp(String effect, float duration,JPanel p, int x, int y) {
        super(effect, duration);
        this.x = x;
        this.y=y;
        width = 22;
        height = 21;
        dx = 6;
        jp=p;
        this.gp = (GamePanel) p;

        blueS = ImageManager.loadImage("images/powerups/powerupBlue_shield.png");
    }

    @Override
    public void applyEffect() {
        //implement function;
        shieldStartTime = System.currentTimeMillis();
        player.activateShield(shieldDuration);
        
        // Visual feedback
        player.setShieldActive(true);
    
    }

    public void setPlayer(Player p){
        this.player = p;
    }

    // @Override
    // public void removeEffect(Player player, GamePanel gamePanel) {
    //     // Handled automatically by Player class after duration
    // }

    public void update(){
        if (!jp.isVisible()) return;

        x = x - dx;
    }
    public void draw(Graphics g2d){
        g2d.drawImage(blueS, x, y, width, height, null);
        if (player.isInWater()) {
            // Darker outline for underwater (e.g., navy blue or dark gray)
            g2d.setColor(new Color(0, 0, 100, 150)); // Semi-transparent dark blue
        } else {
            // Normal outline for above water
            g2d.setColor(new Color(255, 255, 255, 150)); // Semi-transparent white
        }
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