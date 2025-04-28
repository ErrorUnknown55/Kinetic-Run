import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.io.IOException;
import java.util.ArrayList;
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
    private long enemySpawnInterval = 10000;

    private int powerUpSpawnCounter = 0;
    private final int POWERUP_SPAWN_INTERVAL = 2; // Every 2 platforms
    private final double POWERUP_SPAWN_CHANCE = 0.8; // 80% chance when eligible

    private int maxPlatformPlacementAttempts = 5;
    private int retryHorizontalReduction = 50;
    private int retryVerticalAdjustment = 30;


    //need to remove
    private Enemy flyingEnemy;

    // Timer variables
    private long startGameTime;  
    private long pausedTime;     
    private long totalPausedTime = 0;
    private boolean timerRunning = false;

    private int currentLevel = 1;
    private boolean levelChangeNeeded = false;
    private int score, levelCompletionXPosition;

    private boolean isWaterLevel = false;

    private boolean hasSwitchedToWater = false;

    private boolean ground;

    private List<Heart> hearts = new ArrayList<>();

    private boolean gameOver = false;
    private BufferedImage gameOverImage;
    private long gameOverTime; // To track when game over occurred
    private boolean showRestartPrompt = false;
    private final long PROMPT_DELAY = 2000; // 2 seconds before showing restart prompt

    private float enemySpawnMultiplier = 1.0f; // Starts at normal rate
    private final float ENEMY_SPAWN_INCREASE_RATE = 0.1f; // 10% increase per interval
    private long lastDifficultyIncreaseTime = 0;
    private final long DIFFICULTY_INTERVAL = 30000;



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
        gameOverImage = ImageManager.loadBufferedImage("images/player/GameOver.png");

    }

    public void setWaterLevel(boolean waterLevel) {
        this.isWaterLevel = waterLevel;
        
        if (waterLevel) {
            // Load sea background
            bgImage = new Background(this,"images/background/underwaterBackground-2.png", 98);
            bgImage.setY(0);
            // Notify player and enemies
            player.setWaterLevel(true);
            // Remove non-water enemies and convert existing ones
            List<Enemy> toRemove = new LinkedList<>();
            for (Enemy enemy : enemies) {
                if (enemy.getType() != 4) {
                    toRemove.add(enemy);
                } else {
                    enemy.setWaterLevel(true);
                }
            }
            enemies.removeAll(toRemove);
            platforms.clear();
            platform=null;

        } else {
            // Revert to normal visuals
            bgImage = new Background(this,"images/background/backgroundColorForest.png",98);
            bgImage.setY(0);
            player.setWaterLevel(false);
            for (Enemy enemy : enemies) {
                enemy.setWaterLevel(false);
            }
        }
    }
    public void endLevel() {
        currentLevel = currentLevel + 1;
        levelChangeNeeded = true;
        
        // Every 3 levels is a water level
    }

    public void createGameEntities() {
        
        bgImage = new Background(this, "images/background/backgroundColorForest.png", 98);
        bgImage.setY(0);
        platform = new Background(this, "images/platform/greentiles/ground-platform.png", 98);
        platform.setY(545+player.getHeight());

    }
    
    public void gameRender() {
        //Creates a Graphics2D obj for the BufferedImage
        Graphics2D imageContext = (Graphics2D) image.getGraphics();

        if (!gameOver){
            imageContext.setBackground(Color.BLACK);
            imageContext.fillRect(0, 0, image.getWidth(), image.getHeight());
            
            // Draw background - modified for water/land levels
            
            // Add water overlay effect for water levels
            if (isWaterLevel) {
                imageContext.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                imageContext.setColor(new Color(0, 100, 200, 50));
                imageContext.fillRect(0, 0, scrWidth, scrHeight);
                imageContext.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }
            
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

            for (Heart heart : hearts) {
                heart.draw(imageContext);
            }
            // flyingEnemy.draw(imageContext);

            // Draw timer
            imageContext.setFont(gameFont);
            imageContext.setColor(Color.BLACK);
            String timeText = "Time: " + formatTime(getElapsedTime());
            imageContext.drawString(timeText, scrWidth - 200, 30);
        }
        else{
            // Darken the game screen
                imageContext.setColor(new Color(0, 0, 0, 180));
                imageContext.fillRect(0, 0, 900, 700);
                
                // Draw game over image
                int goWidth = 400;
                int goHeight = 200;
                int goX = (scrWidth - goWidth) / 2;
                int goY = (scrHeight - goHeight) / 3;
                imageContext.drawImage(gameOverImage, goX, goY, goWidth, goHeight, null);
                
                // Show restart prompt after delay
                if (showRestartPrompt) {
                    imageContext.setFont(new Font("Arial", Font.BOLD, 24));
                    imageContext.setColor(Color.WHITE);
                    String prompt = "Press R to Restart or ESC to Quit";
                    int promptWidth = imageContext.getFontMetrics().stringWidth(prompt);
                    imageContext.drawString(prompt, (scrWidth - promptWidth)/2, goY + goHeight + 50);
                }
                
                // Show final score
                imageContext.setFont(new Font("Arial", Font.BOLD, 20));
                imageContext.setColor(Color.YELLOW);
                String scoreText = "Final Score: " + score;
                int scoreWidth = imageContext.getFontMetrics().stringWidth(scoreText);
                imageContext.drawString(scoreText, (scrWidth - scoreWidth)/2, goY + goHeight + 100);
            
        }

        
        Graphics2D g2 = (Graphics2D) getGraphics();
        g2.drawImage(image, 0, 0, scrWidth, scrHeight, null);
        
        imageContext.dispose();
        g2.dispose();  
        // startTime = 0;
              
    }

    
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
                    player.loseLife();
                    updateHearts();
                    System.out.println("Player hit! Lives: " + player.getLives());
                    System.out.println("Player collided with enemy - flashing!");

                    if (player.isDead()) {
                        // Handle game over
                        gameOver = true;
                    }
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

        if (gameOver) {
            // Show restart prompt after delay
            if (System.currentTimeMillis() - gameOverTime > PROMPT_DELAY) {
                showRestartPrompt = true;
            }
            return; // Skip normal game updates
        }

        if (levelChangeNeeded) {
            loadLevel(currentLevel);
            levelChangeNeeded = false;
            return; // Skip normal update for this frame
        }

        if (player.isDead()) {
            gameOver = true;
            // Handle game over logic
        }

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
                        player.loseLife();
                        updateHearts();
                        System.out.println("Player collided with enemy - flashing!");
                    }
                    System.out.println("Player hit by projectile!");
                    hitProjectiles.add(projectile); // Mark for removal
                }
            }
            enemyProjectiles.removeAll(hitProjectiles); // Remove hit projectiles from this enemy
        }

        checkEnemyCollisions(); // Check for player-enemy collisions
        checkForWaterLevelTransition();

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
                        
                        if (random.nextDouble() < 0.5) {
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
    
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDifficultyIncreaseTime > DIFFICULTY_INTERVAL) {
            increaseDifficulty();
            lastDifficultyIncreaseTime = currentTime;
        }
        // Enemy spawning logic based on time
        enemySpawnTimer += 50; // Assuming 50ms per update (based on Thread.sleep)
        //can make spawn time a changeable variable for settings 
        if (enemySpawnTimer >= (enemySpawnInterval / enemySpawnMultiplier)) {
            spawnRandomEnemy();
            enemySpawnTimer = 0; // Reset the timer
        }
        
        if (player.isDead() && !gameOver) {
            triggerGameOver();
        }
    }

    private void increaseDifficulty() {
        enemySpawnMultiplier += ENEMY_SPAWN_INCREASE_RATE;
        System.out.println("Difficulty increased! Spawn rate multiplier: " + enemySpawnMultiplier);
        
        
    }

    private void triggerGameOver() {
        gameOver = true;
        gameOverTime = System.currentTimeMillis();
        showRestartPrompt = false;
        soundManager.stopClip("forest-background");
        soundManager.stopClip("water_transition");
        soundManager.playClip("game_over", false);
        isRunning = false;
        System.out.println("tesing ");
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
        int enemyType;
        if (isWaterLevel) {
            // Only spawn water-specific enemies (types 0 and 4)
            enemyType = 4 + random.nextInt(4);
            int x,y;
            if (enemyType == 4){
                x = -50;
                y = random.nextInt(450) + 100;
                System.out.println("scrWidth: " + scrWidth);
                enemies.add(new Enemy(x, y, enemyType, this, player));
                System.out.println("Spawning enemy of type: " + enemyType + " at X: " + x + ", Y: " + y);
            }

            if (enemyType == 5){
                x = scrWidth + 50;
                y = random.nextInt(450) + 100;
                System.out.println("scrWidth: " + scrWidth);
                enemies.add(new Enemy(x, y, enemyType, this, player));
                System.out.println("Spawning enemy of type: " + enemyType + " at X: " + x + ", Y: " + y);
            }

            if (enemyType == 6){
                x = random.nextInt(700);
                y = scrHeight + 50;
                System.out.println("scrWidth: " + scrWidth);
                enemies.add(new Enemy(x, y, enemyType, this, player));
                System.out.println("Spawning enemy of type: " + enemyType + " at X: " + x + ", Y: " + y);
            }

            if (enemyType == 7){
                x = random.nextInt(700);
                y = 0;
                System.out.println("scrWidth: " + scrWidth);
                enemies.add(new Enemy(x, y, enemyType, this, player));
                System.out.println("Spawning enemy of type: " + enemyType + " at X: " + x + ", Y: " + y);
            }
            
            
        } else {
            // Spawn regular enemies (types 0-3 as per your current code)
            enemyType = random.nextInt(4); // Generates 0-3
            int spawnX = 700;
            int spawnY = 350;
            System.out.println("scrWidth: " + scrWidth);
            enemies.add(new Enemy(spawnX, spawnY, enemyType, this, player));
            System.out.println("Spawning enemy of type: " + enemyType + " at X: " + spawnX + ", Y: " + spawnY);
        }
        // int enemyType = random.nextInt(4); // Generates a random number between 0 and 5 (inclusive)
        // int spawnY = random.nextInt(400) + 100; // Spawn Y within a certain range
        // int spawnX = scrWidth + 50; // Spawn off-screen to the right
        // int spawnX = 700;
        // int spawnY = 350;
        // System.out.println("scrWidth: " + scrWidth);
        // enemies.add(new Enemy(spawnX, spawnY, enemyType, this, player));
        
    }

    private void spawnPowerUp(PlatformGen platform) {
        double powerUpType = random.nextDouble();
        PowerUp newPowerUp = null;
        int powerUpX = platform.getX() + platform.getWidth() / 2 - 11; // Center on platform (assuming power-up width is 32)
        int powerUpY = platform.getY() - 21; // Place above platform (assuming power-up height is 32)
    
        if (powerUpType < 0.5) {
            newPowerUp = new BlueShieldPowerUp("test", 1,this,powerUpX, powerUpY);
        } 
        else if (powerUpType < 0.7) { // 25% chance for Red Pill (0.25 to 0.5)
            newPowerUp = new RedPillPowerUp("test", 1, this, powerUpX, powerUpY);
        } 
        else if (powerUpType < 0.9) { // 25% chance for Green Pill (0.5 to 0.75)
            newPowerUp = new BluePillPowerUp("test", 1, this, powerUpX, powerUpY);
        } 
        else { // 25% chance for Yellow Pill (0.75 to 1.0)
            newPowerUp = new YellowBoltPowerUp("test", 1, this, powerUpX, powerUpY);
        }

        newPowerUp.setPlayer(player);
        System.out.println("PowerUp X: " + powerUpX + ", Y: " + powerUpY);
        if (newPowerUp != null) {
            powerups.add(newPowerUp);
        }
    }

    private void checkForWaterLevelTransition() {
        if (!hasSwitchedToWater && getElapsedTime() >= 20) {
            setWaterLevel(true);
            hasSwitchedToWater = true;
            
            // Optional: Play a transition sound
            soundManager.playClip("water_transition", false);
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

    // Timer methods
    private void startTimer() {
        if (!timerRunning) {
            if (totalPausedTime > 0) {
                startGameTime += (System.currentTimeMillis() - pausedTime);
                totalPausedTime = 0;
            } else {
                startGameTime = System.currentTimeMillis();
            }
            timerRunning = true;
        }
    }

    private void pauseTimer() {
        if (timerRunning) {
            pausedTime = System.currentTimeMillis();
            timerRunning = false;
            totalPausedTime += (pausedTime - startGameTime);
        }
    }

    private long getElapsedTime() {
        if (timerRunning) {
            return (System.currentTimeMillis() - startGameTime) / 1000;
        } else {
            return (pausedTime - startGameTime) / 1000;
        }
    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
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
        startTimer();
        initHearts();
        player.debugJump();


        soundManager.playClip("forest-background", true);
        soundManager.setVolume("forest-background",0.7f);

        soundManager.playClip("forest-background", true);
        soundManager.setVolume("forest-background",0.7f);

        gameThread = new Thread(this);
        gameThread.start();

        requestFocusInWindow();
    }

    private void loadLevel(int levelNumber) {
        try {
            // Clear existing entities
            platforms.clear();
            enemies.clear();
            powerups.clear();
            
            // Load level-specific configuration
            // loadPlatformsForLevel(levelNumber);
            // loadEnemiesForLevel(levelNumber);
            
            // Reset player position
            player.setPosition(100, 545);
            
            // Play level start sound
            soundManager.playClip("level_start", false);
        } catch (Exception e) {
            gameOver = true; // End game if level loading fails
        }
    }

    public void pauseGame() {
        isPaused = true;
        pauseTimer();
    }

    public void resumeGame() {
        isPaused = false;
        startTimer();
    }


    public void completeLevel() {
        currentLevel++;
        levelChangeNeeded = true;
        
        // Optional: Add score bonus for level completion
        score += 1000 * currentLevel;
    }

    public void checkLevelCompletion() {
        if (player.getX() > levelCompletionXPosition) {
            completeLevel();
        }
    }

    public void initHearts() {
        hearts.clear();
        int startX = scrWidth - 150; // Right side of screen
        int y = scrHeight - 40; // Bottom of screen
        
        for (int i = 0; i < player.getLives(); i++) {
            Heart heart = new Heart(this, startX - (i * 30), y);
            hearts.add(heart);
        }
    }
    
    public void updateHearts() {
        hearts.clear();
        int startX = scrWidth - 150;
        int y = scrHeight - 40;
        
        for (int i = 0; i < player.getLives(); i++) {
            Heart heart = new Heart(this, startX - (i * 30), y);
            hearts.add(heart);
        }
    }


    //Key Listener
    @Override
    public void keyTyped(KeyEvent e) {}
    

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (gameOver && showRestartPrompt) {
            if (e.getKeyCode() == KeyEvent.VK_R) {
                restartGame();
            } 
            else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                System.exit(0); // Or return to menu
            }
        }
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

            case KeyEvent.VK_P:
                if(isPaused) {
                    resumeGame();
                } else {
                    pauseGame();
                }
            break;
        }
    }

    private void restartGame() {
        // Reset game state
        gameOver = false;
        score = 0;
        currentLevel = 1;
        hasSwitchedToWater = false;
        
        // Clear all entities
        platforms.clear();
        enemies.clear();
        powerups.clear();
        
        // Reset player
        player = new Player(100, 545, platforms);
        initHearts();
        
        // Reset background
        setWaterLevel(false);
        
        // Restart game loop
        startGame();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        player.movement(0);
    }
    
}