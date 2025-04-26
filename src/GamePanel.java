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
    private int platformSpawnDelay = 240;
    private PlatformGen pf,pf2,pf3;
    private int lastPlatformY = -100;
    private int minVerticalSpacing = 20;

    private Random random = new Random();

    public GamePanel(int scrW,  int scrH) {

        this.scrWidth =  scrW;
        this.scrHeight = scrH;

        
        
        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();

        soundManager = SoundManager.getInstance();
        

        startTime = 0;
        mapcount = 1;


        platforms = new LinkedList<>();
        pf = new PlatformGen(this,450, null);
        pf2 = new PlatformGen(this, 300, null);
        pf3 = new PlatformGen(this, 200, null);

        player = new Player(100, 548, platforms);

        platforms.add(pf);
        lastPlatformY = 450;
        // platforms.add(pf2);
        platforms.add(pf3);
        lastPlatformY = 200;

    
        
        image = new BufferedImage(900, 700, BufferedImage.TYPE_INT_RGB);

        //Tile map manager
        tileMapManager =  new TileMapManager(this);

        gameFont = new  Font("Arial", Font.BOLD, 24);  

    }

    public void createGameEntities() {
        
        bgImage = new Background(this, "images/background/backgroundColorForest.png", 98);
        // mapFile = "maps/map"+mapcount+".txt";
        mapFile = "maps/map2.txt";
        try {
            //Load Tile Map
            tileMap = tileMapManager.loadMap(mapFile);
            
            

        } catch (IOException e) {
            System.err.println("Error loading map:" +  e.getMessage());
            e.printStackTrace();
        }
        

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

        if(bgImage != null)
            bgImage.setAutoScroll(isRunning);
        
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
            int newPlatformY;
            boolean validY = false;
            int attempts = 0;
            int maxAttempts = 100;

            while (!validY && attempts < maxAttempts) {
                int potentialY = random.nextInt(450) + 75;
                boolean overlapping = false;
                PlatformGen tempPlatform = new PlatformGen(this, potentialY, null);
                int newPlatformTop = potentialY;
                int newPlatformBottom = potentialY + tempPlatform.getHeight();

                for (PlatformGen existingPlatform : platforms) {
                    int existingPlatformTop = existingPlatform.getY();
                    int existingPlatformBottom = existingPlatform.getY() + existingPlatform.getHeight();

                    // Check for vertical overlap
                    if (newPlatformTop < existingPlatformBottom && newPlatformBottom > existingPlatformTop) {
                        overlapping = true;
                        break; // No need to check further if overlap is found
                    }

                    // Check for minimum vertical spacing
                    if (Math.abs(newPlatformTop - existingPlatformBottom) < minVerticalSpacing && newPlatformTop < existingPlatformBottom ||
                        Math.abs(newPlatformBottom - existingPlatformTop) < minVerticalSpacing && newPlatformBottom > existingPlatformTop) {
                        overlapping = true;
                        break;
                    }
                }

                if (!overlapping) {
                    newPlatformY = potentialY;
                    platforms.add(new PlatformGen(this, newPlatformY, null));
                    lastPlatformY = newPlatformY;
                    validY = true;
                }
                attempts++;
            }
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




    public void startGame() {
        if(isRunning)
            return;

        isRunning = false;
        isPaused = false; 

        createGameEntities();

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