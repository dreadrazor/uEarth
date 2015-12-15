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
import com.jme3.scene.shape.Cylinder;

/**
 *
 * @author DreadRazor
 */
public class Obstacle extends Geometry{
  ////////////////////////non-modifiables
  //physical core of the critter
  public RigidBodyControl bControl;
  public int index;

  //what color the critter is covered in
  public ColorRGBA color = ColorRGBA.randomColor();

  //weight
  public float weight=4999f*(float)(Math.random()+1);

  public Obstacle(Vector3f location, int idx){

      super("Cone "+(int)(Math.random()*10000), new Cylinder(32,32,2f*(float)(Math.random()+1),0f,6f*(float)(Math.random()+0.5f), true, false));
      
      //point it upwards
      this.rotateUpTo(Vector3f.UNIT_Z);
      
      bControl=new RigidBodyControl(weight);
      setLocalTranslation(location);
      addControl(bControl);
      
      setShadowMode(ShadowMode.CastAndReceive);

      index=idx;
  }
          

}