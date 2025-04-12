import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.*;
import java.io.IOException;
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
    private String mapFile = "maps/map1.txt";

    //Veriables
    private int scrWidth;
    private int scrHeight;
    private boolean isRunning;
    private boolean isPaused;

    //Font
    private Font gameFont;

    public GamePanel(int scrW,  int scrH) {

        this.scrWidth =  scrW;
        this.scrHeight = scrH;
        
        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();
    
        
        image = new BufferedImage(900, 700, BufferedImage.TYPE_INT_RGB);

        //Tile map manager
        tileMapManager =  new TileMapManager(this);

        gameFont = new  Font("Arial", Font.BOLD, 24);  

    }

    public void createGameEntities() {
        
        bgImage = new Background(this, "images/background/backgroundColorForest.png", 98);

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
        
        Graphics2D g2 = (Graphics2D) getGraphics();
        g2.drawImage(image, 0, 0, scrWidth, scrHeight, null);
        
        imageContext.dispose();
        g2.dispose();        
    }

    public void gameUpdate() {

        if(bgImage != null)
            bgImage.setAutoScroll(isRunning);
        
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'keyPressed'");
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'keyReleased'");
    }
    
}