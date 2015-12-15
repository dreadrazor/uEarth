//uEarth: Artificial Life Simulation Software
//(C) Alin-Dragos Petculescu 2010
//Univeristy of Leicester
//www2.le.ac.uk
//Please credit the original author if reusing this code

package earth;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import com.jme3.math.Vector3f;

/**
 *
 * @author DreadRazor
 */

public class VMachine{

    //state variables
    public int step=-1;
    public int max_step=0;
    
    public int next_instr=0;
    public int next_step=0;
    
    //maximum delay between instructions
    public int maxdelay=2; //0 means no delay
    public float minfitness=0.01f;
    public int numInstr = 6;
    
    //instruction list
    public List<Instruction> instructions = new ArrayList<Instruction>();
    
    //old location
    public Vector3f oldPos;
    
    public float fitness=0;
    public float run_fitness=0;
    
    //machine details
    public String name;
    public Critter owner;

    public VMachine(String xname, Critter ownr){
        name=xname;
        owner=ownr;
        
        Random randomGenerator = new Random();
        
        //generate an instruction for each limb
        
        for(int i=0; i<owner.limbs.length; i++){
          //generate random instructions for each limb
          if (owner.limbs[i]!=null)
          //for(int j=0; j<5; j++)    
          add(new Instruction(owner.limbs[i],(float)Util.randomClamped(),(float)Util.randomClamped(),(float)Util.randomClamped(),randomGenerator.nextInt(maxdelay)+1));
        }
        
        oldPos=new Vector3f(owner.getLocalTranslation().x,owner.getLocalTranslation().y,owner.getLocalTranslation().z);
        
    }

    public void run(){

        //state();

        //if there is something in the list
        if (!instructions.isEmpty()){
            
        //increment the step

        
        //if we are at the step where the instruction has to be executed
        
        //System.out.println(step + " >>> " +next_step);
        
        if (step==next_step){
            //System.out.print(step+":");
            //then execute it
            Instruction runThis = instructions.get(next_instr);
            //System.out.println("executing "+next_instr+ "@ step" + step);
            
            if (runThis==null && owner.hasLimbs()) Settings.log("(!)"+owner.getName()+" has a broken virtual machine");
            
            //owner.bControl.clearForces();
            //for (int k=0; k<owner.limbs.length; k++) if (owner.limbs[k]!=null) owner.limbs[k].bControl.clearForces();
            owner.lookAt(owner.goToLocal, new Vector3f(0,1,0));
            runThis.run();
            
            //System.out.print("running: " + runThis.toString());
            
            //calculate fitness of the previous instruction instruction (this one is running)
            //has it gotten the creature closer to the target?
            
            Instruction previousRunThis;
            
            if (next_instr > 0)
            previousRunThis = instructions.get(next_instr-1);
            else previousRunThis = instructions.get(instructions.size()-1);
            
            previousRunThis.fitness=oldPos.distance(owner.goToLocal)-owner.getLocalTranslation().distance(owner.goToLocal);
            
            //System.out.print("fitness: "+previousRunThis.fitness);
            //System.out.println(" based on: "+oldPos.toString()+" and "+owner.getLocalTranslation().toString());
            
            //increment total machine fitness per run
            run_fitness+=previousRunThis.fitness;
            
            //if not... mutate it and see what happens
            if (previousRunThis.fitness<minfitness){
                
                    Random randomGenerator = new Random();
                    
                    if (Util.mutate(owner.mutation)) previousRunThis.xParam += Util.onOff() ? -(float)Math.random() : (float)Math.random();
                    if (Util.mutate(owner.mutation)) previousRunThis.yParam += Util.onOff() ? -(float)Math.random() : (float)Math.random();
                    if (Util.mutate(owner.mutation)) previousRunThis.zParam += Util.onOff() ? -(float)Math.random() : (float)Math.random();
                    
                    //delay mutation seems like a bad idea
                    
                    if (Util.mutate(owner.mutation)){
                        max_step-=previousRunThis.delay;
                        previousRunThis.delay = randomGenerator.nextInt(maxdelay)+1;
                        max_step+=previousRunThis.delay;
                     }
                     
                    //mark the instruction down as having produced a bad result
                    previousRunThis.strikes-=1;
            } else previousRunThis.strikes+=1;
            
            //update position
            oldPos=new Vector3f(owner.getLocalTranslation().x,owner.getLocalTranslation().y,owner.getLocalTranslation().z);
            
            //go to the next instruction
            next_instr++;
            
            //and increment the next_step by the delay on that instruction
            next_step=step+runThis.delay;
            
            if (next_step>max_step) {
                next_step=0;
                next_instr=0;
            }
            
            //System.out.println("next is "+next_instr+" @ step "+next_step);
            
            //remove instruction if it had all the strikes against it
 
            
        } //else System.out.println(step+":-");
        
        step++;
        
        //reset the step if required
        if (step>=max_step){
            //then a whole instruction cycle has completed
            step=0; //go back to beginning
            fitness=run_fitness;
            run_fitness=0f;
            
            next_instr=0;
            next_step=0;
            
            //find best instruction
            Instruction best = null;
            float fitn=minfitness;
            
            for (Iterator<Instruction> it=instructions.iterator(); it.hasNext();){
                Instruction in = (Instruction) it.next();
                if (in.fitness>fitn) best=in;
            }
            
            List<Instruction> toRemove = new ArrayList<Instruction>();
            
            //mark low fitness instructions           
            for (Iterator<Instruction> it=instructions.iterator(); it.hasNext();){
                Instruction in = (Instruction) it.next();
                if (in.strikes<0){
                    toRemove.add(in);
                    //add best if one found
                    //if (best!=null) add(new Instruction(best.limb,best.xParam,best.yParam,best.zParam,best.delay));
                }
            }
            
            for (Iterator<Instruction> it=toRemove.iterator(); it.hasNext();){
                Instruction in = (Instruction) it.next();
                    remove(in);
            }
        
            //if there is room
            if (instructions.size()<numInstr){
            
            Random randomGenerator = new Random();
                
            //add new instructions 
            for(int i=0; i<owner.limbs.length; i++){
                //generate random instructions for each limb
                if (owner.limbs[i]!=null)
                //for(int j=0; j<5; j++)    
                add(new Instruction(owner.limbs[i],(float)Util.randomClamped(),(float)Util.randomClamped(),(float)Util.randomClamped(),randomGenerator.nextInt(maxdelay)+1));
            }
            
            //take the one with the best fitness>0 and replicate it
            

            
            //add it if one found
            if (best!=null){
                Instruction newBest = new Instruction(best.limb,best.xParam,best.yParam,best.zParam,best.delay);
                
                randomGenerator = new Random();
                    
                    if (Util.mutate(owner.mutation)) newBest.xParam += Util.onOff() ? -(float)Math.random() : (float)Math.random();
                    if (Util.mutate(owner.mutation)) newBest.yParam += Util.onOff() ? -(float)Math.random() : (float)Math.random();
                    if (Util.mutate(owner.mutation)) newBest.zParam += Util.onOff() ? -(float)Math.random() : (float)Math.random();
                    
                    if (Util.mutate(owner.mutation)){
                        //max_step-=newBest.delay;
                        newBest.delay = randomGenerator.nextInt(maxdelay)+1;
                        //max_step+=newBest.delay; //does not need to be added, the add does it already
                     }
                
              add(newBest);
            
            }
            
            }
        
        }
        
        //if we have run out of instructions, then go to the first one
        //next_instr++;
        if (!(next_instr<instructions.size())) next_instr=0;
        
        } else if (owner.hasLimbs()){ //the instruction list was empty for whatever reason
            Settings.log("(!)"+owner.getName()+" attempting to fix broken virtual machine");
            
            Random randomGenerator = new Random();
        
            for(int i=0; i<owner.limbs.length; i++){
                //generate random instructions for each limb
                if (owner.limbs[i]!=null)
                //for(int j=0; j<5; j++)    
                add(new Instruction(owner.limbs[i],(float)Util.randomClamped(),(float)Util.randomClamped(),(float)Util.randomClamped(),randomGenerator.nextInt(maxdelay)+1));
            }
        }
        
    }
    
