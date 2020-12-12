import org.newdawn.slick.Graphics;

import jig.Entity;
import jig.ResourceManager;

public class MenuOption extends Entity {

	protected String label;
	protected int x;
	protected int y;
	
	public MenuOption(int x, int y, String label) {
		super(x,y);
		this.label = label;
		this.x = x;
		this.y = y;
	}
	
	public String getLabel() {
		return label;
	}
	

	public void render(Graphics g, boolean selected) {
		super.render(g);
		g.drawString(label, x, y);
		if(selected) {
			g.drawImage(ResourceManager.getImage(Tanx.WEAPON_POINTER), x - 20, y);
		}
		
	}
	
}
