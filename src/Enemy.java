import java.awt.Graphics;
import java.awt.Image;
import java.awt.geom.Rectangle2D;

public class Enemy {
    
    // Variables
    private int eX, eY; // Enemy position
    private int eWidth = 45, eHeight = 45; // Size of enemy
    private int eSpeed = 3; // Speed of the enemy
    private int currentType; // To track enemy type (floating, flying, etc.)
    private int animationFrame = 0; // For animation
    private long lastFrameTime = 0; // For animation timing

    // Enemy Animation Sequences
    private Image[] floatingImages = new Image[4];
    private Image[] flyingImages = new Image[4];
    private Image[] flyingAltImages = new Image[4];
    private Image[] spikeyImages = new Image[4];
    private Image[] swimmingImages = new Image[4];
    private Image[] walkingImages = new Image[1]; // Only one walking image provided

    public Enemy(int x, int y, int type) {
        // Enemy position and type
        this.eX = x;
        this.eY = y;
        this.currentType = type;
        
        // Load enemy images based on type
        loadEnemyImages();
    }

    private void loadEnemyImages() {
        // Load Floating Enemy Animation Sequence
        for(int i = 0; i < floatingImages.length; i++)
            floatingImages[i] = ImageManager.loadImage("images/enemies/enemyFloating_"+(i+1)+".png");

        // Load Flying Enemy Animation Sequence
        for(int i = 0; i < flyingImages.length; i++)
            flyingImages[i] = ImageManager.loadImage("images/enemies/enemyFlying_"+(i+1)+".png");

        // Load Alternate Flying Enemy Animation Sequence
        for(int i = 0; i < flyingAltImages.length; i++)
            flyingAltImages[i] = ImageManager.loadImage("images/enemies/enemyFlyingAlt_"+(i+1)+".png");

        // Load Spikey Enemy Animation Sequence
        for(int i = 0; i < spikeyImages.length; i++)
            spikeyImages[i] = ImageManager.loadImage("images/enemies/enemySpikey_"+(i+1)+".png");

        // Load Swimming Enemy Animation Sequence
        for(int i = 0; i < swimmingImages.length; i++)
            swimmingImages[i] = ImageManager.loadImage("images/enemies/enemySwimming_"+(i+1)+".png");

        // Load Walking Enemy Image (only one image provided)
        walkingImages[0] = ImageManager.loadImage("images/enemies/enemyWalking_1.png");
    }

    public int getX() {
        return eX;
    }

    public int getY() {
        return eY;
    }

    public int getWidth() {
        return eWidth;
    }

    public int getHeight() {
        return eHeight;
    }

    // Draw the enemy with appropriate image based on type
    public void draw(Graphics g2d) {
        Image currentImage = getCurrentImage();
        g2d.drawImage(currentImage, eX, eY, eWidth, eHeight, null);
    }

    private Image getCurrentImage() {
        switch(currentType) {
            case 0: // Floating
                return floatingImages[animationFrame];
            case 1: // Flying
                return flyingImages[animationFrame];
            case 2: // Alternate Flying
                return flyingAltImages[animationFrame];
            case 3: // Spikey
                return spikeyImages[animationFrame];
            case 4: // Swimming
                return swimmingImages[animationFrame];
            case 5: // Walking
                return walkingImages[0]; // Only one image available
            default:
                return floatingImages[animationFrame];
        }
    }

    // Update enemy position (moves from right to left)
    public void update() {
        // Move enemy from right to left
        eX -= eSpeed;
        
        // Update animation frame
        updateAnimation();
    }

    // Update animation frames
    private void updateAnimation() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime > 150) { // Change frame every 150ms
            animationFrame = (animationFrame + 1) % 4; // All animations have 4 frames except walking
            lastFrameTime = currentTime;
        }
    }

    public boolean isOffScreen() {
        return eX + eWidth < 0;
    }

    public boolean collidesWith(Player player) {
        Rectangle2D.Double enemyRect = new Rectangle2D.Double(eX, eY, eWidth, eHeight);
        return enemyRect.intersects(player.getBoundingRectangle());
    }

    public Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(eX, eY, eWidth, eHeight);
    }
}