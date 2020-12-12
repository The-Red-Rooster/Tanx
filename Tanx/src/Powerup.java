import jig.ConvexPolygon;
import jig.ResourceManager;
import jig.Vector;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;


/*
Base Powerup class to be used as an interface that all powerups share.
 */
public class Powerup extends PhysicsEntity{
  public static Vector POWERUP_TERMINAL_VELOCITY = new Vector(2f, 2f);

  public Powerup(final float x, final float y){
    super(x, y, 0, POWERUP_TERMINAL_VELOCITY);
  }

  protected void setSprite(Image sprite) {
    addImageWithBoundingBox(sprite);
  }

  public void usePowerup(Tank t){
    isDead = true;
  }

  public Powerup copy(){
    return new Powerup(getX(), getY());
  }
}
