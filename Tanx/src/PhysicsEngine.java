import java.util.ArrayList;

import jig.Collision;
import jig.Vector;

interface CollisionHandler<A extends PhysicsEntity, B extends PhysicsEntity> {
  void handleCollision(A a, B b, Collision c);
}
class TypeMatchingHandler<A extends PhysicsEntity, B extends PhysicsEntity> implements CollisionHandler<PhysicsEntity, PhysicsEntity> {
  Class<A> aClass;
  Class<B> bClass;
  CollisionHandler<A, B> handler;
  
  public TypeMatchingHandler(Class<A> aClass, Class<B> bClass, CollisionHandler<A, B> handler) {
    this.aClass = aClass;
    this.bClass = bClass;
    this.handler = handler;
  }
  
  public void handleCollision(PhysicsEntity a, PhysicsEntity b, Collision c) {
    // Check both orders.
    if (aClass.isInstance(a) && bClass.isInstance(b)) {
      handler.handleCollision(aClass.cast(a), bClass.cast(b), c);
    } else if (aClass.isInstance(b) && bClass.isInstance(a)) {
      handler.handleCollision(aClass.cast(b), bClass.cast(a), c);
    }
  }
}

public class PhysicsEngine {
	
	static public float GRAV_CONSTANT = .5f;
	static public float NORMAL_FRICTION = .1f;
	
	
	private ArrayList<PhysicsEntity> objects;
	private ArrayList<CollisionHandler<PhysicsEntity, PhysicsEntity>> collisionHandlers;
	private World world;
	
	
	public PhysicsEngine(ArrayList<PhysicsEntity> o, World w) {
		objects = o;
		world = w;
		collisionHandlers = new ArrayList<>();
	}
	
	public void addPhysicsEntity(PhysicsEntity e) {
		objects.add(e);
	}
	public void removePhysicsEntity(PhysicsEntity e) {
    objects.remove(e);
  }
	
	public <A extends PhysicsEntity, B extends PhysicsEntity> 
	void registerCollisionHandler(Class<A> aClass, Class<B> bClass, CollisionHandler<A,B> handler) {
    collisionHandlers.add(new TypeMatchingHandler<A,B>(aClass, bClass, handler));
  }
	
	public void update(int delta) {
		
		objects.forEach((n) -> applyPhysics(n, delta));

		applyCollisionDetection(delta);
		
		objects.removeIf(e -> e.getIsDead() || !world.geometry.tilesArea.contains(e.getX(), e.getY()));

	}
	
	private void applyPhysics(PhysicsEntity e, int delta) {	//still needs to handle collisions
		Vector A = e.getAcceleration();	//get movement acceleration
//		System.out.println("incoming" + A);
		A = applyGravity(A);	//add gravity to acceleration
//		System.out.println("post gravity " + A);
		//System.out.println("friction");
		//System.out.println(A);
		applyAccelerationtoVelocity(e, delta, A);	//change velocity
		applyTerminalVelocity(e);	//truncate if greater than terminal velocity


		translateEntity(e, delta);	//move the object
		e.update(delta, world.terrain);
	}
	
	private void applyCollisionDetection(int delta) {
	  int count = objects.size();
	  for (int x = 0; x < count; x++) {
	    PhysicsEntity a = objects.get(x);
	    for (int y = x+1; y < count; y++) {
	      PhysicsEntity b = objects.get(y);
	      if (a == b) { continue; }
	      checkCollision(delta, a, b);
	    }
	    checkTerrainCollision(delta, a);
	  }
  }
	
	private void checkTerrainCollision(int delta, PhysicsEntity entity) {
	  float radius = entity.getCoarseGrainedRadius();
	  Vector position = entity.getPosition();
	  if (world.terrain.checkCircularCollision(position, radius)) {
	    collisionHandlers.forEach(handler -> handler.handleCollision(entity, world.terrain, null));
	    resolveCollision(delta, entity, world.terrain, null);
	  }
	}
	
	private void checkCollision(int delta, PhysicsEntity a, PhysicsEntity b) {
	  Collision c = a.collides(b);
	  if (c != null) {
	    collisionHandlers.forEach(handler -> handler.handleCollision(a, b, c));
	    resolveCollision(delta, a, b, c);
	  }
	}
	
	private void resolveCollision(int delta, PhysicsEntity a, PhysicsEntity b, Collision c) {
//	  System.out.println("Before Resolving collision between p: " + a.getPosition() + " and p: " + b.getPosition() + " c: " + c.getMinPenetration().length());
//	  System.out.println("Before Resolving collision between v: " + a.getVelocity() + " and v: " + b.getVelocity());
	  float f = a.getVelocity().length() / b.getVelocity().length();
	  a.translate(a.getVelocity().negate().scale(delta));
	  b.translate(b.getVelocity().negate().scale(delta));
	  a.setVelocity(new Vector(0,0));
	  b.setVelocity(new Vector(0,0));
//	  System.out.println("After Resolving collision between " + a.getPosition() + " and " + b.getPosition() + " c: " + c.getMinPenetration().length());
	}
	
	private Vector applyGravity(Vector a) {
		a = a.setY(a.getY() + PhysicsEngine.GRAV_CONSTANT);
		return a;
	}
	
	
	//This function needs to be reworked, as the current implementation is not physically correct,
	//and today is friday, and i am ready to be done.
	/*private Vector applyFriction(Vector a, float friction) {
		float newA = a.length() - friction;
		if(newA < 0) {
			newA = 0;
		}
		a = a.setLength(newA);
		System.out.println(a);
		return a;
	}*/
	
	private void applyAccelerationtoVelocity(PhysicsEntity e, int delta, Vector a) {
		Vector v = e.getVelocity();
		v = v.add(a.scale(delta/1000f));
		e.setVelocity(v);
	}
	
	private void applyTerminalVelocity(PhysicsEntity e) {
		Vector v = e.getVelocity();
		float x = v.getX();
		float y = v.getY();
		Vector t = e.getTerminal();
		
		if(x > t.getX()) {
			x = t.getX();
		}
		
		if(y > t.getY()) {
			y = t.getY();
		}
		
		e.setVelocity(new Vector(x, y));
	}
	
	private void translateEntity(PhysicsEntity e, int delta) {
		e.translate(e.getVelocity().scale(delta));
	}
	
}
