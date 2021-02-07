package agent;

import goalplantree.*;
import mcts.BasicMCTSNode;
import java.util.ArrayList;
import java.util.HashMap;


public class MCTSAgent extends AbstractAgent{

    // to record the best simulation choices
    ArrayList<Choice> bestChoices = new ArrayList<>();
    // to record the best simulation result
    double bestResult = -1;
    // alpha and beta are set to 100 and 50 respectively by default
    int alpha = 100;
    int beta = 1;

    /**
     * constructor
     */
    public MCTSAgent(String id, ArrayList<Belief> bs){
        super(id, bs);
    }
    public MCTSAgent(String id, ArrayList<Belief> bs, ArrayList<GoalNode> gs){
        super(id, bs, gs);
    }
    public MCTSAgent(String id, ArrayList<Belief> bs, ArrayList<GoalNode> gs, HashMap<String, Double> vs){
        super(id, bs, gs, vs);
    }

    /**
     * set the iteration
     * @param a
     */
    public void setAlpha(int a){
        alpha = a;
    }

    /**
     * set the number of simulation per iteration
     * @param b
     */
    public void setBeta(int b){
        beta = b;
    }

    @Override
    public boolean deliberate() {
        BasicMCTSNode root = new BasicMCTSNode(this.intentions, this.bb);
        long start = System.currentTimeMillis();
        // run mcts
        root.run(alpha, beta);

        // get the best choice
        ArrayList<Choice> cs = root.bestChoice();

        if(cs.size() > 0){
            this.choices = cs;
            return true;
        }
        return false;
    }





    /**
     * if execution succeeds
     */
    @Override
    public void exeSucceed() {
        // get the last choice
        Choice choice = choices.remove(0);
        // get the intended gpt
        GoalPlanTree gpt = intentions.get(choice.intentionChoice);

        // get the selected action
        ActionNode act = (ActionNode) gpt.getCurrentStep();
        // update the belief base according to the action's postcondition
        Literal[] post = act.getPostc();
        for(Literal l : post){
            this.bb.update(l);
        }

        // update the intention
        gpt.success();
        // if the top-level goal is achieved
        if(gpt.achieved()){
            // add its name to the achieved goal
            achievedGoals.add(gpt.getTlg().getName());
            // remove it from the agent's intention
            //intentions.remove(choice.intentionChoice);
        }

    }

    @Override
    public void exeFail() {
        // get the last choice
        Choice choice = choices.remove(0);
        // get the intended gpt
        GoalPlanTree gpt = intentions.get(choice.intentionChoice);
        System.out.println("cc: " + gpt.getCurrentStep().getName());

        // update the intention
        gpt.fail();
        // if the top-level goal fails
        if(gpt.getTlg().getStatus().equals(TreeNode.Status.FAILURE)){
            // remove it from the agent's intention
            //intentions.remove(choice.intentionChoice);
        }
    }
}
