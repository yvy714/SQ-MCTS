package goalplantree;
import java.util.ArrayList;
import java.util.HashMap;

public class GoalPlanTree {
    /**
     * top-level goal
     */
    final GoalNode tlg;
    /**
     * the list of backtrack goals
     */
    ArrayList<GoalNode> backtrackList;
    /**
     * current step pointer
     */
    TreeNode currentStep;

    /**
     * estimated Simulation Information
     */
    public HashMap<String, Double> averSimFrag = new HashMap<>(); // average flagility of this intention
    public HashMap<String, Double> averSimEstab = new HashMap<>(); // average number of establishment steps
    public double averSimLength = 0; // average simulation length



    /**
     * constructor
     * @param goal
     */
    public GoalPlanTree(GoalNode goal){
        // set the top-level goal
        tlg = goal;
        // initialise the backtrack list
        backtrackList = new ArrayList<>();
        // set the current step and its status
        currentStep = goal;
    }
    /**
     * @return the top-level goal of this tree
     */
    public GoalNode getTlg(){
        return tlg;
    }
    /**
     * @return the list of steps for backtracking
     */
    public ArrayList<GoalNode> getBacktrackList(){
        return backtrackList;
    }

    /**
     * @return the current step of this gpt
     */
    public TreeNode getCurrentStep(){
        return currentStep;
    }
    /**
     * @param node
     */
    public void setCurrentStep(TreeNode node){
        currentStep = node;
    }
    /**
     * @return the conditions this intention is going to achieve
     */
    public Literal[] getGC(){
        return tlg.getGoalConds();
    }

    public ArrayList<TreeNode> getIntention(){
        // initialise the current intention
        ArrayList<TreeNode> intention = new ArrayList<>();
        // get the current step
        TreeNode cnode = this.currentStep;

        if(cnode == null)
            return intention;

        // check if it is the first step of a plan
        if(cnode.getParent() != null) {
            PlanNode pl = (PlanNode) cnode.getParent();
            // if it is
            if (cnode.getName().equals(pl.getPlanbody()[0].getName())){
                intention.add(pl);
                //return intention;
                // the subgoal this plan is going to achieve
                cnode = pl.getParent();
                while (cnode.getNext() == null && cnode.getParent() != null){
                    cnode = cnode.getParent();
                }
                // if we finally find the next step
                if(cnode.getNext() != null){
                    cnode =cnode.getNext();
                }
                // otherwise, the parent of cnode is tlg
                else {
                    cnode = null;
                }
            }
        }




        // get the current intention
        while (cnode != null){
            intention.add(cnode);
            // check if it is the last step in a plan
            if(cnode.getNext() != null) {
                cnode = cnode.getNext();
            }
            // if it is
            else {
                // check if it is the top-level goal
                if(cnode.getParent() == null){
                    cnode = null;
                }else {
                    cnode = cnode.getParent();
                    // if it is again the last step in a plan
                    while (cnode.getNext() == null && cnode.getParent() != null){
                        cnode = cnode.getParent();
                    }

                    // if we finally find the next step
                    if(cnode.getNext() != null){
                        cnode =cnode.getNext();
                    }
                    // otherwise, the parent of cnode is tlg
                    else {
                        cnode = null;
                    }
                }
            }
        }

        return intention;
    }




    /**
     * if the current step is an action and has been successfully executed, then set the current step to the next step.
     * (Note, we don't need to check if the current step succeeds or not)
     * @return the action to be executed or null if there is no such action
     */
    public ActionNode progress(){
        System.out.println("progress!!!");
        // if the top-level goal has not been achieved already
        if(currentStep != null){
            // if the current step is an action
            if(currentStep instanceof ActionNode){

                // get the action
                ActionNode act = (ActionNode) currentStep;
                // activate this action
                act.setStatus(TreeNode.Status.ACTIVE);
                return act;
            }
        }
        return null;
    }

