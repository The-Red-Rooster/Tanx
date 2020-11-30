

import jig.Entity;
import jig.ResourceManager;

import org.newdawn.slick.Animation;

/**
 * A class representing a transient explosion. The game should monitor
 * explosions to determine when they are no longer active and remove/hide
 * them at that point.
 */
class Bang extends Entity {
	private Animation explosion;

	public Bang(final float x, final float y, final float radius) {
		super(x, y);
		explosion = new Animation(ResourceManager.getSpriteSheet(
				Tanx.BANG_EXPLOSIONIMG_RSC, 64, 64), 0, 0, 22, 0, true, 50,
				true);
		this.setScale(radius / 32);
		addAnimation(explosion);
		explosion.setLooping(false);
		ResourceManager.getSound(Tanx.BANG_EXPLOSIONSND_RSC).play();
	}

	public boolean isActive() {
		return !explosion.isStopped();
	}
}