import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JPanel;

public class Enemy {

    private static final int XSIZE = 10;
    private static final int YSIZE = 10;
    private static final int XOFFSET = 75;


    
    // Variables
    private int eX, eY; // Enemy position
    private int initialY;
    private int eWidth = 45, eHeight = 45; // Size of enemy
    private int eSpeed = 15; // Speed of the enemy
    private int currentType; // To track enemy type (floating, flying, etc.)
    private int animationFrame = 0; // For animation
    private long lastFrameTime = 0; // For animation timing

    // Sine Wave Motion Variables
    private SineWaveMotion verticalMotion;
    private boolean movingRight = false;
    private long startTime;
    private int duration = 25000; // 20 seconds in milliseconds

    // Enemy Animation Sequences
    private Image[] floatingImages = new Image[4];
    private Image[] flyingImages = new Image[4];
    private Image[] flyingAltImages = new Image[4];
    private Image[] spikeyImages = new Image[4];
    private Image[] swimmingImages = new Image[4];
    private Image[] walkingImages = new Image[1]; // Only one walking image provided

    private List<Projectile> projectiles;
    
    private long lastShotTime = 0;
    private long shootingInterval = 1500; // Shoot every 1.5 seconds (adjust as needed)
    private Player targetPlayer; // Reference to the player

    private JPanel p;

    public Enemy(int x, int y, int type, JPanel panel, Player player) {
        // Enemy position and type
        this.eX = x;
        this.eY = y;
        this.initialY = y;
        this.currentType = type;
        this.p = panel;

        // Initialize Sine Wave Motion for vertical movement
        this.verticalMotion = new SineWaveMotion(panel);
        verticalMotion.setAmplitudeFactor(60); // Adjust for desired vertical range
        verticalMotion.setFrequencyFactor(1.5); // Adjust for desired vertical speed

        this.startTime = System.currentTimeMillis();

        this.projectiles = new LinkedList<>();
        this.targetPlayer = player;
        
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
            System.out.println();

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

    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    // Draw the enemy with appropriate image based on type
    public void draw(Graphics2D g2d) {
        Image currentImage = getCurrentImage();
        g2d.drawImage(currentImage, eX, eY, eWidth, eHeight, null);

        for (Projectile projectile : projectiles) {
            projectile.draw(g2d);
        }
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
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;

        if (currentType == 1 || currentType == 2) { // Only move flying types

            // Horizontal movement for 10 seconds
            if (elapsedTime < duration) {
                if (movingRight) {
                    eX += eSpeed;
                    if (eX > 800) { // Adjust based on your game width
                        movingRight = false;
                    }
                } else {
                    eX -= eSpeed;
                    if (eX < 0) { // Adjust based on your desired left boundary
                        movingRight = true;
                    }
                }

                // Vertical sine wave motion
                verticalMotion.update();
                eY = initialY - verticalMotion.getY(); // Subtract because SineWaveMotion's Y increases downwards
            } else {
                // After 10 seconds, you can define different behavior if needed
                // For now, let's just continue the right-to-left movement
                eX -= eSpeed;
            }

            // Shooting logic
            if (elapsedTime < duration) { // Only shoot during the 20-second period
                shoot(p); // Cast to JPanel
            }

            // Update projectiles
            List<Projectile> projectilesToRemove = new LinkedList<>();
            for (Projectile projectile : projectiles) {
                projectile.update();
                if (!projectile.isActive()) {
                    projectilesToRemove.add(projectile);
                }
            }
            projectiles.removeAll(projectilesToRemove);

            // Update animation frame
            updateAnimation();
        } else {
            // Default horizontal movement for non-flying enemies
            eX -= eSpeed;
        }
    
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

    public void shoot(JPanel panel) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime > shootingInterval) {
            Projectile projectile = new Projectile(panel, targetPlayer);
            projectile.setXPos(eX + eWidth / 2); // Start from the center of the enemy
            projectile.setYPos(eY + eHeight / 2);
            projectile.activate();
            projectiles.add(projectile);
            lastShotTime = currentTime;
        }
    }
}