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
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import java.util.Iterator;
import java.util.Random;

/**
 *
 * @author DreadRazor
 */
public final class Critter extends Geometry{

   
////physical core of the critter and traits
  public RigidBodyControl bControl;

  //what color the critter is covered in
  public ColorRGBA color = ColorRGBA.randomColor();
  
  //we be needing these ???
  public Node xnode;
  
/////individual vars these make each individual unique
  public float fitness=0f;
  public int generation=0;
  public boolean alive=true;
  public int index;

/////what were the creature's parents
  public Critter parent1=null;
  public Critter parent2=null; //can be same parent
  
  public String parent1name="System";
  public String parent2name="System";
  
  public float[] parent1dna=new float[13];
  public float[] parent2dna=new float[13];
  
////creature attributes
  //how much energy it has
  public float energy;

  //health level
  //how much injury/collision a creature can take before dying
  public float health;

  //speed
  public float speed;

  //range
  public float range;

///// Basic GENOME
  //variable that tells the odds of a mutation occuring on a given genome interaction

  public float mutation;
  public float crossover;

  //////////physical variables:

  public float cRadius;

  //core weight
  public float cWeight;

  //////////basic DNA vars:

  //maximum health
  public float maxHealth;

  //red,blue,green energy storage limits
  //the bigger the limits, the bigger the creature size and weight
  public float maxEnergy;

  //regen percentage (how much health is restored when creature eats / percentage of food value)
  public float regenPerc;

  //hunger thresholds, a creature becomes hungry when one of its energies is below x%
  public float hEnergy;

  //mating thresholds
  public float mEnergy;

  //number of children spawned when mating
  public float children;

  //energy % given to children
  public float cEnergy;

//////////other stuff ???
  //maximum time a creature can live
  public float maxLife;
  public float life;
  public float matingLife;

  //???????sensor range: it costs to use long range sensors
  public float sRange;

  /////////inherited basic behaviors

  public float aggr; //aggression level

////creature behaviour

  public int[] brainStruct = {12,6,6,6,3};
  public NNetwork brain;

  public int state; //what it wants to do: eat,mate,move,evade,etc
  public String[] state_names={"Hungry","Mate","Avoid","Idle"};
  
  //private List<InstructionSequence> states; //the possible outputs of the neural network

  public VMachine move;
  //public InstructionSequence oldMove = new InstructionSequence("move");
  //public InstructionSequence rotate;
   
  //define the different types of targets based on state
  public Critter ctarget; //who or what to interact with
  public Resource rtarget; //what to eat
  public Obstacle otarget; //what to avoid

  public Vector3f goTo = Vector3f.ZERO; //vectorial adjustment for adding forces
  public Vector3f goToLocal = Vector3f.ZERO; //propper target

  public Limb[] limbs = new Limb[6]; //list of all limbs it has
  //there are 6 possible slots: back/front/left/right/top/bottom
  private float contended=0f;
  
  public Limb addLimb(float width, float height, float weight, int slot, float nubRadius, float nubWeight){
      
      Vector3f pos1=new Vector3f();
      
      if (slot==0) {pos1=getWorldTranslation().add(0f,0f,cRadius+0.2f);}
      else if (slot==1) {pos1=getWorldTranslation().add(0f,0f,-cRadius-0.2f);}
      else if (slot==2) {pos1=getWorldTranslation().add(0f,cRadius+0.2f,0);}
      else if (slot==3) {pos1=getWorldTranslation().add(0f,-cRadius-0.2f,0f);}
      else if (slot==4) {pos1=getWorldTranslation().add(cRadius+0.2f,0,0);}
      else if (slot==5) {pos1=getWorldTranslation().add(-cRadius-0.2f,0,0);}
      else return null;
      
      //System.out.println(pos1);
      
      Vector3f pos2=new Vector3f();
      
      if (slot==0) {pos2=getWorldTranslation().add(0f,0f,cRadius+height/2+0.4f);}
      else if (slot==1) {pos2=getWorldTranslation().add(0f,0f,-cRadius-height/2-0.4f);}
      else if (slot==2) {pos2=getWorldTranslation().add(0f,cRadius+height/2+0.4f,0);}
      else if (slot==3) {pos2=getWorldTranslation().add(0f,-cRadius-height/2-0.4f,0f);}
      else if (slot==4) {pos2=getWorldTranslation().add(cRadius+height/2+0.4f,0,0);}
      else if (slot==5) {pos2=getWorldTranslation().add(-cRadius-height/2-0.4f,0,0);}
      else return null;
      
      //System.out.println(pos2);
      
      Limb newLimb = new Limb(width,height,weight,pos2,slot,nubRadius,nubWeight);
      
      newLimb.join(this, pos1);
      
      limbs[slot]=newLimb;
      
      return newLimb;
  }
  
