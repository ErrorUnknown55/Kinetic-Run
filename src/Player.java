// import java.awt.Graphics2D;
// import java.awt.Image;
// import java.awt.geom.Rectangle2D;
// import java.util.List;

// public class Player {
    
//     // Variables
//     private int pX, pY; // Player position
//     private int pWidth = 45, pHeight = 54; // Size of player (added default values)
//     private int pSpeed = 8; // Speed of the player (added default value)
//     private int currentDirection = 0; // To track current movement direction
//     private int walkFrame = 0; // For walk animation
//     private long lastWalkFrameTime = 0; // For animation timing
    
//     private boolean isJumping = false;
//     private boolean isOnGround = false;
//     private float gravity = 0.5f;
//     private float verticalVelocity = 0;
//     private float jumpForce = -1f;

//     //Sound
//     private SoundManager soundManager;
    

//     // Player Animation Sequence
//     private Image[] walkImages = new Image[5];
//     private Image[] swimImages = new Image[2];
//     private Image[] jumpImages = new Image[3];

//     // Player Images
//     private Image deadImage, duckImage, fallImage, hitImage, rollImage, standImage;

//     //Platforms
//     private List<PlatformGen> platforms;

//     public Player(int x, int y, List<PlatformGen> plf) {
//         // Player position
//         this.pX = x;
//         this.pY = y;

//         soundManager = SoundManager.getInstance();

//         // Load images
//         deadImage = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_dead.png");
//         duckImage = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_duck.png");
//         fallImage = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_fall.png");
//         hitImage = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_hit.png");
//         standImage = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_stand.png");
//         rollImage = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_roll.png");

//         // Load Player Walk Animation Sequence
//         for(int i = 0; i < walkImages.length; i++)
//             walkImages[i] = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_walk"+ (i+1)+".png");

//         // Load Player Jump Animation Sequence
//         for(int i = 0; i < jumpImages.length; i++)
//             jumpImages[i] = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_jump"+ (i+1)+".png");

//         // Load player Swim Animation Sequence
//         for(int i = 0; i < swimImages.length; i++)
//             swimImages[i] = ImageManager.loadImage("images/player/PlayerBlue/playerBlue_swim"+ (i+1)+".png");

//         // Initialize platforms
//         this.platforms = plf;
        

//     }

//     public int getX() {
//         return pX;
//     }

//     public int getY() {
//         return pY;
//     }

//     public int getWidth() {
//         return pWidth;
//     }

//     public int getHeight() {
//         return pHeight;
//     }

//     public void update() {
//         //Apply Gravity if not on the ground
//         if(!isOnGround) {
//             verticalVelocity += gravity;
//             pY += verticalVelocity;
//         }

//         //Prevent falling through the buttom
//         PlatformGen collision = collideWithPlatform();
//         // if(pY > 548 ) {//Ground level
//         //     pY = 548;
//         //     isOnGround = true;
//         //     verticalVelocity = 0;
//         // }
//         // if(collision!=null){
//         //     pY = collision.getY() - getHeight();
//         // }
//         // Handle collision with platforms
//         if (collision != null) {
//             // If we are falling onto a platform
//             if (isFalling()) {
//                 pY = collision.getY() - getHeight(); // Land on top of the platform
//                 isOnGround = true;
//                 verticalVelocity = 0;
//                 isJumping = false; // Reset jumping state
//             } else if (verticalVelocity < 0) {
//                 // If we are jumping and hit the bottom of a platform
//                 verticalVelocity = 0; // Stop upward movement
//                 // Optionally, you could move the player slightly down to avoid being stuck
//                 // pY = collision.getY() + collision.getHeight() + 1;
//             }
//         }

//         // Prevent falling through the ground
//         if (pY > 548 && collision == null) { // Only apply if not colliding with a platform
//             pY = 548;
//             isOnGround = true;
//             verticalVelocity = 0;
//             isJumping = false; // Reset jumping state
//         }
//     }

