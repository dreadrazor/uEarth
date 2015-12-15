//uEarth: Artificial Life Simulation Software
//(C) Alin-Dragos Petculescu 2010
//Univeristy of Leicester
//www2.le.ac.uk
//Please credit the original author if reusing this code

package earth;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

/**
 *
 * @author DreadRazor
 */
public class Resource extends Geometry{

  ////////////////////////non-modifiables
  //physical core of the critter
  public RigidBodyControl bControl;
  public int index;

  //what color the critter is covered in
  public ColorRGBA color = ColorRGBA.randomColor();

  //red,blue,green energy
  public float energy;

  //size
  public static float size=Util.newRandom(1f)/2; 
  public static float weight=10f;
  
  //is it being targeted by other creatures?
  public float contended=0f;

  public Resource(Vector3f location, int idx){

      super("Resource "+(int)(Math.random()*10000), new Box(size,size,size));
      bControl=new RigidBodyControl(weight);
      setLocalTranslation(location);
      addControl(bControl);
      setShadowMode(ShadowMode.CastAndReceive);

      index=idx;
      energy = Util.newRandom10(1f)/2;
  }
  
  public Resource(Vector3f location, int idx, float ener){
      this(location,idx);
      energy=ener;
  }
 
  public void contend(float power){
      contended+=power;
  }
  
  public void uncontend(float power){
      contended-=power;
  }
          

}