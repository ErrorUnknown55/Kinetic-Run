import javax.swing.JPanel;
import java.util.Random;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.geom.Rectangle2D;
import java.awt.Color;
public class PlatformGen {
    private JPanel panel;

    private int[] xValues;
    private int[] yValues;
// might change to Buffered image
    private Image [] platformImages;
    private Dimension d;
    private int x,y,dx = 2;
    private int platform_num;
    private int imageWidth;


    Random ran;
    public PlatformGen(JPanel panel, int yPos,Player playe) {
        this.panel = panel;
        d = panel.getSize();
        xValues = new int[3];
        yValues = new int[3];
        platformImages = new Image[14];
        int num;
        for (int i = 0; i< 14; i++ ){
            num=i+1;
            platformImages[i] = ImageManager.loadImage("sprites/spritesheet."+num+".png");
        }
        x=700;
        ran = new Random();
        platform_num = ran.nextInt(13);
        y = yPos;




    }

    public void generatePlatforms() {
        ran = new Random();
        for (int i = 0; i < xValues.length; i++) {
            yValues[i] = ran.nextInt(580) + 100;
        }
    }

    public void drawPlatforms(Graphics2D g){
        
        // for (int i = 0; i < xValues.length; i++) {
        g.drawImage(platformImages[platform_num], x, y, null);
        g.setColor(Color.RED); // Choose a color for the rectangle (e.g., RED)
        Rectangle2D.Double bounds = getBoundingRectangle();
        g.draw(bounds);

        // g.drawImage(platformImages[6], x, 300, null);

        // g.drawImage(platformImages[1], x, 200, null);
        
    }

    public void move(){
        if (!panel.isVisible()) return;

        x = x - dx;
        imageWidth = platformImages[platform_num].getWidth(panel);
        int imageSize = x + imageWidth;
        if (imageSize < 0){
            platform_num = ran.nextInt(13);
            x=700;
            // regenplatforms();
            // newPanelNeeded = true;
        }
    }
    public int getX() {
        return x;
    }

    public int getY(){
        return y;
    }

    public int getWidth() {
        return platformImages[platform_num].getWidth(panel);
    }

    public int getHeight() {
        return platformImages[platform_num].getHeight(panel);
    }

    public Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double (x, y, getWidth(), getHeight());
     }

    // public void regenPlatforms


}
