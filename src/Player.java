import java.awt.Graphics;
import java.awt.Image;
import java.awt.geom.Rectangle2D;

public class Player {
    
    // Variables
    private int pX, pY; // Player position
    private int pWidth = 45, pHeight = 54; // Size of player (added default values)
    private int pSpeed = 5; // Speed of the player (added default value)
    private int currentDirection = 0; // To track current movement direction
    private int walkFrame = 0; // For walk animation
    private long lastWalkFrameTime = 0; // For animation timing

    // Player Animation Sequence
    private Image[] walkImages = new Image[5];
    private Image[] swimImages = new Image[2];
    private Image[] jumpImages = new Image[3];

    // Player Images
    private Image deadImage, duckImage, fallImage, hitImage, rollImage, standImage;

    public Player(int x, int y) {
        // Player position
        this.pX = x;
        this.pY = y;
        

        // Load images
        deadImage = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_dead.png");
        duckImage = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_duck.png");
        fallImage = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_fall.png");
        hitImage = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_hit.png");
        standImage = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_stand.png");
        rollImage = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_roll.png");

        // Load Player Walk Animation Sequence
        for(int i = 0; i < walkImages.length; i++)
            walkImages[i] = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_walk"+ (i+1)+".png");

        // Load Player Jump Animation Sequence
        for(int i = 0; i < jumpImages.length; i++)
            jumpImages[i] = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_jump"+ (i+1)+".png");

        // Load player Swim Animation Sequence
        for(int i = 0; i < swimImages.length; i++)
            swimImages[i] = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_swim"+ (i+1)+".png");
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

    // Draw the player with appropriate image based on movement
    public void draw(Graphics g2d) {
        Image currentImage = standImage; // Default standing image
        
        // Check movement direction and select appropriate image
        switch(currentDirection) {
            case 4: // Left
            case 6: // Right
                // Animate walking
                currentImage = walkImages[walkFrame];
                break;
            case 5: // Duck
                currentImage = duckImage;
                break;
            case 8: // Jump
                currentImage = jumpImages[0]; // Use first jump frame
                break;
            default:
                currentImage = standImage;
        }
        
        g2d.drawImage(currentImage, pX, pY, pWidth, pHeight, null);
    }

    // Control the player movement
    public void movement(int dir) {
        currentDirection = dir;

        System.out.println("PX: "+pX+ " PY: "+pY);
        
        if(dir == 8) { // Player jump
            pY -= pSpeed;
            // Prevents the player from moving out of the screen
            if (pY < 0)
                pY = 0;
        } else if(dir == 5) { //Player Duck
            //pY += pSpeed;
            // Prevents the player from moving out of the screen
            //if(pY > (400 - pHeight))
            //    pY = 400 - pHeight;

        } else if(dir == 4) { // Move the player Left
            pX -= pSpeed;
            // Prevents the player from moving out of the screen
            if (pX < 10)
                 pX = 10;
            updateWalkAnimation();
        } else if(dir == 6) { // Move the player Right
            pX += pSpeed;
            // Prevents the player from moving out of the screen
            if (pX > (880 - pWidth))
                pX = 880 - pWidth;
            updateWalkAnimation();
        } else {
            // No movement - reset walk animation
            walkFrame = 0;
        }
    }

    // Update walk animation frames
    private void updateWalkAnimation() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastWalkFrameTime > 100) { // Change frame every 100ms
            walkFrame = (walkFrame + 1) % walkImages.length;
            lastWalkFrameTime = currentTime;
        }
    }

    public void jump() {
        // You can implement jump animation here
    }

    public boolean isOnPlayer(int x, int y) {
        Rectangle2D.Double myRectangle = getBoundingRectangle();
        return myRectangle.contains(x, y);
    }

    public Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(pX, pY, pWidth, pHeight);
    }
}