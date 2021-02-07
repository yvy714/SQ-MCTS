package environment;

import goalplantree.Literal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SynthEnvironment extends AbstractEnvironment {

    Random rm = new Random();

    public SynthEnvironment(){
        super();
    }

    public SynthEnvironment(ArrayList<Literal> literals, double mean){
        super(literals, mean);
    }

    public SynthEnvironment(ArrayList<Literal> literals){
        super(literals);
    }

    public SynthEnvironment(ArrayList<Literal> literals, ArrayList<Double> means){
        super(literals, means);
    }

    /**
     * given the vision of an agent, return the corresponding percepts
     * @param visions
     * @return the list of new percepts of an agent
     */
    @Override
    public ArrayList<Literal> getStates(HashMap<String, Double> visions) {
        // if no updates, then there is no new percepts
        if(nStates == null)
            return new ArrayList<>();

        ArrayList<Literal> lits = new ArrayList<>();
        for(Literal l : nStates){
            // if the random value is less than or equal to the vision, then the updates will be seen by the agent
            // otherwise, the change is unseen
            if(visions.get(l.getName()) >= rm.nextDouble()){
                lits.add(l);
            }
        }
        return lits;
    }
}