    /**
     * Progressing the gpt by selecting a specified plan to achieve the current (sub)goal
     * @param index the index of the plan
     * @return the plan that is applied to achieve the goal
     */
    public PlanNode progress(int index){
        // if the current step is not null, i.e., this intention has not been achieved
        if(currentStep != null){
            // if the current step is really a goal
            if(currentStep instanceof GoalNode){
                // cast it to a goal node
                GoalNode goalNode = (GoalNode) currentStep;
                // the specified index must be valid
                if(index < goalNode.getPlanNum()){
                    // get the plans
                    PlanNode[] plans = goalNode.getPlans();
                    PlanNode pl = plans[index];
                    // activate this goal
                    goalNode.setStatus(TreeNode.Status.ACTIVE);
                    // activate the plan
                    pl.setStatus(TreeNode.Status.ACTIVE);
                    // this goal is added to the backtrack list
                    backtrackList.add(goalNode);
                    // the first step in this plan is selected as the current step
                    currentStep = plans[index].getPlanbody()[0];
                    return pl;
                }
            }

        }
        // otherwise, return null
        return null;
    }

    /**
     * if the selected action has been executed successfully, then we update the goal-plan tree
     */
    public void success(){
        if(currentStep instanceof ActionNode){
            // cast it to an action
            ActionNode act = (ActionNode) currentStep;
            // set the status to success
            act.setStatus(TreeNode.Status.SUCCESS);
            // get the next step of this goal-plan tree
            TreeNode next = act.getNext();
            // a while loop to update the next step, if this action is the last action in a plan
            while (next == null){

                // get the latest goal
                GoalNode g = backtrackList.remove(backtrackList.size()-1);

                // this goal is achieved
                g.setStatus(TreeNode.Status.SUCCESS);
                // the plan to achieve this goal also succeed
                PlanNode[] pls = g.getPlans();
                for(PlanNode p : pls){
                    if(p.getStatus().equals(TreeNode.Status.ACTIVE)) {
                        p.setStatus(TreeNode.Status.SUCCESS);
                        break;
                    }
                }
                // if g is the top-level goal
                if(backtrackList.size() == 0){
                    break;
                }
                // otherwise, if it is the last action in a plan that is not used to achieve the top-level goal
                else {
                    // we set the next step to the next step of the subgoal
                    next = g.getNext();
                }
            }
            // set the current step to the next step
            currentStep = next;
        }else {
            System.out.println("Error: goal-plan tree update error 1");
            System.exit(0);
        }
    }

    public void fail(){
        // the current step fails
        currentStep.setStatus(TreeNode.Status.FAILURE);

        // if the current step is not the top-level goal
        if(backtrackList.size() > 0){
            System.out.println("eeeeeeeeeeeeeeeee");
            System.out.println(backtrackList.get(backtrackList.size()-1).getName());

            // get the latest subgoal
            GoalNode subgoal = backtrackList.remove(backtrackList.size()-1);
            // a boolean value indicating if there are other plans to achieve this goal
            boolean available = false;
            // find the active plan
            PlanNode[] pls = subgoal.getPlans();
            for(PlanNode p : pls){
                // if there is a plan that hasn't been tried
                if(p.getStatus().equals(TreeNode.Status.DEFAULT)){
                    available = true;
                }
                // set the current active plan to failure
                else if(p.getStatus().equals(TreeNode.Status.ACTIVE)) {
                    // set it to failure state
                    p.setStatus(TreeNode.Status.FAILURE);
                }
            }

            currentStep = subgoal;
            // if there are still plans haven't been tried
            if(!available){
                fail();
            }

        }
    }

    /**
     * @return true, if the top-level goal is achieved
     */
    public boolean achieved(){
        return currentStep == null;
    }





    /**
     * @param node
     * @return the next step in the remaining goal-plan tree
     */
    public TreeNode toNext(TreeNode node){
        // if there is a step next to the given node
        if(node.getNext() != null){
            return node.getNext();
        }
        // if it is the last step in a plan or it is the top-level goal
        else{
            // cast it the its parent
            node = node.getParent();
            // if it is the top-level goal
            if(node == null){
                return null;
            }
            // if it is not, then return the next step of the new node
            else {
                return toNext(node);
            }
        }
    }


    @Override
    public GoalPlanTree clone(){
        GoalPlanTree ng = new GoalPlanTree(this.tlg);
        ng.currentStep = this.currentStep;
        // copy the backtracklist
        ArrayList<GoalNode> bls = new ArrayList<>();
        for(GoalNode g : backtrackList){
            bls.add(g);
        }
        ng.backtrackList = bls;

        return ng;
    }

}
