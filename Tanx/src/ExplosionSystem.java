

import java.util.ArrayList;
import java.util.Iterator;

import org.newdawn.slick.Graphics;

import jig.Vector;

public class ExplosionSystem {
	
	ArrayList<Bang> explosions;

	public ExplosionSystem() {
		explosions = new ArrayList<Bang>();
	}
	
	public void addExplosion(Vector position, float radius) {
		explosions.add(new Bang(position.getX(), position.getY(), radius));
	}
	
	public void update(int delta) {
		// check if there are any finished explosions, if so remove them
		for (Iterator<Bang> i = explosions.iterator(); i.hasNext();) {
			if (!i.next().isActive()) {
				i.remove();
			}
		}
	}
	
	public void render(Graphics g) {
		for (Bang b : explosions) {
			b.render(g);
		}
	}
}
