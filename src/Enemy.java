import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.JPanel;
import java.awt.Dimension;

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

    // Bezier Curve Motion Variables
    private BezierCurveMotion bezierMotion;

    // Enemy Animation Sequences
    private Image[] floatingImages = new Image[4];
    private Image[] flyingImages = new Image[4];
    private Image[] flyingAltImages = new Image[4];
    private Image[] spikeyImages = new Image[4];
    private Image[] swimmingImages = new Image[4];
    private Image[] swimmingImages_right = new Image[4];
    private Image[] swimmingImages_up = new Image[4];
    private Image[] swimmingImages_down = new Image[4];
    private Image[] walkingImages = new Image[1]; // Only one walking image provided

    private List<Projectile> projectiles;
    
    private long lastShotTime = 0;
    private long shootingInterval = 3500; // Shoot every 1.5 seconds (adjust as needed)
    private Player targetPlayer; // Reference to the player

    private int spikeySideToSideCount = 0;
    private int maxSideToSide = 2; // Number of side-to-side movements
    private boolean spikeyMovingLeft = true; // To control the direction of Bezier

    private JPanel p;
    private Dimension dimension;

    private boolean isFrozen = false;
    private int frozenSpeed = 0; // When frozen

    private boolean isInWater = false;
    

    public Enemy(int x, int y, int type, JPanel panel, Player player) {
        // Enemy position and type
        // this.eX = x;
        // this.eY = y;
        this.initialY = y;
        this.currentType = type;
        this.p = panel;
        dimension = panel.getSize();
        // System.out.println("dimension.width: " + dimension.width);

        this.startTime = System.currentTimeMillis();

        this.projectiles = new LinkedList<>();
        this.targetPlayer = player;

        // Initialize motion based on enemy type
        if (currentType == 1 || currentType == 2) { // Flying types
            // Initialize Sine Wave Motion for vertical movement
            this.eX = 800;
            // this.eY = y;
            this.verticalMotion = new SineWaveMotion(panel);
            verticalMotion.setAmplitudeFactor(60); // Adjust for desired vertical range
            verticalMotion.setFrequencyFactor(1.5); // Adjust for desired vertical speed

            // Initialize eY based on initial sine wave position
            verticalMotion.update(); // Call update once to get the initial y
            this.eY = initialY - verticalMotion.getY();

            updateAnimation(); // Initialize the animation frame
        } else if (currentType == 3) { // Spikey type
            // Define control points for the Bezier curve (adjust these as needed)
            Point p0 = new Point(830, 545 + 10); // Starting position, had to be manually set
            Point p1 = new Point(800 / 2 + 10, 545 + 10); // Control point 1
            Point p2 = new Point(0, 545 + 10); // Ending position (can be adjusted for looping)
            this.bezierMotion = new BezierCurveMotion(panel, this, p0, p1, p2);
            this.bezierMotion.activate();
            this.bezierMotion.update();
            this.eX = p0.x;
            this.eY = p0.y;
            // System.out.println("spawn spike: " + eX+"  "+eY);
        }else {
            this.eX=x;
            this.eY=y;
        }
        
        
        // Load enemy images based on type
        loadEnemyImages();
    }

    public void setShootingInterval(long num){
        this.shootingInterval=num;
    }

    // Add this method
    public void setFrozen(boolean frozen) {
        this.isFrozen = frozen;
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

        for(int i = 0; i < swimmingImages_right.length; i++)
            swimmingImages_right[i] = ImageManager.loadImage("images/enemies/esr_"+(i+1)+".png");

        for(int i = 0; i < swimmingImages_up.length; i++)
            swimmingImages_up[i] = ImageManager.loadImage("images/enemies/esup_"+(i+1)+".png");

        for(int i = 0; i < swimmingImages_down.length; i++)
            swimmingImages_down[i] = ImageManager.loadImage("images/enemies/esdown_"+(i+1)+".png");



        // Load Walking Enemy Image (only one image provided)
        walkingImages[0] = ImageManager.loadImage("images/enemies/enemyWalking_1.png");
    }

    public void setWaterLevel(boolean inWater) {
        this.isInWater = inWater;
        loadEnemyImages();
        
        if (inWater) {
            // Change enemy behavior in water
            if (currentType == 0 || currentType == 1 || currentType == 2 || currentType == 3) {
                Random random = new Random();
                currentType = 4 + random.nextInt(4); // Generates 4, 5, 6, or 7
            } else {
                currentType = 0; // Floating type
            }
            
            // Adjust movement for water
            this.eSpeed = 9; // Slower movement in water
        }
    }

    public void setX(int x){
        this.eX = x;
    }
    public void setY(int y){
        this.eY = y;
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
        // System.out.println("drawn y: " + eY);
        g2d.drawImage(currentImage, eX, eY, eWidth, eHeight, null);

        for (Projectile projectile : projectiles) {
            projectile.draw(g2d);
        }
    }

    public int getType(){
        return this.currentType;
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
            case 5: // Swimming right
                return swimmingImages_right[animationFrame];
            case 6: // Swimming up
                return swimmingImages_up[animationFrame];
            case 7: // Swimming down
                return swimmingImages_down[animationFrame];
            case 8: // Walking
                return walkingImages[0]; // Only one image available
            default:
                return floatingImages[animationFrame];
        }
    }

    // Update enemy position (moves from right to left)
    public void update() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;

        if (isFrozen) return; // Skip movement if frozen

        if (isInWater) {
            // Special water movement patterns
            if (currentType == 4) { // Swimming enemy
                verticalMotion.setAmplitudeFactor(100); // More vertical movement
                verticalMotion.setFrequencyFactor(0.8); // Slower oscillation
            }
            // ... rest of water-specific logic
        }
        

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
                // System.out.println("y: " + eY);
                // Shooting logic
                if (elapsedTime < duration) { // Only shoot during the defined period
                    shoot(p); // Cast to JPanel
                }
            } else {
                // After the duration, continue horizontal movement
                eX -= eSpeed;
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
        } else if (currentType == 3) { // Spikey type
            // Update Bezier curve motion
            if (bezierMotion != null && bezierMotion.isActive()) {
                bezierMotion.update();
                this.eX = (int) bezierMotion.getCurrentPoint().getX();
                this.eY = (int) bezierMotion.getCurrentPoint().getY();
                // Check if Bezier motion is complete (approximately)
                if (spikeyMovingLeft && this.eX <= 0) {
                    spikeyMovingLeft = false;
                    spikeySideToSideCount++;
                    // Define Bezier curve to move back to the right
                    Point p0 = new Point(this.eX, this.eY);
                    Point p1 = new Point(800 / 2, 545 + 10); // Control point slightly down
                    Point p2 = new Point(830, 545 + 10);
                    this.bezierMotion = new BezierCurveMotion(p, this, p0, p1, p2);
                    this.bezierMotion.activate();
                } else if (!spikeyMovingLeft && this.eX >= 800) {
                    spikeyMovingLeft = true;
                    spikeySideToSideCount++;
                    // Define Bezier curve to move back to the left
                    Point p0 = new Point(this.eX, this.eY);
                    Point p1 = new Point(800 / 2, 545 + 10); // Control point slightly up
                    Point p2 = new Point(0, 545 + 10);
                    this.bezierMotion = new BezierCurveMotion(p, this, p0, p1, p2);
                    this.bezierMotion.activate();
                }

                if (spikeySideToSideCount >= maxSideToSide * 2) { // Multiply by 2 because one side-to-side is left then right
                    bezierMotion.deActivate();
                }
            } else {
                // Once Bezier motion is done, move to the left
                eX -= eSpeed;
            }
            // Spikey enemies do not shoot projectiles
            updateAnimation(); // Still animate the spikey enemy

        } else if(currentType == 4){
            eX += eSpeed;
            updateAnimation();
        } else if(currentType == 5){
            eX -= eSpeed;
            updateAnimation();
        } else if(currentType == 6){
            eY -= eSpeed;
            updateAnimation();
        } else if(currentType == 7){
            eY += eSpeed;
            updateAnimation();
        } else {
            // Default horizontal movement for other enemy types
            eX -= eSpeed;
            updateAnimation();
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

        if (isFrozen) return;

        if (currentType == 1 || currentType == 2) {
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
}