  public void extendLimb(){} //extend a given limb by another limb
  
  
  //limb class (nested inside critter class)

  //instruction nested class
  


  /*
  public class Target {

   public Critter other=null;
   public Resource res=null;

    //cost of target
    public float cost;

    //benefit of target
    public float benefit;

    //we can already get speed from res or other

    //threat level
    public float threat;

}
*/
  public void tick(){
      
      //determine state
      if (energy < maxEnergy*hEnergy/100) state=0; //hungry
      else if (life<maxLife*matingLife/100) state=1; //mate
      else state=2; //avoid

        if(state==0){    
            
        otarget=null;    
            
        //first we look at all the resources
        float best_reaction=0;
        
        Resource newrtarget=null;

        for (int i=0; i < Simulation.maxloop_resource+1; i++){
            if(Simulation.resources[i] != null){
                //here's where the brain should kick in...
                
                    float distance=getLocalTranslation().distance(Simulation.resources[i].getLocalTranslation());
                    if (distance<sRange){ //check if target is in sensor range
                        //System.out.println(distance+" "+sRange);
                        //load some other relevant data
                        float benefit=Simulation.resources[i].energy;
                        
                        //float[] temp={distance,benefit,tSpeed,threat};
                        float[] temp={distance,0,benefit,0,0,0,0,0,0,0,0,Simulation.resources[i].contended-fitness};
                        
                        float[] thought=brain.zap(temp);
                        if (thought[0]>best_reaction){
                            best_reaction=thought[0];
                            newrtarget = Simulation.resources[i];
                            //System.out.println(best_reaction);
                            //index2=i;
                        }
                    }

                    //if(index1!=index2) System.out.println("NN works!: "+index1+" vs "+index2);
                }
        }

        //then we look at all the other critters
        
        Critter newctarget=null;
        
        for (int i=0; i < Simulation.maxloop_critter+1; i++){
            if(Simulation.critters[i] != null && this!=Simulation.critters[i]){
                //here's where the brain should kick in...
                float distance=getLocalTranslation().distance(Simulation.critters[i].getLocalTranslation());
                    if (distance<sRange){ //check if target is in sensor range
                        //System.out.println(distance+" "+sRange);
                        //load some other relevant data
                        //float benefit=Simulation.critters[i].energy;
                        //float tSpeed=Simulation.critters[i].speed;
                        float threat=Simulation.critters[i].fitness-aggr; // resources pose no threat
                        
                        //float[] temp={distance,benefit,tSpeed,threat};
                        float[] temp={distance,threat,Simulation.critters[i].energy,Simulation.critters[i].move.fitness,Simulation.critters[i].cRadius,Simulation.critters[i].children,Simulation.critters[i].life,Simulation.critters[i].maxLife,Simulation.critters[i].matingLife,Simulation.critters[i].sRange,Simulation.critters[i].aggr,Simulation.critters[i].contended-fitness};

                        float[] thought=brain.zap(temp);
                        if (thought[0]>best_reaction){
                            best_reaction=thought[0];
                            newctarget = Simulation.critters[i];
                            newrtarget=null;
                            //System.out.println(best_reaction);
                            //index2=i;
                        }
                    }
            }
        }
        
        //try{
            if (newctarget!=null){ //is the resource the best choince?
                if (newctarget != ctarget){
                    
                    //contend/uncontend
                    if (ctarget!=null) ctarget.uncontend(fitness);
                    newctarget.contend(fitness);
                    
                    ctarget=newctarget;                   
                }
                goTo=ctarget.bControl.getPhysicsLocation().subtract(bControl.getPhysicsLocation());
                goToLocal=ctarget.getLocalTranslation();
                    
            }
            
            if (newrtarget!=null){ //is the resource the best choince?
                if (newrtarget != rtarget){
                    
                    //contend/uncontend
                    if (rtarget!=null) rtarget.uncontend(fitness);
                    newrtarget.contend(fitness);
                    rtarget=newrtarget;
                   
                }
                goTo=rtarget.bControl.getPhysicsLocation().subtract(bControl.getPhysicsLocation());
                goToLocal=rtarget.getLocalTranslation();
            }
        }
        else if(state==1){    
          
           otarget=null;
           rtarget=null;
            
          Critter newmtarget=null;
          
          float best_fit=0;
          
          for (int i=0; i < Simulation.maxloop_critter+1; i++){
            if(Simulation.critters[i] != null && this!=Simulation.critters[i]){
                //here's where the brain should kick in...
                float distance=getLocalTranslation().distance(Simulation.critters[i].getLocalTranslation());
                    if (distance<sRange){ //check if target is in sensor range
                        //load some other relevant data
                        //float benefit=Simulation.critters[i].energy;
                        //float tSpeed=Simulation.critters[i].speed;
                        //float threat=Simulation.critters[i].fitness-aggr; // resources pose no threat
                        
                        //float[] temp={distance,benefit,tSpeed,threat};
                        float[] temp={distance,Simulation.critters[i].fitness,Simulation.critters[i].energy,Simulation.critters[i].move.fitness,Simulation.critters[i].cRadius,Simulation.critters[i].children,Simulation.critters[i].life,Simulation.critters[i].maxLife,Simulation.critters[i].matingLife,Simulation.critters[i].sRange,Simulation.critters[i].aggr,Simulation.critters[i].contended};

                        float[] thought=brain.zap(temp);
                        if (thought[1]>best_fit){
                            best_fit=thought[1];
                            newmtarget = Simulation.critters[i];
                            //System.out.println(best_reaction);
                            //index2=i;
                        }
                    }
            }
          }
            
            if (newmtarget!=null){
                if (newmtarget!=ctarget){
                    //System.out.println(newmtarget.getName()+" selected for mating");
                    
                    //contend/uncontend
                    if (ctarget!=null) ctarget.uncontend(fitness);
                    newmtarget.contend(fitness);
                    ctarget=newmtarget;
                    
                    }
                    goTo=ctarget.bControl.getPhysicsLocation().subtract(bControl.getPhysicsLocation());
                    goToLocal=ctarget.getLocalTranslation();
            }
        
        }
        
        /*
        //if not hungry and don't want to mate, then idle
        
        else{
          //reset targets
          //rtarget=null;
          //ctarget=null;
            
          //goTo=Util.randomVector3f().divide(1000f);
          goTo=new Vector3f(0,0,0);
          //lookAt(goTo, new Vector3f(0,1,0));
          moveTo(goTo);
          //System.out.println(this.name+": Idle");
            
          //nothing happens in this state
       }
    
      //if it's in danger from something else
         */
        //avoidance state
        else if(state==2){    
            
        rtarget=null;    
            
        //first we look at all the resources
        float best_reaction=0;
        
        Obstacle newotarget=null;

        for (int i=0; i < Simulation.maxloop_obstacle+1; i++){
            if(Simulation.obstacles[i] != null){
                //here's where the brain should kick in...
                
                    float distance=getLocalTranslation().distance(Simulation.obstacles[i].getLocalTranslation());
                    if (distance<sRange){ //check if target is in sensor range
                        //load some other relevant data
                        //float benefit=-Simulation.obstacles[i].weight/1000;
                        //float tSpeed=0;
                        float threat=Simulation.obstacles[i].weight/100; // resources pose no threat
                        
                        //float[] temp={distance,benefit,tSpeed,threat};
                        float[] temp={distance,threat,0,0,0,0,0,0,0,0,0,0};

                        float[] thought=brain.zap(temp);
                        if (thought[2]>best_reaction){
                            best_reaction=thought[2];
                            newotarget = Simulation.obstacles[i];
                            //System.out.println(best_reaction);
                            //index2=i;
                        }
                    }

                    //if(index1!=index2) System.out.println("NN works!: "+index1+" vs "+index2);
                }
        }

        //then we look at all the other critters
        
        Critter newctarget=null;
        
        for (int i=0; i < Simulation.maxloop_critter+1; i++){
            if(Simulation.critters[i] != null && this!=Simulation.critters[i]){
                //here's where the brain should kick in...
                float distance=getLocalTranslation().distance(Simulation.critters[i].getLocalTranslation());
                    if (distance<sRange){ //check if target is in sensor range
                        //System.out.println(distance+" "+sRange);
                        //load some other relevant data
                        //float benefit=Simulation.critters[i].energy;
                        //float tSpeed=Simulation.critters[i].speed;
                        float threat=Simulation.critters[i].fitness-aggr; // resources pose no threat
                        
                        //float[] temp={distance,benefit,tSpeed,threat};
                        float[] temp={distance,threat,Simulation.critters[i].energy,Simulation.critters[i].move.fitness,Simulation.critters[i].cWeight,Simulation.critters[i].children,Simulation.critters[i].life,Simulation.critters[i].maxLife,Simulation.critters[i].matingLife,Simulation.critters[i].sRange,Simulation.critters[i].aggr,Simulation.critters[i].contended};

                        float[] thought=brain.zap(temp);
                        if (thought[2]>best_reaction){
                            best_reaction=thought[2];
                            newctarget = Simulation.critters[i];
                            newotarget=null;
                            //System.out.println(best_reaction);
                            //index2=i;
                        }
                    }
            }
        }
        
        //try{
        if (newotarget!=null){ //is the resource the best choince?
                if (newotarget != otarget){
                    otarget=newotarget; 
                }
                goTo=otarget.bControl.getPhysicsLocation().subtract(bControl.getPhysicsLocation());
                goToLocal=otarget.getLocalTranslation().negate().setZ(otarget.getLocalTranslation().getZ());       
            }
                        
            if (newctarget!=null){ //is the resource the best choince?
                if (newctarget != ctarget){
                    ctarget=newctarget;
                    otarget=null;
                } 
                
                goToLocal=ctarget.getLocalTranslation().negate().setZ(ctarget.getLocalTranslation().getZ());
                goTo=ctarget.bControl.getPhysicsLocation().subtract(bControl.getPhysicsLocation());
                        
            }
            
        }
        
        if(otarget==null && ctarget==null && rtarget==null){
            
            //idle if nothing to do
            goToLocal=getLocalTranslation();
            goTo=getLocalTranslation();
            state=3;
            
            //clear forces
            for (int i=0; i<limbs.length; i++){
                if (limbs[i] != null) limbs[i].nub.bControl.clearForces();
            }

            bControl.clearForces();
            
        } else move.run();
        
        //self preservation
        if (state==3 && energy < maxEnergy*hEnergy/100){ //hungry and have to roam
            goTo=Vector3f.ZERO;
            move.run();
        }    

}

