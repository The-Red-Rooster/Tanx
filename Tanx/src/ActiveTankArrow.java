import jig.Entity;
import jig.ResourceManager;
import jig.Vector;
import org.newdawn.slick.Graphics;

import java.awt.*;

public class ActiveTankArrow extends Entity {
  public static Vector OFFSET = new Vector(0, -60);
  public static float SCALE = .8f;

  private Vector target;
  private float timer;

  public ActiveTankArrow(final float x, final float y){
    super(x,y);
    setScale(SCALE);
    addImage(ResourceManager.getImage(Tanx.FOCUS_ARROW));
    target = new Vector(0, 0);

  }

  public void pointTo(Vector position){
    target = position.add(OFFSET);
  }

  public void update(int delta) {
    timer += delta;
    setPosition(target.add(new Vector(0, 10*(float)Math.sin(timer/250))));
  }
}
