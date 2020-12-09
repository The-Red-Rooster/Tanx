import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import jig.Vector;

class LineSegment {
  public final Vector start;
  public final Vector end;
  public LineSegment(final Vector start, final Vector end) {
    this.start = start;
    this.end = end;
  }
  public static LineSegment offset(final Vector start, final Vector offset) {
    return new LineSegment(start, start.add(offset));
  }
  public static LineSegment approximation(Vector points[]) {
    // Currently just takes the first and last points that are not null and uses them as the endpoints.
    // This is basically avoiding linear regression due to its complexity to implement and run time.
    Vector start = null;
    Vector end = null;
    for (Vector p : points) {
      if (p == null) { continue; }
      
      if (start == null) {
        start = p;
      } else {
        end = p;
      }
    }
    if (start == null || end == null) { return null; }
    return new LineSegment(start, end);
  }
  
  public void draw(Graphics g, Color color) {
    g.setColor(color);
    g.drawLine(start.getX(), start.getY(), end.getX(), end.getY());
  }
  
  public Vector getDifference() {
    return end.subtract(start);
  }
  public Vector getDirection() {
    return getDifference().unit();
  }
  public Vector unitNormal() {
    return getDifference().unit().getPerpendicular();
  }
  public LineSegment rotate(double degrees) {
    return new LineSegment(start.rotate(degrees), end.rotate(degrees));
  }
  public LineSegment translate(Vector delta) {
    return new LineSegment(start.add(delta), end.add(delta));
  }
  public Vector center() {
    return start.add(end).scale(0.5f);
  }
  public Vector pointInDirectionWithX(float x) {
    float run = (this.end.getX() - this.start.getX());
    if (run == 0.0) { return null; } // x isn't on the line
    float rise = (this.end.getY() - this.start.getY());
    float slope = rise / run;
    return new Vector(x, x * slope);
  }
}