 public Critter(/*float[] gnome,*/ float nergy, int gneration, int idx, Vector3f location){

      super("Critter "+(int)(Math.random()*10000),new Sphere(32, 32, Util.newRandom(0.5f), true, false));
      bControl=new RigidBodyControl(Util.newRandom10(10f));
      setLocalTranslation(location);
      addControl(bControl);
      setShadowMode(ShadowMode.CastAndReceive);

      mutation=Util.newRandom(0.1f)/100;
      crossover=Util.newRandom(0.1f);
      cRadius=((Sphere)getMesh()).radius;
      cWeight=bControl.getMass();
      maxEnergy=Util.newRandom10(10f);
      maxHealth=Util.newRandom10(10f);
      regenPerc=(float)Math.random();
      hEnergy=(float)Math.random()*70f+20f;
      mEnergy=hEnergy+(float)Math.random()*40f+50f; //mating energy threshold must be above hunger and below max.
      children=Util.newRandom10(1f);
      cEnergy=(float)Math.random()*5f+5f;
      maxLife=Util.newRandom10(5f);
      matingLife=(float)Math.random()*70f+20f;
      sRange=Util.newRandom10(10f);
      speed=(float)Math.random();
      range=(float)Math.random();
      aggr=(float)Util.randomClamped()*10f;

      life=maxLife;
      energy=nergy;
      generation=gneration;
      index=idx;

      health=maxHealth;

      //now create its brain

      brain=new NNetwork(brainStruct);
      
      //attempt to add limbs
      //add a random limb; first generation has a random limb
      
      Random randomGenerator = new Random();
      int slot = randomGenerator.nextInt(limbs.length);
      addLimb(Util.newRandom(0.2f)/2,Util.newRandom10(0.4f)/2,Util.newRandom10(1f),slot,Util.newRandom(0.3f),Util.newRandom10(5f));

      slot = randomGenerator.nextInt(limbs.length);
      addLimb(Util.newRandom(0.2f)/2,Util.newRandom10(0.4f)/2,Util.newRandom10(1f),slot,Util.newRandom(0.3f),Util.newRandom10(5f));
      
      move = new VMachine("move",this);
           
   }
 
