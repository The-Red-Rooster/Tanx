import jig.Entity;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;

import jig.Vector;

enum camState {IDLE, MOVING, TRACKING};
public class Camera {
  
  public static float MAX_ZOOM = 4f;
  public static float DEFAULT_ZOOM = 1f;
  public static float MAX_CAMERA_SPEED = 2f;
  public static float MIN_CAMERA_SPEED = .2f;
  public static float CAM_ACCELERATION = 1.1f;
  public static float CAM_ZOOM_RATE = .0005f;
  
  /// Portion of the screen that the camera/world occupy.
  /// This may not be the whole screen if we want menus outside the scrolling area.
  protected Rectangle screen;
  
  /// Floating point size of the world.
  protected Rectangle world;
  
  /// Center of the camera in unscaled world points.
  private Vector center;

  ///Camera Motion Variables
  private camState state;
  private float distanceToGoal;
  private Vector velocity;
  private Vector goalPosition;

  ///Camera tracking variables
  private Entity trackedObject;
  private float startTrackHeight;
  private float startTrackZoom;
  
  private float zoom;
  
  public Camera(Rectangle screen, Rectangle world) {
    this.screen = screen;
    this.world = world;
    this.center = new Vector(world.getCenter());
    this.zoom = DEFAULT_ZOOM;
    this.velocity = new Vector(0, 0);
    state = camState.IDLE;
  }
  
  /// Use this to actually change the rendering.
  public void transformContext(Graphics g) {
    Vector translation = getTranslation();

    g.translate(-translation.getX()*zoom, -translation.getY()*zoom);
    g.scale(zoom, zoom);
  }
  
  public Vector worldLocationForScreenLocation(Vector screenLocation) {
    return screenLocation.add(getTranslation().scale(zoom)).scale(1/zoom);
  }
  public Vector screenLocationForWorldLocation(Vector worldLocation) {
    return worldLocation.scale(zoom).subtract(getTranslation().scale(zoom));
  }
  
  /// Center of the camera in unscaled world points.
  public Vector getCenter() { return center; }
  /// Set the center of the camera in unscaled world points.
  public void setCenter(Vector location) {
    this.center = location;
    clampViewPortToWorld();
  }
  /// Set the center of the camera by the specified unscaled world points.
  public void move(Vector worldDistance) {
    this.center = center.add(worldDistance);
    clampViewPortToWorld();
  }
  
  public float getZoom() { return this.zoom; }
  public void setZoom(float scale) {
    float fullWorldScale = screen.getWidth()/world.getWidth();
    this.zoom = Math.min(MAX_ZOOM, Math.max(fullWorldScale, scale));
    clampViewPortToWorld();
  }
  
  /// Shift center so that the whole ViewPort is within the World.
  private void clampViewPortToWorld() {
    Vector viewPortSize = viewPortSize();
    float minValidX = world.getMinX() + viewPortSize.getX()/2;
    float maxValidX = world.getMaxX() - viewPortSize.getX()/2;
    float minValidY = world.getMinY() + viewPortSize.getY()/2;
    float maxValidY = world.getMaxY() - viewPortSize.getY()/2;
    center = center.clampX(minValidX, maxValidX).clampY(minValidY, maxValidY);
  }
  
  /// The ViewPort is the Screen in world points.
  public Vector viewPortSize() {
    return new Vector(screen.getWidth()/zoom, screen.getHeight()/zoom);
  }
  /// The top left corner of the ViewPort. 
  /// The ViewPort is the Screen in world points.
  public Vector getViewPortOrigin() {
    Vector viewPortSize = viewPortSize();
    Vector camera = getCenter();
    return new Vector(camera.getX() - viewPortSize.getX()/2, camera.getY() - viewPortSize.getY()/2);
  }
  
  /// The distance from the top-left corner of the world to the top-left of the viewport.
  public Vector getTranslation() {
    return getViewPortOrigin().subtract(new Vector(world.getMinX(), world.getMinY()));
  }
  
  @Override
  public String toString() {
    return "location: " + center.toString() + ", zoom: " + zoom;
  }

