import jig.ConvexPolygon;
import jig.Vector;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

enum Direction {LEFT, RIGHT, NONE};

class LineSegment {
  public final Vector start;
  public final Vector end;
  public LineSegment(final Vector start, final Vector end) {
    this.start = start;
    this.end = end;
  }
  public static LineSegment approximation(Vector points[]) {
    if (points[0] == null) { return null; }
    if (points[points.length-1] == null) { return null; }
    return new LineSegment(points[0], points[points.length-1]);
  }
  
  public void draw(Graphics g, Color color) {
    g.setColor(color);
    g.drawLine(start.getX(), start.getY(), end.getX(), end.getY());
  }
  
  public Vector getDifference() {
    return end.subtract(start);
  }
  public Vector getDirection() {
    return getDifference().unit();
  }
  public LineSegment rotate(double radians) {
    return new LineSegment(start.rotate(radians), end.rotate(radians));
  }
  public LineSegment translate(Vector delta) {
    return new LineSegment(start.add(delta), end.add(delta));
  }
}

public class Tank extends PhysicsEntity {
  //Constants
  public static final float INF_HEALTH = -9999;
  public static final int INIT_TANK_HEALTH = 100;
  public static final int MAX_TANK_HEALTH = 100;
  public static final float TANK_MOVE_SPEED = .2f;
  public static final float ACCELERATION = .5f;
  public static final float TANK_TERMINAL_VELOCITY = 2f;
  public static final Vector ACCELERATION_JETS = new Vector(0, -.0015f);
  public static final float TANK_WIDTH = 64f;
  public static final float TANK_HEIGHT = 32f;

  //Class Variables
  private Cannon cannon;
  private boolean onGround;
  private Player myPlayer;
  private Healthbar healthbar;
  private boolean invuln;
  
  private LineSegment nearestTerrainSlope;

  public Tank(final float x, final float y, Color c, Player player){
    super(x,y, 0, new Vector(TANK_MOVE_SPEED, TANK_TERMINAL_VELOCITY));
    setVelocity(new Vector(0, 0));
    setAcceleration(new Vector(0,0));

    healthbar = new Healthbar(INIT_TANK_HEALTH);
    cannon = new Cannon(x, y, Cannon.BASE_CANNON);
    myPlayer = player;
    
    this.addShape(new ConvexPolygon(64f, 32f), c, Color.red);
    invuln = false;
  }

  public Projectile fire(float power){
    myPlayer.giveAmmo(cannon.getType(), -1);
    return cannon.fire(power);
  }

  public void rotate(Direction direction, int delta){cannon.rotate(direction, delta);}

  public void move(Direction direction){
    if (direction == Direction.LEFT){
      setAcceleration(new Vector(-ACCELERATION, 0).rotate(this.getRotation()));
    } else if(direction == Direction.RIGHT){
      setAcceleration(new Vector(ACCELERATION, 0).rotate(this.getRotation()));
    } else {
    	setAcceleration(new Vector(0, 0));
    }
//    setAcceleration(getAcceleration());
  }

  public void jet(int delta){
    setVelocity(getVelocity().add(ACCELERATION_JETS.scale(delta)));
  }

  public void update(int delta){
    cannon.setX(this.getX());
    cannon.setY(this.getY());
    cannon.updateOffset(this.getRotation());
  }
  