    public Critter(/*float[] gnome,*/ float nergy, int gneration, int idx, Vector3f location, float mtation){
        this(nergy,gneration,idx,location);
        mutation+=mtation;
    }

/*
  public Critter (Critter c1){

      super("Critter "+(int)(Math.random()*10000),new Sphere(32, 32, (float)Math.random(), true, false));
      bControl=new RigidBodyControl(c1.cWeight);
      addControl(bControl);
      setShadowMode(ShadowMode.CastAndReceive);

      mutation=c1.mutation;
      cRadius=c1.cRadius;
      cWeight=c1.cWeight;
      maxEnergy=c1.maxEnergy;
      maxHealth=c1.maxHealth;
      regenPerc=c1.regenPerc;
      hEnergy=c1.hEnergy;
      mEnergy=(maxEnergy+hEnergy)/2f; //mating energy threshold must be above hunger and below max.
      children=c1.children;
      cEnergy=c1.cEnergy;
      maxLife=c1.maxLife;
      sRange=c1.sRange;
      speed=c1.speed;
      range=c1.range;
      aggr=c1.aggr;

      energy=0; //this has to be gotten from c1
      generation=c1.generation+1;

      health=maxHealth;

  }
 * 
 */

  public void takeEnergy(float en){
      energy-=en;
      if (energy<0) alive=false;
  }
  
