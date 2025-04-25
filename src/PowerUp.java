import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

public abstract class PowerUp {
    private String effect;
    private float duration;

    public PowerUp(String effect, float duration) {
        this.effect = effect;
        this.duration = duration;
    }
    public abstract void applyEffect();
    public abstract void draw(Graphics g2d);
    public abstract void update();
    public abstract int getX();
    public abstract int getY();
    public abstract int getWidth();
    public abstract int getHeight();
    public abstract Rectangle2D.Double getBoundingRectangle();

}
