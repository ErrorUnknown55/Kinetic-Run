import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class Player {
    
    // Position and size
    private int pX, pY;
    private int pWidth = 45, pHeight = 54;
    private int pSpeed = 8;
    
    // Movement and animation
    private int currentDirection = 0;
    private int walkFrame = 0;
    private long lastWalkFrameTime = 0;
    
    // Jumping and physics
    private boolean isJumping = false;
    private boolean isOnGround = false;
    private boolean jumpRequested = false;
    private float gravity = 0.5f;
    private float verticalVelocity = 0;
    private float jumpForce = -14f;
    
    // Input buffering and coyote time
    private long lastJumpPressTime = 0;
    private long lastOnGroundTime = 0;
    private static final long JUMP_BUFFER_TIME = 200; // milliseconds
    private static final long COYOTE_TIME = 100;     // milliseconds
    
    // Sound
    private SoundManager soundManager;
    
    // Player images
    private Image[] walkImages = new Image[5];
    private Image[] swimImages = new Image[2];
    private Image[] jumpImages = new Image[3];
    private Image deadImage, duckImage, fallImage, hitImage, rollImage, standImage;
    
    // Platforms
    private List<PlatformGen> platforms;

    public Player(int x, int y, List<PlatformGen> plf) {
        this.pX = x;
        this.pY = y;
        this.platforms = plf;
        this.soundManager = SoundManager.getInstance();

        // Load images
        loadPlayerImages();
    }

    private void loadPlayerImages() {
        deadImage = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_dead.png");
        duckImage = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_duck.png");
        fallImage = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_fall.png");
        hitImage = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_hit.png");
        standImage = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_stand.png");
        rollImage = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_roll.png");

        // Load animation sequences
        for (int i = 0; i < walkImages.length; i++) {
            walkImages[i] = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_walk" + (i+1) + ".png");
        }

        for (int i = 0; i < jumpImages.length; i++) {
            jumpImages[i] = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_jump" + (i+1) + ".png");
        }

        for (int i = 0; i < swimImages.length; i++) {
            swimImages[i] = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_swim" + (i+1) + ".png");
        }
    }

    public void update() {
        long currentTime = System.currentTimeMillis();
        
        // Handle jump if requested and conditions are met
        if (jumpRequested && (isOnGround || (currentTime - lastOnGroundTime < COYOTE_TIME))) {
            executeJump();
        }
        
        // Apply gravity if not on ground
        if (!isOnGround) {
            verticalVelocity += gravity;
            pY += verticalVelocity;
        }
        
        // Check platform collisions
        PlatformGen collision = collideWithPlatform();
        if (collision != null) {
            handlePlatformCollision(collision);
        }
        
        // Prevent falling through bottom of screen
        if (pY > 548 && collision == null) {
            pY = 548;
            isOnGround = true;
            verticalVelocity = 0;
            isJumping = false;
        }
    }

    private void executeJump() {
        verticalVelocity = jumpForce;
        isOnGround = false;
        isJumping = true;
        jumpRequested = false;
        lastOnGroundTime = 0; // Reset coyote time
        soundManager.playClip("jump", false);
    }

    private void handlePlatformCollision(PlatformGen platform) {
        Rectangle2D.Double playerRect = getBoundingRectangle();
        Rectangle2D.Double platformRect = platform.getBoundingRectangle();
        
        // Landing on top of platform
        if (playerRect.y + playerRect.height >= platformRect.y && 
            playerRect.y + playerRect.height <= platformRect.y + 10 && // Tolerance
            playerRect.x + playerRect.width > platformRect.x && 
            playerRect.x < platformRect.x + platformRect.width) {
            
            pY = (int)platformRect.y - pHeight;
            isOnGround = true;
            verticalVelocity = 0;
            isJumping = false;
            lastOnGroundTime = System.currentTimeMillis();
        }
        // Hitting bottom of platform while jumping
        else if (verticalVelocity < 0 && 
                 playerRect.y < platformRect.y + platformRect.height &&
                 playerRect.y + playerRect.height > platformRect.y + platformRect.height) {
            verticalVelocity = 0;
            pY = (int)(platformRect.y + platformRect.height) + 1;
        }
    }

    public void draw(Graphics2D g2d) {
        Image currentImage = getCurrentAnimationImage();
        g2d.drawImage(currentImage, pX, pY, pWidth, pHeight, null);
    }

    private Image getCurrentAnimationImage() {
        if (isJumping) {
            return jumpImages[0]; // First jump frame
        }
        if (!isOnGround && verticalVelocity > 0) {
            return fallImage; // Falling
        }
        switch(currentDirection) {
            case 4: case 6: // Left or Right
                updateWalkAnimation();
                return walkImages[walkFrame];
            case 5: // Duck
                return duckImage;
            case 8: // Jump (handled above)
                return jumpImages[0];
            default:
                return standImage;
        }
    }

    public void movement(int dir) {
        currentDirection = dir;
        
        switch(dir) {
            case 4: // Left
                moveLeft();
                break;
            case 6: // Right
                moveRight();
                break;
            case 8: // Jump
                jump();
                break;
            case 5: // Duck
                // Duck handling
                break;
            default:
                walkFrame = 0;
                soundManager.stopClip("walk");
        }
    }

    private void moveLeft() {
        pX -= pSpeed;
        if (pX < 10) pX = 10;
        
        PlatformGen collision = collideWithPlatform();
        if (collision != null) {
            pX = collision.getX() + collision.getWidth();
        }
        
        soundManager.playClip("walk", true);
        updateWalkAnimation();
    }

    private void moveRight() {
        pX += pSpeed;
        if (pX > (880 - pWidth)) pX = 880 - pWidth;
        
        PlatformGen collision = collideWithPlatform();
        if (collision != null) {
            pX = collision.getX() - pWidth;
        }
        
        soundManager.playClip("walk", true);
        updateWalkAnimation();
    }

    public void jump() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastJumpPressTime < JUMP_BUFFER_TIME) {
            jumpRequested = true;
        }
        lastJumpPressTime = currentTime;
    }

    private void updateWalkAnimation() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastWalkFrameTime > 100) { // Change frame every 100ms
            walkFrame = (walkFrame + 1) % walkImages.length;
            lastWalkFrameTime = currentTime;
        }
    }

    public PlatformGen collideWithPlatform() {
        Rectangle2D.Double playerRect = getBoundingRectangle();
        for (PlatformGen platform : platforms) {
            if (playerRect.intersects(platform.getBoundingRectangle())) {
                return platform;
            }
        }
        return null;
    }

    // Getters and setters
    public int getX() { return pX; }
    public int getY() { return pY; }
    public int getWidth() { return pWidth; }
    public int getHeight() { return pHeight; }
    public void setY(int y) { this.pY = y; }
    public void setIsOnGround(boolean onGround) { 
        this.isOnGround = onGround; 
        if (onGround) {
            this.isJumping = false;
            this.lastOnGroundTime = System.currentTimeMillis();
        }
    }
    public void setVerticalVelocity(float velocity) { this.verticalVelocity = velocity; }
    public boolean isFalling() { return verticalVelocity > 0; }
    
    public Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(pX, pY, pWidth, pHeight);
    }
    
    public boolean isOnPlayer(int x, int y) {
        return getBoundingRectangle().contains(x, y);
    }
}