import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import jig.Vector;

class RayPair {
  final LineSegment first;
  final LineSegment second;
  public RayPair(LineSegment first, LineSegment second) {
    super();
    this.first = first;
    this.second = second;
  }
  float avgLengthSquared() {
    return (first.getDifference().lengthSquared() + second.getDifference().lengthSquared())/2;
  }
  float avgLength() {
    return (first.getDifference().length() + second.getDifference().length())/2;
  }
  Vector surfaceNormal() {
    return new LineSegment(second.end, first.end).unitNormal();
  }
  public void draw(Graphics g, Color color) {
    first.draw(g, color);
    second.draw(g, color);
  }
}