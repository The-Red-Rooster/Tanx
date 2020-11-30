import org.newdawn.slick.Color;

import jig.ConvexPolygon;
import jig.Vector;

public class Projectile extends PhysicsEntity {
	
	public Projectile(float x, float y, Vector v) {
		super(x, y, 0, new Vector(5f, 5f));
		setVelocity(v);
		setAcceleration(new Vector(0,0));
		this.addShape(new ConvexPolygon(10), Color.blue, Color.blue);
	}
	
	public void explode() {
	  this.isDead = true;
	}
	
}
