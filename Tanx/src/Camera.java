import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;

import jig.Vector;

class Camera {
  
  static float MAX_ZOOM = 4f;
  
  /// Portion of the screen that the camera/world occupy.
  /// This may not be the whole screen if we want menus outside the scrolling area.
  Rectangle screen;
  
  /// Floating point size of the world.
  Rectangle world;
  
  /// Center of the camera in unscaled world points.
  private Vector worldLocation;
  
  private float zoom;
  
  Camera(Rectangle screen, Rectangle world) {
    this.screen = screen;
    this.world = world;
    this.worldLocation = new Vector(world.getCenter());
    this.zoom = 1.0f;
  }
  
  void transformContext(Graphics g) {
    Vector translation = getTranslation();

    g.translate(-translation.getX()*zoom, -translation.getY()*zoom);
    g.scale(zoom, zoom);
  }
  
  /// Screen size in world size points.
  public Vector viewPortSize() {
    return new Vector(screen.getWidth()/zoom, screen.getHeight()/zoom);
  }
  public Vector viewPortOrigin() {
    Vector viewPortSize = viewPortSize();
    Vector camera = getWorldLocation();
    return new Vector(camera.getX() - viewPortSize.getX()/2, camera.getY() - viewPortSize.getY()/2);
  }
  
  /// The distance from the top-left corner of the world to the top-left of the viewport.
  public Vector getTranslation() {
    return viewPortOrigin().subtract(new Vector(world.getMinX(), world.getMinY()));
  }
  
  public Vector getWorldLocation() { return worldLocation; }
  public void setWorldLocation(Vector location) {
    this.worldLocation = location;
    clampViewPortToWorld();
  }
  private void clampViewPortToWorld() {
    Vector viewPortSize = viewPortSize();
    float minValidX = world.getMinX() + viewPortSize.getX()/2;
    float maxValidX = world.getMaxX() - viewPortSize.getX()/2;
    float minValidY = world.getMinY() + viewPortSize.getY()/2;
    float maxValidY = world.getMaxY() - viewPortSize.getY()/2;
    worldLocation = worldLocation.clampX(minValidX, maxValidX).clampY(minValidY, maxValidY);
  }
  
  public float getZoom() { return this.zoom; }
  public void setZoom(float scale) {
    float fullWorldScale = screen.getWidth()/world.getWidth();
    this.zoom = Math.min(MAX_ZOOM, Math.max(fullWorldScale, scale));
    clampViewPortToWorld();
  }
  
  public Vector worldLocationForScreenLocation(Vector screenLocation) {
    return screenLocation.add(getTranslation().scale(zoom)).scale(1/zoom);
  }
  public Vector screenLocationForWorldLocation(Vector worldLocation) {
    return worldLocation.scale(zoom).subtract(getTranslation().scale(zoom));
  }
  
  @Override
  public String toString() {
    return "location: " + worldLocation.toString() + ", zoom: " + zoom;
  }
}

class DebugCamera extends Camera {
  
  private boolean debug = false;
  
  DebugCamera(Rectangle screen, Rectangle world) {
    super(screen, world);
  }
  
  public void toggleDebug() {
    this.debug = !this.debug;
  }
  
  @Override
  void transformContext(Graphics g) {
    if (debug) {
      float toFit = screen.getWidth()/world.getWidth();
      g.scale(toFit, toFit);
      
      Vector translation = getTranslation();
      g.setColor(Color.green);
      g.drawLine(world.getMinX(), world.getMinY(), translation.getX(), translation.getY());
      g.drawRect(translation.getX(), translation.getY(), viewPortSize().getX(), viewPortSize().getY());
    } else {
      super.transformContext(g);
    }
  }
}