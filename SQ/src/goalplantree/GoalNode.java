package goalplantree;

public class GoalNode extends TreeNode {

    /**
     * relevant plans
     */
    final private PlanNode[] plans;

    /**
     * goal-conditions
     */
    final private Literal[] goalConds;



    /**
     * initialisation with no relevant plans
     * @param name name of the goal
     * @param goalCondition the goal condition of this goal
     */
    public GoalNode(String name, String type, Literal[] goalCondition){
        super(name);
        // there is no relevant plan
        plans = new PlanNode[0];
        goalConds = goalCondition;
    }

    /**
     * initialisation with a list of plans to achieve this goal
     * @param name name of the goal
     * @param plannodes a list of relevant plans
     */
    public GoalNode(String name, PlanNode[] plannodes, Literal[] goalCondition){
        super(name);
        this.plans = plannodes == null ? new PlanNode[0] : plannodes;
        // set the parent-child relationship
        for(int i = 0; i < this.plans.length; i++){
            this.plans[i].setParent(this);
        }
        this.goalConds = goalCondition == null ? new Literal[0] : goalCondition;
    }

    /**
     * @return the list of relevant plans
     */
    public PlanNode[] getPlans(){
        return this.plans;
    }

    /**
     *
     * @param index
     * @return a plan with the specified index
     */
    public PlanNode getPlanAt(int index){
        return this.plans[index];
    }


    /**
     * @return the number of plans
     */
    public int getPlanNum(){
        return this.plans.length;
    }


    /**
     * @return null, if it is a top-level goal; the next step of this goal if it is a subgoal in a plan
     */
    @Override
    public TreeNode getNext(){
        // if it is not the last step in a plan or a top-level goal
        if(this.next != null){
            // return the next step of this subgoal in its associated
            return next;
        }
        // otherwise
        else {
            // if it is the last step in a plan
            if(this.parent != null){
                // return the next step of the goal its associated plan tries to achieve
                this.getParent().getParent().getNext();
            }
            // if it is a top-level goal, then return null
            return null;
        }
    }

    /**
     * @return the goal condition of this goal
     */
    public Literal[] getGoalConds(){
        return this.goalConds;
    }


    @Override
    public String onPrintNode(int num) {

        String result = "Goal:[type = " + name +
                        "; status = " + status;
        result += "}; goalConds = {";
        for(int i = 0; i < goalConds.length; i++){
            result += "(" + goalConds[i].getName() + "," + goalConds[i].getState() + ");";
        }

        result += "}; relevant plans = {";
        if(getPlanNum() > 0){
            result+=plans[0].getName();
        }
        for(int i = 1; i < getPlanNum(); i++){
            result += ", " + plans[i].getName();
        }
        result+="}]";

        for(int i = 0; i < plans.length; i++){
            result += "\n";
            for(int j = 0; j < num + 1; j++){
                result += indent;
            }
            result += plans[i].onPrintNode(num + 1);
        }

        return result;
    }

}
