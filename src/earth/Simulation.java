//uEarth: Artificial Life Simulation Software
//(C) Alin-Dragos Petculescu 2010
//Univeristy of Leicester
//www2.le.ac.uk
//Please credit the original author if reusing this code

//Code based on examples: http://jmonkeyengine.org/wiki/doku.php/jme3#tutorials_for_beginners

package earth;

/**
 *
 * @author DreadRazor
 */

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.BasicShadowRenderer;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Simulation extends SimpleApplication {

  //create an instance of a simulation and run it
  public static void main(String args[]){
    try{
        Params p=new Params();
        Simulation sim = new Simulation(p);
        sim.start();
    } catch(Exception e){
        Simulation sim = new Simulation();
        sim.start();
        //Settings.log("(?)Could not find parameters file... Using defaults");
    }    

  }
  
public Simulation(Params p){
 
  this(); 
   
  //some limits set on the simulation
  init_population=p.init_population; //starting population
  max_population=p.max_population;
  min_population=p.min_population;
  init_resources=p.init_resources;
  max_resources=p.max_resources;
  min_resources=p.min_resources;
  
  //best specimens number (mating will happen 'artificially' between these)
  best_num=p.best_num;
  
  //age speed
  age_speed=p.age_speed;
  
  //energy depletion speed
  energy_speed=p.energy_speed;
   
  //simulation tick frequency
  sim_frequency=p.sim_frequency;
  
  //stats gathering frequency
  stat_frequency=p.stat_frequency;
  
  //we add the ability ability to customize world size
  xFloor=p.xFloor;
  yFloor=p.yFloor;
  zFloor=p.zFloor;
  
  range1 = p.range1;
  range2 = p.range2;
  
  init_obstacles = p.init_obstacles;
  max_obstacles=p.max_obstacles;
                
  fixed_mutation = p.fixed_mutation;
  fixed_crossover = p.fixed_crossover;
  enforce_params = p.enforce_params;
  
}
 /* Prepare the Physics Application State (jBullet) */
  public BulletAppState bulletAppState;

  /** Activate custom rendering of shadows */
  BasicShadowRenderer bsr;

  //public GraphingData GD=null;
  //public JFrame StatsWindow=null;
  
  public static Settings options;
  
  public Statistics stats;
  
  public class Statistics {
  Toolkit toolkit;

  Timer timer;
  
  //stats to be tracked
  //population number
  public List<Integer> population  = new ArrayList<Integer>();
  //max fitness
  public List<Float> maxfitness  = new ArrayList<Float>();
  //avg fitness
  public List<Float> avgfitness  = new ArrayList<Float>();
  //number of children
  public List<Float> children  = new ArrayList<Float>();
  //size of core
  public List<Float> coresize  = new ArrayList<Float>();
  //weight
  public List<Float> weights  = new ArrayList<Float>();
  //movement efficiency
  public List<Float> movefficiency  = new ArrayList<Float>();
  //nnet efficiency
  public List<Float> neuralefficiency  = new ArrayList<Float>();
  //mutation
  public List<Float> mutation  = new ArrayList<Float>();
  //maximum lifespan
  public List<Float> maxlives  = new ArrayList<Float>();
  //aggression levels
  public List<Float> aggrs  = new ArrayList<Float>();
  //resource energy fluctuations
  public List<Float> energies  = new ArrayList<Float>();
  //average energy level
  public List<Float> critener  = new ArrayList<Float>();
  //average crossover level
  public List<Float> crossover  = new ArrayList<Float>();

  public Statistics(int freq) {
    toolkit = Toolkit.getDefaultToolkit();
    timer = new Timer();
    timer.schedule(new Gather(), 5000, //initial delay
        freq * 1000); //subsequent rate
  }
  
  public void describe(){
        for (Iterator<Integer> it=population.iterator(); it.hasNext();){
            Integer in = (Integer) it.next();
            System.out.print(in+",");
        }            
  }

  class Gather extends TimerTask {

    public void run() {
        
        //indication that stats have been collected
        toolkit.beep();
        //stats.describe();
        
        //record population at a given point in time
        population.add(current_pop);
        
        //create a load of variables to hold the info
        float maxfit=0;
        float avgfit=0;
        float avgchildren=0;
        float csize=0;
        float avgweight=0;
        float moveeff=0;
        float neuraleff=0;
        float mut=0;
        float mlife=0;
        float aggr=0;
        float ens=0;
        float critterenergies=0;
        float crssovr=0;
        
        //gather averages and maxes from the critter population
        for (int i=0; i<critters.length; i++){
            if (critters[i]!=null){
                if (critters[i].fitness>maxfit) maxfit=critters[i].fitness;
                avgfit+=critters[i].fitness;
                avgchildren+=critters[i].children;
                csize+=critters[i].cRadius;
                avgweight+=critters[i].cWeight;
                moveeff+=critters[i].move.fitness;
                //boolean[] orders = {true,true,false,true};
                neuraleff+=(Util.testNNetwork(critters[i].brain, 0, test_orders0, 5)+
                        Util.testNNetwork(critters[i].brain, 1, test_orders1, 5)+
                        Util.testNNetwork(critters[i].brain, 2, test_orders2, 5))/3;
                mut+=critters[i].mutation;
                mlife+=critters[i].maxLife;
                aggr+=critters[i].aggr;
                critterenergies+=critters[i].energy;
                crssovr+=critters[i].crossover;
            }
        }
        
        //average it
            avgfit=avgfit/current_pop;
            avgchildren=avgchildren/current_pop;
            csize=csize/current_pop;
            avgweight=avgweight/current_pop;
            moveeff=moveeff/current_pop;
            neuraleff=neuraleff/current_pop;
            mut=mut/current_pop;
            mlife=mlife/current_pop;
            aggr=aggr/current_pop;
            critterenergies=critterenergies/current_pop;
            crssovr=crssovr/current_pop;
            
       //add it to the lists
            maxfitness.add(maxfit);
            avgfitness.add(avgfit);
            children.add(avgchildren);
            coresize.add(csize);
            weights.add(avgweight);
            mutation.add(mut);
            maxlives.add(mlife);
            aggrs.add(aggr);
            critener.add(critterenergies);
            movefficiency.add(moveeff);
            neuralefficiency.add(neuraleff);
            crossover.add(crssovr);
       
       //gather energy levels from all resources
       for(int i=0; i<resources.length; i++){
           if (resources[i]!=null) ens+=resources[i].energy;
       }
       
       //average it and add it to stats
       ens=ens/current_res;
       energies.add(ens);
       
       Settings.log("(i)Statistics collected...");
     }
  }
  
  }
   public float counter=0;
   public float sim_frequency=100f;
   public int stat_frequency=10;
  
  //some limits set on the simulation
  public int init_population=10; //starting population
  public static int max_population=10000;
  public int min_population=5;
  public static Critter[] critters;
  public int current_pop=0;
  public int init_resources=20;
  public static int max_resources=10000;
  public int min_resources=10;
  public static Resource[] resources;
  public int current_res=0;
  
  public static int maxloop_critter=0;
  public static int maxloop_resource=0;
  public static int maxloop_obstacle=0;
  
  
  //number of obstacles
  public static int max_obstacles=10000;
  public int init_obstacles=100;
  public static Obstacle[] obstacles;
  
  //best specimens number (mating will happen 'artificially' between these)
  public int best_num=5;
  
  //neural efficiency test sequence
  //float[] temp={distance,fitness,benefit,tSpeed,cRadius,children,life,maxLife,matingLife,sRange,aggr,contended};
                        
  //for hunger
  boolean[] test_orders0 = {false,false,true,false,true,false,true,false,true,false,false,false};
  //for mating
  boolean[] test_orders1 = {false,true,true,false,true,true,true,false,true,true,true,false};
  //for avoidance
  boolean[] test_orders2 = {false,true,false,true,true,true,true,true,true,true,true,false};
  //age speed
  public float age_speed=0.001f;
  
  //energy depletion speed
  public float energy_speed=0.001f;
  
  //minimum mutation and crossover rate
  public float fixed_mutation=0.001f;
  public float fixed_crossover=0.5f;
  public boolean enforce_params=false;
  
  //first we define what the creatures/resources 'stand' on
  private RigidBodyControl    floor_phy;
  private static Box   floor;
  private Geometry floor_geo;
  private Material floor_mat;
  
  //we add the ability ability to customize world size
  public float xFloor=100f;
  public float yFloor=100f;
  public float zFloor=0.1f;
  
  //world range
  public float range1=99f;
  public float range2=99f;

  //we also add some selection stuff so we can copy or track
  public Critter selected_critter=null;
  public Resource selected_resource=null;
  
  private static Geometry tracker1;
  private Material tracker1_mat;
  
  private static Geometry tracker2;
  private Material tracker2_mat;
  
  public int births=0;
  public int deaths=0; 

  public Resource addResource(){
      //this one does not need an index
      for (int i=0; i<resources.length; i++){
          if (resources[i]==null){
              Resource ret=addResource(i);
              return ret;
          }
      }
      return null;       
   }
  
  public Resource addResource(int idx){

        //create a new resource
        Resource r=new Resource(Util.randomVector3f(range1,range2),idx);

        //add it to global index
        resources[idx]=r;

        //load and set material
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/SolidColor.j3md");
        mat.setColor("m_Color", r.color);
        r.setMaterial(mat);

        //attach to rootNode
        rootNode.attachChild(r);
        //attach physics to physics space
        bulletAppState.getPhysicsSpace().add(r.bControl);

        //increment population
        current_res++;
        
        //increment maxloop
        if(r.index>maxloop_resource) maxloop_resource=r.index;
        
        return resources[idx]; //return creature handle
    }

  public void addResources(){
    
    if (init_resources>max_resources) init_resources=max_resources;  
    //Put resources one by one
    for (int i=0; i<init_resources; i++) addResource(i);

  }
  
  public void addObstacles(){
      
    if (init_obstacles>max_obstacles) init_obstacles=max_obstacles;  
      
    //Put obstacles one by one
    for (int i=0; i<init_obstacles; i++){
        //create a new resource
        Obstacle o=new Obstacle(Util.randomLowVector3f(xFloor, yFloor),i);
        
        //add it to global index
        obstacles[i]=o;

        //load and set material
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/SolidColor.j3md");
        mat.setColor("m_Color", o.color);
        o.setMaterial(mat);

        //attach to rootNode
        rootNode.attachChild(o);
        //attach physics to physics space
        bulletAppState.getPhysicsSpace().add(o.bControl);
        
        //increment maxloop
        if (o.index>maxloop_obstacle) maxloop_obstacle=o.index;
    }
           
  }
  
  public Critter addCritter(){
      //this one does not need an index
      for (int i=0; i<critters.length; i++){
          if (critters[i]==null){
              Critter ret=addCritter(i);
              return ret;
          }
      }
      return null;       
   }
  
  public Critter addCritter(int idx){

      //instantiate new critter
      Critter c=new Critter(Util.newRandom(2f), 0, idx, Util.randomVector3f(range1,range2));
      
      //if strict parameters are enforced
      if (enforce_params){
          c.crossover=fixed_crossover;
          c.mutation=fixed_mutation;
      }
  
      //add it to global index
      critters[idx]=c;

      //load and set materials
      Material mat = new Material(assetManager, "Common/MatDefs/Misc/SolidColor.j3md");
      mat.setColor("m_Color", c.color);
      c.setMaterial(mat);

      //attach to root node
      rootNode.attachChild(c);
      //enable physics
      bulletAppState.getPhysicsSpace().add(c.bControl);
      
      //add limbs
      addLimbs(c);

      //increment population
      current_pop++;
      //increment birth toll
      births++;
      
      //maxloop indicates the populated section of the array
      if(idx>maxloop_critter) maxloop_critter=idx;
      
      return critters[idx];
  }

  public void addCritters(){
    
    if (init_population>max_population) init_population=max_population;  
    //Put critters in one by one

    for (int i=0; i<init_population; i++) addCritter(i);

  }
  
  public void addLimb(Critter c, float width, float height, float weight, int slot, float nubRadius, float nubWeight){
          
    Limb nl = c.addLimb(width , height , weight, slot, nubRadius, nubWeight);
    
    if (nl!=null){
    
        //set limb materials
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/SolidColor.j3md");
        mat.setColor("m_Color", nl.color);
        nl.setMaterial(mat);
        
        //set nub material
        mat = new Material(assetManager, "Common/MatDefs/Misc/SolidColor.j3md");
        mat.setColor("m_Color", nl.nub.color);
        nl.nub.setMaterial(mat);
        
        rootNode.attachChild(nl);
        bulletAppState.getPhysicsSpace().addAll(nl);
        
        rootNode.attachChild(nl.nub);
        bulletAppState.getPhysicsSpace().addAll(nl.nub);
    }
    
  }
  
    public void addLimbs(Critter c){
          
    //Limb nl = c.addLimb(width , height , weight, slot);
    
    for (int i=0; i < c.limbs.length; i++){    
        
    if (c.limbs[i]!=null){
    
        //for each limb, materialize the limb in the simulation space
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/SolidColor.j3md");
        mat.setColor("m_Color", c.limbs[i].color);
        c.limbs[i].setMaterial(mat);
        
        //set nub material
        mat = new Material(assetManager, "Common/MatDefs/Misc/SolidColor.j3md");
        mat.setColor("m_Color", c.limbs[i].nub.color);
        c.limbs[i].nub.setMaterial(mat);
    
        rootNode.attachChild(c.limbs[i]);
        bulletAppState.getPhysicsSpace().addAll(c.limbs[i]);
        
        //add the nub
        rootNode.attachChild(c.limbs[i].nub);
        bulletAppState.getPhysicsSpace().addAll(c.limbs[i].nub);
    }
    
    }
    
  }

  public void eat(Critter c, Resource r){
        c.giveEnergy(r.energy);
        Settings.log("(i)"+c.getName()+" ate "+r.getName() +" @ "+r.getLocalTranslation());
        removeResource(r);
        c.bControl.clearForces();
        c.rtarget=null;
  }

  public void attack(Critter c1, Critter c2){
        if (c1.energy>=c2.energy){
            c1.giveEnergy(c2.energy);
            c2.takeEnergy(c2.energy);
            c1.bControl.clearForces();
            Settings.log(c1.getName()+" ate "+c2.getName() +" @ "+c1.getLocalTranslation());
        }else{
            c2.giveEnergy(c1.energy);
            c1.takeEnergy(c1.energy);
            c2.bControl.clearForces();
            Settings.log("(i)"+c2.getName()+" ate "+c1.getName() +" @ "+c2.getLocalTranslation());
        }
        c1.ctarget=null;
        c2.ctarget=null;
        c1.rtarget=null;
        c2.rtarget=null;
        c1.otarget=null;
        c2.otarget=null;
  }
  
  public void removeResource(Resource r){
      rootNode.detachChild(r);
      //System.out.println("Removed "+r.getName()+" "+r.index);
      bulletAppState.getPhysicsSpace().remove(r.bControl);
      
      //decrement maxloop
      if (r.index==maxloop_resource) maxloop_resource--;
      
      resources[r.index]=null;
      
      //decrement resources
      current_res--;

  }
  
  public void removeCritter(Critter c){
      
      //might happen...
      if (c!=null){
     
      //chop off all its limbs
      removeLimbs(c);
      //if (c.bControl!=null)
      rootNode.detachChild(c);
      bulletAppState.getPhysicsSpace().remove(c.bControl);
      
      //it the last critter has been removed, populated area of the array is decremented
      if (c.index==maxloop_critter) maxloop_critter--;
      
      //remove critter from population
      critters[c.index]=null;
      
      //decrement population
      current_pop--;
      //increment death toll
      deaths++;
      

      
      }
  }
  
  public void removeLimbs(Critter c){
      
      for (int i=0; i<c.limbs.length; i++){
          if (c.limbs[i]!=null){
            removeAppendices(c.limbs[i]);  
            rootNode.detachChild(c.limbs[i].nub);
            rootNode.detachChild(c.limbs[i]);
            
            //need to remove not only the body control, but the joints as well, that's why removeAll instead of remove
            bulletAppState.getPhysicsSpace().removeAll(c.limbs[i].nub);
            bulletAppState.getPhysicsSpace().removeAll(c.limbs[i]);
            
            c.limbs[i].nub=null;
            //added this
            c.limbs[i] = null;
          }  
      }

  }
  
  public void removeAppendices(Limb l){
      
      for (int i=0; i<l.appendices.length; i++){
          if (l.appendices[i]!=null){
            rootNode.detachChild(l.appendices[i]);
            //need to remove not only the body control, but the joints as well, that's why removeAll instead of remove
            bulletAppState.getPhysicsSpace().removeAll(l.appendices[i]);
          }  
      }

  }  
  public void mate(Critter c1, Critter c2, boolean auto){
      
      float numChildren=(c1.children+c2.children)/2+1;
      
      //this is how many children they can have
      if (!auto) Settings.log("(i)"+c1.getName()+" mated with "+c2.getName() +" @ "+c2.getLocalTranslation()+" resulting "+(int)numChildren+" children");
      else Settings.log("(i)"+c1.getName()+" was bred by the simulation with "+c2.getName() +" resulting "+(int)numChildren+" children");
      
      for (int j=0; j<numChildren; j++){
      
      //look for availible slot
      for (int i=0; i<critters.length; i++){
          if(critters[i]==null) {
              
              critters[i]=new Critter(c1,c2,Util.randomVector3f(range1,range2));
              critters[i].index=i;
              
              //enforce parameters if required
              if (enforce_params){
                critters[i].crossover=fixed_crossover;
                critters[i].mutation=fixed_mutation;
              }
              
              //energy effect on parents
              if (!auto){ //if it wasn't done by the balancer
                c1.takeEnergy(c1.cEnergy*c1.energy/100);
                c1.fitness+=c1.cEnergy*c1.energy/100;
                c2.takeEnergy(c2.cEnergy*c2.energy/100);
                c2.fitness+=c2.cEnergy*c2.energy/100;
              }
              
              //load and set materials
              Material mat = new Material(assetManager, "Common/MatDefs/Misc/SolidColor.j3md");
              mat.setColor("m_Color", critters[i].color);
              critters[i].setMaterial(mat);

              //attach to root node
              rootNode.attachChild(critters[i]);
              //enable physics
              bulletAppState.getPhysicsSpace().add(critters[i].bControl);
              
              //add limbs
              addLimbs(critters[i]);
              
              //adjust maxloop
              if (i>maxloop_critter) maxloop_critter=i;
              
              //movement initialization;
              //critters[i].wiggle(100);
              
              break;
          }
        }
        //increment population
        current_pop++;
        births++;

      }
              
        c1.ctarget=null;
        c2.ctarget=null;
  }
  
  //Other stuff that is needed to render the simulation and make it look nice

    /** Declaring the "Shoot" action and mapping to its triggers. */
  private void initKeys() {
    inputManager.addMapping("Select",
      new KeyTrigger(KeyInput.KEY_SPACE), // trigger 1: spacebar
      new MouseButtonTrigger(0));         // trigger 2: left-button click
    inputManager.addListener(actionListener, "Select");
    inputManager.addMapping("Menu",
      new KeyTrigger(KeyInput.KEY_TAB));         // trigger 1 : tab key
    inputManager.addListener(actionListener, "Menu");
  }
  
  private ActionListener actionListener = new ActionListener() {
    public void onAction(String name, boolean keyPressed, float tpf) {
        if (name.equals("Select") && !keyPressed) {
        // 1. Reset results list.
        CollisionResults results = new CollisionResults();
        // 2. Aim the ray from cam loc to cam direction.
        Ray ray = new Ray(cam.getLocation(), cam.getDirection());
        // 3. Collect intersections between Ray and Shootables in results list.
        rootNode.collideWith(ray, results);
        // 4. Print the results.
        if (results.size() > 0){
          // The closest collision point is what was truly hit:
          CollisionResult closest = results.getClosestCollision();
          //closest.getClass();
         if ((closest.getGeometry().getClass().getName()).endsWith("Resource")){
             //System.out.println(((Resource)closest.getGeometry()).getName());
             selected_resource=(Resource)closest.getGeometry();
         }    
         if ((closest.getGeometry().getClass().getName()).endsWith("Critter")){
             //System.out.println(((Critter)closest.getGeometry()).getName());
             selected_critter=(Critter)closest.getGeometry();
         }
        }
        } else if (name.equals("Menu") && !keyPressed){
            options.setVisible(true);
            options.toFront();
            options.setResizable(false);
        }
        }
  };
  
 
  /** Initialize the materials used in this scene. */
  public void initMaterials() {

    //floor materials  
    floor_mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
    TextureKey key3 = new TextureKey("Textures/Terrain/splat/grass.jpg");
    key3.setGenerateMips(true);
    Texture tex3 = assetManager.loadTexture(key3);
    tex3.setWrap(WrapMode.Repeat);
    floor_mat.setTexture("ColorMap", tex3);
    
    //tracker materials
    tracker1_mat=new Material(assetManager, "Common/MatDefs/Misc/SolidColor.j3md");
    tracker1_mat.setColor("m_Color", ColorRGBA.Red);
    
    tracker2_mat=new Material(assetManager, "Common/MatDefs/Misc/SolidColor.j3md");
    tracker2_mat.setColor("m_Color", ColorRGBA.Blue);
    
  }
  
  public void initTrackers(){
      
      //declare and attach geometry
      
      //tracker1=new Geometry("Tracker",new Sphere(32, 32, 0.1f, true, false));
      tracker1=new Geometry("Tracker",new Arrow(new Vector3f(0,-1,0)));
      tracker1.setMaterial(tracker1_mat);
      tracker1.setShadowMode(ShadowMode.Off);
      tracker1.setLocalTranslation(0,-1,0);
      rootNode.attachChild(tracker1);
      
      tracker2=new Geometry("Tracker",new Arrow(new Vector3f(0,-1,0)));
      tracker2.setMaterial(tracker2_mat);
      tracker2.setShadowMode(ShadowMode.Off);
      tracker2.setLocalTranslation(0,-1,0);
      rootNode.attachChild(tracker2);
  
      //tracker does not need to be physical
  }

  /** Make a solid floor and add it to the scene. */
  public void initFloor() {

    floor = new Box(Vector3f.ZERO, xFloor, zFloor, yFloor);
    floor.scaleTextureCoordinates(new Vector2f(3, 6));
      
    floor_geo = new Geometry("Floor", floor);
    floor_geo.setMaterial(floor_mat);
    floor_geo.setShadowMode(ShadowMode.Receive);
    floor_geo.setLocalTranslation(0, -0.1f, 0);
    rootNode.attachChild(floor_geo);
    
    /* Make the floor physical with mass 0.0f! */
    floor_phy = new RigidBodyControl(0.0f);
    floor_geo.addControl(floor_phy);
    bulletAppState.getPhysicsSpace().add(floor_phy);
  }
  
  public void initSky(){
      rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/dome.jpg", true));
  }

  /** Activate shadow casting and light direction */
  private void initShadows() {
    bsr = new BasicShadowRenderer(assetManager, 256);
    bsr.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
    viewPort.addProcessor(bsr);
    // Default mode is Off -- Every node declares own shadow mode!
    rootNode.setShadowMode(ShadowMode.Off);
  }

  /** A plus sign used as crosshairs to help the player with aiming.*/
  protected void initGui() {
    guiNode.detachAllChildren();
    
    //attach a crosshair
    guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
    BitmapText ch = new BitmapText(guiFont, false);
    ch.setSize(guiFont.getCharSet().getRenderedSize() * 4);
    ch.setText("+");        // fake crosshairs :)
    ch.setLocalTranslation( // center
      settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
      settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
    guiNode.attachChild(ch);
    
    //start the options frame
    options=new Settings(this);
    java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                options.setVisible(true);
            }
        });
  }
  
  //Simulation/Application setup
  @Override
  public void simpleInitApp() {
    /** Set up Physics Game */
    bulletAppState = new BulletAppState();
    stateManager.attach(bulletAppState);
    bulletAppState.getPhysicsSpace().enableDebug(assetManager);
    /** Configure cam to look at scene */
    cam.setLocation(new Vector3f(0, 6f, 6f));
    cam.lookAt(Vector3f.ZERO, new Vector3f(0, 1, 0));
    cam.setFrustumFar(45);
    
    //start statistics gathering
    stats=new Statistics(stat_frequency);
    
    /** Initialize the scene, materials, and physics space */
    initMaterials();
    initFloor();
    initSky();
    initGui();
    initKeys();
    initShadows();
    initTrackers();
    addObstacles(); //add the evil cones
    addResources(); //resources must be added before creatures
    addCritters();
    Settings.log("(S)Environment initialized...");
}

   /* This is the main event loop */
    @Override
    public void simpleUpdate(float tpf) {
        
        if (tpf*sim_frequency > 1) counter++;
        else counter+=tpf*sim_frequency;
        
        //if a logical step has occured
        if (counter>1){
        
        //System.out.println("Step");    
            
        //reset counter    
        counter=0;    
        
        //only go through the populated sections
        for (int i=0; i<maxloop_critter+1; i++){
            
            //System.out.println(maxloop_critter);
            if (critters[i] != null){
            
            //take the toll of living
            critters[i].takeEnergy(energy_speed);
            
            //age the creature
            critters[i].age(age_speed);
            
            //auto-cleanup if creature is out of world bounds
            if(critters[i].getLocalTranslation().getY() < -5){ //creature is falling
                Settings.log("(?)"+critters[i].getName()+" has fallen off the edge of the world");
                critters[i].alive=false;
            }
            
            if(!critters[i].alive){
                Settings.log("(X)"+critters[i].getName()+" died");
                removeCritter(critters[i]);
            }
            else{
                
            //create a new set of collision results
            CollisionResults cresults = new CollisionResults();

            //creature heart-beat incremented
            critters[i].tick();
            //
            //if (critters[i].goTo!=null)
            //critters[i].bControl.setLinearVelocity(critters[i].goTo); //needs to be here for some reason??? it stabilizez movement

            if (critters[i].rtarget!=null) //if it is targeting something
                critters[i].rtarget.collideWith(critters[i].getWorldBound(), cresults); //get collision if any
 
            // Use the results
            if (cresults.size() > 0) {
                CollisionResult closest  = cresults.getClosestCollision();
                if (closest.getGeometry().equals(critters[i].rtarget)){
                    
                    //eat it
                    eat(critters[i],critters[i].rtarget);
                
                } //else eat(critters[i],(Resource)closest.getGeometry());

            }
            
            
            cresults = new CollisionResults();
            //System.out.println(critters[i].getName());
            if (critters[i]!=null && critters[i].ctarget!=null) //if it is targeting something
                critters[i].ctarget.collideWith(critters[i].getWorldBound(), cresults); //get collision if any
 
            // Use the results
            if (cresults.size() > 0) {
                CollisionResult closest  = cresults.getClosestCollision();
                if (closest.getGeometry().equals(critters[i].ctarget)){
                    
                    //int tempi=critters[i].target.res.index;
                    
                    //mate
                    if (critters[i].state==1)
                    mate(critters[i],critters[i].ctarget, false);
                    
                    //or eat
                    else if (critters[i].state==0)
                    attack(critters[i],critters[i].ctarget);
                    
                }

            }
            
            }
            }
        }
    
        
     // this makes sure that the world is balanced    
     // all hell breaks loose if i put while instead of ifs ?
     //refill resources
        
     if (current_res<=min_resources){
         if(min_resources>max_resources) min_resources=max_resources; 
         addResource();
         // System.out.println("Resource added"+ current_res);
     }

     //refill critters (mechanism that helps population balance and evolve)
     if (current_pop<min_population){
         
         if(min_population>max_population) min_population=max_population; 
         
         //we select the 5 best critters
         Settings.log("(!)Population unstable, at least "+(min_population-current_pop)+" individuals must be generated");
         
         int[] best=new int[best_num];
         for (int k=0; k<best.length; k++) best[k]=0;
         float[] best_fits=new float[best_num];
         for (int k=0; k<best_fits.length; k++) best_fits[k]=0f;
         
         for (int i=0; i<maxloop_critter+1; i++){
             if (critters[i]!=null)
                 for (int j=0; j<best.length; j++){
                     if(critters[i].fitness>best_fits[j]){
                         best[j]=i;
                         best_fits[j]=critters[i].fitness;
                         break;
                     }
                 }
         }
         
         //debug
         /*
         for (int k=0; k<best.length; k++){
            System.out.print(best[k]+"_");
            System.out.println(best_fits[k]);
         }
          * 
          */
         
         //we mate 2 random ones from there
         Random randomGenerator = new Random();
         int i1 = randomGenerator.nextInt(best_num);
         int i2 = randomGenerator.nextInt(best_num);
         
         if (critters[best[i1]]!=null && critters[best[i2]]!=null)
            mate(critters[best[i1]],critters[best[i2]],true);
         else addCritter();
         
         //addCritter();
     }    
     
     }
       
      //do GUI stuff  
        
      if(selected_critter!=null){
            //tracker follows critter
            tracker1.setLocalTranslation(selected_critter.getWorldTranslation().x,selected_critter.getWorldTranslation().y+2f,selected_critter.getWorldTranslation().z);
            tracker2.setLocalTranslation(selected_critter.goToLocal.x,selected_critter.goToLocal.y+2f,selected_critter.goToLocal.z);
      }
      else {
          tracker1.setLocalTranslation(0,-1,0);
          tracker2.setLocalTranslation(0,-1,0);
      } //hide tracker underground
              
      if (options!=null) if (options.isVisible()) options.updateData();
     
     }

    @Override
    public void stop(){
        //stop statistics gathering
        stats.timer.cancel();
        //get rid of options window
        options.dispose();
        super.stop();
        System.exit(0);
    }
    
    public void togglePause(){
        this.paused=!this.paused;
    }
    
    Simulation(){
        
        obstacles = new Obstacle[max_obstacles];
        resources = new Resource[max_resources];
        critters = new Critter[max_population];
                
        showSettings=false;
        AppSettings apS = new AppSettings(true);
        //apS.setFullscreen(true);
        apS.setTitle("uEarth: Artificial Life Simulation");
        //this makes the simulation continue while other windows are focused
        pauseOnFocus=false;
        //apS.setSettingsDialogImage("earth.png");
        this.setSettings(apS);

    }
    
    public void locateCritter(String id){
        selected_critter=null;
        for(int i=0; i<critters.length; i++){
            if (critters[i]!=null) if (critters[i].getName().compareTo("Critter "+id)==0) selected_critter=critters[i];
        }
        //take the camera nearby
        if (selected_critter!=null){
            cam.setLocation(selected_critter.getLocalTranslation().add(new Vector3f(3f,3f,3f)));
            //point it at creature
            cam.lookAt(selected_critter.getLocalTranslation(), new Vector3f(0,1,0));
        }
    }
    
    public void locateResource(String id){
        selected_resource=null;
        for(int i=0; i<resources.length; i++){
            if (resources[i]!=null) if (resources[i].getName().compareTo("Resource "+id)==0) selected_resource=resources[i];
        }
        //take the camera nearby
        if (selected_resource!=null){
            cam.setLocation(selected_resource.getLocalTranslation().add(new Vector3f(3f,3f,3f)));
            //point it at resource
            cam.lookAt(selected_resource.getLocalTranslation(), new Vector3f(0,1,0));
        }
    }
    
    public void locateStrongest(){
        selected_critter=null;
        float moveeff=0;
        for(int i=0; i<critters.length; i++){
            if (critters[i]!=null) if (critters[i].move.fitness>moveeff){
                selected_critter=critters[i];
                moveeff=critters[i].move.fitness;
            }
        }
        //take the camera nearby
        if (selected_critter!=null){
            cam.setLocation(selected_critter.getLocalTranslation().add(new Vector3f(3f,3f,3f)));
            //point it at creature
            cam.lookAt(selected_critter.getLocalTranslation(), new Vector3f(0,1,0));
        }
    }
    
    public void locateSmartest(){
        selected_critter=null;
        float neueff=0;
        for(int i=0; i<critters.length; i++){
            if (critters[i]!=null){
                float temp=(Util.testNNetwork(critters[i].brain, 0, test_orders0, 5)+
                        Util.testNNetwork(critters[i].brain, 1, test_orders1, 5)+
                        Util.testNNetwork(critters[i].brain, 2, test_orders2, 5))/3;
                if (temp>neueff){
                    selected_critter=critters[i];
                    neueff=temp;
                }  
            }
        }
        //take the camera nearby
        if (selected_critter!=null){
            cam.setLocation(selected_critter.getLocalTranslation().add(new Vector3f(3f,3f,3f)));
            //point it at creature
            cam.lookAt(selected_critter.getLocalTranslation(), new Vector3f(0,1,0));
        }
    }
    
    public void locateBest(){
        selected_critter=null;
        float eff=0;
        for(int i=0; i<critters.length; i++){
            if (critters[i]!=null) if (critters[i].fitness>=eff){
                selected_critter=critters[i];
                eff=critters[i].fitness;
            }
        }
        //take the camera nearby
        if (selected_critter!=null){
            cam.setLocation(selected_critter.getLocalTranslation().add(new Vector3f(3f,3f,3f)));
            //point it at creature
            cam.lookAt(selected_critter.getLocalTranslation(), new Vector3f(0,1,0));
        }
        
    }
    
}

