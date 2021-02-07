package agent;

import goalplantree.Literal;

public class Belief {
    /** name of the literal */
    final String lit_name;
    /**
     * the probability of the literal being true
     */
    double prob;

    /**
     * constructor
     * @param name name of the literal
     * @param degree probability of being true
     */
    public Belief(String name, double degree){
        lit_name = name;
        prob = degree;
    }

    public Belief(Literal literal){
        lit_name = literal.getName();
        if(literal.getState())
            prob = 1;
        else
            prob = 0;
    }

    public Belief(Literal literal, double degree){
        lit_name = literal.getName();
        if(literal.getState())
            prob = degree;
        else
            prob = 1 - degree;
    }

    /**
     * @return the name of the literal which this belief is associated
     */
    public String getLit_name(){
        return lit_name;
    }

    /**
     * @return the subjective probability of this literal being true
     */
    public double getProb(){
        return prob;
    }

    /**
     * set the probability to a certain degree
     * @param degree the given probability
     */
    public void setProb(double degree){
        prob = degree;
    }

}