  public void age(float ag){
      life-=ag;
      if (life<0) alive=false;
  }

  public void giveEnergy(float en){
      if (en+energy>maxEnergy) energy=maxEnergy;
      else { energy+=en; fitness+=en;}
  }

  public void takeHit(){
      if (health-1<0) alive=false;
      else health-=1;
  }
  
  public Critter(Critter c1, Critter c2, Vector3f location){
      
      super("Critter "+(int)(Math.random()*10000),new Sphere(32, 32, (Util.mutate((c1.mutation+c2.mutation)/2) ? (Util.onOff() ? c1.cRadius : c2.cRadius) : Util.newRandom(0.5f)), true, false));
      bControl=new RigidBodyControl((Util.mutate((c1.mutation+c2.mutation)/2) ? (Util.onOff() ? c1.cWeight : c2.cWeight) : Util.newRandom10(10f)));
      setLocalTranslation(location);
      addControl(bControl);
      setShadowMode(ShadowMode.CastAndReceive);
      
      //add parents
      parent1=c1;
      parent1name=c1.getName();
      parent1dna=c1.arrayDNA();
      parent2=c2;
      parent2name=c2.getName();
      parent2dna=c2.arrayDNA();
      
      //mix parent colors
      color= (Util.mutate((c1.mutation+c2.mutation)/2)) ? ColorRGBA.randomColor() : new ColorRGBA((Util.onOff() ? c1.color.r : c2.color.r),(Util.onOff() ? c1.color.g : c2.color.g),(Util.onOff() ? c1.color.b : c2.color.b),(Util.onOff() ? c1.color.a : c2.color.a)) ;

      mutation=(Util.mutate((c1.mutation+c2.mutation)/2) ? (Util.onOff() ? c1.mutation : c2.mutation) : Util.newRandom(0.1f)/100);
      crossover=(Util.mutate((c1.mutation+c2.mutation)/2) ? (Util.onOff() ? c1.crossover : c2.crossover) : Util.newRandom(0.1f));
      
      cRadius=((Sphere)getMesh()).radius;
      cWeight=bControl.getMass();
      maxEnergy=(Util.mutate((c1.mutation+c2.mutation)/2) ? (Util.onOff() ? c1.maxEnergy : c2.maxEnergy) : Util.newRandom10(10f));
      maxHealth=(Util.mutate((c1.mutation+c2.mutation)/2) ? (Util.onOff() ? c1.maxHealth : c2.maxHealth) : Util.newRandom10(10f));
      regenPerc=(Util.mutate((c1.mutation+c2.mutation)/2) ? (Util.onOff() ? c1.regenPerc : c2.regenPerc) : (float)Math.random());
      hEnergy=(Util.mutate((c1.mutation+c2.mutation)/2) ? (Util.onOff() ? c1.hEnergy : c2.hEnergy) : (float)Math.random()*70f+20f);
      mEnergy=(Util.mutate((c1.mutation+c2.mutation)/2) ? (Util.onOff() ? c1.mEnergy : c2.mEnergy) : (float)Math.random()*40f+50f);
      children=(Util.mutate((c1.mutation+c2.mutation)/2) ? (Util.onOff() ? c1.children : c2.children) : Util.newRandom10(1f));
      cEnergy=(Util.mutate((c1.mutation+c2.mutation)/2) ? (Util.onOff() ? c1.cEnergy : c2.cEnergy) : (float)Math.random())*5f+5f;
      maxLife=(Util.mutate((c1.mutation+c2.mutation)/2) ? (Util.onOff() ? c1.maxLife : c2.maxLife) : Util.newRandom10(5f));
      matingLife=(Util.mutate((c1.mutation+c2.mutation)/2) ? (Util.onOff() ? c1.matingLife : c2.matingLife) : (float)Math.random()*70f+20f);
      sRange=(Util.mutate((c1.mutation+c2.mutation)/2) ? (Util.onOff() ? c1.sRange : c2.sRange) : Util.newRandom10(10f));
      speed=(Util.mutate((c1.mutation+c2.mutation)/2) ? (Util.onOff() ? c1.speed : c2.speed) : (float)Math.random());
      range=(Util.mutate((c1.mutation+c2.mutation)/2) ? (Util.onOff() ? c1.range : c2.range) : (float)Math.random());
      aggr=(Util.mutate((c1.mutation+c2.mutation)/2) ? (Util.onOff() ? c1.aggr : c2.aggr) : (float)Util.randomClamped()*10f);

      energy=(2+c1.cEnergy*c1.energy/100 + c2.cEnergy*c2.energy/100<maxEnergy) ? 2+c1.cEnergy*c1.energy/100 + c2.cEnergy*c2.energy/100 : maxEnergy; 

      generation=(Util.onOff() ? c1.generation : c2.generation) + 1;
      life=maxLife;
      //index=idx;

      health=maxHealth;

      //now create its brain

      if (c1.brain.compare(c2.brain)){
        //if they have the same structure then they can be combined  
        
        //create brain with similar structure  
        brain=new NNetwork(c1.brainStruct);
        
        //check if crossover will occur
        if (Util.mutate((c1.crossover+c2.crossover)/2)){  
          
            
        
            //get their weights
            float[] c1brain=c1.brain.getWeights();
            float[] c2brain=c2.brain.getWeights();
        
            float[] cbrain = new float[c1brain.length];
        
            //combine their weights
            /*poor approach
            for (int i=0; i<c1brain.length; i++){
                cbrain[i]=(Util.mutate((c1.mutation+c2.mutation)/2) ? (Util.onOff() ? c1brain[i] : c2brain[i]) : (float)Util.randomClamped());
            }
             * 
             */
            
            //apply textbook crossover with mutation probability at each step
            Random randomGenerator = new Random();
            int cpoint = randomGenerator.nextInt(c1brain.length);
            
            if(Util.onOff()){
                //start with first parent
                for (int i=0; i<cpoint; i++){
                    cbrain[i]=(Util.mutate((c1.mutation+c2.mutation)/2) ? c1brain[i] : (float)Util.randomClamped());
                }
                for (int i=cpoint; i<c1brain.length; i++){
                    cbrain[i]=(Util.mutate((c1.mutation+c2.mutation)/2) ? c2brain[i] : (float)Util.randomClamped());
                }
            } else {
                //or start with second
                for (int i=0; i<cpoint; i++){
                    cbrain[i]=(Util.mutate((c1.mutation+c2.mutation)/2) ? c2brain[i] : (float)Util.randomClamped());
                }
                for (int i=cpoint; i<c1brain.length; i++){
                    cbrain[i]=(Util.mutate((c1.mutation+c2.mutation)/2) ? c1brain[i] : (float)Util.randomClamped());
                }
            }
                
            brain.setWeights(cbrain); //set new brain weights
            
        } else {
            //no crossover occurs
            if(Util.onOff()){
                float[] c1brain=c1.brain.getWeights();
                brain.setWeights(c1brain);
            } else {
                float[] c2brain=c2.brain.getWeights();
                brain.setWeights(c2brain);
            }
        }
      }
      else{ //if they do not have the same structure
        if(Util.onOff()){ //select one at random
            brain=new NNetwork(c1.brainStruct); //copy it
           
            float[] c1brain=c1.brain.getWeights();
        
            float[] cbrain = new float[c1brain.length];
        
            for (int i=0; i<c1brain.length; i++){ //introduce some mutation
                cbrain[i]=(Util.mutate(c1.mutation) ? c1brain[i] :  (float)Util.randomClamped());
            }
            
            brain.setWeights(cbrain); //set new brain weights
        }
        else{
            brain=new NNetwork(c2.brainStruct); //or copy the other one
           
            float[] c2brain=c2.brain.getWeights();
        
            float[] cbrain = new float[c2brain.length];
        
            for (int i=0; i<c2brain.length; i++){ //introduce some mutation
                cbrain[i]=(Util.mutate(c2.mutation) ? c2brain[i] :  (float)Util.randomClamped());
            }
            
            brain.setWeights(cbrain); //set new brain weights
        }
      }
      
      //attempt to breed limbs

      for(int i=0; i<limbs.length; i++){
          //if the limbs are there, attempt to breed 
          if (c1.limbs[i]!=null && c2.limbs[i]!=null){
              addLimb(Util.mutate((c1.mutation+c2.mutation)/2) ? (Util.onOff()? c1.limbs[i].width : c2.limbs[i].width) : Util.newRandom(0.2f)/2, Util.mutate((c1.mutation+c2.mutation)/2) ? (Util.onOff()? c1.limbs[i].height : c2.limbs[i].height) : Util.newRandom10(0.4f)/2, Util.mutate((c1.mutation+c2.mutation)/2) ? (Util.onOff()? c1.limbs[i].weight : c2.limbs[i].weight) : Util.newRandom10(1f), i ,Util.mutate((c1.mutation+c2.mutation)/2) ? (Util.onOff()? c1.limbs[i].nubRadius : c2.limbs[i].nubRadius) : Util.newRandom(0.3f), Util.mutate((c1.mutation+c2.mutation)/2) ? (Util.onOff()? c1.limbs[i].nubWeight : c2.limbs[i].nubWeight) : Util.newRandom10(5f));
          }
          //choose at random and add one of them completely... appendices still need to be added
          else
              if (Util.onOff()) if (c1.limbs[i] != null){
              addLimb(Util.mutate(c1.mutation) ? c1.limbs[i].width : Util.newRandom(0.2f)/2, Util.mutate(c1.mutation) ? c1.limbs[i].height : Util.newRandom10(0.4f)/2 , Util.mutate(c1.mutation) ? c1.limbs[i].weight : Util.newRandom10(1f), i, Util.mutate(c1.mutation) ? c1.limbs[i].nubRadius : Util.newRandom(0.3f), Util.mutate(c1.mutation) ?  c1.limbs[i].nubWeight : Util.newRandom10(5f));
              //need to add a deep-copy mechanism for appendices
              }
              else if (c2.limbs[i] != null){
                   addLimb(Util.mutate(c2.mutation) ? c2.limbs[i].width : Util.newRandom(0.2f)/2, Util.mutate(c2.mutation) ? c2.limbs[i].height : Util.newRandom10(0.4f)/2, Util.mutate(c2.mutation) ? c2.limbs[i].weight : Util.newRandom10(1f), i, Util.mutate(c2.mutation) ? c2.limbs[i].nubRadius : Util.newRandom(0.3f), Util.mutate(c2.mutation) ?  c2.limbs[i].nubWeight : Util.newRandom10(5f));
                   //need to add deep-copy mechanism?
              }
      }
      
      //some limbs might randomly appear/replace others via mutation
      
      if (Util.mutate((c1.mutation+c2.mutation)/2)){
                Random randomGenerator = new Random();
                int slot = randomGenerator.nextInt(limbs.length);
                addLimb(Util.newRandom(0.2f)/2,Util.newRandom10(0.4f)/2,Util.newRandom10(1f),slot,Util.newRandom(0.3f),Util.newRandom10(5f));
      }
      
      //breed the movement and rotation sequences
      //make a big list with all the moves while removing all invalid ones
      
      //this part is actually relatively unimportant because the machines change dramatically overt time anyway
      //no knowledge need to be preserved
      
      move=new VMachine("move",this);
      
      VMachine temp_move = new VMachine("move",this);
      
      for (Iterator<Instruction> it=c1.move.instructions.iterator(); it.hasNext();){
            Instruction in = (Instruction) it.next();
            //check if a similar limb is found, then add a new identical instruction
            for (int i=0; i<limbs.length; i++){
                //limb slot might be empty
                if (limbs[i]!=null){
                    if (limbs[i].sameAs(in.limb)) temp_move.add(new Instruction(limbs[i],in.xParam,in.yParam,in.zParam,in.delay));
                    //System.out.println("Common Instruction Found");
                }
            }
      }
      for (Iterator<Instruction> it=c2.move.instructions.iterator(); it.hasNext();){
            Instruction in = (Instruction) it.next();
            //check if a similar limb is found, then add a new identical instruction
            for (int i=0; i<limbs.length; i++){
                if (limbs[i]!=null){
                    //System.out.println("Common Instruction Found");
                    if (limbs[i].sameAs(in.limb)) temp_move.add(new Instruction(limbs[i],in.xParam,in.yParam,in.zParam,in.delay));
                }
            }
      }
      
      //System.out.println(c1.move.instructions.toString()); 
     
    //configure machine with list of instructions
    move.preset(temp_move.instructions);
      
  }
  
