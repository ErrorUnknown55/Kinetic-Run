import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.JPanel;

public class TileMap {

    private static final int TILE_SIZE = 60;
    private Image[][] tiles;
    private int screenWidth, screenHeight;
    private int mapWidth, mapHeight;
    private int offsetY;
    private JPanel panel;

    public TileMap(JPanel panel, int width, int height) {
        this.panel = panel;
        Dimension dimension = panel.getSize();
        screenWidth = dimension.width;
        screenHeight = dimension.height;
        mapWidth = width;
        mapHeight = height;
        offsetY = screenHeight - tilesToPixels(mapHeight);
        tiles = new Image[mapWidth][mapHeight];
        System.out.println("tile map created");
    }

    public int getWidthPixels() {
        return tilesToPixels(mapWidth);
    }

    public int getWidth() {
        return mapWidth;
    }

    public int getHeight() {
        return mapHeight;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public Image getTile(int x, int y) {
        if (x < 0 || x >= mapWidth || y < 0 || y >= mapHeight) {
            return null;
        }
        return tiles[x][y];
    }

    public void setTile(int x, int y, Image tile) {
        tiles[x][y] = tile;
    }

    public static int pixelsToTiles(float pixels) {
        return pixelsToTiles(Math.round(pixels));
    }

    public static int pixelsToTiles(int pixels) {
        return (int)Math.floor((float)pixels / TILE_SIZE);
    }

    public static int tilesToPixels(int numTiles) {
        return numTiles * TILE_SIZE;
    }

    public void draw(Graphics2D g2) {
        // Get map dimensions in pixels
        int mapPixelWidth = tilesToPixels(mapWidth);
        int mapPixelHeight = tilesToPixels(mapHeight);
        
        // Calculate proper centering offsets
        int offsetX = (panel.getWidth() - mapPixelWidth) / 2;
        int offsetY = (panel.getHeight() - mapPixelHeight) / 2;
        
        // Ensure offsets are never negative
        offsetX = Math.max(0, offsetX);
        offsetY = Math.max(0, offsetY);
        
        // Draw all tiles
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                Image tile = getTile(x, y);
                if (tile != null) {
                    int drawX = offsetX + tilesToPixels(x);
                    int drawY = offsetY + tilesToPixels(y);
                    
                    g2.drawImage(tile, drawX, drawY, TILE_SIZE, TILE_SIZE, null);
                    
                    // Debug output
                    //System.out.printf("Fixed - Drawing tile at %d,%d (screen: %d,%d)%n",x, y, drawX, drawY);
                }
            }
        }
    }
    
}