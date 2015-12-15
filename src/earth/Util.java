//uEarth: Artificial Life Simulation Software
//(C) Alin-Dragos Petculescu 2010
//Univeristy of Leicester
//www2.le.ac.uk
//Please credit the original author if reusing this code

//Code based on: http://www.ai-junkie.com/ann/evolved/nnt1.html
//               http://www.ai-junkie.com/board/viewtopic.php?t=241&highlight=java+sweepers
//Submitted by Glenn Ford (Malohkan)

package earth;

import com.jme3.math.Vector3f;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


public class Util {

	/**
	 * @return Returns a double between -1 and 1 exclusive
	 */
	public static double randomClamped() {
		return Math.random() - Math.random();
	}

        public static Vector3f randomVector3f(float range1, float range2){
            return new Vector3f((float)randomClamped()*range1,10f,(float)randomClamped()*range2);
        }
        
        public static Vector3f randomLowVector3f(float range1, float range2){
            return new Vector3f((float)randomClamped()*range1,3f,(float)randomClamped()*range2);
        }

        public static boolean onOff(){
            if(Math.random() > 0.5) return true;
            else return false;
        }
        
        public static boolean mutate(float chance){
            if (Math.random() > chance) return true; //mutation occured too often
            else return false;
        }
        /*
        public static float[] randomGenome(int length){
            float[] output = new float[16];
            for (int i=0; i<length; i++){
                output[i]=(float)Math.random();
            }
            return output;
        }
        */
        
        public static float bigFloat(int restrictor){
            
            Random randomGenerator = new Random();
            
            return (float)(randomGenerator.nextInt(restrictor)+randomClamped());
            
        }
        
        public static float testNNetwork(NNetwork brain, int output, boolean[] orders, int samples){
            //define 5 test inputs and see what the given network chooses
            //in each input, only one parameter changes
            //calacuate correctitude based on what the normal response should be
            
            //float[] temp={distance,benefit,tSpeed,threat};
            
            if (orders.length!=brain.layerNeurons[0]) return 0; //insufficient order data provided
            
            int score = 0;
            
            //test data to be generated
            float[][] testdata;
            //the correct order
            
            for (int c=0; c<brain.layerNeurons[0]; c++){
                
                //test data to be generated
                testdata = new float[samples][brain.layerNeurons[0]];
            
                //initialize test matrix
                for (int i=0; i<samples; i++){
                    
                    for (int j=0; j<brain.layerNeurons[0]; j++){
                    
                        testdata[i][j] = 1f;
                        if (j==c) testdata[i][j] = (float)i;
                        
                        //System.out.print(testdata[i][j]+ " ");
                    }
                    
                 //System.out.println();
               
                }
          
                float[] reactions = new float[samples];
                
                for (int i=0; i<samples; i++){
                    //omg this will fail!!!
                    reactions[i]=brain.zap(testdata[i])[output]; 
                   //System.out.print(reactions[i]+",");
                }
                
                //System.out.println();
                
                if (orders[c]){
                    //test for ascending
                    //count number of inversions simple and inefficient
                    int counter = 0;

                    for(int p=0; p < reactions.length-1; p++){
                        for(int r=p+1 ; r<reactions.length; r++){
                            if( reactions[p] >= reactions[r] ) counter++;
                        }
                    }
                    
                    score+=counter;
                    //System.out.println(counter);

                }else{
                    //test for descending
                    //count number of inversions simple and inefficient
                    int counter = 0;

                    for(int p=0; p < reactions.length-1; p++){
                        for(int r=p+1 ; r<reactions.length; r++){
                            if( reactions[p] <= reactions[r] ) counter++;
                        }
                    }
                    
                    score+=counter;
                    //System.out.println(counter);
                }
                
            }
            
            //calculate how much score is out of maximum score in percentage
            //System.out.println((samples * (samples-1) * brain.layerNeurons[0] /2));
            //System.out.println(score);
            return (float) (1- ((float)score / (float)(samples * (samples-1) * brain.layerNeurons[0] /2)))*100;
           
        }
        
            
    public static String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
    
    public static float newRandom(float min){
        return (float)(min+Math.random());
    }
    
    public static float newRandom10(float min){
        return (float)(min+Math.random()*10f);
    }
    
    public static float newRandom100(float min){
        return (float)(min+Math.random()*100f);
    }


}
