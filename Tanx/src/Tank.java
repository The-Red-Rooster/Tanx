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
  private boolean onGround;
  private Player myPlayer;
  
  private Vector nearestTerrainSlopeStart;
  private Vector nearestTerrainSlopeStop;


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
    if (direction == Direction.LEFT){
      setAcceleration(new Vector(-ACCELERATION, 0).rotate(this.getRotation()));
    } else if(direction == Direction.RIGHT){
      setAcceleration(new Vector(ACCELERATION, 0).rotate(this.getRotation()));
    } else {
//    	setAcceleration(new Vector(0, getAcceleration().getY()));
    }
//    setAcceleration(getAcceleration());
  }

  //NEED REWORK TO JUMPJETS
  public void jump(){
      setVelocity(new Vector(getVelocity().getX(), JUMP_SPEED));
  }

  public void update(int delta){
    cannon.setX(this.getX());
    cannon.setY(this.getY());
    cannon.updateOffset(this.getRotation());
  }
  
  public boolean checkTerrainCollision(int delta, Terrain terrain) {
    this.setRotation(0);
    int visionThreshold = 200;
    int downwardRayCount = 2;
    Vector terrainBoundary[] = new Vector[downwardRayCount];
    Vector tankBottomDirection = Vector.getUnit(this.getRotation());
    Vector tankCenter = getPosition();

    Vector tankBottomLeft = tankCenter.subtract(tankBottomDirection.scale(TANK_WIDTH/2));
//    Vector tankBottomRight = new Vector(this.getCoarseGrainedMinX(), this.getCoarseGrainedMaxY());
    Vector step = tankBottomDirection.scale(TANK_WIDTH/(downwardRayCount-1));
    Vector current = tankBottomLeft;
    for (int i = 0; i < downwardRayCount; i++) {
      Vector collisionPoint = terrain.nearestNonEmptyPoint(current, Terrain.Direction.DOWN, visionThreshold);
      terrainBoundary[i] = collisionPoint;
      current = current.add(step);
    }
//    for (Vector point : terrainBoundary) {
//      nearestTerrainSlopeStart
//    }
    nearestTerrainSlopeStart = terrainBoundary[0];
    nearestTerrainSlopeStop = terrainBoundary[downwardRayCount-1];
    if (nearestTerrainSlopeStart == null || nearestTerrainSlopeStop == null) {
      return super.checkTerrainCollision(delta, terrain);
    }
    Vector terrainSlope = nearestTerrainSlopeStop.subtract(nearestTerrainSlopeStart);
    
    double angleToTerrain = tankBottomDirection.angleTo(terrainSlope);
    System.out.println("Angle: " + angleToTerrain);
    this.rotate(angleToTerrain);
    
//    return false;
    return super.checkTerrainCollision(delta, terrain);
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
//		  System.out.println("left d: " + d);
		  if(leftYDistance > d) {
//			  System.out.println("foo");
			  leftYDistance = d;
			  leftXDistance = i;
		  }
		  d = t.castRay(new Vector(this.getX() + i, this.getY()+TANK_HEIGHT/2), Terrain.Direction.DOWN);
//		  System.out.println("right d: " + d);
		  if(rightYDistance > d) {
//			  System.out.println("bar");
			  rightYDistance = d;
			  rightXDistance = i;
		  }
	  }
//	  System.out.println("left: <" + leftXDistance + ", " + leftYDistance + "> right: <" + rightXDistance + ", " + rightYDistance + ">");
	  
	  if(leftYDistance > rightYDistance) {
		  sign = -1;
	  }
	  if(leftYDistance < rightYDistance) {
		  sign = 1;
	  }
	  
	  x = leftXDistance + rightXDistance;
	  
	  y = Math.abs(leftYDistance - rightYDistance);
	  
//	  System.out.println("x: " + x);
//	  System.out.println("y:" + y);
	  
	  if(x == 0) {
		  this.rotate(0);
	  }else {
		  this.rotate(sign * Math.toDegrees(Math.atan(y/x)));
//		  System.out.println("rotation: " + sign * Math.toDegrees(Math.atan(y/x)));
	  }
	  
  }
  
  @Override
  public void render(Graphics g) {
    super.render(g);
    cannon.render(g);
    
    if (nearestTerrainSlopeStart != null && nearestTerrainSlopeStop != null) {
      g.setColor(Color.red);
      g.drawLine(nearestTerrainSlopeStart.getX(), nearestTerrainSlopeStart.getY(), 
          nearestTerrainSlopeStop.getX(), nearestTerrainSlopeStop.getY());
    }
  }

  //set/get functions
  public void takeDmg(int dmg){ this.health -= dmg; }
  public int getHealth() {return health;}
  public void setHealth(int health) {this.health = health;}
  public void setOnGround(boolean onGround) {this.onGround = onGround;}
  public boolean isOnGround() {return onGround;}
  public Player getMyPlayer() {return myPlayer;}
}