  public void update(int delta){
    if (state == camState.MOVING){
      cameraMotionHandler(delta);
    } else if (state == camState.TRACKING){
      cameraTrackingHandler(delta);
    }
  }

  //Object Tracking
  private void cameraTrackingHandler(int delta){
    setCenter(trackedObject.getPosition());
    setTrackedZoom();
  }

  public void setTrackedZoom(){
    float heightDiff = startTrackHeight - trackedObject.getY();
    setZoom(-CAM_ZOOM_RATE*heightDiff + startTrackZoom);
  }

  public void trackObject(Entity e){
    cleanCameraState();
    trackedObject = e;
    startTrackHeight = e.getY();
    startTrackZoom = getZoom();
    state = camState.TRACKING;
  }

  private void cleanCameraState() {
    if (state == camState.TRACKING) {
      stopTracking();
    } else if (state == camState.MOVING) {
      stopMoving();
    }
  }

  public void stopTracking(){
    trackedObject = null;
    setZoom(startTrackZoom);
    state = camState.IDLE;
  }



  //camera smoothing code
  private void cameraMotionHandler(int delta){
    final float MIN_CAMERA_VELOCITY = .5f;
    double dist = getDistToGoal();
    if (dist > distanceToGoal/2){
      velocity = velocity.scale(CAM_ACCELERATION);
    } else {
      velocity = velocity.scale(1/CAM_ACCELERATION);
    }
    Vector move = velocity.scale(delta);

    if (move.length() <= MIN_CAMERA_VELOCITY){ move = move.setLength(MIN_CAMERA_VELOCITY); }
    if (move.length() >= dist || Float.isNaN(move.getX()) || Float.isNaN(move.getY())){
      stopMoving();
      setCenter(goalPosition);
    } else {
      move(move);
    }
  }

  public void stopMoving() {
    state = camState.IDLE;
    velocity = new Vector(0, 0);
  }

  private double getDistToGoal() {
    double x1 = goalPosition.getX();
    double x2 = center.getX();
    double y1 = goalPosition.getY();
    double y2 = center.getY();
    double ac = Math.abs(y2 - y1);
    double cb = Math.abs(x2 - x1);
    return Math.hypot(ac, cb);
  }

  public void moveTo(Vector position){
    cleanCameraState();
    goalPosition = position;
    clampGoal();
    if (goalPosition == center){ return; }
    state = camState.MOVING;
    float x1 = center.getX();
    float x2 = goalPosition.getX();
    float y1 = center.getY();
    float y2 = goalPosition.getY();
    Vector fullLength = new Vector(x2 - x1, y2 - y1);
    Vector unitVect = fullLength.scale(1/fullLength.length());
    velocity = unitVect.scale(MIN_CAMERA_SPEED);
    distanceToGoal = (float)getDistToGoal();
  }

  private void clampGoal() {
    Vector viewPortSize = viewPortSize();
    float minValidX = world.getMinX() + viewPortSize.getX()/2;
    float maxValidX = world.getMaxX() - viewPortSize.getX()/2;
    float minValidY = world.getMinY() + viewPortSize.getY()/2;
    float maxValidY = world.getMaxY() - viewPortSize.getY()/2;
    goalPosition = goalPosition.clampX(minValidX, maxValidX).clampY(minValidY, maxValidY);
  }

  public camState getState() { return state; }
}

/// When debug is true, it shows the full world and an border for the viewport.
class DebugCamera extends Camera {
  
  private boolean debug = false;
  
  public DebugCamera(Rectangle screen, Rectangle world) {
    super(screen, world);
  }
  
  public void toggleDebug() {
    this.debug = !this.debug;
  }
  
  @Override
  public void transformContext(Graphics g) {
    if (debug) {
      float toFit = screen.getWidth()/world.getWidth();
      g.scale(toFit, toFit);
    } else {
      super.transformContext(g);
    }
  }
  
  public void renderDebugOverlay(Graphics g) {
    Vector translation = getTranslation();
    g.setColor(Color.green);
    g.drawLine(world.getMinX(), world.getMinY(), translation.getX(), translation.getY());
    g.drawRect(translation.getX(), translation.getY(), viewPortSize().getX(), viewPortSize().getY());
  }
}