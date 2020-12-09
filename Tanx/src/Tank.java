import jig.ConvexPolygon;
import jig.Vector;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

enum Direction {LEFT, RIGHT, NONE};

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
  private double targetRotation;
  
  private LineSegment terrainNormals[];
  private LineSegment terrainBoundaryRays[];
  private int shortestRaysIndexes[];
  private Vector shortestNormals[];
  private LineSegment nearestTerrainSlope;
  private LineSegment terrainNormal;

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
    
    double ANGULAR_VELOCITY = 0.1;
    double diff = this.getRotation()- targetRotation;
    System.out.println("diff: " + diff);
    if (Math.abs(diff) > ANGULAR_VELOCITY*delta) {
      this.rotate(-Math.signum(diff)*ANGULAR_VELOCITY*delta);
    }
  }
  
  private LineSegment bottomEdge() {
    Vector tankBottomDirection = Vector.getUnit(this.getRotation());
    Vector tankCenter = getPosition();
    Vector tankBottomFromCenter = tankBottomDirection.getPerpendicular().scale(TANK_HEIGHT/2);

    Vector tankBottomLeft = tankCenter.subtract(tankBottomDirection.scale(TANK_WIDTH/2));
    Vector tankBottomRight = tankCenter.add(tankBottomDirection.scale(TANK_WIDTH/2));
    return LineSegment.approximation(new Vector[] {tankBottomLeft, tankBottomRight}).translate(tankBottomFromCenter);
  }
  private LineSegment topEdge() {
    Vector tankHorizontal = Vector.getUnit(this.getRotation());
    Vector tankCenter = getPosition();
    Vector tankTopFromCenter = tankHorizontal.getPerpendicular().scale(-TANK_HEIGHT/2);

    Vector tankBottomLeft = tankCenter.subtract(tankHorizontal.scale(TANK_WIDTH/2));
    Vector tankBottomRight = tankCenter.add(tankHorizontal.scale(TANK_WIDTH/2));
    return new LineSegment(tankBottomLeft, tankBottomRight).translate(tankTopFromCenter);
  }
  private LineSegment calculateNearestTerrainSlope(Terrain terrain) {
    int visionForwardThreshold = 500;
    int visionBackwardThreshold = 500;
    int downwardRayCount = 5;
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
  private LineSegment calculateNearestTerrainNormal(Terrain terrain) {
//    int visionForwardThreshold = 500;
//    int visionBackwardThreshold = 500;
    int downwardRayCount = 6;
    LineSegment tankTop = this.topEdge();
//    LineSegment tankTop = new LineSegment(new Vector(this.getCoarseGrainedMinX(), this.getCoarseGrainedMinY()), new Vector(this.getCoarseGrainedMaxX(), this.getCoarseGrainedMaxY()));
    Vector terrainBoundary[] = new Vector[downwardRayCount];
    Vector terrainNormals[] = new Vector[downwardRayCount/2];
    this.terrainNormals = new LineSegment[downwardRayCount/2];
    this.terrainBoundaryRays = new LineSegment[downwardRayCount];
    this.shortestRaysIndexes = new int[] { -1, -1 };
    Vector step = tankTop.getDirection().scale(TANK_WIDTH/(downwardRayCount/2-1));
    Vector tinyStep = tankTop.getDirection().scale(5);
    Vector current = tankTop.start;
    Vector start = tankTop.start;
    for (int i = 0; i < downwardRayCount; i++) {
      if (i % 2 == 0) {
        current = start.add(step.scale(i/2)).add(tinyStep);
      } else {
        current = start.add(step.scale(i/2));
      }
      Vector collisionPoint = terrain.surfacePointForRay(current, tankTop.getDirection().getPerpendicular());
      terrainBoundary[i] = collisionPoint;
      if (collisionPoint != null) {
        terrainBoundaryRays[i] = new LineSegment(current, collisionPoint);
      }
    }
    for (int i = 0; i < downwardRayCount; i += 2) {
      if (terrainBoundary[i+1] == null || terrainBoundary[i] == null) {
        continue;
      }
      if (shortestRaysIndexes[0] == -1) {
        shortestRaysIndexes[0] = i;
      } else if (terrainBoundaryRays[i].getDifference().lengthSquared() < terrainBoundaryRays[shortestRaysIndexes[0]].getDifference().lengthSquared()) {
        shortestRaysIndexes[1] = shortestRaysIndexes[0];
        shortestRaysIndexes[0] = i;
      } else if (shortestRaysIndexes[1] == -1) {
        shortestRaysIndexes[1] = i;
      } else if (terrainBoundaryRays[i].getDifference().lengthSquared() < terrainBoundaryRays[shortestRaysIndexes[1]].getDifference().lengthSquared()) {
        shortestRaysIndexes[1] = i;
      }
    }
    shortestNormals = new Vector[2];
    Vector contactPoint = new Vector(0, 0);
    
    for (int i = 0; i < shortestRaysIndexes.length; i++) {
      int rayIndex = shortestRaysIndexes[i];
      if (rayIndex == -1) {
        continue;
      }
      Vector normal = new LineSegment(terrainBoundary[rayIndex], terrainBoundary[rayIndex+1]).unitNormal();
      contactPoint = avgVectors(new Vector[] { terrainBoundary[rayIndex], terrainBoundary[rayIndex+1] });
      shortestNormals[i] = normal;
      terrainNormals[rayIndex/2] = normal;
      this.terrainNormals[rayIndex/2] = new LineSegment(contactPoint, contactPoint.add(normal));
    }
    Vector terrainNormal = avgVectors(shortestNormals);
    this.terrainNormal = new LineSegment(contactPoint, contactPoint.add(terrainNormal.scale(50)));
    
    LineSegment maxPenetrationRay = terrainBoundaryRays[shortestRaysIndexes[0]]; // TODO: handle -1
    float distanceToTerrain = maxPenetrationRay.getDifference().length() - TANK_HEIGHT;
    
    if (distanceToTerrain < 0) {
      this.translate(maxPenetrationRay.getDirection().scale(distanceToTerrain));
  //    this.applyFriction(delta, nearestTerrainSlope);
      this.setVelocity(this.getVelocity().project(terrainNormal.getPerpendicular()));
//      this.rotate(degrees);
      double terrainAngle = Math.acos(terrainNormal.dot(new Vector(0, -1))) * 180.0/Math.PI;

      if(new Vector(0, -1).dot(terrainNormal.getPerpendicular()) > 0) {
        terrainAngle = -terrainAngle;
      }
      System.out.println("terrainNormal angle: " + terrainAngle + ", tank: " + this.getRotation());
      assert(terrainAngle <= 90);
      this.targetRotation = terrainAngle;
//      this.setRotation(rotationToNormal(terrainNormal));
  //    this.setRotation(terrainNormal.getDirection().angleTo(tankBottom.getDirection().getPerpendicular())+180);
  //    this.setVelocity(this.getVelocity().add(translation));
    }
//    for (int i = 0; i < downwardRayCount; i += 2) {
//      if (terrainBoundary[i+1] == null ||terrainBoundary[i] == null) {
//        continue;
//      }
//      terrainNormals[i/2] = new LineSegment(terrainBoundary[i], terrainBoundary[i+1]).unitNormal();
//      this.terrainNormals[i/2] = new LineSegment(terrainBoundary[i+1], terrainBoundary[i+1].add(terrainNormals[i/2]));
//    }
//    Vector terrainNormal = avgVectors(terrainNormals);
    Vector terrainSurfacePoint = avgVectors(terrainBoundary);
    return new LineSegment(terrainSurfacePoint, terrainSurfacePoint.add(terrainNormal));
    // TODO: watch for null
    
  }
  private double rotationToNormal(Vector normal) {
    return (normal.angleTo(new Vector(0, -1)));
  }
  private Vector avgVectors(Vector vs[]) {
    if (vs.length <= 0) { return null; }
    Vector sum = new Vector(0, 0);
    for (Vector v : vs) {
      sum = sum.add(v);
    }
    return sum.scale(1.0f/(float)vs.length);
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
//    System.out.println("Friction: " + friction + ", parallel: " + vParallel);
    debugFriction = friction;
  }
  private void calculateTranslation(int delta, Terrain terrain) {
    calculateNearestTerrainNormal(terrain);
    nearestTerrainSlope = calculateNearestTerrainSlope(terrain);
    if (nearestTerrainSlope == null) { return; }
    LineSegment tankBottom = this.bottomEdge(); // recalculate based on new rotation.
    Vector leftDistance = tankBottom.start.subtract(nearestTerrainSlope.start);
    Vector rightDistance = tankBottom.end.subtract(nearestTerrainSlope.end);
    Vector avgDistance = leftDistance.add(rightDistance).scale(-0.5f);
    
    Vector translation = avgDistance;
//    Vector translation = this.getVelocity().project(terrainNormal.getDirection());//.project(nearestTerrainSlope.getDirection().getPerpendicular());
    // not quite working yet...
//    System.out.println("Translation: " + translation);
    double maxTranslationSquared = 10*10;
    if (translation.lengthSquared() < maxTranslationSquared) {
//      this.translate(translation);
//      this.applyFriction(delta, nearestTerrainSlope);
//      this.setVelocity(this.getVelocity().project(nearestTerrainSlope.getDirection()));
//      this.setRotation(terrainNormal.getDirection().angleTo(tankBottom.getDirection().getPerpendicular())+180);
//      this.setVelocity(this.getVelocity().add(translation));
    }
  }
  public boolean checkTerrainCollision(int delta, Terrain terrain) {
    calculateTranslation(delta, terrain);

//    calculateRotation(delta, terrain);
    
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
    
    //renderDebugRays(g);
  }
    
  private void renderDebugRays(Graphics g) {

    if (nearestTerrainSlope != null) {
      nearestTerrainSlope.draw(g, Color.orange);
      this.bottomEdge().draw(g, Color.green);
    }
    if (terrainNormal != null) {
      new LineSegment(terrainNormal.start, terrainNormal.start.add(terrainNormal.getDirection().setLength(30))).draw(g, Color.pink);
//      for (LineSegment normal : terrainNormals) {
//        if (normal != null) {
//          new LineSegment(normal.start, normal.start.add(normal.getDirection().setLength(30))).draw(g, Color.magenta);
//        }
//      }
//      for (LineSegment ray : terrainBoundaryRays) {
//        if (ray != null) {
//          ray.draw(g, Color.orange);
//        }
//      }
      for (int i : shortestRaysIndexes) {
        if (i == -1) {
          continue;
        }
        terrainBoundaryRays[i].draw(g, Color.red);
      }
    }
//    LineSegment v = new LineSegment(getPosition(), this.getPosition().add(this.getVelocity().scale(1000)));
//    v.draw(g, Color.lightGray);
//    if (debugFriction != null) {
//      LineSegment f = new LineSegment(getPosition(), this.getPosition().add(this.debugFriction.scale(1000)));
//      f.draw(g, Color.red);
//    }
//    LineSegment a = new LineSegment(getPosition(), this.getPosition().add(this.getAcceleration().scale(1000)));
//    a.draw(g, Color.green);
  
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