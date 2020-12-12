import java.util.ArrayList;

import org.newdawn.slick.Graphics;

import jig.ResourceManager;

public class MultiValueOption extends MenuOption {

	ArrayList<String> options;
	int curr;
	
	public MultiValueOption(int x, int y, String label, ArrayList<String> options) {
		super(x, y, label);
		this.options = options;
		curr = 0;
	}
	
	public void render(Graphics g, boolean selected) {
		g.drawString(label, x, y);
		int width = g.getFont().getWidth(label);
		g.drawString(options.get(curr), x + width + 5, y);
		if(selected) {
			int optionWidth = g.getFont().getWidth(options.get(curr));
			g.drawImage(ResourceManager.getImage(Tanx.WEAPON_POINTER), x + width + optionWidth + 10, y);
			g.drawImage(ResourceManager.getImage(Tanx.WEAPON_POINTER).getFlippedCopy(true, false), x - 25, y);
		}
	}
	
	public void next() {
		curr++;
		if(curr > options.size() - 1) curr = 0;
	}
	
	public void prev() {
		curr--;
		if(curr < 0) curr = options.size() - 1;
	}
	
	public String getSelection() {
		return options.get(curr);
	}
	
}
