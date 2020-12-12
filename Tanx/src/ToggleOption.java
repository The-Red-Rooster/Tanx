import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class ToggleOption extends MenuOption {

	boolean state;
	
	public ToggleOption(int x, int y, String label, boolean initState) {
		super(x, y, label);
		state = initState;
	}
	
	
	public void render(Graphics g, boolean selected) {
		super.render(g, selected);
		int width = g.getFont().getWidth(label);
		g.setColor(state ? Color.green : Color.red);
		g.drawString(state ? "ON" : "OFF", x + width + 5, y);
		g.setColor(Color.white);
	}
	
	public void toggle() {
		state = !state;
	}
	
	public boolean getSelection() {
		return state;
	}
}