  public Critter(Critter c1, Critter c2, int idx, Vector3f location){
      this(c1,c2,location);
      index=idx;
  }
  
  
  public String describeDNA(){
      String output="";
      output+="Name: "+name+"\n";
      output+="Internal ID: "+index+"\n";
      output+="Generation: "+generation+"\n";
      output+="Parent 1:"+parent1name+"\n";
      output+="Parent 2:"+parent2name+"\n";
      output+="--------------\n";
      output+="State: "+state_names[state]+"\n";
      output+="Location: "+getLocalTranslation() +"\n";
      output+="Going To: "+goToLocal+"\n";
      output+="Distance: "+getLocalTranslation().distance(goToLocal) +"\n";
      if (ctarget!=null)
          output+="Target :"+ctarget.getName()+"\n";
      if (rtarget!=null)
          output+="Target :"+rtarget.getName()+"\n";
      if (otarget!=null)
          output+="Target :"+otarget.getName()+"\n";
      output+="Fitness: "+fitness+"\n";
      output+="Energy: "+energy+"\n";
      output+="--------------\n";
      output+="Sensor Range: "+sRange+"\n";
      output+="Max Energy: "+maxEnergy+"\n";
      output+="Mating Threshold: "+mEnergy+"\n";
      output+="Hunger Threshold: "+hEnergy+"\n";
      output+="Max Life: "+maxLife+"\n";
      output+="Remaining Life: "+life+"\n";
      output+="Maturity Threshold: "+matingLife+"\n";
      output+="Aggression: "+aggr+"\n";
      output+="Mutation :"+mutation+"\n";
      output+="Crossover :"+crossover+"\n";
      output+="--------------\n";
      output+="Core Radius: "+cRadius+"\n";
      output+="Core Weight: "+cWeight+"\n";
      
      return output;
  }
  
