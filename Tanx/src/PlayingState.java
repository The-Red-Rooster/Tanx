import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.Color;

import jig.Entity;
import jig.Vector;

enum phase {MOVEFIRE, FIRING};

public class PlayingState extends BasicGameState {
  static private int TURNLENGTH = 11*1000;
  static private int FIRING_TIMEOUT = 5*1000;
	World world;
	DebugCamera camera;
  ArrayList<PhysicsEntity> PE_list;
  PhysicsEngine PE;
  ArrayList<Player> players;
  phase state;
  Projectile activeProjectile;
  int pIndex;
  int turnTimer;

	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
		Entity.setCoarseGrainedCollisionBoundary(Entity.CIRCLE);
		Rectangle worldBounds = new Rectangle(0, 0, container.getWidth()*2, container.getHeight()*2);
		Rectangle screenBounds = new Rectangle(0, 0, container.getWidth(), container.getHeight());//new Rectangle(0, 0, container.getScreenWidth(), container.getScreenHeight());
		world = new World(worldBounds);
		camera = new DebugCamera(screenBounds, worldBounds);
		System.out.println("world size: " + worldBounds + ", screen size: " + screenBounds);
		world.loadLevel("YAY");
	}
	
	@Override
  public void enter(GameContainer container, StateBasedGame game)
    throws SlickException {
    
    PE_list = new ArrayList<PhysicsEntity>();

    PE_list.add(new Projectile(20, 300, new Vector(2f, -2f)));

    players = new ArrayList<Player>();

    //setup players test-THIS SHOULD BE SETUP IN A LEVEL CONFIG
    players.add(new Player(Color.blue, 1));
    players.add(new Player(Color.green, 2));
    players.get(0).addTank(50, 400);
    players.get(0).addTank(500, 400);
    players.get(1).addTank(200, 400);
    players.get(1).addTank(800, 400);
    //end of test stub

    for (Player p: players){
      for (Tank t: p.getTanks()){
        PE_list.add(t);
      }
    }

    state = phase.MOVEFIRE;
    pIndex = 0;
    turnTimer = TURNLENGTH;

    PE = new PhysicsEngine(PE_list, world);
    
    // Example use case. Probably not complete.
    PE.registerCollisionHandler(Tank.class, Terrain.class, (tank, terrain, c) -> {
        tank.setOnGround(true);	//if we've collided with the ground, we should set onGround true
        
        tank.setVelocity(tank.getVelocity().scale(.99f));
        
       /* float v = tank.getVelocity().getX();
        if(v > 0) { //if we have rightward velocity but there is a slope in the way, move us up
        	if(terrain.checkLineCollision(
        			new Vector(tank.getX() + Tank.TANK_WIDTH/2 + 1, tank.getY() - Tank.TANK_HEIGHT/2),
        			new Vector(tank.getX() + Tank.TANK_WIDTH/2 + 1, tank.getY() + Tank.TANK_HEIGHT/4))) {
        		tank.setY(tank.getY() - 10);
        	}
        }
        
        if(v < 0) {	//if we have leftward velocity but there is a slope in the way, move us up
        	if(terrain.checkLineCollision(
        			new Vector(tank.getX() - Tank.TANK_WIDTH/2 - 1, tank.getY() - Tank.TANK_HEIGHT/2),
        			new Vector(tank.getX() - Tank.TANK_WIDTH/2 - 1, tank.getY() + Tank.TANK_HEIGHT/4))){
        		tank.setY(tank.getY() - 10);
        	}
        }*/
    });
    
    PE.registerCollisionHandler(Projectile.class, PhysicsEntity.class, (projectile, obstacle, c) -> {
      if (obstacle instanceof Projectile) { return; } // Don't explode on other projectiles.
      if (projectile == activeProjectile && state == phase.FIRING) { changePlayer(); }
      projectile.explode();
    });
    
    camera.toggleDebug();
  }

	@Override
	public void render(GameContainer container, StateBasedGame game,
			Graphics g) throws SlickException {
		Tanx bg = (Tanx) game;
		
		g.pushTransform();
		camera.transformContext(g);
		// Render anything that should be affected by the camera location.

    world.terrain.render(g);
		PE_list.forEach((e)->e.render(g));
		players.forEach((t) ->t.render(g));

		//placeholder, should put an arrow sprite pointing to currently active tank
    if (state == phase.MOVEFIRE){
      Tank currentTank = players.get(pIndex).getTank();
      g.drawString("Active", currentTank.getX() - 20, currentTank.getY() + 30);
      g.drawString(Integer.toString(turnTimer/1000), currentTank.getX() - 40, currentTank.getY() + 30);
    }
		
		camera.renderDebugOverlay(g);
		
		g.popTransform();
		// Render anything that shouldn't be transformed below here.
	}

	@Override
	public void update(GameContainer container, StateBasedGame game,
			int delta) throws SlickException {
		Input input = container.getInput();

		turnTimer -= delta;
		if (state == phase.MOVEFIRE){
			Tank currentTank = players.get(pIndex).getTank();

			if (input.isKeyDown(Input.KEY_E)){
				currentTank.rotate(Direction.RIGHT, delta);
			} else if (input.isKeyDown(Input.KEY_Q)){
				currentTank.rotate(Direction.LEFT, delta);
			}
      
			if(input.isKeyDown(Input.KEY_A)) {
				currentTank.move(Direction.LEFT);
			}else if(input.isKeyDown(Input.KEY_D)) {
				currentTank.move(Direction.RIGHT);
			} else {
				currentTank.move(Direction.NONE);
			}
      
      
			if (input.isKeyPressed(Input.KEY_SPACE)){
				activeProjectile = currentTank.fire(1);
				PE.addPhysicsEntity(activeProjectile);
				state = phase.FIRING;
				turnTimer = FIRING_TIMEOUT;
			}
			
			if (turnTimer <= 0){
				changePlayer();
			}
			
		} else if(state == phase.FIRING){
			if (turnTimer <= 0) { changePlayer(); }
		}

		for(Player p: players){p.update(delta);}
		PE.update(delta);
		controlCamera(delta, input);
	}

  private void changePlayer() {
	players.get(pIndex).getTank().move(Direction.NONE);
    activeProjectile = null;
    state = phase.MOVEFIRE;
    turnTimer = TURNLENGTH;
    pIndex ++;
    if (pIndex >= players.size()){pIndex = 0;}
    players.get(pIndex).getNextTank();
    camera.setCenter(players.get(pIndex).getTank().getPosition());
  }

  private void controlCamera(int delta, Input input) {
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

	@Override
	public int getID() {
		return Tanx.PLAYINGSTATE;
	}
}
