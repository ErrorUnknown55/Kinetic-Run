import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
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

    public BluePillPowerUp(String effect, float duration,JPanel p, int x, int y) {
        super(effect, duration);
        //initialize extra fields here;
        this.x = x;
        this.y=y;
        width = 22;
        height = 21;
        dx = 6;
        jp=p;

        blueP = ImageManager.loadImage("images/powerups/pill_blue.png");
    }

    public void applyEffect() {
        // implement function;

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