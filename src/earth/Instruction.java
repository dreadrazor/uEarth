//uEarth: Artificial Life Simulation Software
//(C) Alin-Dragos Petculescu 2010
//Univeristy of Leicester
//www2.le.ac.uk
//Please credit the original author if reusing this code

package earth;

import com.jme3.math.Vector3f;

/**
 *
 * @author DreadRazor
 */
public class Instruction{

    public Limb limb; //limb which is being used
    //private String verb; //function which is being used (this is rotate anyway)

    //parameters (these are evolved, along with the sequence)
    public float xParam;
    public float yParam;
    public float zParam;

    //delay in executing
    public int delay;

    //cost of executing instruction
    public float cost=zParam+yParam+xParam;
    
    //fitness of instruction
    public float fitness=0;
    
    //fitness chances... 3 strikes and the instruction is out
    public float strikes=3;
    public float amplification=1f;
    public float nerf=2f;

    public Instruction(Limb l,float x, float y, float z, int dlay){
        
        limb=l;
        
        xParam=x;
        yParam=y;
        zParam=z;
        delay=dlay;
    }

    public void run(){
        if (limb!=null){
            //limb.nub.rotate(xParam,yParam,zParam);
            //need to cancel out ALL velocities:
            
            for (int i=0; i<limb.owner.limbs.length; i++){
                if (limb.owner.limbs[i] != null) limb.owner.limbs[i].nub.bControl.clearForces();
            }

            
            Vector3f temp = new Vector3f(limb.owner.goToLocal);
            
            limb.bControl.clearForces();
            //limb.owner.bControl.clearForces();
            //limb.nub.bControl.setLinearVelocity(limb.owner.goTo.mult(3*limb.owner.speed).mult(Math.abs((float)Math.sin((double)limb.owner.bControl.getPhysicsLocation().normalizeLocal().angleBetween(Vector3f.UNIT_XYZ.normalizeLocal())))).divide(limb.owner.bControl.getPhysicsLocation().normalizeLocal().distance(limb.owner.goTo.normalizeLocal())).add(new Vector3f(xParam,yParam,zParam)).mult(amplification));
            
            //GREAT IDEA: nerf the Z component of the vector so creatures don't go up/down
            //Vector3f vel = limb.owner.goToLocal.mult(Math.abs((float)Math.sin((double)limb.owner.bControl.getPhysicsLocation().normalizeLocal().angleBetween(Vector3f.UNIT_XYZ.normalizeLocal())))).divide(limb.owner.bControl.getPhysicsLocation().normalizeLocal().distance(limb.owner.goToLocal.normalizeLocal())).add(new Vector3f(xParam,yParam,zParam)).mult(amplification);
            
            //Vector3f vel=limb.nub.getLocalTranslation().subtract(limb.owner.goToLocal).add(new Vector3f(xParam,yParam,zParam)).mult(amplification);
            Vector3f vel=limb.owner.goToLocal.subtract(limb.owner.getLocalTranslation()).add(new Vector3f(xParam,yParam,zParam)).mult(amplification); 
            //Vector3f vel=limb.owner.getWorldTranslation().subtract(limb.owner.goToLocal);
            
            //scale down
                if (vel.y>0.5f) vel.y=0.5f;
                    //System.out.println(vel);
                    
                //nerf values that are too big    
                if(Math.abs(vel.x)>nerf){  
                    vel.x=vel.x/Math.abs(vel.x);
                    vel.z=vel.z/Math.abs(vel.x);
                }
                if(Math.abs(vel.z)>nerf){
                    vel.x=vel.x/Math.abs(vel.z);
                    vel.z=vel.z/Math.abs(vel.z);
                }
                
             //apply velocity to nub, and some to core   
             limb.nub.bControl.setLinearVelocity(vel);
             limb.owner.bControl.setLinearVelocity(vel.divide(nerf));
             
             //System.out.println(vel);
            
             limb.owner.goToLocal=temp;
             //limb.nub.bControl.setLinearVelocity(limb.nub.bControl.getPhysicsLocation().subtract(limb.owner.goTo).negate());
            //limb.nub.bControl.applyCentralForce(new Vector3f(xParam,yParam,zParam));
            //limb.bControl.clearForces();
            //limb.joint.getPivotB();
        }
    }
  }
