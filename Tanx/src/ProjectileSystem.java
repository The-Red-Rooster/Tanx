import org.newdawn.slick.Graphics;
import java.util.ArrayList;
import java.util.Iterator;

public class ProjectileSystem {
  ArrayList<Projectile> projectiles;
  ArrayList<Projectile> toAdd;

  public ProjectileSystem() {
    projectiles = new ArrayList<Projectile>();
    toAdd = new ArrayList<Projectile>();
  }

  public void addProjectile (Projectile p) {
    toAdd.add(p);
  }

  public void update(int delta) {
    for (Projectile p: projectiles) {
      p.update(delta);
    }
    projectiles.removeIf(e -> e.getIsDead() );
    projectiles.addAll(toAdd);
    toAdd.clear();
    System.out.println(projectiles);
  }

  public void render(Graphics g) {
    for (Projectile p : projectiles) {
      p.render(g);
    }
  }

}