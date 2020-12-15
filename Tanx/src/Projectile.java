import org.newdawn.slick.Color;

import jig.ConvexPolygon;
import jig.Vector;

public class Projectile extends PhysicsEntity {
  private int explosionRadius;
  private int damage;
	

	enum TerrainInteraction{
		BASIC,
		MOUNTAIN_MAKER,
		CLUSTER
	}
	
	protected TerrainInteraction TI;
	
	public Projectile(float x, float y, Vector v, int r, int d) {
		super(x, y, 0, new Vector(5f, 5f));
		setVelocity(v);
		setAcceleration(new Vector(0,0));
		this.addShape(new ConvexPolygon(5), Color.gray, Color.black);

		TI = TerrainInteraction.BASIC;

		explosionRadius = r;
		damage = d;

	}
	public void update(int delta) {
	  
	}
	
	public void explode() {
	  setDead(true);
	}

	public int getExplosionRadius() {return explosionRadius; }

	public int getDamage() { return damage; }
	
	public TerrainInteraction getTerrainInteraction() {
		return TI;
	}
	
}
