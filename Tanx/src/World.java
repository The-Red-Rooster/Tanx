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
	}
	
	public void loadLevel(String name) {
	  int width = (int)worldBounds.getWidth();
    int height = (int)worldBounds.getHeight();
    BitmapGenerator bg = new BitmapGenerator(width, height);
    terrain = new Terrain(width, height, bg.generateRandomSineMap());
    terrain.setTerrainInRectangle(new Vector(640f, 200f), new Vector(1280f, 1000f), Terrain.TerrainType.NORMAL);
	}
}

/// Manages coodinate space transformations between tile coordinates and world locations.
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