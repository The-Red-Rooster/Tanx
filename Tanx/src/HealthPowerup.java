import jig.ConvexPolygon;
import jig.ResourceManager;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class HealthPowerup extends Powerup{
  private int amount;
  public HealthPowerup(final float x, final float y, int a){
    super(x,y);
    amount = a;
    setSprite(ResourceManager.getImage(Tanx.HEALTH_POWERUP_SPRITE));
  }

  @Override
  public void usePowerup(Tank t) {
    super.usePowerup(t);
    t.giveHealth(amount);
  }

  @Override
  public Powerup copy() {
    return new HealthPowerup(getX(), getY(), amount);
  }
}
