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
  public static float BASE_CANNON_POWER = 0.5f;
  public static float BASE_CANNON_OFFSET = 50;
  //class variables
  private int type;
  private float power;
  private float fireOffset;
  private float rotationFactor;
  private float rotationOffset;

  public Cannon(final float x, final float y){
    super(x,y);
    changeType(BASE_CANNON);
    this.addShape(new ConvexPolygon(10f, 45f), Color.red, Color.blue);
  }

  public void changeType(int newType){
    type = newType;
    if (newType == BASE_CANNON){
      power = BASE_CANNON_POWER;
      fireOffset = BASE_CANNON_OFFSET;
      //changeSprite(Tanx.BASIC_CANNON_SPRITE);
    }
  }

  public void changeSprite(String sprite){
    removeImage(ResourceManager.getImage(Tanx.BASIC_CANNON_SPRITE));
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
    if (type == BASE_CANNON) return new Projectile(x, y, projVelocity);
    return null;
  }
}
