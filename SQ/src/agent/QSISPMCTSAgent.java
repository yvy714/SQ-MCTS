package agent;

import goalplantree.*;
import mcts.QSIMCTSNode;

import java.util.ArrayList;
import java.util.HashMap;

public class QSISPMCTSAgent extends MCTSAgent {

    /**
     * constructor
     */
    public QSISPMCTSAgent(String id, ArrayList<Belief> bs){
        super(id, bs);
    }
    public QSISPMCTSAgent(String id, ArrayList<Belief> bs, ArrayList<GoalNode> gs){
        super(id, bs, gs);
    }
    public QSISPMCTSAgent(String id, ArrayList<Belief> bs, ArrayList<GoalNode> gs, HashMap<String, Double> vs){
        super(id, bs, gs, vs);
    }

    @Override
    public boolean deliberate() {


        QSIMCTSNode root = new QSIMCTSNode(this.intentions, this.bb);
        long start = System.currentTimeMillis();
        root.run(alpha, beta, 0.5, 0.1);

        /**
         * compare the new result with the existing choices
         */
        double a = root.getAllBestResult();
        double b = simulation(this.bestChoices);
        if(a >= b){
            this.bestChoices = root.getAllChoices();
            b = a;
        }
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
            if(c.isPlanSelection()) {
                GoalPlanTree gpt = cgpts.get(c.intentionChoice);
                TreeNode current = gpt.getCurrentStep();
                if (current instanceof GoalNode) {
                    GoalNode goal = (GoalNode) current;
                    PlanNode[] pls = goal.getPlans();
                    PlanNode pl;
                    if (c.planChoice < pls.length) {
                        pl = pls[c.planChoice];
                        Literal[] context = pl.getPrec();
                        if (cbb.evaluate(context) == 1) {
                            gpt.getBacktrackList().add(goal);
                            gpt.setCurrentStep(pl.getPlanbody()[0]);
                        } else {
                            System.err.println("Simulation Error: context condition does not hold");
                            break;
                        }

                    } else {
                        System.err.println("Simulation Error: the specified plan cannot be found!");
                        break;
                    }
                } else {
                    System.err.println("Simulation Error: a plan choice cannot be applied to an action!");
                    break;
                }
            }
            else if(c.isActionExecution()){
                GoalPlanTree gpt = cgpts.get(c.intentionChoice);
                TreeNode current = gpt.getCurrentStep();
                if(current instanceof ActionNode){
                    ActionNode act = (ActionNode) current;
                    Literal[] prec = act.getPrec();
                    if(cbb.evaluate(prec) == 1){
                        Literal[] post = act.getPostc();
                        cbb.update(post);
                        current = act.getNext();
                        while (current == null){
                            if(gpt.getBacktrackList().size() == 0){
                                break;
                            }
                            current = gpt.getBacktrackList().remove(gpt.getBacktrackList().size()-1);
                            current = current.getNext();
                        }
                        gpt.setCurrentStep(current);
                    }
                    else {
                        System.err.println("Simulation Error: the selected action cannot be executed");
                    }
                }
                else {
                    System.err.println("Simulation Error: an action choice cannot be applied to a subgoal");
                    break;
                }
            }
        }
        double result = 0;
        for(GoalPlanTree gpt: cgpts){
            if(gpt == null || gpt.getCurrentStep() == null){
                result++;
            }
        }
        return result;
    }
}
