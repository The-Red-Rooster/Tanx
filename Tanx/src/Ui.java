import jig.ConvexPolygon;
import jig.Entity;
import jig.ResourceManager;
import jig.Vector;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;

import java.util.ArrayList;

class Ui {
  BottomUi bottomUi;

  public Ui(Rectangle bottomUiBounds, Vector bottomUiPosition){
    bottomUi = new BottomUi(bottomUiBounds, bottomUiPosition);
  }

  public void render(Graphics g){
    bottomUi.render(g);
  }

  public void update(int delta, Player player, int turnTimer, phase state){
    bottomUi.update(delta, player,turnTimer, state );
  }
}

class BottomUi extends UiContainer{
  final float JET_ELEMENT_MIN_ANGLE = -135;
  final float JET_ELEMENT_MAX_ANGLE = 135f;
  final float TIMER_ELEMENT_MIN_ANGLE = -135f;
  final float TIMER_ELEMENT_MAX_ANGLE = 135f;
  final Vector FUEL_POS_OFFSET = new Vector(800, 250);
  final Vector WEP_POS_OFFSET = new Vector(200, 200);
  final Vector TIMER_POS_OFFSET = new Vector(-100, 250);
  final float GAUGE_SCALE = 1.5f;
  GaugeElement jetFuelElement;
  WeaponSelect weaponSelect;
  GaugeElement timerElement;

  public BottomUi(Rectangle bounds, Vector pos){
    super(bounds, pos);
    Entity uiBack = new Entity(position.getX(), position.getY());
    uiBack.addShape(new ConvexPolygon(bounds.getWidth(), bounds.getHeight()), Color.black, Color.red);
    addEntity(uiBack, position);
    Vector fuelPos = position.add(FUEL_POS_OFFSET);
    Vector weaponPos = position.add(WEP_POS_OFFSET);
    Vector timerPos = position.add(TIMER_POS_OFFSET);
    jetFuelElement = new GaugeElement(fuelPos, Tank.INIT_FUEL_BURNTIME,
        JET_ELEMENT_MIN_ANGLE, JET_ELEMENT_MAX_ANGLE,Tanx.FUEL_GAUGE_OVERLAY, Tanx.FUEL_GAUGE_ARROW);
    jetFuelElement.setScale(GAUGE_SCALE);
    weaponSelect = new WeaponSelect(weaponPos);
    timerElement = new GaugeElement(timerPos, PlayingState.TURNLENGTH,
        TIMER_ELEMENT_MIN_ANGLE, TIMER_ELEMENT_MAX_ANGLE, Tanx.TIMER_GAUGE, Tanx.FUEL_GAUGE_ARROW);
    timerElement.setScale(GAUGE_SCALE);
  }

  @Override
  public void render(Graphics g) {
    super.render(g);
    jetFuelElement.render(g);
    weaponSelect.render(g);
    timerElement.render(g);
  }

  public void update(int delta, Player player, int turnTimer, phase state) {
    jetFuelElement.setValue(player.getTank().getFuel());
    weaponSelect.update(delta, player);
    if (state == phase.MOVEFIRE){
      timerElement.setValue(turnTimer);
    }
  }
}

class UiContainer {
  protected Vector position;
  protected Rectangle containerBounds;
  protected ArrayList<Entity> entities;
  protected ArrayList<UiContainer> uiContainers;

  public UiContainer(Rectangle bounds, Vector pos){
    position = pos;
    containerBounds = bounds;
    entities = new ArrayList<Entity>();
    uiContainers = new ArrayList<UiContainer>();
  }

  public void addEntity(Entity e, Vector relPosition){
    entities.add(e);
    e.setPosition(position.add(relPosition));
  }

  public void removeEntity(Entity e) {
    if (entities.contains(e)) {
      entities.remove(e);
    } else {
      System.out.println("UiContainer.removeEntityERROR: Tried to remove entity that did not exist in container!");
    }
  }

  public void render(Graphics g){
    for (Entity e: entities){ e.render(g); }
    for (UiContainer u: uiContainers) { u.render(g); }
  }
}

class WeaponSelect {
  final float Y_OFFSET = 20f;
  final Vector POINTER_OFFSET = new Vector(-10, 10);
  private Vector position;
  private Entity pointer;
  private ArrayList<Ammo> ammoList;
  private int currentAmmo;

  public WeaponSelect(Vector pos) {
    ammoList = new ArrayList<Ammo>();
    position = pos;
    pointer = new Entity(0, 0);
    pointer.addImage(ResourceManager.getImage(Tanx.WEAPON_POINTER));
  }

  public void update(int delta, Player player){
    ammoList = player.getAmmoList();
    currentAmmo = player.getCurrentAmmo();
  }

  public void render(Graphics g) {
    for(int index = 0; index < ammoList.size(); index++) {
      String wepString = Cannon.getTypeStr(ammoList.get(index).type) + " ";
      if (ammoList.get(index).amount == Ammo.INF_AMMO) {
        wepString += "INF";
      } else {
        wepString += Integer.toString((ammoList.get(index).amount));
      }
      g.drawString(wepString, position.getX(), position.getY() + Y_OFFSET*index);
      if (currentAmmo == ammoList.get(index).type){
        pointer.setPosition(position.add(POINTER_OFFSET));
        pointer.setY(pointer.getY() + Y_OFFSET*index);
        pointer.render(g);
      }
    }
  }

}

class GaugeElement extends StatusBarElement {
  private float maxAngle;
  private float minAngle;
  private float angle;
  private Entity arrow;
  private Entity overlay;

  public GaugeElement(Vector pos, float maxVal, float minA, float maxA, String overLaySprite, String arrowSprite){
    super(pos, maxVal);
    setMaxAngle(maxA);
    setMinAngle(minA);
    arrow = new Entity(0,0);
    arrow.addImage(ResourceManager.getImage(arrowSprite));
    overlay = new Entity(0,0);
    overlay.addImage(ResourceManager.getImage(overLaySprite));
    setPosition(pos);
  }

  public void changeAngle() {
    float full = maxAngle + Math.abs(minAngle);
    float ratio = value/maxValue;
    angle = minAngle + full*ratio;
    arrow.setRotation(angle);
  }

  public void render(Graphics g) {
    overlay.render(g);
    arrow.render(g);
  }

  @Override
  public void setValue(float val) {
    super.setValue(val);
    changeAngle();
  }

  @Override
  public void setPosition(Vector p) {
    super.setPosition(p);
    arrow.setPosition(position);
    overlay.setPosition(position);
  }

  private void setMinAngle(float minA) { minAngle = minA; }
  private void setMaxAngle(float maxA) { maxAngle = maxA; }

  public void setScale(float gauge_scale) {
    arrow.setScale(gauge_scale);
    overlay.setScale(gauge_scale);
  }
}

class StatusBarElement {
  protected Vector position;
  public float value;
  public float maxValue;

  public StatusBarElement(Vector pos, float maxVal){
    position = pos;
    value = 0;
    maxValue = maxVal;
  }

  public void add(float amount){
    amount += amount;
    checkValue();
  }

  public void sub(float amount) {
    amount -= amount;
    checkValue();
  }

  private void checkValue() {
    if (value > maxValue) { setValue(maxValue); }
    if (value < 0) { setValue(0); }
  }

  public void setValue(float val) {
    value = val;
    checkValue();
  }

  public void setPosition(Vector p){
    position = p;
  }

  public float getMaxValue() { return maxValue; }

  public void setMaxValue(float maxValue) { this.maxValue = maxValue; }

  public float getValue() { return value; }
}
