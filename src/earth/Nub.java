//uEarth: Artificial Life Simulation Software
//(C) Alin-Dragos Petculescu 2010
//Univeristy of Leicester
//www2.le.ac.uk
//Please credit the original author if reusing this code

package earth;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.ConeJoint;
import com.jme3.bullet.joints.PhysicsJoint;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;

/**
 *
 * @author DreadRazor
 */
public class Nub extends Geometry{
    
  ////physical core of the critter and traits
  public RigidBodyControl bControl;
  
  public PhysicsJoint joint;

  //what color the critter is covered in
  public ColorRGBA color = ColorRGBA.randomColor();
  
  //physical properties
  public float radius;
  public float weight;
  
  //rotation limits
  public float swing1Limit=3f;
  public float swing2Limit=3f;
  public float twistLimit=0.5f;
  
 //is it a joint?
  public Limb owner;
  
  public Nub(float xradius, float xweight, Vector3f location) {
        
        /*
        int axis = rotate ? PhysicsSpace.AXIS_X : PhysicsSpace.AXIS_Y;
        CylinderCollisionShape shape = new CylinderCollisionShape(new Vector3f( 0.1f, width, height), axis);
        Node node = new Node("Limb");
        
        */
        super("Limb "+(int)(Math.random()*10000),new Sphere(32, 32, xradius));
        
        //implement rotation using this:
        //rotate(rotation.x,rotation.y,rotation.z); // this rotates is horizontal
        
        setLocalTranslation(location);
        bControl=new RigidBodyControl(xweight);
        addControl(bControl);
        
        //physical characteristics
        weight=xweight;
        radius=xradius;
        
        //RigidBodyControl rigidBodyControl = new RigidBodyControl(this, 1);
        
        //node.setLocalTranslation(location);
        //node.addControl(rigidBodyControl);
        //xnode=node;
        
    }
    
   public PhysicsJoint join(Limb ownr, Vector3f connectionPoint) {
        
        owner=ownr;
       
        Vector3f pivotA = worldToLocal(connectionPoint, new Vector3f());
        Vector3f pivotB = owner.worldToLocal(connectionPoint, new Vector3f());
        ConeJoint xjoint = new ConeJoint(getControl(RigidBodyControl.class), owner.getControl(RigidBodyControl.class), pivotA, pivotB);
        xjoint.setLimit(1f, 1f, 0);
        return xjoint;
    }
   
   public void setLimits(float s1l, float s2l, float tl){
       
       swing1Limit=s1l;
       swing2Limit=s2l;
       twistLimit=tl;
       
   }
   
}
