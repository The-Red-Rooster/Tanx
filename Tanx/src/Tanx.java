import jig.ResourceManager;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

/**
 * A Simple Game of Tanx. (Skeleton leveraged from Bounce game.)
 *
 * The game has three states: StartUp, Playing, and GameOver, the game
 * progresses through these states based on the user's input and the events that
 * occur. Each state is modestly different in terms of what is displayed and
 * what input is accepted.
 *
 *
 *
 * Skeleton Code Copied from Bounce by wallaces
 * @author Matthew Scofield
 *
 */
public class Tanx extends StateBasedGame {

	public static final int STARTUPSTATE = 0;
	public static final int PLAYINGSTATE = 1;
	public static final int GAMEOVERSTATE = 2;

	public static final String FUEL_GAUGE_OVERLAY = "resources/FuelGauge.png";
	public static final String FUEL_GAUGE_ARROW = "resources/FuelGaugeArrow.png";
	public static final String WEAPON_POINTER = "resources/weaponPointer.png";
	public static final String HEALTH_BAR = "resources/healthBar.png";
	public static final String FOCUS_ARROW = "resources/tankPointer.png";
	public static final String TIMER_GAUGE = "resources/timerGauge.png";
	public static final String BANG_EXPLOSIONIMG_RSC = "resources/explosion.png";
	public static final String BANG_EXPLOSIONSND_RSC = "resources/explosion.wav";
	public final int ScreenWidth;
	public final int ScreenHeight;

	/**
	 * Create the Keep Away frame, saving the width and height for later use.
	 *
	 * @param title
	 *            the window's title
	 * @param width
	 *            the window's width
	 * @param height
	 *            the window's height
	 */
	public Tanx(String title, int width, int height) {
		super(title);
		ScreenHeight = height;
		ScreenWidth = width;

	}


	@Override
	public void initStatesList(GameContainer container) throws SlickException {
		//      addState(new StartUpState());
		//      addState(new GameOverState());
		addState(new PlayingState());

		// preload all the resources to avoid warnings & minimize latency...
		//ex: ResourceManager.loadImage(BALL_BALLIMG_RSC);
    ResourceManager.loadImage(FUEL_GAUGE_ARROW);
    ResourceManager.loadImage(FUEL_GAUGE_OVERLAY);
    ResourceManager.loadImage(WEAPON_POINTER);
		ResourceManager.loadImage(HEALTH_BAR);
		ResourceManager.loadImage(FOCUS_ARROW);
		ResourceManager.loadImage(TIMER_GAUGE);
		ResourceManager.loadImage(Tanx.BANG_EXPLOSIONIMG_RSC);
		ResourceManager.loadSound(Tanx.BANG_EXPLOSIONSND_RSC);
    
	}

	public static void main(String[] args) {
		AppGameContainer app;
		try {
			int height = World.tileLength * 20;
			int width = World.tileLength * 40;
			app = new AppGameContainer(new Tanx("Tanx", width, height));
			app.setDisplayMode(width, height, false);
			app.setVSync(true);
			app.start();
		} catch (SlickException e) {
			e.printStackTrace();
		}

	}
}
