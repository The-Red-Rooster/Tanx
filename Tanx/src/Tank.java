import jig.ConvexPolygon;
import jig.Vector;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Transform;

enum Direction {LEFT, RIGHT, NONE};

public class Tank extends PhysicsEntity {
  //Constants
  public static final int INIT_TANK_HEALTH = 90;
  public static final int MAX_TANK_HEALTH = 100;
  public static final float INIT_FUEL_BURNTIME = 2*1000;
  public static final float TANK_MOVE_SPEED = .2f;

  public static final float ACCELERATION = .5f;
  public static final float JUMP_SPEED = .5f;
  public static final float TANK_WIDTH = 64f;
  public static final float TANK_HEIGHT = 32f;
  public static final float TANK_TERMINAL_VELOCITY = 2f;

  public static final Vector ACCELERATION_JETS = new Vector(0, -.0015f);


  //Class Variables
  private float fuel;
  private Cannon cannon;
  private Player myPlayer;
  private Healthbar healthbar;


  public Tank(final float x, final float y, Color c, Player player){

    super(x,y, 0, new Vector(TANK_MOVE_SPEED, TANK_TERMINAL_VELOCITY));

    setVelocity(new Vector(0, 0));
    setAcceleration(new Vector(0,0));

    healthbar = new Healthbar(INIT_TANK_HEALTH);
    cannon = new Cannon(x, y, Cannon.BASE_CANNON);
    myPlayer = player;
    this.addShape(new ConvexPolygon(TANK_WIDTH, TANK_HEIGHT), c, Color.red);
  }

  public Projectile fire(int power){
	  	this.move(Direction.NONE);
		myPlayer.giveAmmo(cannon.getType(), -1);
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
    
  }

  public void jet(int delta){
    if (fuel > 0){
      setVelocity(getVelocity().add(ACCELERATION_JETS.scale(delta)));
      fuel -= delta;
    }
  }

  public void update(int delta){ }
  
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
	  
	  System.out.println(this.onGround);
	  
	  for(int i = 0; i < TANK_WIDTH/2; i++) {
		  int d = t.castRay(new Vector(this.getX() - i, this.getY()+TANK_HEIGHT/2), Terrain.Direction.DOWN);
		  if(leftYDistance > d) {
			  leftYDistance = d;
			  leftXDistance = i;
			  System.out.println("new highest left: " + d + "," + i);
		  }
		  d = t.castRay(new Vector(this.getX() + i, this.getY()+TANK_HEIGHT/2), Terrain.Direction.DOWN);
		  if(rightYDistance > d) {
			  rightYDistance = d;
			  rightXDistance = i;
			  System.out.println("new highest right: " + d + "," + i);
		  }
	  }
	    
	  System.out.println("leftY: " + leftYDistance);
	  System.out.println("rightY: " + rightYDistance);
	  System.out.println("leftX: " + leftXDistance);
	  System.out.println("rightX: " + rightXDistance);
	  if(leftYDistance > rightYDistance) {
		  sign = -1;
	  }
	  if(leftYDistance < rightYDistance) {
		  sign = 1;
	  }
	  
	  x = leftXDistance + rightXDistance;
	  
	  y = Math.abs(leftYDistance - rightYDistance);
	  
	  System.out.println("x: " + x);
	  System.out.println("y: " + y);
	  
	  if(x == 0) {
		  this.rotate(0);
	  }else {
		  this.rotate(sign * Math.toDegrees(Math.atan(y/x)));
	  }
	  System.out.println(this.getRotation());
	  
	  
	  cannon.updateOffset(this.getRotation());
	  
  }
  
  @Override
  public void render(Graphics g) {
    super.render(g);
    cannon.setX(this.getX());
    cannon.setY(this.getY());
    cannon.render(g);
    float bottomSpacing = 20;
    healthbar.render(g, this.getCoarseGrainedMaxY() + bottomSpacing, this.getX());
  }
  public void changeWeapon(int type){
    cannon.changeType(type);
  }

  
  public void giveHealth(int amount) {
    healthbar.receiveHealth(amount);
  }
  public void takeDamage(int amount) {
    healthbar.receiveDamage(amount);
  }
  @Override
  public boolean getIsDead() {
    return healthbar.getIsDead();
  }
  
  //set/get functions

  //public void takeDmg(int dmg){ this.health -= dmg; }
  //public int getHealth() {return health;}
  //public void setHealth(int health) {this.health = health;}
  //public void setOnGround(boolean onGround) {this.onGround = onGround;}
  public Player getMyPlayer() {return myPlayer;}

  public void setFuel(float fuel) { this.fuel = fuel; }
  public float getFuel() { return fuel; }
  public int getFuelPercentage() {return (int)(fuel/INIT_FUEL_BURNTIME*100);}
}