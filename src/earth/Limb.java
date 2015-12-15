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
import com.jme3.scene.shape.Cylinder;

/**
 *
 * @author DreadRazor
 */
public class Limb extends Geometry{
    
  ////physical core of the critter and traits
  public RigidBodyControl bControl;
  
  public PhysicsJoint joint;

  //what color the critter is covered in
  public ColorRGBA color = ColorRGBA.randomColor();
  
  //physical properties
  public float weight;
  public float width;
  public float height;
  
  //rotation limits
  public float swing1Limit=5f;
  public float swing2Limit=5f;
  public float twistLimit=0f;
  
 //is it a joint?
  public Critter owner;
  public int slot;
  
  //terminal sphere
  public Nub nub;
  public float nubRadius = 0.4f;
  public float nubWeight = 3f;
   
  //sub-limbs (direct descending children)
  public int no_appendices = 0;
  public int max_appendices=5;
  public Limb[] appendices = new Limb[max_appendices];
  
  public Limb(float xwidth, float xheight, float xweight, Vector3f location, int slot, float nubrad, float nubwei) {
        
        /*
        int axis = rotate ? PhysicsSpace.AXIS_X : PhysicsSpace.AXIS_Y;
        CylinderCollisionShape shape = new CylinderCollisionShape(new Vector3f( 0.1f, width, height), axis);
        Node node = new Node("Limb");
        
        */
        super("Limb "+(int)(Math.random()*10000),new Cylinder(32, 32, xwidth, xheight,true));
        
        //implement rotation using this:
        //rotate(rotation.x,rotation.y,rotation.z); // this rotates is horizontal
        
        setLocalTranslation(location);
        bControl=new RigidBodyControl(xweight);
        addControl(bControl);
        
        //physical characteristics
        weight=xweight;
        height=xheight;
        width=xwidth;
        
        nubRadius=nubrad;
        nubWeight=nubwei;
        
        if (slot==2) {rotate((float)Math.PI/2,0f,0f);}
        else if (slot==3) {rotate((float)Math.PI/2,0f,0f);}
        else if (slot==4) {rotate(0f,(float)Math.PI/2,0f);}
        else if (slot==5) {rotate(0f,(float)Math.PI/2,0f);}
        
        Vector3f pos1=new Vector3f();
        
        if (slot==0) {pos1=getWorldTranslation().add(0f,0f,height/2+0.2f);}
        else if (slot==1) {pos1=getWorldTranslation().add(0f,0f,-height/2-0.2f);}
        else if (slot==2) {pos1=getWorldTranslation().add(0f,height/2+0.2f,0);}
        else if (slot==3) {pos1=getWorldTranslation().add(0f,-height/2-0.2f,0f);}
        else if (slot==4) {pos1=getWorldTranslation().add(height/2+0.2f,0,0);}
        else if (slot==5) {pos1=getWorldTranslation().add(-height/2-0.2f,0,0);}
        
        Vector3f pos2=new Vector3f();
      
        if (slot==0) {pos2=getWorldTranslation().add(0f,0f,height/2+0.4f+nubRadius/2);}
        else if (slot==1) {pos2=getWorldTranslation().add(0f,0f,-height/2-0.4f-nubRadius/2);}
        else if (slot==2) {pos2=getWorldTranslation().add(0f,+height/2+0.4f+nubRadius/2,0);}
        else if (slot==3) {pos2=getWorldTranslation().add(0f,-height/2-0.4f-nubRadius/2,0f);}
        else if (slot==4) {pos2=getWorldTranslation().add(height/2+0.4f+nubRadius/2,0,0);}
        else if (slot==5) {pos2=getWorldTranslation().add(-height/2-0.4f-nubRadius/2,0,0);}
        
        nub = new Nub(nubRadius,nubWeight,pos2);
        nub.join(this,pos1);
        
        //RigidBodyControl rigidBodyControl = new RigidBodyControl(this, 1);
        
        //need to do some rotation for these slots

        
        //node.setLocalTranslation(location);
        //node.addControl(rigidBodyControl);
        //xnode=node;
        
    }
  
  public PhysicsJoint join(Limb other, Vector3f connectionPoint) {
      
        Vector3f pivotA = worldToLocal(connectionPoint, new Vector3f());
        Vector3f pivotB = other.worldToLocal(connectionPoint, new Vector3f());
        ConeJoint xjoint = new ConeJoint(getControl(RigidBodyControl.class), other.getControl(RigidBodyControl.class), pivotA, pivotB);
        xjoint.setLimit(1f, 1f, 0);
        
        appendices[no_appendices]=other;
        no_appendices++;
        
        joint=xjoint;
        
        return xjoint;
    }
  
    public PhysicsJoint join(Limb other, Vector3f connectionPoint, float point) {
      
        owner=other.owner;
        
        Vector3f pivotA = worldToLocal(connectionPoint, new Vector3f());
        Vector3f pivotB = other.worldToLocal(connectionPoint, new Vector3f());
        ConeJoint xjoint = new ConeJoint(getControl(RigidBodyControl.class), other.getControl(RigidBodyControl.class), pivotA, pivotB);
        xjoint.setLimit(swing1Limit, swing2Limit, twistLimit);
        
        appendices[no_appendices]=other;
        no_appendices++;
        
        joint=xjoint;
        
        return xjoint;
    }
    
   public PhysicsJoint join(Critter ownr, Vector3f connectionPoint) {
        
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
   
   public boolean sameAs(Limb l){
       if(height==l.height && width==l.width) return true;
       else return false;
   }
   
   
}
