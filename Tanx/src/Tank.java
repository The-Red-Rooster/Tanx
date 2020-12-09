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
  
  static boolean showDebugRays = false;
  private LineSegment terrainNormals[];
  private RayPair terrainBoundaryRays[];
  private int shortestRaysIndexes[];
  private Vector shortestNormals[];
  private LineSegment nearestTerrainSlope;
  private Vector debugTerrainNormal;
  private Vector debugFriction;

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
    double diff = targetRotation - this.getRotation();
    System.out.println("diff: " + diff);
    if (diff > 0) {
      this.rotate(Math.min(ANGULAR_VELOCITY*delta, diff));
    } else if (diff < 0) {
      this.rotate(Math.max(-ANGULAR_VELOCITY*delta, diff));
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
  private RayPair[] calculateTerrainRays(Terrain terrain, int downwardRayCount) {
    RayPair terrainBoundaryRays[] = new RayPair[downwardRayCount];
    LineSegment tankTop = this.topEdge();
    Vector step = tankTop.getDirection().scale(TANK_WIDTH/(downwardRayCount-1));
    Vector current = tankTop.start;
    Vector start = tankTop.start;
    for (int i = 0; i < downwardRayCount; i++) {
      current = start.add(step.scale(i));
      terrainBoundaryRays[i] = terrain.surfaceDistanceRays(current, tankTop.getDirection().getPerpendicular());
    }
    return terrainBoundaryRays;
  }
  private void calculateTranslation(int delta, Terrain terrain) {
    int downwardRayCount = 3;
    
    Vector terrainNormals[] = new Vector[downwardRayCount];
    this.terrainNormals = new LineSegment[downwardRayCount];
    this.shortestRaysIndexes = new int[] { -1, -1 };
    
    this.terrainBoundaryRays = this.calculateTerrainRays(terrain, downwardRayCount);
    
    Vector terrainBoundary[] = new Vector[downwardRayCount];
    for (int i = 0; i < downwardRayCount; i++) {
      terrainBoundary[i] = terrainBoundaryRays[i].first.end; // Consider avg
    }
    
    shortestRaysIndexes = indexesOfShortest(terrainBoundaryRays);
    shortestNormals = new Vector[2];
    
    for (int i = 0; i < shortestRaysIndexes.length; i++) {
      int rayIndex = shortestRaysIndexes[i];
      if (rayIndex == -1) {
        continue;
      }
      Vector normal = terrainBoundaryRays[rayIndex].surfaceNormal();
//      Vector contactPoint = avgVectors(new Vector[] { terrainBoundary[rayIndex], terrainBoundary[rayIndex+1] });
      shortestNormals[i] = normal;
      terrainNormals[rayIndex] = normal;
//      this.terrainNormals[rayIndex] = new LineSegment(contactPoint, contactPoint.add(normal));
    }
    Vector terrainNormal = avgVectors(shortestNormals);
    debugTerrainNormal = terrainNormal;
    
    RayPair maxPenetrationRay = terrainBoundaryRays[shortestRaysIndexes[0]]; // TODO: handle -1
    float distanceToTerrain = maxPenetrationRay.avgLength() - TANK_HEIGHT;
    
    if (distanceToTerrain < 0) {
      this.translate(maxPenetrationRay.first.getDirection().scale(distanceToTerrain));
      this.applyFriction(delta, terrainNormal);
      this.setVelocity(this.getVelocity().project(terrainNormal.getPerpendicular()));
      this.rotateToNormal(terrainNormal);
    }
  }
  private int[] indexesOfShortest(RayPair lines[]) {
    int k = 2;
    int bestIndexes[] = new int[k];
    float bestLengthSqs[] = new float[k];
    for (int i = 0; i < k; i++) { bestIndexes[i] = -1; }
    for (int i = 0; i < lines.length; i++) {
      float lengthSq = lines[i].avgLengthSquared();
      if (bestIndexes[0] == -1) {
        bestIndexes[0] = i;
        bestLengthSqs[0] = lengthSq;
      } else if (lengthSq < bestLengthSqs[0]) {
        bestIndexes[1] = bestIndexes[0];
        bestLengthSqs[1] = bestLengthSqs[0];
        bestIndexes[0] = i;
        bestLengthSqs[0] = lengthSq;
      } else if (bestIndexes[1] == -1) {
        bestIndexes[1] = i;
        bestLengthSqs[1] = lengthSq;
      } else if (lengthSq < bestLengthSqs[1]) {
        bestIndexes[1] = i;
        bestLengthSqs[1] = lengthSq;
      }
    }
    return bestIndexes;
  }
  private void rotateToNormal(Vector slopeNormal) {
    Vector vertical = new Vector(0, -1);
    double terrainAngle = Math.acos(slopeNormal.dot(vertical)) * 180.0/Math.PI;

    if(vertical.dot(slopeNormal.getPerpendicular()) > 0) {
      terrainAngle = -terrainAngle;
    }
    System.out.println("terrainNormal angle: " + terrainAngle + ", tank: " + this.getRotation());
    
    this.targetRotation = terrainAngle;
  }
  private Vector avgVectors(Vector vs[]) {
    if (vs.length <= 0) { return null; }
    Vector sum = new Vector(0, 0);
    for (Vector v : vs) {
      sum = sum.add(v);
    }
    return sum.scale(1.0f/(float)vs.length);
  }
  
  private void applyFriction(int delta, Vector terrainNormal) {
    float mue = 0.04f;
    Vector vNormal = this.getVelocity().project(terrainNormal);
    Vector vParallel = this.getVelocity().project(terrainNormal.getPerpendicular());
    float normalVelocityFactor = vNormal.length();
    Vector friction = vParallel.negate().setLength(mue*normalVelocityFactor*delta).clampLength(0, vParallel.length());
    this.setVelocity(this.getVelocity().add(friction));
//    System.out.println("Friction: " + friction + ", parallel: " + vParallel);
    debugFriction = friction;
  }
  
  public boolean checkTerrainCollision(int delta, Terrain terrain) {
    calculateTranslation(delta, terrain);
    
    return false; // we handled it ourselves, don't let others handle it.
  }
  
  @Override
  public void render(Graphics g) {
    if (showDebugRays) {
      renderDebugRays(g);
      return;
    }
    super.render(g);
    cannon.setX(this.getX());
    cannon.setY(this.getY());
    cannon.render(g);

    float bottomSpacing = 20;
    healthbar.render(g, this.getCoarseGrainedMaxY() + bottomSpacing, this.getX());
  }
    
  private void renderDebugRays(Graphics g) {

    if (nearestTerrainSlope != null) {
      nearestTerrainSlope.draw(g, Color.orange);
      this.bottomEdge().draw(g, Color.green);
    }
    if (debugTerrainNormal != null) {
      new LineSegment(getPosition(), getPosition().add(debugTerrainNormal.setLength(30))).draw(g, Color.pink);
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