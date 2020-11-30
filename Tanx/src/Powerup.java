import jig.ConvexPolygon;
import jig.Vector;
import org.newdawn.slick.Color;


/*
Base Powerup class to be used as an interface that all powerups share.
 */
public class Powerup extends PhysicsEntity{
  public static Vector POWERUP_TERMINAL_VELOCITY = new Vector(2f, 2f);

  public Powerup(final float x, final float y){
    super(x, y, 0, POWERUP_TERMINAL_VELOCITY);
    setSprite();
  }

  protected void setSprite() {
    this.addShape(new ConvexPolygon(20f, 20f), Color.yellow, Color.red);
  }

  public void usePowerup(Tank t){
    isDead = true;
  }

  public Powerup copy(){
    return new Powerup(getX(), getY());
  }
}
