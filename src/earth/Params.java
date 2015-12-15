//uEarth: Artificial Life Simulation Software
//(C) Alin-Dragos Petculescu 2010
//Univeristy of Leicester
//www2.le.ac.uk
//Please credit the original author if reusing this code

//Code based on: http://www.ai-junkie.com/ann/evolved/nnt1.html
//               http://www.ai-junkie.com/board/viewtopic.php?t=241&highlight=java+sweepers
//Submitted by Glenn Ford (Malohkan)

package earth;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Scanner;

public class Params {
	

  //some limits set on the simulation
  public int init_population=1; //starting population
  public int max_population=1;
  public int min_population=1;
  public int init_resources=1;
  public int max_resources=1000;
  public int min_resources=1;
  
  //best specimens number (mating will happen 'artificially' between these)
  public int best_num=5;
  
  //age speed
  public float age_speed=0.0001f;
  
  //energy depletion speed
  public float energy_speed=0.0001f;
  
  //we add the ability ability to customize world size
  public float xFloor=100f;
  public float yFloor=100f;
  public float zFloor=0.1f;
  
  public float range1=50f;
  public float range2=50f;
  
  public int init_obstacles = 0;
  public int max_obstacles = 10000;
  
//minimum mutation and crossover rate
  public float fixed_mutation=0.001f;
  public float fixed_crossover=0.5f;
  public boolean enforce_params=false;
  
  public float sim_frequency = 1f;
  public int stat_frequency = 10;

	
	public Params() throws FileNotFoundException {
		
		Scanner scanner = new Scanner(new File("params.ini"));

		
		Map map = new Hashtable();
		while (scanner.hasNext()) {
			map.put(scanner.next(), scanner.next());
		}
		
		init_population = Integer.parseInt((String)map.get("init_population"));
		max_population = Integer.parseInt((String)map.get("max_population"));
		min_population = Integer.parseInt((String)map.get("min_population"));
		init_resources = Integer.parseInt((String)map.get("init_resources"));
		max_resources = Integer.parseInt((String)map.get("max_resources"));
		min_resources = Integer.parseInt((String)map.get("min_resources"));
		
                best_num = Integer.parseInt((String)map.get("best_num"));
                
		age_speed = Float.parseFloat((String)map.get("age_speed"));
		energy_speed = Float.parseFloat((String)map.get("energy_speed"));
                
                sim_frequency = Float.parseFloat((String)map.get("sim_frequency"));
		stat_frequency =  Integer.parseInt((String)map.get("stat_frequency"));
                
		xFloor = Float.parseFloat((String)map.get("xFloor"));
		yFloor = Float.parseFloat((String)map.get("yFloor"));
		zFloor = Float.parseFloat((String)map.get("zFloor"));
                
                range1 = Float.parseFloat((String)map.get("range1"));
                range2 = Float.parseFloat((String)map.get("range2"));
                
		init_obstacles = Integer.parseInt((String)map.get("init_obstacles"));
                
                fixed_mutation=Float.parseFloat((String)map.get("fixed_mutation"));
                fixed_crossover=Float.parseFloat((String)map.get("fixed_crossover"));
                enforce_params=Boolean.parseBoolean((String)map.get("enforce_params"));
                
                max_obstacles = Integer.parseInt((String)map.get("max_obstacles"));
		//init_obstacles = Integer.parseInt((String)map.get("init_obstacles"));
                
                //System.out.println(map.toString());
	}
}
