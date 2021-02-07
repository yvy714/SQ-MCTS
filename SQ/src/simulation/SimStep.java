package simulation;

import goalplantree.Literal;

/**
 * a step in the simulation path including its precondition, post-condition and the number of cycle it is executed
 */
public class SimStep {

    /**
     * the precondition of this step
     */
    private Literal[] preCon;
    /**
     * the postcondition of this step
     */
    private Literal[] postCon;

    private int cycNum;

    public SimStep(Literal[] pre, Literal[] post, int c){
        this.preCon = pre;
        this.postCon = post;
        this.cycNum = c;
    }

    /**
     * @return the precondition of this step
     */
    public Literal[] getPreCon(){
        return this.preCon;
    }

    /**
     * @return the postcondition of this step
     */
    public Literal[] getPostCon(){
        return this.postCon;
    }

    /**
     * @return the cycle number
     */
    public int getCycNum(){
        return this.cycNum;
    }
}
