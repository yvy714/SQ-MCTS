package mcts;

import agent.BeliefBaseImp;
import agent.Choice;
import goalplantree.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class BasicMCTSNode {
    /**
     * static parameters
     */
    // random seed
    static Random rm = new Random();
    // a small value to break the tie and to divided by 0
    static final double epsilon = 1e-6;
    // the goal-plan trees of the initial state in the root node
    static GoalPlanTree[] gpts;
    // the belief base of the initial state in the root node
    static BeliefBaseImp beliefs;
    // best simulation choices
    static ArrayList<Choice> bChoices;
    // best simulation result
    static double bResult;

    public static int simNum = 0;

    /**
     * the choices which leads the parent node to this node (i.e., the edge that connects the parent state to this node)
    (we do not copy and record the agent's mental state to avoid duplication and to save space)
     */
    ArrayList<Choice> choices;

    /**
     * children of this node
     */
    ArrayList<BasicMCTSNode> children = new ArrayList<>();

    /**
     * statistics
     */
    Statistic statistic = new Statistic();

    /**
     * constructor for the root node
     * @param trees the set of goal-plan trees in the current state
     * @param bb
     */
    public BasicMCTSNode(GoalPlanTree[] trees, BeliefBaseImp bb){
        gpts = trees;
        beliefs = bb;
        choices = new ArrayList<>();
        bChoices = new ArrayList<>();
        bResult = -1;
    }

    /**
     * constructor for the root node
     * @param trees the set of goal-plan trees in the current state
     * @param bb
     */
    public BasicMCTSNode(ArrayList<GoalPlanTree> trees, BeliefBaseImp bb){
        gpts = new GoalPlanTree[trees.size()];
        for(int i = 0; i < trees.size(); i++){
            gpts[i] = trees.get(i);
        }
        beliefs = bb;
        choices = new ArrayList<>();
        bChoices = new ArrayList<>();
        bResult = -1;
    }

    /**
     * constructor for other nodes
     * @param c the choices which leads its parent node to this node
     */
    public BasicMCTSNode(ArrayList<Choice> c){
        choices = c;
    }

    /**
     * @return true, if this node is a leaf node; false, otherwise.
     */
    public boolean isLeaf(){
        return children.size() == 0;
    }

    /**
     * @return the number of child nodes
     */
    public int arity(){
        return children.size();
    }

    /**
     * The process of iteratively building the MCTS search trees
     * @param alpha the number of iteration
     * @param beta the number of simulations in each iteration
     */
    public void run(int alpha, int beta){

        // run alpha iterations
        for(int i = 0; i < alpha; i++){

            simNum += beta;

            // in each iteration, we record the list of nodes that have been visited
            List<BasicMCTSNode> visited = new LinkedList<>();
            // we also record the choices made so far
            ArrayList<Choice> cs = new ArrayList<>();

            // copy the current intentions
            GoalPlanTree[] sGPTs = new GoalPlanTree[gpts.length];
            for(int x = 0; x < gpts.length; x++){
                sGPTs[x] = gpts[x].clone();
            }
            // copy the current belief base
            BeliefBaseImp sBeliefs = beliefs.clone();


            // start from the root node
            BasicMCTSNode current = this;
            // add the root node to the list of visited node
            visited.add(current);
            // the root node does not contain any choices, thus we ignore this process


            /**
             * selection phase: find a leaf node that has the largest UCT value
             */
            while (!current.isLeaf()){
                // the current node is set to its child node which has the largest UCT value
                current = current.select();
                // once a node is selected, its choices are also added to the list
                cs.addAll(current.choices);
                // the selected node is also added to the list of visited nodes
                visited.add(current);
            }
            // get the intention and belief bases after these choices
            for(Choice c : cs){
                biUpdate(c, sGPTs, sBeliefs);
                //System.out.println("Test: (" + c.intentionChoice + "," + c.planChoice + ")");
            }
            //System.out.println("---------------------------------------------------------------");




            /**
             * expansion phase: expand the selected node by adding all its possible child nodes
             */
            current.expand(sGPTs, sBeliefs);

            /**
             * simulation phase: select one of the newly created node for simulation
             */


            if(current != null && !current.isLeaf()){
                // randomly select a node for simulation
                BasicMCTSNode sNode = null;
                double max = 0;
                for(BasicMCTSNode n : current.children){
                    double randomValue = rm.nextDouble();
                    if(randomValue > max){
                        max = randomValue;
                        sNode = n;
                    }
                }

                // get the selected node and update the intention and belief base
                ArrayList<Choice> sChoices = sNode.choices;

                for(Choice c : sChoices){
                    biUpdate(c,sGPTs,sBeliefs);
                }

                // add the choices of the new node to the list of choices
                cs.addAll(sChoices);

                // run beta simulations
                for(int j = 0; j < beta; j++){
                    double sValue = sNode.rollOut(sGPTs, sBeliefs, cs);
                    /**
                     * back-propagation
                     */
                    for(BasicMCTSNode node : visited){
                        node.statistic.addValue(sValue);
                    }
                }


            }
            // if it is a leaf node
            else if(!current.isLeaf()){
                // check the number of goals achieved
                double sValue = current.getAchievedNum();
                /**
                 * back-propagation
                 */
                for(BasicMCTSNode node : visited){
                    node.statistic.addValue(sValue);
                }
            }
        }
    }

    /**
     * @return a child node with maximum UCT value
     */
     protected BasicMCTSNode select(){
        // initialisation
        BasicMCTSNode selected = null;
        double bestUCT = Double.MIN_VALUE;
        // calculate the uct value for each of its selected nodes
        for(int i = 0; i < children.size(); i++){
            // UCT calculation
            double uctValue = children.get(i).statistic.totValue/ (children.get(i).statistic.nVisits + epsilon)+
                    Math.sqrt(Math.log(statistic.nVisits + 1)/(children.get(i).statistic.nVisits + epsilon))+ rm.nextDouble() * epsilon;
            // compare the uct value with the current maximum value
            if(uctValue > bestUCT){
                selected = children.get(i);
                bestUCT = uctValue;
            }
        }
        // return the nodes with maximum UCT value, null if current node is a leaf node (contains no child nodes)
        return selected;
    }

    /**
     * expand the current node by adding child nodes to it
     */
    private void expand(GoalPlanTree[] sgpts, BeliefBaseImp sbeliefs){

        // check all intentions
        for(int i = 0; i < sgpts.length; i++){
            // get the current step of the selected intention
            TreeNode cStep = sgpts[i].getCurrentStep();

            // if the current step of this intention is a subgoal
            if(cStep instanceof GoalNode){
                // cast it to a goal
                GoalNode sg = (GoalNode) cStep;
                // get the list of choices corresponding to different mcts nodes
                ArrayList<ArrayList<Integer>> sChoice = getPosChoices(sg, sbeliefs);

                // for each of these lists, generate an MCTS node
                for(ArrayList<Integer> lists : sChoice){
                    // create a choice list
                    ArrayList<Choice> ncs = new ArrayList<>();
                    // add all plan choices
                    for(int l : lists){
                        Choice c = new Choice(i, l);
                        ncs.add(c);
                    }
                    // add the last action choice
                    Choice ca = new Choice(i);
                    ncs.add(ca);
                    // create the MCTS node
                    BasicMCTSNode child = new BasicMCTSNode(ncs);
                    this.children.add(child);
                }

            }
            // if the current step of this intention is an action
            else if(cStep instanceof ActionNode){
                // cast it to action
                ActionNode act = (ActionNode) cStep;
                // get its precondition
                Literal[] prec = act.getPrec();
                // if the precondition holds
                if(sbeliefs.evaluate(prec) == 1){
                    ArrayList<Choice> ncs = new ArrayList<>();
                    // new intention choice
                    Choice c = new Choice(i);
                    ncs.add(c);
                    // create new MCTS node
                    BasicMCTSNode child = new BasicMCTSNode(ncs);
                    // add it as the child of this node
                    this.children.add(child);
                }
            }
        }


    }

    /**
     * update belief base and intention base according to a choice
     * @param c the given choice
     * @param sGPTs the set of gpts
     * @param sBeliefs the belief base
     */
    private void biUpdate(Choice c, GoalPlanTree[] sGPTs, BeliefBaseImp sBeliefs){

        // get the target goal-plan tree
        GoalPlanTree gpt = sGPTs[c.intentionChoice];
        // if it is a plan selection and the current step in this gpt is a (sub)goal
        if(c.isPlanSelection() && gpt.getCurrentStep() instanceof GoalNode){
            // then we use the corresponding plan to achieve it
            // cast the current step to a subgoal
            GoalNode sg = (GoalNode) gpt.getCurrentStep();
            // get the plans
            PlanNode[] pls = sg.getPlans();
            // add to backtrack list
            gpt.getBacktrackList().add(sg);
            // set the current step to the first step in this plan
            gpt.setCurrentStep(pls[c.planChoice].getPlanbody()[0]);
        }
        // if it is an intention selection and the current step of this gpt is an action
        else if(c.isActionExecution() && gpt.getCurrentStep() instanceof ActionNode){
            // get the action
            ActionNode act = (ActionNode) gpt.getCurrentStep();
            // get its postcondition
            Literal[] postc = act.getPostc();
            // apply changes to the simulation belief base
            for(Literal l : postc){
                sBeliefs.update(l);
            }
            // get next step
            TreeNode cstep = act.getNext();
            // if it is null, i.e., it is the last step in the plan to achieve a goal
            while(cstep == null){
                // if it is the top-level goal
                if(gpt.getBacktrackList().size() == 0)
                    break;

                // get its parent goal
                GoalNode pg = gpt.getBacktrackList().remove(gpt.getBacktrackList().size() - 1);
                // set the next step
                cstep = pg.getNext();
            }
            // finally set the next step
            gpt.setCurrentStep(cstep);

        }else {
            System.out.println(c.intentionChoice);
            System.out.println(c.planChoice);
            System.out.println(c.isActionExecution());
            System.out.println(c.isPlanSelection());
            System.out.println(gpt.getCurrentStep().getName());

            System.err.println("MCTS Expansion Error0");
            System.exit(0);
        }
    }


    private ArrayList<ArrayList<Integer>> getPosChoices(GoalNode sg, BeliefBaseImp bb){
        // initialise the list
        ArrayList<ArrayList<Integer>> result = new ArrayList<>();
        // get its associated plans
        PlanNode[] pls = sg.getPlans();
        // check each plan
        for(int i = 0; i < pls.length; i++){
            // get the precondition of the plan
            Literal[] context = pls[i].getPrec();
            // if its precondition holds
            if(bb.evaluate(context) == 1){
                // get the first step of this plan
                TreeNode first = pls[i].getPlanbody()[0];
                // if the first step is an action
                if(first instanceof ActionNode){
                    // initialise the list
                    ArrayList<Integer> cs = new ArrayList<>();
                    // add the plan choice
                    cs.add(i);
                    result.add(cs);
                }
                // if the first step is a subgoal
                else {
                    // cast it to a subgoal
                    GoalNode g = (GoalNode) first;
                    // get the list of choice lists
                    ArrayList<ArrayList<Integer>> css = getPosChoices(g, bb);
                    // for each of these lists
                    for(ArrayList<Integer> s : css){
                        ArrayList<Integer> cs = new ArrayList<>();
                        // Add the plan choice
                        cs.add(i);
                        // then add all the choices in the list
                        cs.addAll(s);
                        result.add(cs);
                    }
                }
            }
        }
        return result;
    }

    /**
     * @return the simulation rollouts
     */
    private double rollOut(GoalPlanTree[] gpts, BeliefBaseImp beliefs, ArrayList<Choice> sChoices){
        // to store the choices made in the simulation
        ArrayList<Choice> css = new ArrayList<>();

        // the list of available intentions
        ArrayList<Integer> indexes = new ArrayList<>();
        // copy the list of gpts

        GoalPlanTree[] sgpts = new GoalPlanTree[gpts.length];
        for(int i = 0; i < gpts.length; i++){
            sgpts[i] = gpts[i].clone();
            // if this gpt has not been achieved or dropped already, then it is availabel
            if(sgpts[i].getCurrentStep() != null){
                indexes.add(i);
            }
        }
        // copy the belief base
        BeliefBaseImp sbb = beliefs.clone();


        // simulation starts
        // the simulation stops only when all intention becomes non-progressable
        intentionloop:
        while (indexes.size() > 0){
            // the list of choices in the current iteration
            ArrayList<Choice> cx = new ArrayList<>();

            // randomly pick a choice
            int rc = rm.nextInt(indexes.size());
            int index = indexes.remove(rc);
            // get the gpt
            GoalPlanTree gpt = sgpts[index];

            // if the current step of the selected gpt is a subgoal
            TreeNode currentStep = sgpts[index].getCurrentStep();
            goalloop:
            while(currentStep instanceof GoalNode){
                // cast it to a goal
                GoalNode goal = (GoalNode) currentStep;
                // get the plans
                PlanNode[] pls = goal.getPlans();
                // initially all plans are available
                ArrayList<Integer> avaPlans = new ArrayList<>();
                for(int x = 0; x < pls.length; x++){
                    avaPlans.add(x);
                }
                //
                planloop:
                while (avaPlans.size() > 0){
                    // randomly select a plan
                    int l = rm.nextInt(avaPlans.size());
                    int pi = avaPlans.remove(l);
                    PlanNode pl = pls[pi];
                    // check its precondition
                    Literal[] prec = pl.getPrec();
                    // if its precondition holds
                    if(sbb.evaluate(prec) == 1){
                        // current step becomes the first step in the selected plan
                        currentStep = pl.getPlanbody()[0];
                        // add the plan choice to the list of choices
                        cx.add(new Choice(index, pi));
                        // check if the current step is still a subgoal
                        continue goalloop;
                    }
                    // else, we need to select another plan
                }
                // if there is no applicable plans, then select another one
                continue intentionloop;
            }

            // if the current step is an action (either because it is an action initially, or because we iteratively
            // select plans to achieve subgoals and finally reach an action)
            if(currentStep instanceof ActionNode){
                // cast it to an action
                ActionNode act = (ActionNode) currentStep;
                // get its precondition
                Literal[] prec = act.getPrec();
                // if its precondition holds
                if(sbb.evaluate(prec) == 1){
                    // add the action choice
                    cx.add(new Choice(index));
                    // update the simulation intentions and beliefs
                    for(Choice c : cx){
                        biUpdate(c, sgpts, sbb);
                    }
                    // add the choices to the list
                    css.addAll(cx);
                    // reset the list of available intentions
                    indexes.clear();
                    for(int x = 0; x < sgpts.length; x++){
                        if(sgpts[x] != null && sgpts[x].getCurrentStep() != null){
                            indexes.add(x);
                        }
                    }
                    continue intentionloop;

                }
                // if this action cannot be executed
                else {
                    // clear the choices so far
                    cx.clear();
                    continue intentionloop;
                }
            }

        }

        // when there is no intention can be executed further, return the simulation score according to the utility function
        double uResult = utility(sgpts);

        ArrayList<Choice> temp = new ArrayList<>();
        temp.addAll(sChoices);
        temp.addAll(css);


        // if this simulation performs better than previous run
        if(uResult > bResult){
            bResult = uResult;
            bChoices = temp;
        }

        return uResult;
    }


    public Statistic getStatistic(){
        return this.statistic;
    }

    public double getAchievedNum(){
        return utility(gpts);
    }

    private double utility(GoalPlanTree[] gpts){
        double num = 0;
        for(GoalPlanTree gpt : gpts){
            if(gpt == null || gpt.getCurrentStep() == null){
                num++;
            }
        }
        return num;
    }

    /**
     * @return the best choices
     */
    public ArrayList<Choice> bestChoice(){
        // if the root node cannot be expanded any further
        if(this.children.size() == 0){
            return new ArrayList<>();
        }
        // otherwise, find the child node that has been visited most
        else {
            int maxVisit = this.children.get(0).statistic.nVisits;
            double best = this.children.get(0).statistic.best;
            double total = this.children.get(0).statistic.totValue;
            double average = this.children.get(0).statistic.totValue / this.children.get(0).statistic.nVisits;
            BasicMCTSNode bestChild = this.children.get(0);
            for(BasicMCTSNode child: children){
                if(child.statistic.totValue / child.statistic.nVisits > average){
                //if(child.statistic.totValue > total){
                //if(child.statistic.nVisits > maxVisit){
                //if(child.statistic.best > best){
                    maxVisit = child.statistic.nVisits;
                    best = child.statistic.best;
                    total = child.statistic.totValue;
                    average = child.statistic.totValue / child.statistic.nVisits;
                    bestChild = child;
                }
            }

            System.out.println("best: " + best);
            return bestChild.choices;

        }
    }


    public ArrayList<Choice> getAllChoices(){
        return bChoices;
    }

    public double getAllBestResult(){
        return bResult;
    }

}



