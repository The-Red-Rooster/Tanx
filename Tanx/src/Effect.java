import jig.Entity;
import jig.ResourceManager;
import org.newdawn.slick.Animation;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.state.StateBasedGame;

public class Effect extends Entity {
  private Animation effectAnimation;
  private String sound;
  private int soundLength;
  private boolean soundOn;
  private float volume;
  private float pitch;

  private int soundCD;

  public Effect(final float x, final float y, Animation anim) {
    super(x,y);
    effectAnimation = anim;
    addAnimation(anim);
    effectAnimation.setLooping(true);
    effectAnimation.start();
    soundOn = false;
    volume = 1f;
    pitch = 1f;
  }

  public void setSound (String soundString, int length, final float vol, final float ptch) {
    sound = soundString;
    soundLength = length;
    volume = vol;
    pitch = ptch;
    soundCD = 0;
  }

  public void setVolume(final float vol) { volume = vol; }

  public void setPitch(final float ptch) { pitch = ptch;}

  public void toggleSound() { soundOn = !soundOn; }

  public void turnOffSound() { soundOn = false; }

  public void turnOnSound() { soundOn = true; }

  public void update (int delta){
    if (soundOn) {
      soundCD -= delta;
      if (soundCD <= 0) {
        if (sound != null){
          ResourceManager.getSound(sound).play(pitch, volume);
        }
        soundCD = soundLength;
      }
    }
  }

  public void render(Graphics g, final float x, final float y) {
    setPosition(x, y);
    super.render(g);
  }
}
