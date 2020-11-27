import java.util.LinkedList;

import jig.Entity;
import jig.Shape;
import jig.Vector;

public class PhysicsEntity extends Entity {
  //constants

  //class variables
  protected boolean isDead;
  private Vector acceleration;
  private Vector velocity;
  private float drag;
  private Vector terminal;

  public PhysicsEntity (final float x, final float y, final float d, final Vector t){
    super(x,y);
    isDead = false;
    acceleration = new Vector(0, 0);
    velocity = new Vector(0, 0);
    drag = d;
    terminal = t;
  }
  
  public boolean checkTerrainCollision(int delta, Terrain terrain) {
	  /*
	   	Check for collision with the terrain. This method is specific to each entity because of
	   	differing entity shapes.
	   */
    LinkedList<Shape> shapes = getShapes();
    Vector position = getPosition();
    float radius = getCoarseGrainedRadius();
    for(int i = 0; i < shapes.size(); i++) {
      Shape s = shapes.get(i);
      switch(s.getPointCount()) {
      case 4:
        if(terrain.checkRectangularCollision(new Vector(getX() - s.getWidth()/2, getY() - s.getHeight()/2 ), new Vector(getX() + s.getWidth()/2, getY() + s.getHeight()/2))) {
          return true;
        }
        break;
      default:
        if(terrain.checkCircularCollision(position, radius)) {
          return true;
        }
      }
    }
    return false;
  }

  protected void setVelocity(Vector v){velocity = v;}
  protected void setAcceleration(Vector a){acceleration = a;}
  public Vector getAcceleration(){return acceleration;}
  public Vector getVelocity(){return velocity;}
  public float getDrag(){return drag;}
  public Vector getTerminal() {return terminal;}
  public boolean getIsDead() { return isDead; };
}
