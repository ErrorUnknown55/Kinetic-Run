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
public class Heart {
    private Image heart;
    private int x,y,width,height,dx;
    private JPanel jp;
    private GamePanel gp;
    private Player player;

    public Heart(JPanel p, int x, int y){
        this.x = x;
        this.y=y;
        width = 22;
        height = 21;
        dx = 6;
        jp=p;
        this.gp = (GamePanel) p;

        heart = ImageManager.loadImage("images/player/heart.png");
    }
    public void setPlayer(Player p){
        this.player = p;
    }
    public void draw(Graphics g2d){
        g2d.drawImage(heart, x, y, width, height, null);
        
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