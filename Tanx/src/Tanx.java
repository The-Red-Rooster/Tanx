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
 * @authors:
 * Matthew Scofield
 * Joseph Van Boxtel
 * Benjamin Eavenson
 * Henry Unruh
 *
 *    Flame Effect
 *    Artist: Division Plus
 *    links: https://opengameart.org/content/fantasy-character-npc-sprites
 *
 *    Sound Effects
 *    Artist: Juhani Junkala
 *    link: https://opengameart.org/content/512-sound-effects-8-bit-style
 *
 */
public class Tanx extends StateBasedGame {

	public static final int STARTUPSTATE = 0;
	public static final int PLAYINGSTATE = 1;
	public static final int GAMEOVERSTATE = 2;

	public static final String SPLASH_LOGO = "resources/tanxlogo.png";
	public static final String BACKGROUND_DESERT = "resources/desertBG.png";
	public static final String MENU_MUSIC = "resources/tanxMenuTheme.wav";
	public static final String POWER_GAUGE_OVERLAY = "resources/PowerGauge.png";
	public static final String FUEL_GAUGE_OVERLAY = "resources/FuelGauge.png";
	public static final String FUEL_GAUGE_ARROW = "resources/FuelGaugeArrow.png";
	public static final String WEAPON_POINTER = "resources/weaponPointer.png";
	public static final String DASH_BACKGROUND = "resources/Dashboard_resize.png";
	public static final String HEALTH_BAR = "resources/healthBar.png";
	public static final String FOCUS_ARROW = "resources/tankPointer.png";
	public static final String TIMER_GAUGE = "resources/timerGauge.png";
	public static final String BANG_EXPLOSIONIMG_RSC = "resources/explosion.png";
	public static final String BANG_EXPLOSIONSND_RSC = "resources/explosion.wav";
	public static final String PLAYER_WIN_BLUE = "resources/blue_win.png";
  public static final String PLAYER_WIN_RED = "resources/red_win.png";
  public static final String PLAYER_WIN_YELLOW = "resources/yellow_win.png";
  public static final String PLAYER_WIN_GREEN = "resources/green_win.png";
  public static final String NO_WINNER_MSG = "resources/no_winner.png";
  public static final String RESET_MSG = "resources/pushSpaceResetMsg.png";
	public static final String TANK_SPRITE = "resources/tankBase.png";
	public static final String BASE_CANNON_SPRITE = "resources/tankBaseCannon.png";
	public static final String AMMO_POWERUP_SPRITE = "resources/weaponPowerupSprite.png";
	public static final String HEALTH_POWERUP_SPRITE = "resources/hpPowerupSprite.png";
  public static final String TERRAIN_IMG = "resources/dirtTile.png";
  public static final String FIRE_ANIMATION = "resources/Flame.png";
  public static final String JET_SOUND = "resources/sfx_exp_shortest_hard5.wav";
  
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
   *
   *    Flame Effect
   *    Artist: Division Plus
   *    links: https://opengameart.org/content/fantasy-character-npc-sprites
   *
   *    Sound Effects
   *    Artist: Juhani Junkala
   *    link: https://opengameart.org/content/512-sound-effects-8-bit-style
   *
   *
	 */
	public Tanx(String title, int width, int height) {
		super(title);
		ScreenHeight = height;
		ScreenWidth = width;

	}


	@Override
	public void initStatesList(GameContainer container) throws SlickException {
	  addState(new StartUpState());
		//      addState(new GameOverState());
		addState(new PlayingState());

		// preload all the resources to avoid warnings & minimize latency...
		//ex: ResourceManager.loadImage(BALL_BALLIMG_RSC);
		ResourceManager.loadImage(SPLASH_LOGO);
		ResourceManager.loadImage(BACKGROUND_DESERT);
		ResourceManager.loadSound(MENU_MUSIC);
    ResourceManager.loadImage(FUEL_GAUGE_ARROW);
    ResourceManager.loadImage(FUEL_GAUGE_OVERLAY);
    ResourceManager.loadImage(POWER_GAUGE_OVERLAY);
    ResourceManager.loadImage(WEAPON_POINTER);
		ResourceManager.loadImage(HEALTH_BAR);
		ResourceManager.loadImage(FOCUS_ARROW);
		ResourceManager.loadImage(TIMER_GAUGE);
		ResourceManager.loadImage(PLAYER_WIN_BLUE);
    ResourceManager.loadImage(PLAYER_WIN_GREEN);
    ResourceManager.loadImage(PLAYER_WIN_RED);
    ResourceManager.loadImage(PLAYER_WIN_YELLOW);
    ResourceManager.loadImage(NO_WINNER_MSG);
    ResourceManager.loadImage(RESET_MSG);
    ResourceManager.loadImage(TERRAIN_IMG);
		ResourceManager.loadImage(Tanx.BANG_EXPLOSIONIMG_RSC);
		ResourceManager.loadSound(Tanx.BANG_EXPLOSIONSND_RSC);
		ResourceManager.loadImage(TANK_SPRITE);
		ResourceManager.loadImage(BASE_CANNON_SPRITE);
		ResourceManager.loadImage(AMMO_POWERUP_SPRITE);
		ResourceManager.loadImage(HEALTH_POWERUP_SPRITE);
		ResourceManager.loadImage(FIRE_ANIMATION);
		ResourceManager.loadSound(JET_SOUND);
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
