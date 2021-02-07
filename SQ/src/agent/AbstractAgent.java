package agent;

import environment.AbstractEnvironment;
import goalplantree.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractAgent {

    /**
     * name of the agent
     */
    public final String name;
    /**
     * agent's belief base
     */
    BeliefBaseImp bb;
    /**
     * agent's current intentions
     */
    ArrayList<GoalPlanTree> intentions;
    /**
     * plan choice and intention choice in the current cycle
     */
    ArrayList<Choice> choices;
    /**
     * the list of goals that have been achieved so far
     */
    ArrayList<String> achievedGoals;

    /**
     * the vision of the agent.
     * 1 represents the agent has full vision on this literal, i.e., the agent can sense the exact change
     * of the environment;
     * 0 represents the agent has no vision on this literal, i.e., the agent cannot sense the change on this literal;
     * values between 0 and 1 represents that the agent can sense the changes but not will full confidence
     */
    private HashMap<String, Double> vision;

    /**
     * constructor
     * @param id name of this agent
     * @param bs the initial beliefs
     */
    public AbstractAgent(String id, ArrayList<Belief> bs){
        // set the name of this agent
        this.name = id;
        // the visions to all literals are set to 1 by default
        HashMap<String, Double> vs = new HashMap<>();
        for(int i = 0; i < bs.size(); i++){
            vs.put(bs.get(i).lit_name, 1d);
        }
        // initialise the agent's state with given belief base
        init(bs, new ArrayList<>(), vs);
    }

    public AbstractAgent(String id, ArrayList<Belief> bs, ArrayList<GoalNode> gs){
        // set the name
        this.name = id;
        // the visions are all set to 1 by default
        HashMap<String, Double> vs = new HashMap<>();
        for(int i = 0; i < bs.size(); i++){
            vs.put(bs.get(i).lit_name, 1d);
        }
        // initialise the agent's state with given belief base and top-level goals
        init(bs, gs, vs);
    }

    public AbstractAgent(String id, ArrayList<Belief> bs, ArrayList<GoalNode> gs, HashMap<String, Double> vs){
        this.name = id;
        // initialise the agent's state with given belief base, top-level goals and visions
        init(bs, gs, vs);
    }

    /**
     * initialisation
     * @param bs a list of initial beliefs
     * @param gs a list of top-level goals
     * @param vs the initial vision
     */
    private void init(ArrayList<Belief> bs, ArrayList<GoalNode> gs, HashMap<String, Double> vs){
        // initialise the belief base
        bb = new BeliefBaseImp(bs);
        // initialise the intentions
        intentions = new ArrayList<>();
        for(GoalNode g : gs){
            intentions.add(new GoalPlanTree(g));
        }
        // initialise the vision
        vision = new HashMap<>();
        for(Map.Entry<String, Double> entry : vs.entrySet()){
            vision.put(entry.getKey(), entry.getValue());
        }
        // initialise the choices
        choices = new ArrayList<>();
        // initialise the list of achieved goals
        achievedGoals = new ArrayList<>();
    }

    /**
     * sense the environment
     * @param environment
     */
    public void sense(AbstractEnvironment environment){
        // get the percepts from the environment
        ArrayList<Literal> percepts = environment.getStates(vision);

        // update agent's belief base, the percepts might be wrong or with a certain probability
        // here we use the default updates
        bb.update(percepts);
    }

    /**
     * adopt new goals
     * @param goals new goals
     */
    public void adoptGoals(ArrayList<GoalNode> goals){
        if(goals == null)
            return;
        for(GoalNode g : goals){
            intentions.add(new GoalPlanTree(g));
        }
    }

    /**
     * @return the number of goals achieved
     */
    public int getNumAchivedGoal(){
        return this.achievedGoals.size();
    }

    /**
     * @return the plan selection and intention selection in this cycle
     */
    public abstract boolean deliberate();

    /**
     * progress the agent's intentions and return the action to execute at the current cycle
     * @param environment the associated environment
     * @return the action to execute at the current cycle
     */
    public ActionNode execute(AbstractEnvironment environment) {

        // check if there is a decision has been made already. If there is, then execute it
        while(this.choices.size() > 0){
            // get the immediate choice
            Choice choice = this.choices.get(0);
            // if it is a plan choice
            if(choice.isPlanSelection()){
                // remove the choice
                choices.remove(0);
                // get the intention
                GoalPlanTree gpt = this.intentions.get(choice.intentionChoice);
                // if applying the specified plan fails
                // in case the design of the agent is incorrect
                if(gpt.progress(choice.planChoice) == null){
                    // print out the error messages
                    System.err.println("Select Plan fails!\n" + "current step: " + gpt.getCurrentStep().getName());
                    if(gpt.getCurrentStep() instanceof GoalNode){
                        GoalNode g = (GoalNode) gpt.getCurrentStep();
                        PlanNode[] ps = g.getPlans();
                        System.err.println("target plan: " + choice.planChoice);
                        System.err.println("number of plan:" + ps.length);
                    }
                    System.exit(0);
                }
            }
            // if the choice is to execute an action
            else if(choice.isActionExecution()){
                // get the goal-plan tree
                GoalPlanTree gpt = this.intentions.get(choice.intentionChoice);
                // get the action to execute
                ActionNode act = gpt.progress();
                return act;
            }
        }
        return null;
    }

    /**
     * if an action has been executed successfully, then we update agent's mental states accordingly
     */
    public abstract void exeSucceed();

    /**
     * if an action fails, then we update agent's mental state accordingly
     */
    public abstract void exeFail();

    public String onPrintBeliefs(){
        return bb.onPrintBB();
    }

    public String onPrintIntentions(){
        String output = "Intentions:{";
        for(GoalPlanTree gpt : intentions){
            output += "(" + gpt.getTlg().getName() + ",";
            output += (gpt.getCurrentStep() == null ? "null" : gpt.getCurrentStep().getName()) + ");";
        }
        output+="}";
        return output;
    }

    public String onPrintVision(){
        String output = "Vision:{";
        for(Map.Entry<String, Double> entry : vision.entrySet()){
            output += "(" + entry.getKey() + ",";
            output += (entry.getValue() == 1 ? "visible" : "not visible") + ");";
        }
        output+="}";
        return output;
    }
}
