import jig.ConvexPolygon;
import jig.Vector;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Transform;

enum Direction {LEFT, RIGHT, NONE};

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
  private Player myPlayer;


  public Tank(final float x, final float y, Color c, Player player){
    super(x,y, 0, new Vector(TANK_MOVE_SPEED, 100));
    setVelocity(new Vector(0, 0));
    setAcceleration(new Vector(0,0));

    setHealth(INIT_TANK_HEALTH);
    cannon = new Cannon(this.getX(), this.getY());
    myPlayer = player;
    this.addShape(new ConvexPolygon(TANK_WIDTH, TANK_HEIGHT), c, Color.red);
  }

  public Projectile fire(int power){
	  this.move(Direction.NONE);
	  return cannon.fire(power);
  }
  public void rotate(Direction direction, int delta){cannon.rotate(direction, delta);}

  public void move(Direction direction){
	double rotation = (float)Math.toRadians(-this.getRotation());
	
	if(!this.getOnGround()) rotation = 0; 
    if (direction == Direction.LEFT){
    	setAcceleration(new Vector((float)-Math.cos(rotation), (float)Math.sin(rotation)).scale(ACCELERATION));
    } else if(direction == Direction.RIGHT){
    	setAcceleration(new Vector((float)Math.cos(rotation), (float)Math.sin(rotation)).scale(ACCELERATION));
    } else {
    	setAcceleration(new Vector(0, 0));
    }
    System.out.println(rotation);
    System.out.println(this.getAcceleration());
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
	 
  }
  
  public void rotateToSlope(Terrain t) {
	  this.setRotation(0);
	  int leftYDistance = 99999;
	  int rightYDistance = 99999;
	  int leftXDistance = 0;
	  int rightXDistance = 0;
	  double x;
	  double y;
	  int sign = 0;
	  
	  
	  
	  for(int i = 0; i < TANK_WIDTH/2; i++) {
		  int d = t.castRay(new Vector(this.getX() - i, this.getY()+TANK_HEIGHT/2), Terrain.Direction.DOWN);
		  if(leftYDistance > d) {
			  leftYDistance = d;
			  leftXDistance = i;
		  }
		  d = t.castRay(new Vector(this.getX() + i, this.getY()+TANK_HEIGHT/2), Terrain.Direction.DOWN);
		  if(rightYDistance > d) {
			  rightYDistance = d;
			  rightXDistance = i;
		  }
	  }
	    
	  if(leftYDistance > rightYDistance) {
		  sign = -1;
	  }
	  if(leftYDistance < rightYDistance) {
		  sign = 1;
	  }
	  
	  x = leftXDistance + rightXDistance;
	  
	  y = Math.abs(leftYDistance - rightYDistance);
	  
	  
	  if(x == 0) {
		  this.rotate(0);
	  }else {
		  this.rotate(sign * Math.toDegrees(Math.atan(y/x)));
	  }
	  
	  
	  
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
  public Player getMyPlayer() {return myPlayer;}
}