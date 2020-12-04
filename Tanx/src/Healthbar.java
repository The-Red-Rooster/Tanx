import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class Healthbar {
  int health;
  int maxHealth;
  
  Healthbar(int initialHealth) {
    this.health = initialHealth;
    this.maxHealth = initialHealth;
  }
  
  public boolean getIsDead() {
    return health <= 0;
  }
  public void receiveDamage(int damage) {
    if (damage > 0) {
      health -= damage;
      if (health < 0) {
        health = 0;
      }
    }
  }
  public void receiveHealth(int bonusHealth) {
    if (bonusHealth > 0) {
      health += bonusHealth;
      if (health > maxHealth) {
        health = maxHealth;
      }
    }
  }
  
  public void render(Graphics g, float topY, float centerX) {
    float barWidth = 50;
    float barHeight = 20;
    float healthWidth = barWidth * (float)health / (float)maxHealth;
    float leadingX = centerX - barWidth/2;
    
    // gone health (overlaid on the left by the health)
    g.setColor(Color.red);
    g.fillRect(leadingX, topY, barWidth, barHeight);
    
    // current health
    g.setColor(Color.green);
    g.fillRect(leadingX, topY, healthWidth, barHeight);
    
    // border
    g.setColor(Color.black);
    g.drawRect(leadingX, topY, barWidth, barHeight);
  }
}