//     // Draw the player with appropriate image based on movement
//     public void draw(Graphics2D g2d) {
//         Image currentImage = standImage; // Default standing image
        
//         // Check movement direction and select appropriate image
//         switch(currentDirection) {
//             case 4: // Left
//             case 6: // Right
//                 // Animate walking
//                 currentImage = walkImages[walkFrame];
//                 soundManager.playClip("walk", true);
//                 break;
//             case 5: // Duck
//                 currentImage = duckImage;
//                 break;
//             case 8: // Jump
//                 currentImage = jumpImages[0]; // Use first jump frame
//                 break;
//             default:
//                 currentImage = standImage;
//                 soundManager.stopClip("walk");
//         }
        
//         g2d.drawImage(currentImage, pX, pY, pWidth, pHeight, null);
//     }

//     // Control the player movement
//     public void movement(int dir) {
//         currentDirection = dir;

//         System.out.println("PX: "+pX+ " PY: "+pY);
        
//         if(dir == 8 && isOnGround) { // Player jump
//             verticalVelocity =  jumpForce;
//             isOnGround = false;
//             soundManager.playClip("jump", false);
//             isJumping = true;
//             //pY -= pSpeed;
//             // Prevents the player from moving out of the screen
//             if (pY < 0)
//                 pY = 0;
//         } else if(dir == 5) { //Player Duck
//             //pY += pSpeed;
//             // Prevents the player from moving out of the screen
//             //if(pY > (400 - pHeight))
//             //    pY = 400 - pHeight;

//         } else if(dir == 4) { // Move the player Left
//             pX -= pSpeed;
            
//             // Prevents the player from moving out of the screen
//             if (pX < 10)
//                  pX = 10;
//             PlatformGen collision = collideWithPlatform();
//             if(collision != null){
//                 pX = collision.getX() + collision.getWidth();
//             }
//             updateWalkAnimation();
//         } else if(dir == 6) { // Move the player Right
//             pX += pSpeed;
            
//             // Prevents the player from moving out of the screen
//             if (pX > (880 - pWidth))
//                 pX = 880 - pWidth;
//             PlatformGen collision = collideWithPlatform();
//             if(collision != null){
//                 pX = collision.getX();
//             }
//             updateWalkAnimation();
//         } else {
//             // No movement - reset walk animation
//             walkFrame = 0;
//         }
//     }

//     public PlatformGen collideWithPlatform(){
//         for(PlatformGen p: platforms){
//             Rectangle2D.Double myRect = getBoundingRectangle();
//             Rectangle2D.Double platformRect = p.getBoundingRectangle();
//             if (myRect.intersects(platformRect)){
//                 return p;
//             }
//         }
//         return null;
//     }

//     // Update walk animation frames
//     private void updateWalkAnimation() {
//         long currentTime = System.currentTimeMillis();
//         if (currentTime - lastWalkFrameTime > 100) { // Change frame every 100ms
//             walkFrame = (walkFrame + 1) % walkImages.length;
//             lastWalkFrameTime = currentTime;
//         }
//     }

//     public void jump() {
//         if(isOnGround) {    
//             verticalVelocity =  jumpForce;
//             isOnGround = false;
//             isJumping =  true;
//         }
//     }


//     public void setIsOnGround(boolean onGround) {
//         this.isOnGround = onGround;
//         if (onGround) {
//             this.isJumping = false;
//         }
//     }

//     public void setVerticalVelocity(float velocity) {
//         this.verticalVelocity = velocity;
//     }

//     public boolean isFalling() {
//         return verticalVelocity > 0;
//     }

//     public void setY(int y) {
//         this.pY = y;
//     }

//     public boolean isOnPlayer(int x, int y) {
//         Rectangle2D.Double myRectangle = getBoundingRectangle();
//         return myRectangle.contains(x, y);
//     }

//     public Rectangle2D.Double getBoundingRectangle() {
//         return new Rectangle2D.Double(pX, pY, pWidth, pHeight);
//     }
// }
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