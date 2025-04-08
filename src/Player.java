import java.awt.Graphics;
import java.awt.Image;
import java.awt.geom.Rectangle2D;

public class Player {
    
    //Veriables
    private int pX, pY; //Player position
    private int pWidth, pHeight; // Size of player
    private int pSpeed; // Speed of the player

    //Player Animation Sequence
    private Image[] walkImages =  new Image[5];
    private Image[] swimImages = new Image[2];
    private Image[] jumpImages =  new Image[3];

    //Player Images
    private Image deadImage, duckImage, fallImage, hitImage, rollImage, standImage;

    public Player(int x,  int y) {
        //Player position
        this.pX = x;
        this.pY = y;

        //Load images
        deadImage = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_dead.png");
        duckImage = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_duck.png");
        fallImage = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_fall.png");
        hitImage = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_hit.png");
        standImage = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_stand.png");

        //Laod Player Walk Animation Sequence
        for(int i = 0; i < walkImages.length; i++)
            walkImages[i] = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_walk"+ (i+1)+".png");

        //Load Player Jump Animation Sequence
        for(int i = 0;  i < jumpImages.length; i++)
            jumpImages[i] = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_jump"+ (i+1)+".png");

        //Load player Swim Animation Sequence
        for(int i = 0; i < swimImages.length; i++)
            swimImages[i] = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_swim1"+ (i+1)+".png");
    }


    public int getX() {
        return pX;
    }

    public int getY() {
        return pY;
    }

    public int getWidth() {
        return pWidth;
    }

    public int getHeight() {
        return pHeight;
    }

    // Draw the player
    public void draw(Graphics g2d) {
        g2d.drawImage(standImage, pX, pY, pWidth, pHeight, null);
    }

    //Control the player movement
    public void movement(int dir) {
        
        if(dir == 8) {//Moves the player up
            pY -= pSpeed;
            // Prevents the player from moving out of the screen
            if (pY < 0)
                pY = 0;
        } else if(dir == 5 ) {//Move the player down
            pY += pSpeed;
            // Prevents the player from moving out of the screen
            if(pY > (400 - pHeight))
                pY = 400 - pHeight;

        } else if(dir == 4){//Move the player Left
            pX -= pSpeed;
            //Prevents the player from moving out of the screen
            if (pX < 0)
                 pX = 0;
        } else if(dir == 6){// Move the player Right
            pX += pSpeed;
            // Prevents the player from moving out of the screen
            if (pX > (400 - pWidth))
                pX = 400 - pWidth;
        }

    }

    //
    public void jump() {

    }

    public boolean isOnPlayer(int x, int y) {
        Rectangle2D.Double myRectangle = getBoundingRectangle();
        return myRectangle.contains(x, y);
    }

    public Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(pX, pY, pWidth, pHeight);
    }


}
