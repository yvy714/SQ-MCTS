package mcts;

import agent.BeliefBaseImp;
import agent.Choice;
import goalplantree.GoalPlanTree;

import java.util.ArrayList;

public class SPMCTSNode extends BasicMCTSNode{

    static final double constC = 0.1;
    static final double constD = 32;



    /**
     * constructor for the root node
     * @param trees the set of goal-plan trees in the current state
     * @param bb
     */
    public SPMCTSNode(GoalPlanTree[] trees, BeliefBaseImp bb){
        super(trees,bb);
    }

    /**
     * constructor for the root node
     * @param trees the set of goal-plan trees in the current state
     * @param bb
     */
    public SPMCTSNode(ArrayList<GoalPlanTree> trees, BeliefBaseImp bb){
        super(trees,bb);
    }

    /**
     * constructor for other nodes
     * @param c the choices which leads its parent node to this node
     */
    public SPMCTSNode(ArrayList<Choice> c){
        super(c);
    }


    @Override
    protected BasicMCTSNode select(){
        // initialisation
        BasicMCTSNode selected = null;

        double bestUCT = Double.MIN_VALUE;
        // calculate the uct value for each of its selected nodes
        for(int i = 0; i < children.size(); i++){

            // UCT calculation for single player MCTS
            double uctValue =
                    children.get(i).statistic.totValue/ (children.get(i).statistic.nVisits + epsilon)
                    + constC * Math.sqrt(Math.log(statistic.nVisits + 1)/(children.get(i).statistic.nVisits + epsilon))+ rm.nextDouble() * epsilon
                    + Math.sqrt(
                            (children.get(i).statistic.totSquare - children.get(i).statistic.nVisits *
                            (children.get(i).statistic.totValue/(children.get(i).statistic.nVisits + epsilon))*
                            (children.get(i).statistic.totValue/(children.get(i).statistic.nVisits + epsilon))
                                    + constD)
                                    / (children.get(i).statistic.nVisits + epsilon));
            // compare the uct value with the current maximum value
            if(uctValue > bestUCT){
                selected = children.get(i);
                bestUCT = uctValue;
            }
        }
        // return the nodes with maximum UCT value, null if current node is a leaf node (contains no child nodes)
        return selected;
    }

}
