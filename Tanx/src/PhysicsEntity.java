import jig.Entity;
import jig.Vector;

public class PhysicsEntity extends Entity {
  //constants

  //class variables
  protected boolean isDead;
  protected boolean onGround;
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
  
  /**
    Check for collision with the terrain. This method is specific to each entity because of
    differing entity shapes. The default implementation checks the coarse-grained bounding circle.
   */
  public boolean checkTerrainCollision(Terrain terrain) {
    float radius = this.getCoarseGrainedRadius();
    Vector position = this.getPosition();
    return terrain.checkCircularCollision(position, radius);
  }
  
  public void update(int delta) {
	  /*
	   	basic physics entities should probably do nothing,
	  	this method is just here for the more specific physics entities to inherit
	  	and allow for generalized calling by the engine
	  */
  }

  protected void setVelocity(Vector v){velocity = v;}
  protected void setAcceleration(Vector a){acceleration = a;}
  public Vector getAcceleration(){return acceleration;}
  public Vector getVelocity(){return velocity;}
  public float getDrag(){return drag;}
  public Vector getTerminal() {return terminal;}
  public boolean getIsDead() { return isDead; }
  public boolean getOnGround() { return onGround; }
  public void setOnGround(boolean b) { onGround = b; }
}
