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
    private int platformDelayCounter = 0;
    private int platformSpawnDelay = 0; //suppose to control the speed at which th platforms generate
    private PlatformGen pf,pf2,pf3;
    private int lastPlatformY = -100;
    private int minVerticalSpacing = 15;
    private int targetVerticalDifference = 12;
    private int verticalVariance = 3;
    private int targetJumpSpace = 12;
    private int jumpSpaceVariance = 3;
    private int lastPlatformBottom;

    private Random random = new Random();

    public GamePanel(int scrW,  int scrH) {

        this.scrWidth =  scrW;
        this.scrHeight = scrH;
        
        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();
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

        //Check collisions
        checkPlatformCollisions();

        player.update();

        // Control platform generation with a delay
    if (platformDelayCounter >= platformSpawnDelay) {
        int newPlatformY = 0; // Initialize

        // *** MANUAL Y COORDINATE SELECTION LOGIC (THIS IS WHERE YOU'LL MODIFY) ***
        if (platforms.size() == 0) {
            newPlatformY = 450; // First platform at Y = 450
            tempPlatform = new PlatformGen(this, newPlatformY, "large");
        } else if (platforms.size() == 1) {
            newPlatformY = 330; // Second platform at Y = 300
            tempPlatform = new PlatformGen(this, newPlatformY, "medium");
        } else if (platforms.size() == 2) {
            newPlatformY = 270; // Third platform at Y = 200
            tempPlatform = new PlatformGen(this, newPlatformY, "small");
        } else {
            // After the initial manual placements, you might revert to some other logic
            // or stop generating new platforms. For this example, let's stop.
            platformDelayCounter = 0; // Prevent further generation
            return;
        }

        boolean validY = true; // Since we're manually setting it, assume it's valid initially
        boolean overlapping = false;
        // PlatformGen tempPlatform = new PlatformGen(this, newPlatformY);
        int newPlatformTop = newPlatformY;
        int newPlatformBottom = newPlatformY + tempPlatform.getHeight(); // ERROR HERE: Should be calculated

        for (PlatformGen existingPlatform : platforms) {
            // ... (overlap and closeness checks) ...

            // *** JUMP SPACE CHECK (THIS PART IS ALREADY SET UP) ***
            if (platforms.size() > 0) {
                int spaceBelowPrevious = lastPlatformBottom;
                int spaceAboveNew = newPlatformTop;
                int spaceDifference = Math.abs(spaceAboveNew - spaceBelowPrevious);

                if (spaceDifference < targetJumpSpace - jumpSpaceVariance || spaceDifference > targetJumpSpace + jumpSpaceVariance) {
                    overlapping = true;
                    validY = false; // Mark as invalid due to spacing
                    break;
                }
            }
        }

        if (!overlapping && validY) {
            if(newPlatformY == 450){
                platforms.add(new PlatformGen(this, newPlatformY, "large"));
            }
            if(newPlatformY == 330){
                platforms.add(new PlatformGen(this, newPlatformY, "medium"));
            }
            if(newPlatformY == 270){
                platforms.add(new PlatformGen(this, newPlatformY, "small"));
            }
            // platforms.add(new PlatformGen(this, newPlatformY));
            lastPlatformY = newPlatformTop; // Corrected: Use newPlatformTop
            lastPlatformBottom = newPlatformBottom;
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




    public void startGame() {
        if(isRunning)
            return;

        isRunning = false;
        isPaused = false; 

        createGameEntities();

        gameThread = new Thread(this);
        gameThread.start();

        requestFocusInWindow();
    }


    //Key Listener
    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'keyTyped'");
    }

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