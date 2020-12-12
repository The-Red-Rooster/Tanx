import jig.ResourceManager;
import org.newdawn.slick.Graphics;

public class AmmoPowerup extends Powerup{

  private int type;
  private int amount;

  public AmmoPowerup(final float x, final float y, int cannonType, int num){
    super(x,y);
    type = cannonType;
    amount = num;
    setSprite(ResourceManager.getImage(Tanx.AMMO_POWERUP_SPRITE));
  }

  @Override
  public void usePowerup(Tank t) {
    super.usePowerup(t);
    t.getMyPlayer().giveAmmo(type , amount);
  }

  @Override
  public Powerup copy() {
    return new AmmoPowerup(getX(), getY(), type, amount);
  }
}
