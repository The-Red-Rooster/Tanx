import java.util.ArrayList;
import org.newdawn.slick.GameContainer;
import jig.ResourceManager;
import org.newdawn.slick.*;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import jig.Entity;
import jig.Vector;

enum phase {MOVEFIRE, FIRING, CHARGING, TURNCHANGE, GAMEOVER};

public class PlayingState extends BasicGameState {
	
  final int NO_WINNER_ID = -1;
  static public int TURNLENGTH = 10*1000;
  static public int INPUT_TIMER_CD = 100;
  static public int FIRING_TIMEOUT = 5*1000;
  static public int SHOTRESOLVE_TIMEOUT = 2*1000;
  static public int BOTTOM_UI_HEIGHT = 300;
  
  PlayerConfigurator PC;
  Rectangle worldBounds;
	
	World world;
	DebugCamera camera;
	Ui ui;
  ArrayList<PhysicsEntity> PE_list;
  PhysicsEngine PE;
  ArrayList<Player> players;
  ProjectileSystem projectileSystem;
  ExplosionSystem explosionSystem;
  FireSystem fireSystem;
  phase state;
  Projectile activeProjectile;
  int pIndex;
  int turnTimer;
  ActiveTankArrow tankPointer;
  boolean toggleCheats;
  int cleanInputTimer;

  	public void setPlayerConfig(PlayerConfigurator pc) {
  		PC = pc;
  	}
  	
