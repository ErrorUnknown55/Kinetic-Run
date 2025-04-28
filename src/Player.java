import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;

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
    private Image[] swimRight = new Image[2];
    private Image[] swimDown = new Image[2];
    private Image[] jumpImages = new Image[3];

    // Player Images
    private Image deadImage, duckImage, fallImage, hitImage, rollImage, standImage;

    //Platforms
    private List<PlatformGen> platforms;

    // Tinting variables for flashing red
    private boolean isFlashingRed = false;
    private long flashStartTime;
    private long flashDuration = 500;
    private int flashInterval = 100;
    private int flashCount = 0;
    private int totalFlashes = 3;
    private boolean redTinted = false;
    private BufferedImage currentBufferedImage;
    private BufferedImage currentBaseImage;
    private RedTintEffect redTintEffect;  // Create the tint effect
    private boolean isFlashingSequenceActive = false;

    private boolean isInvincible = false;
    private long invincibilityStartTime;
    private long invincibilityDuration = 2000; // 2 seconds of invincibility

    private boolean shieldActive = false;
    private long shieldEndTime;

    // Animation control variables
    private int animationFrame = 0;
    private long lastFrameTime = 0;
    private final int ANIMATION_DELAY = 150;

    //to control player speed
    private int originalSpeed;
    private int currentSpeed;
    private long speedBoostEndTime;
    private boolean isSpeedBoosted = false;

    private boolean isInWater = false;
    
    private int lives = 3; // Starting lives
    private List<Heart> hearts = new ArrayList<>();

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

        // Load player Swim right Animation Sequence
        for(int i = 0; i < swimRight.length; i++)
            swimRight[i] = ImageManager.loadImage("images/player/PlayerBlue/swimright_"+ (i+1)+".png");

        // Load player Swim down Animation Sequence
        for(int i = 0; i < swimDown.length; i++)
            swimDown[i] = ImageManager.loadImage("images/player/PlayerBlue/swimdown_"+ (i+1)+".png");

        // Initialize platforms
        this.platforms = plf;

        redTintEffect = new RedTintEffect(255);

        currentBufferedImage = ImageManager.toBufferedImage(standImage);
        
        currentBaseImage = ImageManager.toBufferedImage(standImage);

        this.originalSpeed = pSpeed; // Store original speed
        this.currentSpeed = pSpeed;

    }

    public void setPosition(int x, int y){
        this.pX = x;
        this.pY = y;
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

    public int getLives() {
        return lives;
    }
    
    public void setLives(int lives) {
        this.lives = lives;
    }
    
    public void addLife() {
        if (lives < 5) { // Max 5 lives
            lives++;
        }
    }
    
    public void loseLife() {
        lives--;
        if (lives < 0) lives = 0;
    }
    
    public boolean isDead() {
        return lives <= 0;
    }

    public void activateShield(long duration) {
        this.shieldActive = true;
        this.shieldEndTime = System.currentTimeMillis() + duration;
        this.isInvincible = true; // Also set invincible
    }
    
    public void setShieldActive(boolean active) {
        this.shieldActive = active;
        if (!active) {
            this.isInvincible = false;
        }
    }

    public void increaseSpeed(float multiplier, long duration) {
        if (!isSpeedBoosted) {
            this.originalSpeed = pSpeed; // Store original speed if not already boosted
        }
        this.currentSpeed = (int) Math.round(originalSpeed * multiplier);
        this.pSpeed = currentSpeed;
        this.speedBoostEndTime = System.currentTimeMillis() + duration;
        this.isSpeedBoosted = true;
        
        // Optional: Add visual effect or sound

        // SoundManager.getInstance().playClip("powerup", false);
    }
    
    public boolean hasShield() {
        return shieldActive && System.currentTimeMillis() < shieldEndTime;
    }

    public void update() {

        if (isInWater) {
            // Apply small buoyancy force when not actively swimming up
            if (currentDirection != 5) {
                verticalVelocity -= 0.05f; // Small upward force
            }
            
            // Apply water resistance
            verticalVelocity *= 0.95f;
            
            // Update position
            pY += verticalVelocity;
            
            // Keep within vertical bounds
            if (pY < 0) pY = 0;
            if (pY > 548) {
                pY = 548;
                verticalVelocity = 0;
            }
        }

        if (shieldActive && System.currentTimeMillis() > shieldEndTime) {
            shieldActive = false;
            isInvincible = false;
        }

        //Apply Gravity if not on the ground
        if(!isOnGround) {
            verticalVelocity += gravity;
            pY += verticalVelocity;
        }

        // Check if speed boost has expired
        if (isSpeedBoosted && System.currentTimeMillis() > speedBoostEndTime) {
            resetSpeed();
        }

        //Prevent falling through the buttom
        PlatformGen collision = collideWithPlatform();
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
        // Handle flashing red effect - REPLACE with this:
        if (isFlashingRed) {
            long currentTime = System.currentTimeMillis();
            long elapsed = currentTime - flashStartTime;
            
            if (elapsed < flashDuration) {
                // Calculate flash state based on elapsed time
                int flashState = (int)(elapsed / flashInterval) % 2;
                redTinted = (flashState == 0);
                
                if (redTinted) {
                    applyRedTint();
                } else {
                    resetTint();
                }
            } else {
                endFlash();
            }
        }
        // Handle invincibility timer
        if (isInvincible) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - invincibilityStartTime > invincibilityDuration) {
                isInvincible = false;
                // Optional: Add a visual effect when invincibility ends
            }
        }
    }

    public void setWaterLevel(boolean inWater) {
        this.isInWater = inWater;
        
        if (inWater) {
            
            // Adjust physics for water
            this.gravity = 0.1f; // Reduced gravity in water
            this.jumpForce = 0; // Reduced jump in water
            this.verticalVelocity = 0;
            this.isJumping = false;
        } else {
            // Revert to land physics
            this.gravity = 0.5f;
            this.jumpForce = -12f;
        }
    }

    private void resetSpeed() {
        this.pSpeed = originalSpeed;
        this.currentSpeed = originalSpeed;
        this.isSpeedBoosted = false;
    }

    public boolean isSpeedBoosted() {
        return isSpeedBoosted;
    }

    private void endFlash() {
        isFlashingRed = false;
        isFlashingSequenceActive = false;
        redTinted = false;
        resetTint();
    }

    public boolean isInvincible() {
        return isInvincible;
    }
    public void debugJump() {
        System.out.println("Max jump height: " + calculateMaxJumpHeight() + " pixels");
    }
    
    private int calculateMaxJumpHeight() {
        // Use your actual physics values
        float testVelocity = -jumpForce; // Note: Negative if y increases downward
        float testY = 0;
        int frames = 0;
        int peakHeight = 0;
        
        while (testVelocity < 0) { // While moving upward
            testY += testVelocity;
            testVelocity += gravity; // Gravity should be positive if y increases downward
            frames++;
            
            if (testY < peakHeight) { // Track lowest (highest) point
                peakHeight = (int)testY;
            }
        }
        return -peakHeight; // Return as positive number
    }

    public void draw(Graphics2D g2d) {
        Image currentImage;
        if (isInWater) {
            

            // Update swim animation
            updateWaterAnimation();
        
            // Determine which swim animation to use
            if (currentDirection == 6) { // Right
                int swimFrame = animationFrame % swimRight.length;
                currentImage = swimImages[swimFrame];
            } 
            else if(currentDirection == 4) { // Left or neutral
                int swimFrame = animationFrame % swimImages.length;
                currentImage = swimRight[swimFrame];
            }

            else if (currentDirection == 5) { // Down
                int swimFrame = animationFrame % swimDown.length;
                currentImage = swimDown[swimFrame];
            }

            else{
                int swimFrame = animationFrame % swimRight.length;
                currentImage = swimImages[swimFrame];
            }
            
            // Apply red tint if needed
            if (isFlashingRed && redTinted) {
                currentImage = redTintEffect.apply(ImageManager.toBufferedImage(currentImage));
            }

            if (!isInvincible || (System.currentTimeMillis() % 200) < 100) {
                g2d.drawImage(currentImage, pX, pY, pWidth, pHeight, null);
            }
        }

        else{
            if (!isFlashingRed) {
                switch (currentDirection) {
                    case 4: case 6: currentBaseImage = ImageManager.toBufferedImage(walkImages[walkFrame]);
                    soundManager.playClip("walk", true); break;
                    case 5: currentBaseImage = ImageManager.toBufferedImage(duckImage); break;
                    case 8: currentBaseImage = ImageManager.toBufferedImage(jumpImages[0]); break;
                    default: currentBaseImage = ImageManager.toBufferedImage(standImage); 
                    soundManager.stopClip("walk");break;
                }
            }
        
            currentImage = getCurrentImage();
            if (!isInvincible || (System.currentTimeMillis() % 200) < 100) {
                g2d.drawImage(currentImage, pX, pY, pWidth, pHeight, null);
            }
            
        }
        // Optional: Draw an outline when invincible
        if (isInvincible) {
            g2d.setColor(new Color(255, 255, 255, 100)); // Semi-transparent white
            g2d.drawRect(pX, pY, pWidth, pHeight); // Or use a more fancy effect
        }
        // g2d.drawImage(currentImage, pX, pY, pWidth, pHeight, null);
        if (hasShield()) {
            if (isInWater) {
                g2d.setColor(new Color(0, 0, 80, 150)); // Darker blue underwater
            } else {
                g2d.setColor(new Color(100, 100, 255, 100)); // Original light blue
            }
            g2d.fillOval(pX - 5, pY - 5, pWidth + 10, pHeight + 10);
        }

        if (isSpeedBoosted) {
            if (isInWater) {
                g2d.setColor(new Color(150, 150, 0, 120)); // Darker yellow underwater
            } else {
                g2d.setColor(new Color(255, 255, 0, 100)); // Original bright yellow
            }
            g2d.fillOval(pX - 5, pY - 5, pWidth + 10, pHeight + 10);
        }

        
    }

    public boolean isInWater() {
        return isInWater;
    }

    // Control the player movement
    public void movement(int dir) {
        currentDirection = dir;

        System.out.println("PX: "+pX+ " PY: "+pY);
        
        if (isInWater) {
            // Water movement - no jumping, free movement
            if (dir == 4) { // Left
                pX -= pSpeed;
                if (pX < 10) pX = 10;
                PlatformGen collision = collideWithPlatform();
                if (collision != null) {
                    pX = collision.getX() + collision.getWidth();
                }
                updateWaterAnimation();
            } 
            else if (dir == 6) { // Right
                pX += pSpeed;
                if (pX > (880 - pWidth)) pX = 880 - pWidth;
                PlatformGen collision = collideWithPlatform();
                if (collision != null) {
                    pX = collision.getX();
                }
                updateWaterAnimation();
            }
            // Disable jump in water
            else if (dir == 8) {
                // Optional: Could implement swim-up animation here
                pY -= pSpeed;
                if(pY < 0){
                    pY =0;
                }
                verticalVelocity = -3f; // Constant upward speed in water
                updateWaterAnimation();
            }
            else if (dir == 5) { // Swim down
                // Apply small downward force
                verticalVelocity = 3f; // Slow sinking when pressing down
                updateWaterAnimation();
            }
            else {
                // No vertical movement key pressed - slow sinking
                verticalVelocity = 1.25f; // Natural slow sinking in water
            }
            // Apply water resistance (damping) to vertical movement
            verticalVelocity *= 0.9f;
            
            // Update position based on velocity
            pY += verticalVelocity;
            
            // Keep player within screen bounds
            if (pY < 0) pY = 0;
            if (pY > 548) pY = 548;
        }
        else{
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

    private void updateWaterAnimation() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime > ANIMATION_DELAY) {
            animationFrame++;
            lastFrameTime = currentTime;
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

    private Image getCurrentImage() {
        if (isFlashingRed && redTinted && currentBufferedImage != null) {
            System.out.println("Returning tinted image"); // DEBUG
            return currentBufferedImage;
        }
    
        if (!isFlashingRed) { // Only select base image if not flashing
            switch (currentDirection) {
                case 4: case 6: return walkImages[walkFrame];
                case 5: return duckImage;
                case 8: return jumpImages[0];
                default: return standImage;
            }
        }
        return currentBufferedImage; // Should not reach here if flashing and tinted
    }

    public void flashRed() {
        if (!isFlashingSequenceActive && !isInvincible) {
            isFlashingRed = true;
            isFlashingSequenceActive = true;
            isInvincible = true; // Start invincibility
            invincibilityStartTime = System.currentTimeMillis();
            flashStartTime = System.currentTimeMillis();
            // redTinted = false;
            flashCount = 0;
            System.out.println("Starting flash sequence");
            // System.out.println("flashRed() called");
        }
    }
    public boolean isFlashingRed() {
        return isFlashingSequenceActive;
    }

    private void applyRedTint() {
        BufferedImage sourceImage = getSourceBufferedImage();
        System.out.println("getSourceBufferedImage(): " + sourceImage); // DEBUG
        if (sourceImage != null) {
            currentBufferedImage = redTintEffect.apply(sourceImage);
            System.out.println("currentBufferedImage after tint: " + currentBufferedImage); // DEBUG
        }
    }

    private void resetTint() {
        currentBufferedImage = ImageManager.toBufferedImage(getCurrentImage());
        System.out.println("resetTint() called"); // DEBUG
    }

    // private BufferedImage getSourceBufferedImage() {
    //     switch (currentDirection) {
    //         case 4:
    //         case 6:
    //             return ImageManager.toBufferedImage(walkImages[walkFrame]);
    //         case 5:
    //             return ImageManager.toBufferedImage(duckImage);
    //         case 8:
    //             return ImageManager.toBufferedImage(jumpImages[0]);
    //         default:
    //             return ImageManager.toBufferedImage(standImage);
    //     }
    // }

    private BufferedImage getSourceBufferedImage() {
        return currentBaseImage;
    }
}