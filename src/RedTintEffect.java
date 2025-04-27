import java.awt.image.BufferedImage;
import java.awt.Color;

public class RedTintEffect implements ImageFX {

    private int tintAmount;

    public RedTintEffect(int amount) {
        this.tintAmount = amount;
    }

    @Override
    public BufferedImage apply(BufferedImage image) {
        if (image == null) {
            return null;
        }

        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage tintedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color color = new Color(image.getRGB(x, y), true);
                int red = color.getRed();
                int green = color.getGreen();
                int blue = color.getBlue();
                int alpha = color.getAlpha();

                // Apply a stronger red tint
                int newRed = Math.min(255, red + tintAmount);

                // Optionally, you could reduce the blue to make the red/purple more prominent
                int newBlue = Math.max(0, blue - (tintAmount / 2)); // Reduce blue slightly

                Color tintedColor = new Color(newRed, green, newBlue, alpha);
                tintedImage.setRGB(x, y, tintedColor.getRGB());
            }
        }
        return tintedImage;
    }
}