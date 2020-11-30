import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.Consumer;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;

import jig.Vector;

public class World {
	
	public static int tileLength = 32;
	
	Terrain terrain;
	TileGeometry geometry;
	Rectangle worldBounds;

	public World(Rectangle worldBounds) {
	  this.worldBounds = worldBounds;
		int xTilesCount = (int) (worldBounds.getWidth()/((float)World.tileLength));
		int yTilesCount = (int) (worldBounds.getHeight()/((float)World.tileLength));
		this.geometry = new TileGeometry(worldBounds, xTilesCount, yTilesCount);
		availPowerups = new ArrayList<Powerup>();
		availPowerups.add(new AmmoPowerup(0, 0, Cannon.BIG_CANNON, 1));
		availPowerups.add(new HealthPowerup(0, 0, 20));
	}
	
	public void loadLevel(String name) {
		int width = (int)worldBounds.getWidth();
		int height = (int)worldBounds.getHeight();
		BitmapGenerator bg = new BitmapGenerator(width, height);
		terrain = new Terrain(width, height, bg.generateRandomSineMap());
		//terrain.setTerrainInRectangle(new Vector(600f, 200f), new Vector(1280f, 1000f), Terrain.TerrainType.NORMAL);
		terrain.setTerrainInCircle(new Vector(1280f, 600f), 500f, Terrain.TerrainType.OPEN);
		terrain.setTerrainInCircle(new Vector(0, 600f), 500f, Terrain.TerrainType.OPEN);
		
	}

  //World update methods to run things such as powerup generation
  public static int POWERUP_SPAWNRATE = 10*1000;
	public static float POWERUP_SPAWN_HEIGHT = 100f;
	public static float POWERUP_SPAWN_TANK_DIST = 50f;

	private int pSpawnTimer;
	private ArrayList<Powerup> availPowerups;

  public void update(int delta, PhysicsEngine PE, ArrayList<Player> players) {
    pSpawnTimer -= delta;
    if (pSpawnTimer <= 0){
      PE.addPhysicsEntity(randomPowerup(players));
      pSpawnTimer = POWERUP_SPAWNRATE;
    }
  }

  private Powerup randomPowerup(ArrayList<Player> players){
    Random rand = new Random();
    int upperBound = availPowerups.size();
    Powerup newPowerup = availPowerups.get(rand.nextInt(upperBound)).copy();
    float newX = rand.nextFloat()*worldBounds.getMaxX();
    while (tankBelow(newX, players)){
      newX = rand.nextFloat()*worldBounds.getMaxX();
    }
    newPowerup.setX(newX);
    newPowerup.setY(POWERUP_SPAWN_HEIGHT);
    return newPowerup;
  }

  private boolean tankBelow(float newX, ArrayList<Player> players) {
    for (Player p: players) {
      for (Tank t: p.getTanks()) {
        if ((newX < (t.getX() + POWERUP_SPAWN_TANK_DIST))
          && (newX > (t.getX() - POWERUP_SPAWN_TANK_DIST))) {
          return true;
        }
      }
    }
    return false;
  }
}

/// Manages coordinate space transformations between tile coordinates and world locations.
/// Tile coordinates are integer x and y. They can be passed together using the `Coordinate` class.
/// World locations are the float x and y of the location on screen before any camera calculation is made.
/// They can be passed together using the `Vector` class.
class TileGeometry {

	public Rectangle tilesArea;
	public int xTilesCount;
	public int yTilesCount;
	
	public TileGeometry(Rectangle tilesArea, int xTilesCount, int yTilesCount) {
		System.out.println("Tiles area: " + tilesArea);
		System.out.println("Tiles " + xTilesCount + "x" + yTilesCount);
		this.tilesArea = tilesArea;
		this.xTilesCount = xTilesCount;
		this.yTilesCount = yTilesCount;
	}
	
	public Vector centerLocationOfTile(Coordinate tile) {
		return centerLocationOfTile(tile.x, tile.y);
	}
	public Vector centerLocationOfTile(int x, int y) {
		if (x < 0 || x >= xTilesCount) { return null; }
		if (y < 0 || y >= yTilesCount) { return null; }
		float minX = tilesArea.getMinX() + (float) x * (float)World.tileLength;
		float minY = tilesArea.getMinY() + (float) y * (float)World.tileLength;
		return new Vector(minX + (float)World.tileLength/2.0f, minY + (float)World.tileLength/2.0f);
	}
	
	public Rectangle worldBoundsOfTile(Coordinate tile) {
		return worldBoundsOfTile(tile.x, tile.y);
	}
	public Rectangle worldBoundsOfTile(int x, int y) {
		if (x < 0 || x >= xTilesCount) { return null; }
		if (y < 0 || y >= yTilesCount) { return null; }
		float minX = tilesArea.getMinX() + (float) x * (float)World.tileLength;
		float minY = tilesArea.getMinY() + (float) y * (float)World.tileLength;
		return new Rectangle(minX, minY, World.tileLength, World.tileLength);
	}
	
	public Coordinate tileAtLocation(Vector location) {
		return tileAtLocation(location.getX(), location.getY());
	}
	public Coordinate tileAtLocation(float x, float y) {
		if (!tilesArea.contains(x, y)) { return null; }
		int tileX = (int) ((x - tilesArea.getMinX()) / World.tileLength);
		int tileY = (int) ((y - tilesArea.getMinY()) / World.tileLength);
		return new Coordinate(tileX, tileY);
	}
}