  public String describeLimbs(){
      
      String output="";
      
      for(int i=0; i<limbs.length; i++){
          if (limbs[i] != null){
              output+="["+i+"] "+limbs[i].getName()+"\n";
              output+="   --Length: "+limbs[i].height+"\n";
              output+="   --Thickness: "+limbs[i].width+"\n";
              output+="   --Weight: "+limbs[i].weight+"\n";
          } else output+="["+i+"] empty \n";          
      }
      
      return output;
  }
  
  public String describeVMachine(){
      
      String output="";
      
      output+="Step: "+move.step+"/"+move.max_step+" ( "+move.instructions.size()+" Instructions ) Movement fitness: "+move.fitness+"\n";
      

      int counter=0;
      for (Iterator<Instruction> it=move.instructions.iterator(); it.hasNext();){
            Instruction in = (Instruction) it.next();
            //depending on what is executing, add >>> in front
            if (move.next_instr==counter) output+=">>> "+counter+" "; 
            else output+="--- "+counter+" ";
            output+=in.limb.getName()+"("+in.xParam+","+in.yParam+","+in.zParam+") \n     -Delay: "+in.delay+" \n     -Fitness: "+in.fitness+"\n";
            counter++;
      }

      return output;
      
  }
  
  public String describeNNetwork(){
     
     //for hunger
     boolean[] test_orders0 = {false,false,true,false,true,false,true,false,true,false,false,false};
     //for mating
     boolean[] test_orders1 = {false,true,true,false,true,true,true,false,true,true,true,false};
     //for avoidance
     boolean[] test_orders2 = {false,true,false,true,true,true,true,true,true,true,true,false};
      
      String output="Brain Structure: {";
      for(int i=0; i<brainStruct.length; i++) 
          if (i==0) output+=brainStruct[i];
          else output+=","+brainStruct[i];
      output+="}\n";
      
      float neuraleff=Util.testNNetwork(brain, 0, test_orders0, 5);
      output+="Feeding Efficiency: "+neuraleff+"\n";
      
      neuraleff=Util.testNNetwork(brain, 1, test_orders1, 5);
      output+="Mate Choosing: "+neuraleff+"\n";
      
      neuraleff=Util.testNNetwork(brain, 2, test_orders2, 5);
      output+="Avoidance: "+neuraleff+"\n";
      
      
      output+="\n"+brain.toString();
      
      return output;
      
  }
  
  public float[] arrayDNA(){
      
      float[] output=new float[13];
      
      output[0]=generation;
      output[1]=fitness;
      output[2]=sRange;
      output[3]=maxEnergy;
      output[4]=mEnergy;
      output[5]=hEnergy;
      output[6]=maxLife;
      output[7]=matingLife;
      output[8]=aggr;
      output[9]=mutation;
      output[10]=crossover;
      output[11]=cRadius;
      output[12]=cWeight;
      
      return output;
  }
  
  public void contend(float power){
      contended+=power;
  }
  
  public void uncontend(float power){
      contended-=power;
  }
  
  public boolean hasLimbs(){
      
      boolean aLimb=false;
      for (int i=0;i<limbs.length;i++)
          if (limbs[i]!=null) aLimb=true;
      
      return aLimb;
  }
          
}
