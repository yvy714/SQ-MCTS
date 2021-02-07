package agent;

import goalplantree.*;
import mcts.BasicMCTSNode;
import mcts.SPMCTSNode;

import java.util.ArrayList;
import java.util.HashMap;

public class SPMCTSAgent extends MCTSAgent {

    /**
     * constructor
     */
    public SPMCTSAgent(String id, ArrayList<Belief> bs){
        super(id, bs);
    }
    public SPMCTSAgent(String id, ArrayList<Belief> bs, ArrayList<GoalNode> gs){
        super(id, bs, gs);
    }
    public SPMCTSAgent(String id, ArrayList<Belief> bs, ArrayList<GoalNode> gs, HashMap<String, Double> vs){
        super(id, bs, gs, vs);
    }

    @Override
    public boolean deliberate() {

        /**
         * run MCTS alpha iteration, each with beta iterations to generate an MCTS search tree
         */
        SPMCTSNode root = new SPMCTSNode(this.intentions, this.bb);
        long start = System.currentTimeMillis();
        // run mcts
        root.run(alpha, beta);
        System.out.println("time: " + (System.currentTimeMillis() - start) + "ms");


        /**
         * compare the new result with the existing choices
         */
        // get the best simulation result
        double a = root.getAllBestResult();

        // get the simulation value for the existing choices
        // to make sure it still work even if the environment changes
        double b = simulation(this.bestChoices);

        // if the new result is better than the old one
        if(a >= b){
            // assign the new best choices
            this.bestChoices = root.getAllChoices();
            b = a;
        }

        //System.out.println("visits:" + root.getStatistic().nVisits);

        System.out.println("best: " + b);

        // assign the choices for the current cycle
        if(this.bestChoices.size() > 0){
            this.choices = new ArrayList<>();
            while (this.bestChoices.size() > 0){
                Choice cs = this.bestChoices.remove(0);
                this.choices.add(cs);
                if(cs.isActionExecution())
                    break;
            }
            return true;
        }
        return false;

    }


    /**
     * get the simulation result given a list of choices
     * @param css
     * @return
     */
    double simulation(ArrayList<Choice> css){
        // copy the gpts
        ArrayList<GoalPlanTree> cgpts = new ArrayList<>();
        for(GoalPlanTree gpt: this.intentions){
            cgpts.add(gpt.clone());
        }
        // copy the belief base
        BeliefBaseImp cbb = this.bb.clone();

        // start simulation given the choices
        for(Choice c : css){
            // if it is a plan selection
            if(c.isPlanSelection()){
                // get the corresponding gpt
                GoalPlanTree gpt = cgpts.get(c.intentionChoice);
                // get the current step
                TreeNode current = gpt.getCurrentStep();
                // if it is a goal node
                if(current instanceof GoalNode){
                    GoalNode goal = (GoalNode) current;
                    // get plans
                    PlanNode[] pls = goal.getPlans();
                    // get the corresponding plan
                    PlanNode pl;
                    if(c.planChoice < pls.length){
                        pl = pls[c.planChoice];
                        // get its context condition
                        Literal[] context = pl.getPrec();
                        // check if its context condition holds
                        if(cbb.evaluate(context) == 1){
                            // add this subgoal to the backtrack list
                            gpt.getBacktrackList().add(goal);
                            // set the first step in this plan as the current step
                            gpt.setCurrentStep(pl.getPlanbody()[0]);
                        }
                        // if it does not hold
                        else {
                            System.err.println("Simulation Error: context condition does not hold");
                            break;
                        }

                    }
                    // out of index, we stop the incorrect list of choices
                    else {
                        System.err.println("Simulation Error: the specified plan cannot be found!");
                        break;
                    }
                }
                // if the current step is not a (sub)goal
                else {
                    System.err.println("Simulation Error: a plan choice cannot be applied to an action!");
                    break;
                }
            }
            // if the choice is to execute an action
            else if(c.isActionExecution()){
                // get the goal-plan tree
                GoalPlanTree gpt = cgpts.get(c.intentionChoice);
                // get the current step
                TreeNode current = gpt.getCurrentStep();
                // check if it is an action
                if(current instanceof ActionNode){
                    ActionNode act = (ActionNode) current;
                    // get its precondition
                    Literal[] prec = act.getPrec();
                    // check if its precondition holds
                    if(cbb.evaluate(prec) == 1){
                        // apply its postcondition
                        Literal[] post = act.getPostc();
                        cbb.update(post);
                        // get the next step
                        current = act.getNext();
                        // if it is the last step in this plan
                        while (current == null){
                            // if the top-level goal has been achieved
                            if(gpt.getBacktrackList().size() == 0){
                                break;
                            }
                            // get the last subgoal in the backtrack list
                            current = gpt.getBacktrackList().remove(gpt.getBacktrackList().size()-1);
                            // get its next step
                            current = current.getNext();
                        }
                        // set current step
                        gpt.setCurrentStep(current);
                    }
                    // if this action cannot be executed
                    else {
                        System.err.println("Simulation Error: the selected action cannot be executed");
                    }
                }
                // if it is not an action
                else {
                    System.err.println("Simulation Error: an action choice cannot be applied to a subgoal");
                    break;
                }
            }
        }

        // calculate the results
        double result = 0;
        for(GoalPlanTree gpt: cgpts){
            if(gpt == null || gpt.getCurrentStep() == null){
                result++;
            }
        }
        return result;
    }
}
