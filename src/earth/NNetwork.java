//uEarth: Artificial Life Simulation Software
//(C) Alin-Dragos Petculescu 2010
//Univeristy of Leicester
//www2.le.ac.uk
//Please credit the original author if reusing this code

//Code based on: http://www.ai-junkie.com/ann/evolved/nnt1.html
//               http://www.ai-junkie.com/board/viewtopic.php?t=241&highlight=java+sweepers
//Submitted by Glenn Ford (Malohkan)

package earth;

/**
 *
 * @author DreadRazor
 */
public class NNetwork {

    private Layer[] layers;
    public int[] layerNeurons;
    private float activationResponse=1f;

    public NNetwork(int[]layrs, float actResp){
        this(layrs);
        activationResponse=actResp;
    }

    public NNetwork(int[] layrs) /*throws Exception*/{

        layerNeurons=layrs; //the initial setup of the network is always good to have
         
        if(layrs.length > 2){ //there are hidden layers
        
        layers = new Layer[layrs.length]; //create how many hidden layers are given

        //create input layer
        layers[0]=new Layer(layrs[0],1);

        //create hidden layers ????
        for(int i=1; i<layrs.length-1; i++){
            layers[i]=new Layer(layrs[i],layrs[i-1]); //only use positive numbers for layers
        }

        //create output layer
        layers[layrs.length-1]=new Layer(layrs[layrs.length-1],layrs[layrs.length-2]);
        }
        else{ //no hidden layer used?

        //create input layer
        layers[0]=new Layer(layrs[0],1);

        //create output layer
        layers[layrs.length-1]=new Layer(layrs[layrs.length-1],layrs[layrs.length-2]);
        }
    }

    public float[] getWeights(){
        
        int numWeights=0;

        //calculate how many weights we have
        for(int i=0; i<layerNeurons.length; i++){
            if (i==0) numWeights+=layerNeurons[0]*2;
            else numWeights+=layerNeurons[i]*(layerNeurons[i-1]+1);
        }

        float[] output =new float[numWeights];

        int counter=0; //used to hold the index for the weights

        //get sensor layer weights
        for(int i=0; i<layers.length; i++){ //layer level

            for (int j=0; j<layers[i].neurons.length; j++){ //neuron level

                for (int k=0; k<layers[i].neurons[j].weights.length; k++){ //weight level

                    output[counter]=layers[i].neurons[j].weights[k];
                    counter++;

                }

            }

        }

        return output;
    }

    public void setWeights(float[] weights){

        int counter=0; //used to hold the index for the weights

        //get sensor layer weights
        for(int i=0; i<layers.length; i++){ //layer level

            for (int j=0; j<layers[i].neurons.length; j++){ //neuron level

                for (int k=0; k<layers[i].neurons[j].weights.length; k++){ //weight level

                    layers[i].neurons[j].weights[k]=weights[counter];
                    counter++;

                }

            }

        }

    }

    public void describe(){

        //get sensor layer weights
        for(int i=0; i<layers.length; i++){ //layer level

            for (int j=0; j<layers[i].neurons.length; j++){ //neuron level

                System.out.print("(");

                for (int k=0; k<layers[i].neurons[j].weights.length; k++){ //weight level

                    System.out.print(layers[i].neurons[j].weights[k]);
                    System.out.print("|");

                }

                System.out.print(")");

            }

            System.out.println();

        }

    }

    public boolean compare(NNetwork n){
        if (n.layers.length != layers.length) return false;
        else{
            for (int i=0; i<layers.length; i++){
                if (n.layers[i]!=layers[i]) return false;
            }
        return true;
        }
    }

    public float[] zap(float[] inputs){

        if (layerNeurons[0] != inputs.length) {
			//just return an empty vector if incorrect.
			throw new IllegalStateException("Input array is of incorrect length");
        }

        int cWeight=0;

        float[] outputs = null;

        //for each layer
        for (int i=0; i< layers.length; i++){

            if (i>0){  //for each layer apart from the first (because there are no outputs to act as inputs, just the sensor inputs)
                inputs=outputs.clone();
            }

            outputs = new float[layerNeurons[i]];
            cWeight=0;
            
            //System.out.println("Layer " + i);
            
            //for each neuron
            for(int j=0; j<layers[i].neurons.length; j++){
                float netInput = 0;
                //int numInputs = layers[i].neurons[j].weights.length;
                //System.out.println("Neuron "+j);
                //for each weight
                for (int k=0; k<layers[i].neurons[j].weights.length-1; k++){
                    if (i==0) cWeight=j; //for the first layer only one input is provided to each sensor, not all of them
                    //if this is not reset, the seonsor only detect the first input of the provided data
                    netInput+=layers[i].neurons[j].weights[k] * inputs[cWeight];
                    //System.out.println(cWeight);
                    //System.out.print(layers[i].neurons[j].weights[k] + "x" +inputs[cWeight] + "+");
                    cWeight++;
                }
                //System.out.println();
                netInput-=layers[i].neurons[j].weights[layers[i].neurons[j].weights.length-1]; //the last weight is always the bias and therefore substracted

                outputs[j]=sigmoid(netInput);
                //System.out.println("Output:"+outputs[j]);

                cWeight=0;
            }
                       
        }

        return outputs;
    }

    private float sigmoid(float netinput) {
		return (float)( 1.0 / ( 1.0 + Math.exp(-netinput / activationResponse)));
    }
    
    private class Layer{

        Neuron[] neurons;

        public Layer(int nurons, int inputs){
            neurons = new Neuron[nurons];
            for(int i=0; i<nurons; i++) neurons[i]=new Neuron(inputs);
        }

        public void describe(){
            for(int i=0; i<neurons.length; i++){

                System.out.print("(");

                for (int j=0; j<neurons[i].weights.length; j++){
                    System.out.print(neurons[i].weights[j]);
                    System.out.print("|");
                }

                System.out.print(")");

            }

        }

    }

    private class Neuron {
		//the weights for each input
		float[] weights;

		public Neuron(int inputs) {
			weights = new float[inputs+1];
			//we need an additional weight for the bias hence the +1
			for (int i=0; i<inputs+1; ++i) {
				//set up the weights with an initial random value between -1 and 1
				weights[i] = (float)Util.randomClamped(); //why do i have to cast this?
			}
		}

                public void describe(){
                    for(int i=0; i<weights.length; i++){
                        System.out.print(weights[i]);
                        System.out.print("|");
                    }
                }
    }
    
    @Override
    public String toString(){

        String output="";
        //get sensor layer weights
        for(int i=0; i<layers.length; i++){ //layer level

            for (int j=0; j<layers[i].neurons.length; j++){ //neuron level

                output+="(";

                for (int k=0; k<layers[i].neurons[j].weights.length; k++){ //weight level

                    output+=layers[i].neurons[j].weights[k];
                    output+="|";

                }

                output+=")";

            }

            output+="\n";

        }
        
        return output;

    }
    
 }
