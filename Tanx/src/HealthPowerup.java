import jig.ConvexPolygon;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class HealthPowerup extends Powerup{
  private int amount;
  public HealthPowerup(final float x, final float y, int a){
    super(x,y);
    amount = a;
  }

  @Override
  public void usePowerup(Tank t) {
    super.usePowerup(t);
    t.giveHealth(amount);
  }
  @Override
  protected void setSprite() {
    this.addShape(new ConvexPolygon(20f, 20f), Color.green, Color.red);
  }

  @Override
  public void render(Graphics g) {
    super.render(g);
    g.drawString(Integer.toString(amount), getX()-10, getY()-10);
  }

  @Override
  public Powerup copy() {
    return new HealthPowerup(getX(), getY(), amount);
  }
}
