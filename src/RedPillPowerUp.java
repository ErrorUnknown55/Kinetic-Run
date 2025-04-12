import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import javax.swing.JPanel;
import java.util.Random;
import java.awt.Image;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
public class RedPillPowerUp extends PowerUp {
    private String effect;
    private float duration;

    public RedPillPowerUp(String effect, float duration) {
        super(effect, duration);
        this.effect = effect;
        this.duration = duration;
    }

    @Override
    public void applyEffect() {
        // Implement the effect of the red pill power-up here
        // System.out.println("Applying red pill effect: " + effect + " for " + duration + " seconds.");
    }
    
}