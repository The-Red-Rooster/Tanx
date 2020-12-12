import org.newdawn.slick.Graphics;
import org.newdawn.slick.Color;

import java.util.*;
import java.util.function.Consumer;

public class Player {
  public static final float MAX_FUEL_BURNTIME = 2*1000;
  public static float TIME_TO_CHARGE = 1*1000;
  private ArrayList<Tank> tanks;
  private int tankIndex;
  private Color playerColor;
  private int playerId;
  private ArrayList<Ammo> ammo;
  private int ammoIndex;
  private float fuel;
  private boolean infFuel;
  private float chargedPower;
  private float maxChargedPower;
  private boolean chargeRising;

  public Player(Color c, int id) {
    tanks = new ArrayList<Tank>();
    tankIndex = 0;
    playerColor = c;
    playerId = id;
    ammo = new ArrayList<Ammo>();
    giveAmmo(Cannon.BASE_CANNON, Ammo.INF_AMMO);
    giveAmmo(Cannon.BIG_CANNON, 10);
    giveAmmo(Cannon.CLUSTER_CANNON, 10);
    ammoIndex = 0;
    infFuel = false;
    maxChargedPower = TIME_TO_CHARGE;
  }

  public void render(Graphics g){
    for (Tank tank : tanks) {
      tank.render(g);
    }
  }


  public Tank addTank(float x, float y) {
	Tank t = new Tank(x, y, playerColor, this);
    tanks.add(t);
    return t;
  }

  public void giveAmmo(int type, int amount) {
    for (Ammo a: ammo ){
      if (a.type == type){
        if(a.amount == Ammo.INF_AMMO) { return; }
        if (amount == Ammo.INF_AMMO) { a.amount = amount; return;}
        a.amount += amount;
        return;
      }
    }
    ammo.add(new Ammo(type, amount));
  }

  private void newAmmoIndex(int amount) {
    ammoIndex += amount;
    if (ammoIndex >= ammo.size()){
      ammoIndex = 0;
    } else if (ammoIndex < 0){
      ammoIndex = ammo.size() - 1;
    }
    if (ammo.get(ammoIndex).amount == 0){ newAmmoIndex(amount); }
  }

  public void nextWeapon() {
    newAmmoIndex(1);
    tanks.get(tankIndex).changeWeapon(ammo.get(ammoIndex).type);
  }

  public void prevWeapon() {
    newAmmoIndex(-1);
    tanks.get(tankIndex).changeWeapon(ammo.get(ammoIndex).type);
  }

  public void startTurn() {
    getNextTank();
    chargedPower = 0;
    chargeRising = true;
    checkWeapon();
    setFuel(MAX_FUEL_BURNTIME);
  }
  
  private void checkWeapon(){
    if (ammo.get(ammoIndex).amount == 0){
      nextWeapon();
    } else {
      tanks.get(tankIndex).changeWeapon(ammo.get(ammoIndex).type);
    }
  }

  public void useJets(int delta) {
    if (fuel > 0 || infFuel){
      getTank().jet(delta);
      if (infFuel) return;
      fuel -= delta;
    }
  }

  public void removeTank(Tank t) {
    if (tanks.contains(t)){
      tanks.remove(t);
    } else {
      System.out.println("removeTankERROR: Tank not in this player!");
    }
  }

  public Tank getTank(int index){
    if (tanks.isEmpty()) {
      System.out.println("getTankERROR: No more tanks to get!");
      return null;
    }
    return tanks.get(index);
  }

  public void getNextTank() {
    tankIndex++;
    if (tankIndex >= tanks.size()) {
      tankIndex = 0;
    }
  }

  public void charging(int delta) {
    if (chargeRising){
      chargedPower += delta;
    } else {
      chargedPower -= delta;
    }
    if (chargedPower >= TIME_TO_CHARGE) {
      chargeRising = false;
      chargedPower = maxChargedPower;
    } else if (chargedPower <= 0) {
      chargeRising = true;
      chargedPower = 0;
    }
  }

  public void rotate (Direction d, int delta){
    getTank().rotate(d, delta);
  }

  public void fire(Consumer<Projectile> spawnP){
    getTank().fire(chargedPower/TIME_TO_CHARGE, spawnP);
  }

  public void getPrevTank() {
    tankIndex--;
    if (tankIndex < 0){
      tankIndex = tanks.size() - 1;
    }
  }

  public Tank getTank() {
    return getTank(tankIndex);
  }

  public void update(int delta){
    for (Tank t: tanks) { t.update(delta); }
    tanks.removeIf((t) -> t.getIsDead());
  }

  public int tanksLeft(){return tanks.size();}

  public boolean isDead() { return tanks.isEmpty(); }

  public ArrayList<Tank> getTanks() { return tanks; }

  public int getAmmo(){return ammo.get(ammoIndex).amount;}

  public ArrayList<Ammo> getAmmoList() { return ammo; }

  public int getAmmoIndex() { return ammoIndex; }

  public int getCurrentAmmo() { return ammo.get(ammoIndex).type; }

  public void setFuel(float fuel) { this.fuel = fuel; }

  public int getPlayerId() { return playerId; }
  
  public float getFuel() {
    return fuel;
  }

  public float getChargedPower() { return chargedPower; }

  public Color getPlayerColor() { return playerColor; }

  //cheats
  public void giveAllWeapons() {
    giveAmmo(Cannon.BASE_CANNON, Ammo.INF_AMMO);
    giveAmmo(Cannon.BIG_CANNON, Ammo.INF_AMMO);
  }


  public void toggleInfFuel() {
    infFuel = !infFuel;
  }

  public void toggleInfHealth() {
    getTank().toggleInfHealth();
  }

  public boolean isInfFuel() { return infFuel; }

  public boolean isInfHealth() {
    return getTank().isInfHealth();
  }

  public Color getColor() {
    return playerColor;
  }
}