  private LineSegment bottomEdge() {
    Vector tankBottomDirection = Vector.getUnit(this.getRotation());
    Vector tankCenter = getPosition();
    Vector tankBottomFromCenter = tankBottomDirection.getPerpendicular().scale(TANK_HEIGHT/2);

    Vector tankBottomLeft = tankCenter.subtract(tankBottomDirection.scale(TANK_WIDTH/2));
    Vector tankBottomRight = tankCenter.add(tankBottomDirection.scale(TANK_WIDTH/2));
    return LineSegment.approximation(new Vector[] {tankBottomLeft, tankBottomRight}).translate(tankBottomFromCenter);
  }
  private LineSegment calculateNearestTerrainSlope(Terrain terrain) {
    int visionForwardThreshold = 20;
    int visionBackwardThreshold = 50;
    int downwardRayCount = 2;
    LineSegment tankBottom = this.bottomEdge();
    Vector terrainBoundary[] = new Vector[downwardRayCount];
    Vector step = tankBottom.getDirection().scale(TANK_WIDTH/(downwardRayCount-1));
    Vector current = tankBottom.start;
    for (int i = 0; i < downwardRayCount; i++) {
      Vector collisionPoint = terrain.nearestEdgeForwardOrBackward(current, Terrain.Direction.DOWN, visionForwardThreshold, visionBackwardThreshold);
      terrainBoundary[i] = collisionPoint;
      current = current.add(step);
    }
    return LineSegment.approximation(terrainBoundary);
    
  }
  private void calculateRotation(int delta, Terrain terrain) {
    LineSegment tankBottom = this.bottomEdge();
    nearestTerrainSlope = calculateNearestTerrainSlope(terrain);
    if (nearestTerrainSlope == null) {
      return;
    }
    
    double angleToTerrain = tankBottom.getDirection().angleTo(nearestTerrainSlope.getDifference());
//    System.out.println("Angle: " + angleToTerrain);
    this.setRotation(angleToTerrain);
  }
  private Vector debugFriction;
  private void applyFriction(int delta, LineSegment terrainSlope) {
    float mue = 0.04f;
    Vector vNormal = this.getVelocity().project(nearestTerrainSlope.getDirection().getPerpendicular());
    Vector vParallel = this.getVelocity().project(nearestTerrainSlope.getDirection());
    float normalVelocityFactor = vNormal.length();
    Vector friction = vParallel.negate().setLength(mue*normalVelocityFactor*delta).clampLength(0, vParallel.length());
    this.setVelocity(this.getVelocity().add(friction));
    System.out.println("Friction: " + friction + ", parallel: " + vParallel);
    debugFriction = friction;
  }
  private void calculateTranslation(int delta, Terrain terrain) {
    nearestTerrainSlope = calculateNearestTerrainSlope(terrain);
    if (nearestTerrainSlope == null) { return; }
    LineSegment tankBottom = this.bottomEdge(); // recalculate based on new rotation.
    Vector leftDistance = tankBottom.start.subtract(nearestTerrainSlope.start);
    Vector rightDistance = tankBottom.end.subtract(nearestTerrainSlope.end);
    Vector avgDistance = leftDistance.add(rightDistance).scale(-0.5f);
    
    Vector translation = avgDistance;//.project(nearestTerrainSlope.getDirection().getPerpendicular());
    // not quite working yet...
    System.out.println("Translation: " + translation);
    if (translation.lengthSquared() < 30*30) {
      this.translate(translation);
      this.applyFriction(delta, nearestTerrainSlope);
      this.setVelocity(this.getVelocity().project(nearestTerrainSlope.getDirection()));
    }
  }
  public boolean checkTerrainCollision(int delta, Terrain terrain) {
    calculateRotation(delta, terrain);
    calculateTranslation(delta, terrain);
    
    return false; // we handled it ourselves, don't let others handle it.
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
    cannon.setX(this.getX());
    cannon.setY(this.getY());
    cannon.render(g);
    
    float bottomSpacing = 20;
    healthbar.render(g, this.getCoarseGrainedMaxY() + bottomSpacing, this.getX());
    
    
    if (nearestTerrainSlope != null) {
      nearestTerrainSlope.draw(g, Color.orange);
      this.bottomEdge().draw(g, Color.green);
    }
    LineSegment v = new LineSegment(getPosition(), this.getPosition().add(this.getVelocity().scale(1000)));
    v.draw(g, Color.lightGray);
    if (debugFriction != null) {
      LineSegment f = new LineSegment(getPosition(), this.getPosition().add(this.debugFriction.scale(1000)));
      f.draw(g, Color.red);
    }
    LineSegment a = new LineSegment(getPosition(), this.getPosition().add(this.getAcceleration().scale(1000)));
    a.draw(g, Color.green);
  }
  public void changeWeapon(int type){
    cannon.changeType(type);
  }

  
  public void giveHealth(int amount) {
    healthbar.receiveHealth(amount);
  }
  public void takeDamage(int amount) {
    if (!invuln) healthbar.receiveDamage(amount);
  }
  @Override
  public boolean getIsDead() {
    return healthbar.getIsDead();
  }
  
  //set/get functions
  public void setOnGround(boolean onGround) { this.onGround = onGround; }
  public boolean isOnGround() { return onGround; }
  public Player getMyPlayer() { return myPlayer; }

  //tank cheat handlers
  public void toggleInfHealth() {
    invuln = !invuln;
  }

  public boolean isInfHealth() {
    return invuln;
  }

  public void killTank() {
    healthbar.receiveDamage(healthbar.health);
  }
}