  	public void setWorldBounds(Rectangle wb) {
  		worldBounds = wb;
  	}
  
	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
    Entity.setCoarseGrainedCollisionBoundary(Entity.CIRCLE);
	}
	
	@Override
  public void enter(GameContainer container, StateBasedGame game)
    throws SlickException {
	
    Rectangle screenBounds = new Rectangle(0, 0, container.getWidth(), container.getHeight() - BOTTOM_UI_HEIGHT/2);//new Rectangle(0, 0, container.getScreenWidth(), container.getScreenHeight());
    Rectangle bottomUiBounds = new Rectangle(0, 0, screenBounds.getWidth(), BOTTOM_UI_HEIGHT);
    Vector bottomUiPosition = new Vector(screenBounds.getWidth()/4, BOTTOM_UI_HEIGHT);
    ui = new Ui(bottomUiBounds, bottomUiPosition);
    world = new World(worldBounds);
    camera = new DebugCamera(screenBounds, worldBounds);
    System.out.println("world size: " + worldBounds + ", screen size: " + screenBounds);
    world.loadLevel("YAY");
    explosionSystem = new ExplosionSystem();
    projectileSystem = new ProjectileSystem();
    fireSystem = new FireSystem();

    PE_list = new ArrayList<PhysicsEntity>();

    players = new ArrayList<Player>();

    players = PC.config();

    for (Player p: players){
      for (Tank t: p.getTanks()){
        PE_list.add(t);
      }
    }
    tankPointer = new ActiveTankArrow(0, 0);
    pIndex = 0;
    changePlayer();

    PE = new PhysicsEngine(PE_list, world);
    
    PE.setCollisionPredicate((a, b) -> {
      boolean isAProjectile = a instanceof Projectile;
      boolean isBProjectile = b instanceof Projectile;
      if (isAProjectile && isBProjectile) return false;
      boolean isAFire = a instanceof GroundFire;
      boolean isBFire = b instanceof GroundFire;
      if (isAFire && isBFire) return false;
      if ((isAFire && isBProjectile) || (isAProjectile && isBFire)) {
        return false;
      }
      if (isAFire || isBFire) return true;
      if (isAProjectile || isBProjectile) return true;
      boolean isATank = a instanceof Tank;
      boolean isBTank = b instanceof Tank;
      if (isATank || isBTank) return true;
      return false;
    });
    
    // Example use case. Probably not complete.
    PE.registerCollisionHandler(Tank.class, Terrain.class, (tank, terrain, c) -> {
      if (tank.getY() < terrain.getY()) {
        tank.setOnGround(true);
      }
    });

    PE.registerCollisionHandler(Powerup.class, Tank.class, (powerup, tank, c) -> {
      powerup.usePowerup(tank);
    });

    PE.registerCollisionHandler(Tank.class, GroundFire.class, (tank, fire, c) -> {
      fire.applyFire(tank);
    });

    PE.registerCollisionHandler(Powerup.class, GroundFire.class, (powerup, fire, c) -> {
      fire.setIsDead(true);
      powerup.setIsDead(true);
      ResourceManager.getSound(Tanx.FIRE_DEBUFF_SND).play();
    });

    PE.registerCollisionHandler(Projectile.class, PhysicsEntity.class, (projectile, obstacle, c) -> {
      if (obstacle instanceof Projectile) { return; } // Don't explode on other projectiles.
      if (obstacle instanceof GroundFire) { return; } // Don't explode on GroundFire Entities
      if (projectile instanceof FireMiniBomb) {
        GroundFire newFire = new GroundFire(projectile.getX(), projectile.getY() + FireMiniBomb.Y_SPAWN_OFFSET);
        fireSystem.addFire(newFire);
        PE.addPhysicsEntity(newFire);
      }
      if (projectile == activeProjectile && state == phase.FIRING) { turnTimer = SHOTRESOLVE_TIMEOUT; }
      projectile.explode();
      int blastRadius = projectile.getExplosionRadius();
      int damage = projectile.getDamage();
      Vector location = projectile.getPosition();
      explosionSystem.addExplosion(location, (float)blastRadius);
      world.terrain.setTerrainInCircle(location, blastRadius, Terrain.TerrainType.OPEN);
      
      PE.forEachEntityInCircle(location, (float)blastRadius, (e) -> {
        if (e instanceof Tank) {
          Tank tank = (Tank)e;
          tank.takeDamage(damage);
        }
      });
    });
    
    //camera.toggleDebug();
  }

	@Override
	public void render(GameContainer container, StateBasedGame game,
			Graphics g) throws SlickException {
    Tanx bg = (Tanx) game;

    g.pushTransform();
    camera.transformContext(g);
    // Render anything that should be affected by the camera location.

    world.terrain.render(g);
    PE_list.forEach((e) -> e.render(g));
    players.forEach((p) -> p.render(g));
    explosionSystem.render(g);
    projectileSystem.render(g);
    fireSystem.render(g);

    //placeholder, should put an arrow sprite pointing to currently active tank
    if (state == phase.MOVEFIRE) {
      tankPointer.render(g);
    }

    camera.renderDebugOverlay(g);

    g.popTransform();
    // Render anything that shouldn't be transformed below here.
    ui.render(g);
    if (toggleCheats) {
      Player current = players.get(pIndex);
      float yOffset = 20;
      g.drawString("CHEATS ON", 0, yOffset);
      if (current.isInfFuel()) {
        yOffset += 20;
        g.drawString("Infinite Fuel On!", 0, yOffset);
      }
      if (current.isInfHealth()) {
        yOffset += 20;
        g.drawString("Current Tank has Infinite Health!", 0, yOffset);
      }
    }
    if (state == phase.GAMEOVER) {
      renderGameOver(g, bg);
    }
  }

  private void renderGameOver(Graphics g, Tanx bg) {
	  final float GAME_OVER_X = bg.ScreenWidth/2 - 200;
	  final float GAME_OVER_Y = 0;
    final float RESET_OFFSETX = 0;
    final float RESET_OFFSETY = 100;
    final float IMG_SCALE = 2f;


    //setup gameover screen
    Image playerWinImg;
    int winningPlayer = getWinningPlayer();
    switch(winningPlayer) {
      case 1:
        playerWinImg = ResourceManager.getImage(Tanx.PLAYER_WIN_BLUE);
        break;
      case 2:
        playerWinImg = ResourceManager.getImage(Tanx.PLAYER_WIN_RED);
        break;
      case 3:
        playerWinImg = ResourceManager.getImage(Tanx.PLAYER_WIN_GREEN);
        break;
      case 4:
        playerWinImg = ResourceManager.getImage(Tanx.PLAYER_WIN_YELLOW);
        break;
      default:
        playerWinImg = ResourceManager.getImage(Tanx.NO_WINNER_MSG);
    }
    g.drawImage(playerWinImg.getScaledCopy(IMG_SCALE), GAME_OVER_X, GAME_OVER_Y);
    g.drawImage(ResourceManager.getImage(Tanx.RESET_MSG), GAME_OVER_X + RESET_OFFSETX, GAME_OVER_Y + RESET_OFFSETY);
  }

  private int getWinningPlayer() {
    for (Player p: players) {
      if (!p.isDead()) { return p.getPlayerId(); }
    }
    return NO_WINNER_ID;
  }

  @Override
	public void update(GameContainer container, StateBasedGame game,
			int delta) throws SlickException {
	  Tanx tg = (Tanx)game;
		Input input = container.getInput();
		Player player = players.get(pIndex);
    turnTimer -= delta;
    
    updateState(input, player, delta, tg);
    explosionSystem.update(delta);
    projectileSystem.update(delta);
		for(Player p: players){p.update(delta);}
    PE.update(delta);
		controlCamera(delta, input);
		world.update(delta, PE, players);
		ui.update(delta, players.get(pIndex), turnTimer, state);
		tankPointer.update(delta);
		cleanInputHandler(delta, input);
	}

  private void cleanInputHandler(int delta, Input input) {
	  cleanInputTimer += delta;
	  if (cleanInputTimer <= INPUT_TIMER_CD) {
	    input.clearKeyPressedRecord();
	    cleanInputTimer = 0;
    }
  }

  private void updateState(Input input, Player player, int delta, Tanx tg) {
    if (state == phase.CHARGING) {
      if (input.isKeyDown(Input.KEY_SPACE) && turnTimer > 0){
        player.charging(delta);
      } else {
        player.fire((Projectile p) -> {
          activeProjectile = p;
          projectileSystem.addProjectile(p);
          PE.addPhysicsEntity(activeProjectile);
          camera.trackObject(activeProjectile);
        });
        state = phase.FIRING;
        turnTimer = FIRING_TIMEOUT;
      }
    }
    if (state == phase.MOVEFIRE){
      cheatCodeHandler(input, player);
      if (player.getTank().getVelocity().lengthSquared() > 0) { camera.moveTo(player.getTank().getPosition()); }
      Tank currentTank = players.get(pIndex).getTank();
      tankPointer.pointTo(currentTank.getPosition());
      if (turnTimer <= 0){
        changePlayer();
      }
      if (input.isKeyDown(Input.KEY_E)){
        currentTank.rotate(Direction.RIGHT, delta);
      } else if (input.isKeyDown(Input.KEY_Q)){
        player.rotate(Direction.LEFT, delta);
      }
      
      if (input.isKeyPressed(Input.KEY_C)) {
        player.nextWeapon();
      }
      if (input.isKeyPressed(Input.KEY_Z)) {
        player.prevWeapon();
      }

      if (input.isKeyDown(Input.KEY_SPACE)) {
        state = phase.CHARGING;
      }

      if (input.isKeyDown(Input.KEY_LCONTROL)) {
        players.get(pIndex).useJets(delta);
      }
                                  
    } else if(state == phase.FIRING) {
      if (turnTimer <= 0) { camera.stopTracking(); changePlayer(); }
    } else if (state == phase.TURNCHANGE) {
        turnTimer = TURNLENGTH;
        state = phase.MOVEFIRE;
    } else if (state == phase.GAMEOVER) {
      if (input.isKeyDown(Input.KEY_SPACE)) {
        tg.enterState(Tanx.STARTUPSTATE);
        input.clearKeyPressedRecord();
      }
    }
  }

  private void cheatCodeHandler(Input input, Player player) {
    if (input.isKeyPressed(Input.KEY_F1)){
      toggleCheats = !toggleCheats;
    }
    if (toggleCheats){
      if (input.isKeyPressed(Input.KEY_F2)) {
        //give player all weapons
        player.giveAllWeapons();
      }
      if (input.isKeyPressed(Input.KEY_F3)) {
        //infinate jet fuel
        player.toggleInfFuel();
      }
      if (input.isKeyPressed(Input.KEY_F4)) {
        //infinate health
        player.toggleInfHealth();
      }
      if (input.isKeyPressed(Input.KEY_F5)) {
        //kill tank
        player.getTank().killTank();
        changePlayer();
      }
    }

  }

  private void changePlayer() {
    if (isGameOver()) {
      state = phase.GAMEOVER;
      camera.setZoom(.1f);
      turnTimer = TURNLENGTH;
      System.out.println("Game is over!");
      return;
    }
    activeProjectile = null;
    state = phase.TURNCHANGE;
    turnTimer = FIRING_TIMEOUT;
    Player currentPlayer;
    do {
      pIndex ++;
      if (pIndex >= players.size()){pIndex = 0;}
      currentPlayer = players.get(pIndex);
    } while (currentPlayer.isDead());
    currentPlayer.startTurn();
    camera.moveTo(currentPlayer.getTank().getPosition());
    tankPointer.pointTo(currentPlayer.getTank().getPosition());
    fireSystem.updateTurn();
  }
  private boolean isGameOver() {
    int livingPlayersCount = 0;
    for (Player p : players) {
      if (!p.isDead()) { livingPlayersCount++; }
    }
    return livingPlayersCount < 2;
  }

  private void controlCamera(int delta, Input input) {
	  if (camera.getState() == camState.IDLE){
      if (input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
        camera.setZoom(camera.getZoom() + 0.25f);
      }
      if (input.isMousePressed(Input.MOUSE_RIGHT_BUTTON)) {
        camera.setZoom(camera.getZoom() - 0.25f);
      }
      if (input.isKeyPressed(Input.KEY_O)) {
        camera.toggleDebug();
      }
      if (input.isKeyDown(Input.KEY_LEFT)) {
        camera.move(new Vector(-delta/3, 0));
      }
      if (input.isKeyDown(Input.KEY_RIGHT)) {
        camera.move(new Vector(delta/3, 0));
      }
      if (input.isKeyDown(Input.KEY_UP)) {
        camera.move(new Vector(0, -delta/3));
      }
      if (input.isKeyDown(Input.KEY_DOWN)) {
        camera.move(new Vector(0, delta/3));
      }
    }
    camera.update(delta);
	}

	@Override
	public int getID() {
		return Tanx.PLAYINGSTATE;
	}
}
