import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class Player {
    
    // Variables
    private int pX, pY; // Player position
    private int pWidth = 45, pHeight = 54; // Size of player (added default values)
    private int pSpeed = 8; // Speed of the player (added default value)
    private int currentDirection = 0; // To track current movement direction
    private int walkFrame = 0; // For walk animation
    private long lastWalkFrameTime = 0; // For animation timing
    
    private boolean isJumping = false;
    private boolean isOnGround = false;
    private float gravity = 0.5f;
    private float verticalVelocity = 0;
    private float jumpForce = -14f;

    //Sound
    private SoundManager soundManager;
    

    // Player Animation Sequence
    private Image[] walkImages = new Image[5];
    private Image[] swimImages = new Image[2];
    private Image[] jumpImages = new Image[3];

    // Player Images
    private Image deadImage, duckImage, fallImage, hitImage, rollImage, standImage;

    //Platforms
    private List<PlatformGen> platforms;

    public Player(int x, int y, List<PlatformGen> plf) {
        // Player position
        this.pX = x;
        this.pY = y;

        soundManager = SoundManager.getInstance();

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

        // Initialize platforms
        this.platforms = plf;
        

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

    public void update() {
        //Apply Gravity if not on the ground
        if(!isOnGround) {
            verticalVelocity += gravity;
            pY += verticalVelocity;
        }

        //Prevent falling through the buttom
        PlatformGen collision = collideWithPlatform();
        // if(pY > 548 ) {//Ground level
        //     pY = 548;
        //     isOnGround = true;
        //     verticalVelocity = 0;
        // }
        // if(collision!=null){
        //     pY = collision.getY() - getHeight();
        // }
        // Handle collision with platforms
        if (collision != null) {
            // If we are falling onto a platform
            if (isFalling()) {
                pY = collision.getY() - getHeight(); // Land on top of the platform
                isOnGround = true;
                verticalVelocity = 0;
                isJumping = false; // Reset jumping state
            } else if (verticalVelocity < 0) {
                // If we are jumping and hit the bottom of a platform
                verticalVelocity = 0; // Stop upward movement
                // Optionally, you could move the player slightly down to avoid being stuck
                // pY = collision.getY() + collision.getHeight() + 1;
            }
        }

        // Prevent falling through the ground
        if (pY > 548 && collision == null) { // Only apply if not colliding with a platform
            pY = 548;
            isOnGround = true;
            verticalVelocity = 0;
            isJumping = false; // Reset jumping state
        }
    }

    // Draw the player with appropriate image based on movement
    public void draw(Graphics2D g2d) {
        Image currentImage = standImage; // Default standing image
        
        // Check movement direction and select appropriate image
        switch(currentDirection) {
            case 4: // Left
            case 6: // Right
                // Animate walking
                currentImage = walkImages[walkFrame];
                soundManager.playClip("walk", true);
                break;
            case 5: // Duck
                currentImage = duckImage;
                break;
            case 8: // Jump
                currentImage = jumpImages[0]; // Use first jump frame
                break;
            default:
                currentImage = standImage;
                soundManager.stopClip("walk");
        }
        
        g2d.drawImage(currentImage, pX, pY, pWidth, pHeight, null);
    }

    // Control the player movement
    public void movement(int dir) {
        currentDirection = dir;

        System.out.println("PX: "+pX+ " PY: "+pY);
        
        if(dir == 8 && isOnGround) { // Player jump
            verticalVelocity =  jumpForce;
            isOnGround = false;
            soundManager.playClip("jump", false);
            isJumping = true;
            //pY -= pSpeed;
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
            PlatformGen collision = collideWithPlatform();
            if(collision != null){
                pX = collision.getX() + collision.getWidth();
            }
            updateWalkAnimation();
        } else if(dir == 6) { // Move the player Right
            pX += pSpeed;
            
            // Prevents the player from moving out of the screen
            if (pX > (880 - pWidth))
                pX = 880 - pWidth;
            PlatformGen collision = collideWithPlatform();
            if(collision != null){
                pX = collision.getX();
            }
            updateWalkAnimation();
        } else {
            // No movement - reset walk animation
            walkFrame = 0;
        }
    }

    public PlatformGen collideWithPlatform(){
        for(PlatformGen p: platforms){
            Rectangle2D.Double myRect = getBoundingRectangle();
            Rectangle2D.Double platformRect = p.getBoundingRectangle();
            if (myRect.intersects(platformRect)){
                return p;
            }
        }
        return null;
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
        if(isOnGround) {    
            verticalVelocity =  jumpForce;
            isOnGround = false;
            isJumping =  true;
        }
    }


    public void setIsOnGround(boolean onGround) {
        this.isOnGround = onGround;
        if (onGround) {
            this.isJumping = false;
        }
    }

    public void setVerticalVelocity(float velocity) {
        this.verticalVelocity = velocity;
    }

    public boolean isFalling() {
        return verticalVelocity > 0;
    }

    public void setY(int y) {
        this.pY = y;
    }

    public boolean isOnPlayer(int x, int y) {
        Rectangle2D.Double myRectangle = getBoundingRectangle();
        return myRectangle.contains(x, y);
    }

    public Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(pX, pY, pWidth, pHeight);
    }
}