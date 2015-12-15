//uEarth: Artificial Life Simulation Software
//(C) Alin-Dragos Petculescu 2010
//Univeristy of Leicester
//www2.le.ac.uk
//Please credit the original author if reusing this code

package earth;

/**
 *
 * @author DreadRazor
 */
public class NNTest {

    public static void main(String[] args){

        int[] layers = new int[] {3,4,4,2};
        NNetwork n=new NNetwork(layers, 1);
        //n.describe();

        /*
        float[] w=n.getWeights();
        for (int i=0; i<w.length; i++){
            System.out.print(w[i]+",");
        }*/

        NNetwork n2=new NNetwork(layers, 1);
        //n2.setWeights(w);

        //n2.describe();

        float[] testinput = new float[] {0.2f,0.6f,0.5f};
        float[] results = n.zap(testinput);
       /*
       for (int i=0; i<results.length; i++){
            System.out.print(results[i]+",");
        }
       System.out.println();
        * 
        */
       float[] testinput2 = new float[] {0.2f,0.9f,0.7f};
       float[] results2 = n.zap(testinput2);
       
       
       //n.describe();
       /*
         for (int i=0; i<results2.length; i++){
            System.out.print(results2[i]+",");
        }
       System.out.println();
        */

       float res= Util.testNNetwork(n, 0, new boolean[]{true,true,true}, 10);
       
       System.out.println(res);
       
       System.out.println(0%5);
       
    }

}
