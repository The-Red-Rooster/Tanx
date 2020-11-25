import jig.ConvexPolygon;
import jig.Vector;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

enum Direction {LEFT, RIGHT};

public class Tank extends PhysicsEntity {
  //Constants
  public static final int INIT_TANK_HEALTH = 100;
  public static final float TANK_MOVE_SPEED = .2f;
  public static final float ACCELERATION = .5f;
  public static final float JUMP_SPEED = .5f;
  public static final float TANK_WIDTH = 64f;
  public static final float TANK_HEIGHT = 32f;
  
  //Class Variables
  private int health;
  private Cannon cannon;
  private boolean onGround;
  private Player myPlayer;


  public Tank(final float x, final float y, Color c, Player player){
    super(x,y, 0, new Vector(100, 100));
    setVelocity(new Vector(0, 0));
    setAcceleration(new Vector(0,0));

    setHealth(INIT_TANK_HEALTH);
    cannon = new Cannon(this.getX(), this.getY());
    myPlayer = player;
    this.addShape(new ConvexPolygon(TANK_WIDTH, TANK_HEIGHT), c, Color.red);
  }

  public Projectile fire(int power){return cannon.fire(power);}
  public void rotate(Direction direction, int delta){cannon.rotate(direction, delta);}

  public void move(Direction direction){
    if (direction == Direction.LEFT){
      setAcceleration(new Vector(-ACCELERATION, getAcceleration().getY()));
    } else {
      setAcceleration(new Vector(ACCELERATION, getAcceleration().getY()));
    }
  }

  //NEED REWORK TO JUMPJETS
  public void jump(){
      setVelocity(new Vector(getVelocity().getX(), JUMP_SPEED));
  }

  public void update(int delta){
    cannon.setX(this.getX());
    cannon.setY(this.getY());
  }
  
  public void update(int delta, Terrain t) {
	  if(onGround) {
		  rotateToSlope(t);
	  }
	  
	  //System.out.println("tank: " + this + " onGround: " + onGround);
  }
  
  private void rotateToSlope(Terrain t) {
	  this.setRotation(0);
	  int leftDistance = t.castRay(new Vector(this.getX() - TANK_WIDTH/2, this.getY() + TANK_HEIGHT/2), Terrain.Direction.DOWN);
	 //int midDistance = t.castRay(new Vector(this.getX(), this.getCoarseGrainedMaxY()), Terrain.Direction.DOWN);
	  int rightDistance = t.castRay(new Vector(this.getX() + TANK_WIDTH/2, this.getY() + TANK_HEIGHT/2), Terrain.Direction.DOWN);
	  
	  int sign = 0;
	  
	  if(leftDistance > rightDistance) sign = -1;
	  if(leftDistance < rightDistance) sign = 1;
	  
	  int y = Math.abs(leftDistance - rightDistance);
	  this.rotate(sign * Math.toDegrees(Math.tan((double)y/(double)this.getCoarseGrainedWidth())));
	  
	  cannon.updateOffset(this.getRotation());
	  
  }
  
  @Override
  public void render(Graphics g) {
    super.render(g);
    cannon.render(g);
  }

  //set/get functions
  public void takeDmg(int dmg){ this.health -= dmg; }
  public int getHealth() {return health;}
  public void setHealth(int health) {this.health = health;}
  public void setOnGround(boolean onGround) {this.onGround = onGround;}
  public boolean isOnGround() {return onGround;}
  public Player getMyPlayer() {return myPlayer;}
}