    public void describe(){
        for (Iterator<Instruction> it=instructions.iterator(); it.hasNext();){
            Instruction in = (Instruction) it.next();
            System.out.println(in.limb.getName() + " " + in.xParam + " " + in.yParam + " " + in.zParam + " " +in.delay);
        }
    }
    
    public void state(){
       Settings.log("(!)"+owner.getName()+" debug: "+step + " "+max_step+" "+next_step+" "+next_instr+" "+instructions.size());
    }
    
    public void add(Instruction instr){
        instructions.add(instr);
        max_step+=instr.delay;
        
    }
    
    public void remove(Instruction instr){
        boolean removed = false;
        removed = instructions.remove(instr);
        if (removed) max_step-=instr.delay;
    }
    
    public void clear(){
        instructions.clear();
        max_step=0;
    }
    
    public void preset(List<Instruction> given){
            
            clear();
        
            for (Iterator<Instruction> it=given.iterator(); it.hasNext();){
                Instruction in = (Instruction) it.next();
                //copy each instruction
                add(new Instruction(in.limb,in.xParam,in.yParam,in.zParam,in.delay));
                max_step+=in.delay;
            }
            
    }
    
    public VMachine clone(Critter ownr){
        //todo
        VMachine out=new VMachine(name,ownr);
        
        out.clear();
        
        for (Iterator<Instruction> it=instructions.iterator(); it.hasNext();){
            Instruction in = (Instruction) it.next();
            //copy each instruction
            out.add(new Instruction(in.limb,in.xParam,in.yParam,in.zParam,in.delay));
            
        }
        
        //copy some of the machine variables
        out.fitness=fitness;
        out.max_step=max_step;
        
        return out;
    }
            
  }