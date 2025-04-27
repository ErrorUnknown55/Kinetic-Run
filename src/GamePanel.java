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

    //Variables
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
    private int platformSpawnDelay;

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

    // Timer variables
    private long startGameTime;  
    private long pausedTime;     
    private long totalPausedTime = 0;
    private boolean timerRunning = false;

    public GamePanel(int scrW, int scrH) {
        this.scrWidth = scrW;
        this.scrHeight = scrH;

        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();

        soundManager = SoundManager.getInstance();
            
        startTime = 0;
        mapcount = 1;

        platforms = new LinkedList<>();
        pf = new PlatformGen(this,450, "large");
        pf2 = new PlatformGen(this, 300, "medium");
        pf3 = new PlatformGen(this, 200, "small");

        player = new Player(100, 545, platforms);

        platforms.add(pf);
        lastPlatformY = 450;
        platforms.add(pf2);

        powerups = new LinkedList<>();
        
        platformSpawnDelay=80;
        minVerticalSpacing = 15;
        targetVerticalDifference = 12;
        verticalVariance = 3;
        targetJumpSpace = 12;
        jumpSpaceVariance = 3;

        flyingEnemy = new Enemy(700, 350, 2, this, player);

        image = new BufferedImage(900, 700, BufferedImage.TYPE_INT_RGB);

        //Tile map manager
        tileMapManager = new TileMapManager(this);

        gameFont = new Font("Arial", Font.BOLD, 24);  
    }

    public void createGameEntities() {
        bgImage = new Background(this, "images/background/backgroundColorForest.png", 98);
        bgImage.setY(0);
        
        platform = new Background(this, "images/platform/greentiles/ground-platform.png", 98);
        platform.setY(545+player.getHeight());
    }
    
    public void gameRender() {
        Graphics2D imageContext = (Graphics2D) image.getGraphics();

        imageContext.setBackground(Color.BLACK);
        imageContext.fillRect(0, 0, image.getWidth(), image.getHeight());
        
        if(bgImage != null) {
            bgImage.update();
            bgImage.draw(imageContext);
        }

        if(platform != null){
            platform.update();
            platform.draw(imageContext);
        }

        if(tileMap != null) {
            tileMap.draw(imageContext);
        }

        player.draw(imageContext);

        for (PlatformGen platform : platforms) {
            platform.drawPlatforms(imageContext);
        }

        for (PowerUp pu : powerups) {
            pu.draw(imageContext);
        }
        
        flyingEnemy.draw(imageContext);
        
        // Draw timer
        imageContext.setFont(gameFont);
        imageContext.setColor(Color.WHITE);
        String timeText = "Time: " + formatTime(getElapsedTime());
        imageContext.drawString(timeText, scrWidth - 150, 30);
        
        Graphics2D g2 = (Graphics2D) getGraphics();
        g2.drawImage(image, 0, 0, scrWidth, scrHeight, null);
        
        imageContext.dispose();
        g2.dispose();              
    }

    private void checkPlatformCollisions() {
        player.setIsOnGround(false);
        
        for (PlatformGen platform : platforms) {
            if (player.getBoundingRectangle().intersects(platform.getBoundingRectangle())) {
                Rectangle2D.Double playerRect = player.getBoundingRectangle();
                Rectangle2D.Double platformRect = platform.getBoundingRectangle();
                
                if (playerRect.y + playerRect.height >= platformRect.y && 
                    playerRect.y + playerRect.height <= platformRect.y + 10 && 
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
        
        List<Projectile> enemyProjectiles = flyingEnemy.getProjectiles();
        List<Projectile> hitProjectiles = new LinkedList<>();
        for (Projectile projectile : enemyProjectiles) {
            if (projectile.getBoundingRectangle().intersects(player.getBoundingRectangle())) {
                System.out.println("Player hit by projectile!");
                hitProjectiles.add(projectile);
            }
        }
        enemyProjectiles.removeAll(hitProjectiles);
        
        if(bgImage != null)
            bgImage.setAutoScroll(isRunning);
        
        if(platform != null){
            platform.setAutoScroll(isRunning);
        }

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
            powerUp.update();
            if (powerUp.getX() + powerUp.getWidth() < 0) {
                powerUpsToRemove.add(powerUp);
            }
        }
        powerups.removeAll(powerUpsToRemove);
        
        checkPlatformCollisions();
        checkPowerUpCollisions();
        player.update();
        
        if (platformDelayCounter >= platformSpawnDelay) {
            int newPlatformY = 0;
            if (platforms.isEmpty()) {
                newPlatformY = 450;
                tempPlatform = new PlatformGen(this, newPlatformY, "large");
            } else {
                int lastY = platforms.getLast().getY();
                int lastHeight = platforms.getLast().getHeight();
                int minNewY = lastY - targetJumpSpace - jumpSpaceVariance - lastHeight;
                int maxNewY = lastY - targetJumpSpace + jumpSpaceVariance - lastHeight;
                newPlatformY = Math.max(120, random.nextInt(maxNewY - minNewY + 1) + minNewY);
                String[] sizes = {"small", "medium", "large"};
                String newSize = sizes[random.nextInt(sizes.length)];
                tempPlatform = new PlatformGen(this, newPlatformY, newSize);
            }
            boolean validPlacement = true;
            Rectangle2D.Double newPlatformRect = tempPlatform.checkPlatformIntersect();
            for (PlatformGen existingPlatform : platforms) {
                Rectangle2D.Double existingPlatformRect = existingPlatform.checkPlatformIntersect();
                if (newPlatformRect.intersects(existingPlatformRect)) {
                    validPlacement = false;
                    break;
                }
                if (newPlatformRect.y < existingPlatformRect.y) {
                    if (newPlatformRect.y + newPlatformRect.height + minVerticalSpacing > existingPlatformRect.y) {
                        validPlacement = false;
                        break;
                    }
                } else {
                    if (existingPlatformRect.y + existingPlatformRect.height + minVerticalSpacing > newPlatformRect.y) {
                        validPlacement = false;
                        break;
                    }
                }
                int minHorizontalSpacing = 5;
                if (newPlatformRect.y == existingPlatformRect.y) {
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
                if (random.nextDouble() < 0.9) {
                    spawnPowerUp(platforms.getLast());
                }
            }
            platformDelayCounter = 0;
        }
        platformDelayCounter++;
    }

    public void run() {
        try {
            isRunning = true;
            while (isRunning) {
                if (!isPaused) {
                    gameUpdate();
                }
                gameRender();
                Thread.sleep(50);    
            }
        }
        catch(InterruptedException e) {}
    }

    private void spawnPowerUp(PlatformGen platform) {
        double powerUpType = random.nextDouble();
        PowerUp newPowerUp = null;
        int powerUpX = platform.getX() + platform.getWidth() / 2 - 11;
        int powerUpY = platform.getY() - 21;
    
        if (powerUpType < 0.25) {
            newPowerUp = new BluePillPowerUp("test", 1, this, powerUpX, powerUpY);
        } else if (powerUpType < 0.5) {
            newPowerUp = new RedPillPowerUp("test", 1, this, powerUpX, powerUpY);
        } else if (powerUpType < 0.75) {
            newPowerUp = new BlueShieldPowerUp("test", 1, this, powerUpX, powerUpY);
        } else {
            newPowerUp = new YellowBoltPowerUp("test", 1, this, powerUpX, powerUpY);
        }
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
                collectedPowerUps.add(powerUp);
                System.out.println("Player collected a " + powerUp.getClass().getSimpleName());
            }
        }
        powerups.removeAll(collectedPowerUps);
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

    public void startGame() {
        if(isRunning)
            return;

        isRunning = false;
        isPaused = false; 

        createGameEntities();
        startTimer();

        soundManager.playClip("forest-background", true);
        soundManager.setVolume("forest-background",0.7f);

        gameThread = new Thread(this);
        gameThread.start();

        requestFocusInWindow();
    }

    public void pauseGame() {
        isPaused = true;
        pauseTimer();
    }

    public void resumeGame() {
        isPaused = false;
        startTimer();
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        switch(key) {
            case KeyEvent.VK_LEFT:
                player.movement(4);
            break;
            
            case KeyEvent.VK_RIGHT:
                player.movement(6);
            break;
            
            case KeyEvent.VK_UP:
                player.movement(8);
            break;
            
            case KeyEvent.VK_DOWN:
                player.movement(5);
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

    @Override
    public void keyReleased(KeyEvent e) {
        player.movement(0);
    }
}