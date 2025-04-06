import java.awt.Graphics2D;
import java.awt.Image;

public class Background {
    private GamePanel panel;

	private Image bgImage;
    private int bgImageWidth;//width of the background (>= panel Width)
    
    private int bg1X;//X-coordinate of first background
    private int bg2X;//X-coordinate of second background
    private int bgDX;//size of the background move (in pixels)

    private boolean autoScroll;//flag for automatic scrolling
    private int scrollSpeed;//speed of scrolling

    public Background(GamePanel panel, String imageFile, int bgDX) {
        this.panel = panel;
        this.bgImage = ImageManager.loadImage(imageFile);
        bgImageWidth = bgImage.getWidth(null);// get width of the background

        this.bgDX = bgDX;
        this.autoScroll = false;
        this.scrollSpeed = 6;// default scroll speed

        bg1X = 0;
        bg2X = bgImageWidth;
    }

    public void setAutoScroll(boolean autoScroll) {
        this.autoScroll = autoScroll;
    }

    public void setScrollSpeed(int speed) {
        this.scrollSpeed = speed;
    }

    public void update() {
        if (autoScroll) {
            moveLeft();
        }
    }

    public void move(int direction) {
        if (direction == 1)
            moveRight();
        else if (direction == 2)
            moveLeft();
    }

    public void moveLeft() {
        bg1X -= scrollSpeed;
        bg2X -= scrollSpeed;

        //When the first image is completely off-screen, reset its position
        if (bg1X + bgImageWidth <= 0) {
            bg1X = bg2X + bgImageWidth;
        }
        
        //When the second image is completely off-screen, reset its position
        if (bg2X + bgImageWidth <= 0) {
            bg2X = bg1X + bgImageWidth;
        }
    }

    public void moveRight() {
        bg1X += bgDX;
        bg2X += bgDX;

        if (bg1X > 0) {
            bg1X = bgImageWidth * -1;
            bg2X = 0;
        }
    }

    public void draw(Graphics2D g2) {
        g2.drawImage(bgImage, bg1X, 0, null);
        g2.drawImage(bgImage, bg2X, 0, null);
    }
}