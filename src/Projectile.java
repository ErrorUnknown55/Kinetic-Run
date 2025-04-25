import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;

public class Projectile implements Motion {

   	private static final int XSIZE = 10;
   	private static final int YSIZE = 10;

   	private JPanel panel;
	private Player player;
   	private int x = 0;
   	private int y = 200;
   	private int dx = 5;
   	private int dy = 2;
	private int xPos, yPos;			// location from which to generate projectile
   	private int initialVelocityX = 25;
   	private int initialVelocityY = 25;
    private boolean movingRight;

   	private Dimension dimension;
    

   	double timeElapsed;
	boolean active;

	public Projectile (JPanel p, Player player) {
      	panel = p;
		this.player = player;
		active = false;
		// timeElapsed = 0;
      	dimension = panel.getSize();
		xPos = 25;
		yPos = 425 - YSIZE;
	}


	public boolean isActive() {
		return active;
	}


	public void activate() {
		// xPos = player.getX();
		// yPos = player.getY();
		// active = true;
		// // timeElapsed = 0;
        // movingRight = (player.getX() > xPos);
        active = true;
        int targetX = player.getX() + player.getWidth() / 2; // Aim for the center of the player
        int targetY = player.getY() + player.getHeight() / 2;
        int deltaX = targetX - xPos;
        int deltaY = targetY - yPos;

        // Calculate a normalized direction vector
        double magnitude = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (magnitude > 0) {
            dx = (int) (5 * deltaX / magnitude); // Adjust speed multiplier as needed
            dy = (int) (5 * deltaY / magnitude);
        } else {
            // Player is at the same position, avoid division by zero
            dx = 5; // Or some default direction
            dy = 0;
        }
	}
	

	public void deActivate() {
		active = false;
	}

    public void setXPos(int x){
        this.xPos = x;
        this.x = x;
    }
    public void setYPos(int y){
        this.yPos=y;
    }

    @Override
    public void update() {
        if (!panel.isVisible() || !active) return;
        x += dx;
        y += dy;
        // if (movingRight) {
        //     x += dx;
        // } else {
        //     x -= dx;
        // }

        // Basic projectile lifespan - remove if it goes off-screen
        if (x < -XSIZE || x > dimension.width || y < -YSIZE || y > dimension.height) {
            active = false;
        }
    }

   	public void update_old () {  

		int oldX, oldY;

      	if (!panel.isVisible ()) return;

		oldX = x;
		oldY = y;
     
		timeElapsed = timeElapsed + 0.5;

      		x = (int) (initialVelocityX * timeElapsed);
      		y = (int) (initialVelocityY * timeElapsed - 4.9 * timeElapsed * timeElapsed);

      		if (y > 0)
	 		y = yPos - y;			// yPos is the height at which ball is thrown
      		else
         		y = yPos + y * -1;

         	if (x < -XSIZE || x > dimension.width) {		// outside left and right boundaries
	    		active = false;
			return;
		}

         	if (y > 425 - YSIZE) {	 		// below floor; extrapolate to bring ball on top of floor.
	    		System.out.println ("Going below floor: Y = " + y);
			int yDistance = y - oldY;
			int xDistance = x - oldX;
			y = 425 - YSIZE;		// re-position so that ball is on top of floor
			int aboveGround = 425 - (oldY + YSIZE);
			x = oldX + (int) (aboveGround * 1.0 / yDistance) * xDistance; 

            		active = false;
		}
/*
         	if (y > 425 - YSIZE) {	 		// below floor; extrapolate to bring ball on top of floor.
	    		System.out.println ("Y = " + y);
	    		int amountOver = y - (425 - YSIZE); 
	    		y = 425 - YSIZE;
	    		System.out.println ("New Y = " + y);
            		double fractionOver = (amountOver * 1.0) / (y - oldY);
	    		timeElapsed = timeElapsed - (1 - fractionOver) * 0.5;
	    		x = (int) (initialVelocityX * timeElapsed);
            		active = false;
		}
*/
         }
	

   	public void draw (Graphics2D g2) { 
        if (active) {
            g2.setColor(Color.RED); // Choose projectile color
            g2.fill(new Ellipse2D.Double(x, y, XSIZE, YSIZE));
        }
/*
		if (!active)
			return;
*/
      		// g2.setColor (Color.BLUE);
            // g2.fill (new Ellipse2D.Double (x+xPos, y, XSIZE, YSIZE));
		// if (player.getDirection() == 2)		// going right: add x to xPos
      			
		// else					// going left: subtract x from xPos
      	// 		g2.fill (new Ellipse2D.Double (xPos - x, y, XSIZE, YSIZE));

   	}
    public Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(x, y, XSIZE, YSIZE);
    }

}