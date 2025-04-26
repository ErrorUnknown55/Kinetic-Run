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


        player = new Player(100, 545, platforms);

        platforms.add(pf);
        lastPlatformY = 450;
        // platforms.add(pf2);
        platforms.add(pf2);
        // lastPlatformY = 200;

        powerups = new LinkedList<>();  //initialize powerups list
        
        platformSpawnDelay=80;  //adjust to generate platforms slower
        minVerticalSpacing = 15;
        targetVerticalDifference = 12;
        verticalVariance = 3;
        targetJumpSpace = 12;
        jumpSpaceVariance = 3;

        flyingEnemy = new Enemy(700, 350, 2, this, player);


    
        
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
        flyingEnemy.draw(imageContext);

        
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



    public void gameUpdate() {

        PlatformGen tempPlatform;
        flyingEnemy.update();
        // Check for collisions between enemy projectiles and the player
        List<Projectile> enemyProjectiles = flyingEnemy.getProjectiles();
        List<Projectile> hitProjectiles = new LinkedList<>();
        for (Projectile projectile : enemyProjectiles) {
            if (projectile.getBoundingRectangle().intersects(player.getBoundingRectangle())) {
                // Handle player being hit (e.g., decrease health, game over)
                System.out.println("Player hit by projectile!");
                hitProjectiles.add(projectile); // Mark for removal
            }
        }
        enemyProjectiles.removeAll(hitProjectiles); // Remove hit projectiles
        if(bgImage != null)
            bgImage.setAutoScroll(isRunning);
        
        if(platform != null){
            platform.setAutoScroll(isRunning);
        }
        // pf.move();
        // pf2.move();
        // pf3.move();
        List<PlatformGen> platformsToRemove = new LinkedList<>();
        for (PlatformGen platform : platforms) {
            platform.move();
            if (platform.getX() + platform.getWidth() < 0) {
                platformsToRemove.add(platform);
            }
        }
        platforms.removeAll(platformsToRemove);

        List<PowerUp> powerUpsToRemove = new LinkedList<>();
        for (PowerUp powerUp : powerups) {
            powerUp.update(); // If your PowerUp class has an update method
            if (powerUp.getX() + powerUp.getWidth() < 0) {
                powerUpsToRemove.add(powerUp);
            }
        }
        powerups.removeAll(powerUpsToRemove);
        //Check collisions
        checkPlatformCollisions();
        checkPowerUpCollisions();
        player.update();
        System.out.println("platformDelayCounter: " + platformDelayCounter);
        // Control platform generation with a delay
        if (platformDelayCounter >= platformSpawnDelay) {
            int newPlatformY = 0; // Initialize
            if (platforms.isEmpty()) {
                // Spawn the first platform at a fixed Y
                newPlatformY = 450;
                tempPlatform = new PlatformGen(this, newPlatformY, "large");
            } else {
                // Get the Y coordinate of the last platform
                int lastY = platforms.getLast().getY();
                int lastHeight = platforms.getLast().getHeight();
                int minNewY = lastY - targetJumpSpace - jumpSpaceVariance - lastHeight;
                int maxNewY = lastY - targetJumpSpace + jumpSpaceVariance - lastHeight;
                // Ensure the new platform doesn't go too high
                newPlatformY = Math.max(120, random.nextInt(maxNewY - minNewY + 1) + minNewY);
                // Randomly choose a size for the new platform
                String[] sizes = {"small", "medium", "large"};
                String newSize = sizes[random.nextInt(sizes.length)];
                tempPlatform = new PlatformGen(this, newPlatformY, newSize);
            }
            boolean validPlacement = true;
            Rectangle2D.Double newPlatformRect = tempPlatform.checkPlatformIntersect();
            for (PlatformGen existingPlatform : platforms) {
                Rectangle2D.Double existingPlatformRect = existingPlatform.checkPlatformIntersect();
                // Check for direct overlap (both horizontal and vertical)
                if (newPlatformRect.intersects(existingPlatformRect)) {
                    validPlacement = false;
                    break;
                }
                // Check for minimum vertical spacing
                if (newPlatformRect.y < existingPlatformRect.y) { // newPlatform is above existing
                    if (newPlatformRect.y + newPlatformRect.height + minVerticalSpacing > existingPlatformRect.y) {
                        validPlacement = false;
                        break;
                    }
                } else { // existingPlatform is above or at the same level as newPlatform
                    if (existingPlatformRect.y + existingPlatformRect.height + minVerticalSpacing > newPlatformRect.y) {
                        validPlacement = false;
                        break;
                    }
                }
                // Optional: Implement a minimum horizontal spacing if desired
                int minHorizontalSpacing = 5; // Adjust this value as needed
                if (newPlatformRect.y == existingPlatformRect.y) { // Only check horizontal spacing if at the same vertical level
                    if (newPlatformRect.x < existingPlatformRect.x) {
                        if (newPlatformRect.x + newPlatformRect.width + minHorizontalSpacing > existingPlatformRect.x) {
                            validPlacement = false;
                            break;
                        }
                    } else {
                        if (existingPlatformRect.x + existingPlatformRect.width + minHorizontalSpacing > newPlatformRect.x) {
                            validPlacement = false;
                            break;
                        }
                    }
                }
            }
            if (validPlacement) {
                platforms.add(tempPlatform);
                System.out.println("New platform at Y: " + newPlatformY);
                // Chance to spawn a power-up on the new platform
                
                if (random.nextDouble() < 0.9) {
                    spawnPowerUp(platforms.getLast());
                }
            }
            platformDelayCounter = 0; // Reset the delay
        }
        platformDelayCounter++;

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

    private void spawnPowerUp(PlatformGen platform) {
        double powerUpType = random.nextDouble();
        PowerUp newPowerUp = null;
        int powerUpX = platform.getX() + platform.getWidth() / 2 - 11; // Center on platform (assuming power-up width is 32)
        int powerUpY = platform.getY() - 21; // Place above platform (assuming power-up height is 32)
    
        if (powerUpType < 0.25) {
            newPowerUp = new BluePillPowerUp("test", 1,this,powerUpX, powerUpY);
        } else if (powerUpType < 0.5) { // 25% chance for Red Pill (0.25 to 0.5)
            newPowerUp = new RedPillPowerUp("test", 1, this, powerUpX, powerUpY);
        } else if (powerUpType < 0.75) { // 25% chance for Green Pill (0.5 to 0.75)
            newPowerUp = new BlueShieldPowerUp("test", 1, this, powerUpX, powerUpY);
        } else { // 25% chance for Yellow Pill (0.75 to 1.0)
            newPowerUp = new YellowBoltPowerUp("test", 1, this, powerUpX, powerUpY);
        }
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
                collectedPowerUps.add(powerUp);
                System.out.println("Player collected a " + powerUp.getClass().getSimpleName());
            }
        }
        powerups.removeAll(collectedPowerUps); // Remove collected power-ups
    }





    public void startGame() {
        if(isRunning)
            return;

        isRunning = false;
        isPaused = false; 

        createGameEntities();

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