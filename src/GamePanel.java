import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements KeyListener, Runnable {

    private Thread gameThread;

    //Sound
    private SoundManager soundManager;

    //Image
    private BufferedImage image;
    private Background bgImage;
    private Background platform;


    //Tile Map
    private TileMap tileMap;
    private TileMapManager tileMapManager;
    private String mapFile;

    //Player
    private Player player;

    //Veriables
    private int scrWidth;
    private int scrHeight;
    private boolean isRunning;
    private boolean isPaused;

    //Font
    private Font gameFont;

    private long startTime;
    private int mapcount;
    
    private List<PlatformGen> platforms;
    private List<PowerUp> powerups;
    private List<Enemy> enemies;

    private int platformDelayCounter = 0;
    private int platformSpawnDelay; //suppose to control the speed at which th platforms generate

    private PlatformGen pf,pf2,pf3;
    private int lastPlatformY = -100;
    private int minVerticalSpacing;
    private int targetVerticalDifference;
    private int verticalVariance;
    private int targetJumpSpace;
    private int jumpSpaceVariance;
    private int lastPlatformBottom;

    private Random random = new Random();

    private long enemySpawnTimer = 0;
    private long enemySpawnInterval = 5000;

    private int powerUpSpawnCounter = 0;
    private final int POWERUP_SPAWN_INTERVAL = 2; // Every 2 platforms
    private final double POWERUP_SPAWN_CHANCE = 0.8; // 80% chance when eligible

    private int maxPlatformPlacementAttempts = 5;
    private int retryHorizontalReduction = 50;
    private int retryVerticalAdjustment = 30;


    //need to remove
    private Enemy flyingEnemy;


    public GamePanel(int scrW,  int scrH) {

        this.scrWidth =  scrW;
        this.scrHeight = scrH;

    
        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();

        soundManager = SoundManager.getInstance();
        


        // soundManager = SoundManager.getInstance();
        

        startTime = 0;
        mapcount = 1;



        platforms = new LinkedList<>();
        pf = new PlatformGen(this,450, "large");
        pf2 = new PlatformGen(this, 300, "medium");
        pf3 = new PlatformGen(this, 200, "small");
        pf.setX(100);
        pf2.setX(400);
        pf3.setX(700);

        player = new Player(100, 545, platforms);
        
        platforms.add(pf);
        lastPlatformY = 450;
        // platforms.add(pf2);
        platforms.add(pf2);
        // lastPlatformY = 200;

        powerups = new LinkedList<>();  //initialize powerups list
        // Initialize counters
        powerUpSpawnCounter = POWERUP_SPAWN_INTERVAL;

        enemies = new LinkedList<>();
        
        platformSpawnDelay=10;  //adjust to generate platforms slower
        minVerticalSpacing = 15;
        targetVerticalDifference = 12;
        verticalVariance = 3;
        targetJumpSpace = 12;
        jumpSpaceVariance = 3;

        flyingEnemy = new Enemy(700, 350, 3, this, player);


    
        
        image = new BufferedImage(900, 700, BufferedImage.TYPE_INT_RGB);

        //Tile map manager
        tileMapManager =  new TileMapManager(this);

        gameFont = new  Font("Arial", Font.BOLD, 24);  

    }

    public void createGameEntities() {
        
        bgImage = new Background(this, "images/background/backgroundColorForest.png", 98);
        bgImage.setY(0);
        platform = new Background(this, "images/platform/greentiles/ground-platform.png", 98);
        platform.setY(545+player.getHeight());

        // mapFile = "maps/map"+mapcount+".txt";
        // mapFile = "maps/map2.txt";
        // try {
        //     //Load Tile Map
        //     tileMap = tileMapManager.loadMap(mapFile);
            
            

        // } catch (IOException e) {
        //     System.err.println("Error loading map:" +  e.getMessage());
        //     e.printStackTrace();
        // }
        

    }
    
    public void gameRender() {
        //Creates a Graphics2D obj for the BufferedImage
        Graphics2D imageContext = (Graphics2D) image.getGraphics();

        imageContext.setBackground(Color.BLACK);
        imageContext.fillRect(0, 0, image.getWidth(), image.getHeight());
        
        //Draw background 
        if(bgImage != null) {
            bgImage.update();
            bgImage.draw(imageContext);
        }


        if(platform != null){
            platform.update();
            platform.draw(imageContext);
        }


        //Draw tile map
        if(tileMap != null) {
            tileMap.draw(imageContext);
        }

        player.draw(imageContext);


        // System.out.println("Loading map:" + mapFile);

        // pf.drawPlatforms(imageContext);
        // pf2.drawPlatforms(imageContext);
        // pf3.drawPlatforms(imageContext);;
        for (PlatformGen platform : platforms) {
            platform.drawPlatforms(imageContext);
        }

        for (PowerUp pu : powerups) {
            pu.draw(imageContext);
        }

        for (Enemy enemy : enemies) {
            enemy.draw(imageContext); // Cast to Graphics2D for the draw method
        }
        // flyingEnemy.draw(imageContext);

        
        Graphics2D g2 = (Graphics2D) getGraphics();
        g2.drawImage(image, 0, 0, scrWidth, scrHeight, null);
        
        imageContext.dispose();
        g2.dispose();  
        // startTime = 0;
              
    }

    // In GamePanel.java
    private void checkPlatformCollisions() {
        player.setIsOnGround(false); // Assume not on ground until collision check
        
        for (PlatformGen platform : platforms) {
            if (player.getBoundingRectangle().intersects(platform.getBoundingRectangle())) {
                Rectangle2D.Double playerRect = player.getBoundingRectangle();
                Rectangle2D.Double platformRect = platform.getBoundingRectangle();
                
                // Check if player is landing on top of platform
                if (playerRect.y + playerRect.height >= platformRect.y && 
                    playerRect.y + playerRect.height <= platformRect.y + 10 && // Tolerance
                    playerRect.x + playerRect.width > platformRect.x && 
                    playerRect.x < platformRect.x + platformRect.width) {
                    
                    player.setY((int)platformRect.y - player.getHeight());
                    player.setIsOnGround(true);
                    player.setVerticalVelocity(0);
                }
            }
        }
    }

    private void checkEnemyCollisions() {
        List<Enemy> enemiesToRemove = new LinkedList<>();
        for (Enemy enemy : enemies) {
            if (enemy.collidesWith(player) && !player.isInvincible() && !player.hasShield()) {
                if (!player.isFlashingRed()) {  // Add this check
                    player.flashRed();
                    System.out.println("Player collided with enemy - flashing!");
                }
                enemiesToRemove.add(enemy);
            }
            // Check for collisions between player projectiles and enemies (if you implement player shooting)
            // ...
        }
        enemies.removeAll(enemiesToRemove);
    }



    public void gameUpdate() {

        PlatformGen tempPlatform;
        PlatformGen newestPlatform = null;
        // flyingEnemy.update();
        // // Check for collisions between enemy projectiles and the player
        // List<Projectile> enemyProjectiles = flyingEnemy.getProjectiles();
        // List<Projectile> hitProjectiles = new LinkedList<>();
        // for (Projectile projectile : enemyProjectiles) {
        //     if (projectile.getBoundingRectangle().intersects(player.getBoundingRectangle())) {
        //         // Handle player being hit (e.g., decrease health, game over)
        //         System.out.println("Player hit by projectile!");
        //         hitProjectiles.add(projectile); // Mark for removal
        //     }
        // }
        // enemyProjectiles.removeAll(hitProjectiles); // Remove hit projectiles

        // Update all enemies
        List<Projectile> hitProjectiles = new LinkedList<>();
        for (Enemy enemy : enemies) {
            enemy.update();
            // Check for collisions between enemy projectiles and the player
            List<Projectile> enemyProjectiles = enemy.getProjectiles();
            for (Projectile projectile : enemyProjectiles) {
                if (projectile.getBoundingRectangle().intersects(player.getBoundingRectangle()) && !player.isInvincible() && !player.hasShield()) {
                    // Handle player being hit
                    if (!player.isFlashingRed()) {  // Add this check
                        player.flashRed();
                        System.out.println("Player collided with enemy - flashing!");
                    }
                    System.out.println("Player hit by projectile!");
                    hitProjectiles.add(projectile); // Mark for removal
                }
            }
            enemyProjectiles.removeAll(hitProjectiles); // Remove hit projectiles from this enemy
        }

        checkEnemyCollisions(); // Check for player-enemy collisions

        if(bgImage != null)
            bgImage.setAutoScroll(isRunning);
        
        if(platform != null){
            platform.setAutoScroll(isRunning);
        }
        List<PlatformGen> platformsToRemove = new LinkedList<>();
        for (PlatformGen platform : platforms) {
            platform.move();
            if (platform.getX() + platform.getWidth() < -200) {
                platformsToRemove.add(platform);
                System.out.println("Platform removed");
            }
        }
        platforms.removeAll(platformsToRemove);

        List<PowerUp> powerUpsToRemove = new LinkedList<>();
        for (PowerUp powerUp : powerups) {
            powerUp.update(); // If your PowerUp class has an update method
            if (powerUp.getX() + powerUp.getWidth() < -50) {
                powerUpsToRemove.add(powerUp);
            }
        }
        powerups.removeAll(powerUpsToRemove);
        //Check collisions
        checkPlatformCollisions();
        checkPowerUpCollisions();
        player.update();
        // System.out.println("platformDelayCounter: " + platformDelayCounter);
        // Control platform generation with a delay
        if (platformDelayCounter >= platformSpawnDelay) {
            if (!platforms.isEmpty()) {
                PlatformGen lastPlatform = platforms.getLast();
                boolean platformAdded = false;
                int retryCount = 0;
                int maxRetries = 5; // Maximum attempts to place a platform
                
                while (!platformAdded && retryCount < maxRetries) {
                    // Calculate new platform position with better vertical variation
                    int minJumpHeight = 50;
                    int maxJumpHeight = 200;
                    int horizontalSpacing = 300 + random.nextInt(200);
                    
                    // Alternate between upward and downward jumps
                    boolean jumpUp = random.nextBoolean();
                    int newPlatformY;
                    
                    if (jumpUp) {
                        newPlatformY = lastPlatform.getY() - (minJumpHeight + random.nextInt(maxJumpHeight - minJumpHeight));
                    } else {
                        newPlatformY = lastPlatform.getY() + (minJumpHeight + random.nextInt(maxJumpHeight - minJumpHeight));
                    }
                    
                    // Keep platform within screen bounds
                    newPlatformY = Math.max(120, Math.min(scrHeight - 150, newPlatformY));
                    
                    // On retries, adjust the position more aggressively
                    if (retryCount > 0) {
                        horizontalSpacing -= 50; // Reduce spacing on retries
                        if (jumpUp) {
                            newPlatformY -= 30; // Move higher
                        } else {
                            newPlatformY += 30; // Move lower
                        }
                        newPlatformY = Math.max(120, Math.min(scrHeight - 150, newPlatformY));
                    }
                    
                    int newPlatformX = lastPlatform.getX() + horizontalSpacing;
                    String[] sizes = {"small", "medium", "large"};
                    String newSize = sizes[random.nextInt(sizes.length)];
                    
                    tempPlatform = new PlatformGen(this, newPlatformY, newSize);
                    tempPlatform.setX(newPlatformX);
                    
                    // Check for overlaps with existing platforms
                    boolean validPlacement = true;
                    Rectangle2D.Double newRect = tempPlatform.getBoundingRectangle();
                    
                    for (PlatformGen existing : platforms) {
                        Rectangle2D.Double existingRect = existing.getBoundingRectangle();
                        
                        // Slightly relaxed collision checks on retries
                        if (newRect.intersects(existingRect) || 
                            (retryCount < 2 && Math.abs(newRect.y - existingRect.y) < 50)) {
                            validPlacement = false;
                            break;
                        }
                    }
                    
                    if (validPlacement && newPlatformX > lastPlatform.getX() + 150) {
                        platforms.add(tempPlatform);
                        platformAdded = true;
                        
                        if (random.nextDouble() < 0.2) {
                            spawnPowerUp(tempPlatform);
                        }
                        
                        System.out.println("Added platform after " + (retryCount+1) + 
                                         " tries at Y: " + newPlatformY);
                    } else {
                        retryCount++;
                    }
                }
                
                if (!platformAdded) {
                    System.out.println("Failed to place platform after " + maxRetries + " attempts");
                    // As last resort, place a guaranteed platform with relaxed rules
                    int newPlatformX = lastPlatform.getX() + 400;
                    int newPlatformY = Math.max(120, Math.min(scrHeight - 150, 
                        lastPlatform.getY() + (random.nextBoolean() ? 150 : -150)));
                    
                    tempPlatform = new PlatformGen(this, newPlatformY, "large");
                    tempPlatform.setX(newPlatformX);
                    platforms.add(tempPlatform);
                }
            }
            platformDelayCounter = 0;
        }
        platformDelayCounter++;
        // if (platformDelayCounter >= platformSpawnDelay) {
        //     if (!platforms.isEmpty()) {
        //         PlatformGen lastPlatform = platforms.getLast();
                
        //         // Calculate new platform position
        //         int minJumpHeight = 100;
        //         int maxJumpHeight = 200;
        //         int horizontalSpacing = 300 + random.nextInt(200);
                
        //         int newPlatformY = lastPlatform.getY() - (minJumpHeight + random.nextInt(maxJumpHeight - minJumpHeight));
        //         newPlatformY = Math.max(120, Math.min(scrHeight - 100, newPlatformY));
                
        //         int newPlatformX = lastPlatform.getX() + horizontalSpacing;
        //         String[] sizes = {"small", "medium", "large"};
        //         String newSize = sizes[random.nextInt(sizes.length)];
                
        //         tempPlatform = new PlatformGen(this, newPlatformY, newSize);
        //         tempPlatform.setX(newPlatformX);
                
        //         // Add after basic validation
        //         if (newPlatformX > lastPlatform.getX() + 200) { // Ensure minimum spacing
        //             platforms.add(tempPlatform);
                    
        //             // Occasional power-up
        //             if (random.nextDouble() < 0.2) {
        //                 spawnPowerUp(tempPlatform);
        //             }
        //         }
        //     }
        //     platformDelayCounter = 0;
        // }
        // platformDelayCounter++;


// THIS ONE WORKS BUT NOT PERFECTLY
        // if (platformDelayCounter >= platformSpawnDelay) {
        //     if (!platforms.isEmpty()) {
        //         PlatformGen lastPlatform = platforms.getLast();
                
        //         // Calculate new platform position with better vertical variation
        //         int minJumpHeight = 100;  // Minimum vertical space needed to jump
        //         int maxJumpHeight = 200;  // Maximum vertical space player can jump
        //         int horizontalSpacing = 300 + random.nextInt(200); // Space between platforms
                
        //         // Alternate between upward and downward jumps
        //         boolean jumpUp = random.nextBoolean();
        //         int newPlatformY;
                
        //         if (jumpUp) {
        //             newPlatformY = lastPlatform.getY() - (minJumpHeight + random.nextInt(maxJumpHeight - minJumpHeight));
        //         } else {
        //             newPlatformY = lastPlatform.getY() + (minJumpHeight + random.nextInt(maxJumpHeight - minJumpHeight));
        //         }
                
        //         // Keep platform within screen bounds
        //         newPlatformY = Math.max(120, Math.min(scrHeight - 150, newPlatformY));
                
        //         int newPlatformX = lastPlatform.getX() + horizontalSpacing;
        //         String[] sizes = {"small", "medium", "large"};
        //         String newSize = sizes[random.nextInt(sizes.length)];
                
        //         tempPlatform = new PlatformGen(this, newPlatformY, newSize);
        //         tempPlatform.setX(newPlatformX);
                
        //         // Check for overlaps with existing platforms
        //         boolean validPlacement = true;
        //         Rectangle2D.Double newRect = tempPlatform.getBoundingRectangle();
                
        //         for (PlatformGen existing : platforms) {
        //             Rectangle2D.Double existingRect = existing.getBoundingRectangle();
                    
        //             // Check for direct overlap
        //             if (newRect.intersects(existingRect)) {
        //                 validPlacement = false;
        //                 break;
        //             }
                    
        //             // Ensure minimum vertical spacing (at least 50 pixels between platforms)
        //             if (Math.abs(newRect.y - existingRect.y) < 50) {
        //                 validPlacement = false;
        //                 break;
        //             }
        //         }
                
        //         // Only add if valid position
        //         if (validPlacement && newPlatformX > lastPlatform.getX() + 200) {
        //             platforms.add(tempPlatform);
                    
        //             // Occasional power-up (20% chance)
        //             if (random.nextDouble() < 0.2) {
        //                 spawnPowerUp(tempPlatform);
        //             }
                    
        //             System.out.println("Added platform at Y: " + newPlatformY + 
        //                              " (Previous Y: " + lastPlatform.getY() + ")");
        //         } else {
        //             System.out.println("Skipped overlapping platform");
        //         }
        //     }
        //     platformDelayCounter = 0;
        // }
        // platformDelayCounter++;



        // if (platformDelayCounter >= platformSpawnDelay) {
        //     int newPlatformY = 0; // Initialize
        //     if (platforms.isEmpty()) {
        //         // Spawn the first platform at a fixed Y
        //         newPlatformY = 450;
        //         tempPlatform = new PlatformGen(this, newPlatformY, "large");
        //     } else {
        //         // Get the Y coordinate of the last platform
        //         int lastY = platforms.getLast().getY();
        //         int lastHeight = platforms.getLast().getHeight();
        //         int minNewY = lastY - targetJumpSpace - jumpSpaceVariance - lastHeight;
        //         int maxNewY = lastY - targetJumpSpace + jumpSpaceVariance - lastHeight;
        //         // Ensure the new platform doesn't go too high
        //         newPlatformY = Math.max(120, random.nextInt(maxNewY - minNewY + 1) + minNewY);
        //         // Randomly choose a size for the new platform
        //         String[] sizes = {"small", "medium", "large"};
        //         String newSize = sizes[random.nextInt(sizes.length)];
        //         tempPlatform = new PlatformGen(this, newPlatformY, newSize);
        //     }

        //     boolean validPlacement = true;
        //     Rectangle2D.Double newPlatformRect = tempPlatform.checkPlatformIntersect();
        //     for (PlatformGen existingPlatform : platforms) {
        //         Rectangle2D.Double existingPlatformRect = existingPlatform.checkPlatformIntersect();
        //         // Check for direct overlap (both horizontal and vertical)
        //         if (newPlatformRect.intersects(existingPlatformRect)) {
        //             validPlacement = false;
        //             break;
        //         }
        //         // Check for minimum vertical spacing
        //         if (newPlatformRect.y < existingPlatformRect.y) { // newPlatform is above existing
        //             if (newPlatformRect.y + newPlatformRect.height + minVerticalSpacing > existingPlatformRect.y) {
        //                 validPlacement = false;
        //                 break;
        //             }
        //         } else { // existingPlatform is above or at the same level as newPlatform
        //             if (existingPlatformRect.y + existingPlatformRect.height + minVerticalSpacing > newPlatformRect.y) {
        //                 validPlacement = false;
        //                 break;
        //             }
        //         }
        //         // Optional: Implement a minimum horizontal spacing if desired
        //         int minHorizontalSpacing = 5; // Adjust this value as needed
        //         if (newPlatformRect.y == existingPlatformRect.y) { // Only check horizontal spacing if at the same vertical level
        //             if (newPlatformRect.x < existingPlatformRect.x) {
        //                 if (newPlatformRect.x + newPlatformRect.width + minHorizontalSpacing > existingPlatformRect.x) {
        //                     validPlacement = false;
        //                     break;
        //                 }
        //             } else {
        //                 if (existingPlatformRect.x + existingPlatformRect.width + minHorizontalSpacing > newPlatformRect.x) {
        //                     validPlacement = false;
        //                     break;
        //                 }
        //             }
        //         }
        //     }
        //     System.out.println("size: " + platforms.size());
        //     if (validPlacement) {
        //         platforms.add(tempPlatform);
        //         newestPlatform = tempPlatform;
        //         System.out.println("New platform at Y: " + newPlatformY);
        //         // Chance to spawn a power-up on the new platform
                
        //         if (random.nextDouble() < 0.4) {
        //             spawnPowerUp(platforms.getLast());
        //         }
        //         // powerUpSpawnCounter++;
        //         // boolean forceSpawn = powerUpSpawnCounter >= POWERUP_SPAWN_INTERVAL;
        //         // boolean normalSpawn = random.nextDouble() < POWERUP_SPAWN_CHANCE;
                
        //         // if (forceSpawn || normalSpawn) {
        //         //     spawnPowerUp(platforms.getLast());
        //         //     powerUpSpawnCounter = 0; // Reset counter after spawn
        //         // }
        //         // Every 3 platforms OR 80% chance, spawn a power-up
                
        //     }
            
        //     // if (platforms.size() <= 4) {
        //     //     spawnPowerUp(platforms.get(platforms.size()-1));
        //     // }
        //     // // Regular spawning logic after
        //     // else {
        //     //     // Only increment counter if no power-up exists
        //     //     if (powerups.isEmpty()) {
        //     //         powerUpSpawnCounter++;
        //     //     }
                
        //     //     boolean shouldSpawn = powerUpSpawnCounter >= POWERUP_SPAWN_INTERVAL || 
        //     //                          (powerups.isEmpty() && random.nextDouble() < POWERUP_SPAWN_CHANCE);
                
        //     //     if (shouldSpawn && (powerups.isEmpty() || 
        //     //         powerups.getLast().getX() < scrWidth/2)) {
        //     //         spawnPowerUp(platforms.get(1));
        //     //         powerUpSpawnCounter = 0;
        //     //     }
        //     // }
            
            
        //     platformDelayCounter = 0; // Reset the delay
        // }
        // platformDelayCounter++;

        // Enemy spawning logic based on time
        enemySpawnTimer += 50; // Assuming 50ms per update (based on Thread.sleep)
        //can make spawn time a changeable variable for settings 
        if (enemySpawnTimer >= enemySpawnInterval) {
            spawnRandomEnemy();
            enemySpawnTimer = 0; // Reset the timer
        }

        // startTime = startTime + 1;
        // if (startTime >= 50){
        //     mapcount = mapcount + 1;
        //     if (mapcount > 5){
        //         mapcount = 1;
        //     }
        //     mapFile = "maps/map"+mapcount+".txt";
        //     // mapFile = "maps/map3.txt";
        //     try {
        //         //Load Tile Map
        //         System.out.println("Loading map:" + mapFile);
        //         tileMap = tileMapManager.loadMap(mapFile);
    
        //     } catch (IOException e) {
        //         System.err.println("Error loading map:" +  e.getMessage());
        //         e.printStackTrace();
        //     }
        //     startTime = 0;
        
        // }
        // System.out.println("Game:" + startTime);
        
    }

    public void run() {
        try {
			isRunning = true;
			while (isRunning) {
				if (!isPaused)
					gameUpdate();
				gameRender();
				Thread.sleep (50);	
			}
		}
		catch(InterruptedException e) {}
    }

    private void spawnRandomEnemy() {
        Random random = new Random();
        int enemyType = random.nextInt(4); // Generates a random number between 0 and 5 (inclusive)
        // int spawnY = random.nextInt(400) + 100; // Spawn Y within a certain range
        // int spawnX = scrWidth + 50; // Spawn off-screen to the right
        int spawnX = 700;
        int spawnY = 350;
        System.out.println("scrWidth: " + scrWidth);
        enemies.add(new Enemy(spawnX, spawnY, enemyType, this, player));
        System.out.println("Spawning enemy of type: " + enemyType + " at X: " + spawnX + ", Y: " + spawnY);
    }

    private void spawnPowerUp(PlatformGen platform) {
        double powerUpType = random.nextDouble();
        PowerUp newPowerUp = null;
        int powerUpX = platform.getX() + platform.getWidth() / 2 - 11; // Center on platform (assuming power-up width is 32)
        int powerUpY = platform.getY() - 21; // Place above platform (assuming power-up height is 32)
    
        if (powerUpType < 0.7) {
            newPowerUp = new BluePillPowerUp("test", 1,this,powerUpX, powerUpY);
            newPowerUp.setPlayer(player);
        } 
        // else if (powerUpType < 0.7) { // 25% chance for Red Pill (0.25 to 0.5)
        //     newPowerUp = new RedPillPowerUp("test", 1, this, powerUpX, powerUpY);
            // newPowerUp.setPlayer(player);
        // } 
        else if (powerUpType < 0.9) { // 25% chance for Green Pill (0.5 to 0.75)
            newPowerUp = new BlueShieldPowerUp("test", 1, this, powerUpX, powerUpY);
            newPowerUp.setPlayer(player);
        } 
        // else { // 25% chance for Yellow Pill (0.75 to 1.0)
        //     newPowerUp = new YellowBoltPowerUp("test", 1, this, powerUpX, powerUpY);
        // newPowerUp.setPlayer(player);
        // }
        System.out.println("PowerUp X: " + powerUpX + ", Y: " + powerUpY);
        if (newPowerUp != null) {
            powerups.add(newPowerUp);
        }
    }
    private void checkPowerUpCollisions() {
        List<PowerUp> collectedPowerUps = new LinkedList<>();
        Rectangle2D.Double playerRect = player.getBoundingRectangle();
        for (PowerUp powerUp : powerups) {
            Rectangle2D.Double powerUpRect = powerUp.getBoundingRectangle();
            if (playerRect.intersects(powerUpRect)) {
                // Apply the effect of the power-up
                // powerUp.applyEffect(player); // Assuming you have this method in your PowerUp class
                powerUp.applyEffect();
                collectedPowerUps.add(powerUp);
                System.out.println("Player collected a " + powerUp.getClass().getSimpleName());
            }
        }
        powerups.removeAll(collectedPowerUps); // Remove collected power-ups
    }

    public List<Enemy> getEnemies(){
        return this.enemies;
    }



    public void startGame() {
        if(isRunning)
            return;

        isRunning = false;
        isPaused = false; 

        createGameEntities();
        player.debugJump();


        soundManager.playClip("forest-background", true);
        soundManager.setVolume("forest-background",0.7f);

        soundManager.playClip("forest-background", true);
        soundManager.setVolume("forest-background",0.7f);

        gameThread = new Thread(this);
        gameThread.start();

        requestFocusInWindow();
    }


    //Key Listener
    @Override
    public void keyTyped(KeyEvent e) {}
    

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        switch(key) {
            case KeyEvent.VK_LEFT:
                player.movement(4);  // Move left
            break;
            
            case KeyEvent.VK_RIGHT:
                player.movement(6);  // Move right
            break;
            
            case KeyEvent.VK_UP:
                player.movement(8);  // Jump
            break;
            
            case KeyEvent.VK_DOWN:
                player.movement(5);  // Duck
            break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        player.movement(0);
    }
    
}