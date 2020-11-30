import jig.ConvexPolygon;
import jig.Entity;
import jig.ResourceManager;
import jig.Vector;
import org.newdawn.slick.Color;

public class Cannon extends Entity {
  //constants
  public static float ROTATION_SPEED = 100;
  public static float MAX_ROTATION_FACTOR = 90;
  public static float ANGLE_CORRECTION = -90;
  public static int BASE_CANNON = 0;
  public static String BASE_CANNON_STR = "Basic Cannon";
  public static float BASE_CANNON_POWER = 1f;
  public static float BASE_CANNON_OFFSET = 50;
  public static int BIG_CANNON = 1;
  public static String BIG_CANNON_STR = "Long Range Cannon";
  public static float BIG_CANNON_POWER = 1f;
  public static float BIG_CANNON_OFFSET = 50;
  //class variables
  private int type;
  private float power;
  private float fireOffset;
  private float rotationFactor;
  private float rotationOffset;

  public Cannon(final float x, final float y, int type){
    super(x,y);
    changeType(type);
    this.addShape(new ConvexPolygon(10f, 45f), Color.red, Color.blue);
  }

  public static String getTypeStr(int type) {
    if (type == BIG_CANNON) { return BIG_CANNON_STR;
    } else if (type == BASE_CANNON) { return BASE_CANNON_STR;
    } else { return null; }
  }

  public void changeType(int newType){
    type = newType;
    if (newType == BASE_CANNON){
      power = BASE_CANNON_POWER;
      fireOffset = BASE_CANNON_OFFSET;
      //changeSprite(Tanx.BASIC_CANNON_SPRITE);
    } else if (newType == BIG_CANNON){
      power = BIG_CANNON_POWER;
      fireOffset = BIG_CANNON_OFFSET;
      //changeSprite(tanx.BIG_CANNON_SPRITE);
    }
  }

  public void changeSprite(String sprite){
    //removeImage(ResourceManager.getImage(Tanx.BASIC_CANNON_SPRITE));
    //removeImage(ResourceManager.getImage(Tanx.BIG_CANNON_SPRITE));
    addImage(ResourceManager.getImage(sprite));
  }

  /* This Method rotates the cannon with a set speed defined above
  The angle calculations are done in degrees
  ROTATIONSPEED should be in degrees per second
  */
  public void rotate(Direction direction, int delta){
    float rotationAmount = ROTATION_SPEED *delta/1000;
    if (direction == Direction.RIGHT) {
      rotationFactor += rotationAmount;
      if (Math.abs(rotationFactor) > MAX_ROTATION_FACTOR){
        rotationFactor = MAX_ROTATION_FACTOR;
      }
    } else {
      rotationFactor -= rotationAmount;
      if (Math.abs(rotationFactor) > MAX_ROTATION_FACTOR){
        rotationFactor = -MAX_ROTATION_FACTOR;
      }
    }
    setRotation(rotationFactor + rotationOffset);
  }

  public void updateOffset(double tankRotation) {
	  rotationOffset = (float)tankRotation;
	  setRotation(rotationFactor + rotationOffset);
  }
  
  //input:float from 0 to 1 determing power strength
  //output: projectile of the cannon's type on firing
  //onError: outputs null projectile
  public Projectile fire(float p){
    if (power < 0) power = 0;
    float launchPower = p*power;
    double angle = Math.toRadians(rotationFactor + rotationOffset + ANGLE_CORRECTION);
    Vector projVelocity = new Vector((float)Math.cos(angle), (float)Math.sin(angle));
    projVelocity = projVelocity.setLength(launchPower);
    float x = getX() + fireOffset*(float)Math.cos(angle);
    float y = getY() + fireOffset*(float)Math.sin(angle);
    return new Projectile(x, y, projVelocity);
  }

  public int getType() { return type; }
}
