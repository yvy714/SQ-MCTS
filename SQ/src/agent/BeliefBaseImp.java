package agent;
import goalplantree.Literal;

import java.util.*;


/**
 * @author yuanyao
 *
 * Agent's belief about the environment
 */

public class BeliefBaseImp implements Cloneable, Iterable<Map.Entry<String, Belief>> {

    /**
     * a belief base is a map of proposition and probability pairs.
     * The probability represent the agent's subjective probability of the corresponding proposition being true
     * in the current environment
     */
    public Map<String, Belief> beliefs;

    /**
     * initial capacity of this belief base
     */
    private int initialCapacity = 32;


    public BeliefBaseImp(){
        beliefs = new HashMap<>(initialCapacity);
    }

    /**
     * initialisation
     * @param capacity
     */
    public BeliefBaseImp(int capacity){
        initialCapacity = capacity;
        beliefs = new HashMap<>(initialCapacity);
    }

    public BeliefBaseImp(ArrayList<Belief> percept){
        beliefs = new HashMap<>(percept.size());
        for(Belief p : percept){
            update(p.lit_name, p.prob);
        }
    }

    /**
     * @return the size of this belief base
     */
    public int getSize(){
        return beliefs.size();
    }


    /**
     * clear the belief set
     */
    public void clear(){
        beliefs.clear();
    }

    /**
     * update the belief base according to a given literal
     * @param literal a given literal
     * @return
     */
    public void update(Literal literal){
        if (literal==null)
            return;

        if(literal.getState()){
            // if it is a positive literal, then set the probability to 1
            beliefs.put(literal.getName(), new Belief(literal.getName(), 1));
        }else {
            // if it is a negative literal, then set it to 0
            beliefs.put(literal.getName(), new Belief(literal.getName(), 0));
        }
    }

    /**
     * update the belief base according to a literal name, and its probability of being true
     * @param name the name of the literal
     * @param prob the probability of this literal being true
     */
    public void update(String name, Double prob){
        if(prob >=0 && prob <= 1){
           beliefs.put(name, new Belief(name, prob));
        }else {
            System.err.println("The probability of a literal being true must within the range of 0 to 1");
        }
    }

    /**
     * update the belief base according to a list of literals
     * @param literals a list of literals
     */
    public void update(List<Literal> literals){
        if(literals == null)
            return;
        for(int i = 0; i < literals.size(); i++){
            update(literals.get(i));
        }
    }

    public void update(Literal[] literals){
        if(literals == null)
            return;
        for(int i = 0; i < literals.length; i++){
            update(literals[i]);
        }
    }

    /**
     * Given lists of literal names and their probabilities, update the belief base
     * @param names a list of literal names
     * @param probs a list of corresponding probability
     */
    public void  update(List<String> names, List<Double> probs){
        if(names == null || probs == null || names.size() != probs.size())
            return;
        for(int i = 0; i < names.size(); i++){
            update(names.get(i), probs.get(i));
        }
    }


    /**
     * @param literal the given literal;
     * @return the probability of the given literal holds in the current environment
     */
    public double evaluate(Literal literal){


        // for a positive literal, return the probability
        if(literal.getState())
            return beliefs.get(literal.getName()).prob;
        // for a negative literal, return the probability of its negation being false
        else
            return 1 - beliefs.get(literal.getName()).prob;
    }

    /**
     * @param literals a list of literals
     * @return the probability of these literals holds
     */
    public double evaluate(Literal[] literals){
        double prob = 1;
        for(int i = 0; i < literals.length; i++){
            prob *= evaluate(literals[i]);
        }
        return prob;
    }

    public double evaluate(List<Literal> literals){
        double prob = 1;
        for(int i = 0; i < literals.size(); i++){
            prob *= evaluate(literals.get(i));
        }
        return prob;
    }


    /**
     * clone
     * @return
     */
    @Override
    public BeliefBaseImp clone(){
        BeliefBaseImp beliefBaseImp = new BeliefBaseImp(getSize());
        for(Map.Entry<String, Belief> b : this){
            beliefBaseImp.beliefs.put(b.getKey(), b.getValue());
        }
        return beliefBaseImp;
    }

    /**
     * iterator
     * @return
     */
    @Override
    public Iterator<Map.Entry<String, Belief>> iterator(){
        return beliefs.entrySet().iterator();
    }

    /**
     * @return the string representation of belief base
     */
    public String onPrintBB(){
        String result = "Belief Base = {";
        for(Map.Entry<String, Belief> b : this){
            result += "(" + b.getKey() + "," + b.getValue().prob + ");";
        }
        result += " }.";
        return result;
    }

}


