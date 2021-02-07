package simulation;

import goalplantree.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Simulator {

    public Simulator(){

    }

    /**
     * run alpha times simulations for a given goal
     * @param alpha
     * @param goal
     */
    public void runSimulation(int alpha, GoalNode goal){
        // get the plans to achieve the goal
        PlanNode[] pls = goal.getPlans();
        // for each plan run simulation
        for(int i = 0; i < pls.length; i++){
            simulate(alpha, pls[i]);
            // get its plan body
            TreeNode[] body = pls[i].getPlanbody();
            // for each step in its planbody
            // if it is a subgoal, run alpha simulation for the plans to achieve it
            for(int j = 0; j < body.length; j++){
                if(body[j] instanceof GoalNode){
                    GoalNode subgoal = (GoalNode) body[j];
                    runSimulation(alpha, subgoal);
                }
            }
        }
    }

    /**
     * simulate alpha times
     * @param alpha
     * @param plan
     */
    private void simulate(int alpha, PlanNode plan){
        // simulate alpha times
        for(int i = 0; i < alpha; i++){
            // simulation path
            ArrayList<SimStep> path = new ArrayList<>();
            // cycle number
            int cycle = 0;
            // generate a random path
            cycle = randomSelect(plan, path, cycle);
            // summarise the path
            summarise(plan, path);
        }

        // calculate the simulaiton information
        //calculation(plan);
        // clear info
        //plan.clearDate();
    }

    /**
     * recursively generate a simulation path
     * @param plan
     * @param path
     * @param cycle
     * @return
     */
    private int randomSelect(PlanNode plan, ArrayList<SimStep> path, int cycle){
        // get the body of this plan
        TreeNode[] body = plan.getPlanbody();
        // check each step in this plan
        for(int i = 0; i < body.length; i++){
            // if it is an action
            if(body[i] instanceof ActionNode){
                // cast it to an action
                ActionNode action = (ActionNode) body[i];
                // the number of cycle is increased by 1
                cycle++;
                // get the precondition of this action
                Literal[] pre = action.getPrec();
                // get the postcondition of this action
                Literal[] post = action.getPostc();
                // add this step to the path
                path.add(new SimStep(pre, post, cycle));

                //System.out.println(cycle);
                //System.out.println(action.getName());

            }
            // if it is a subgoal
            else if(body[i] instanceof GoalNode){
                // cast it to a goal
                GoalNode subgoal = (GoalNode) body[i];
                // get the plans to achieve it
                PlanNode[] pls = subgoal.getPlans();
                // randomly select one plan
                if(pls.length > 0){
                    Random rm = new Random();
                    PlanNode pl = pls[rm.nextInt(pls.length)];
                    // recursively run selection
                    cycle = randomSelect(pl, path, cycle);
                }
            }
        }
        return cycle;
    }

    /**
     * summarise the simulation information given the plan, and a simulation path from this plan
     * @param plan
     * @param path
     */
    private void summarise(PlanNode plan, ArrayList<SimStep> path){


        /**
         * latest position of each literal appear as precondition
         */
        HashMap<String, Integer> prePos = new HashMap<>();
        /**
         * latest position of each literal appear as postcondition
         */
        HashMap<String, Integer> postPos = new HashMap<>();
        /**
         * count the number of time a literal being established
         */
        //HashMap<String, Integer> postCount = new HashMap<>();
        /** fragility of each literal
         */
        //HashMap<String, Integer> frag = new HashMap<>();

        // for each step in the simulation path do the following
        for(int i = 0; i < path.size(); i++){
            /** first update the position for postcondition */
            // get its postcondition
            Literal[] post = path.get(i).getPostCon();
            // check each literal in its postcondition
            for(int j = 0; j < post.length; j++){
                String lname = post[j].toString();
                // the latest position l appears as a precondition
                int pos1 = 0;
                if(prePos.containsKey(lname))
                    pos1 = prePos.get(lname);
                // the latest position l appears as a postcondition
                int pos2 = 0;
                if(postPos.containsKey(lname))
                    pos2 = postPos.get(lname);

                // if there is a l being required before this l is established
                if(pos1 > 0){
                    // get the position of l's negation
                    int pos3 = 0;
                    String lnegname = post[j].toNegString();
                    if(postPos.containsKey(lnegname))
                        pos3 = postPos.get(lnegname);
                    // if it appears before the last appearance of l (as postcondition)
                    if(pos3 <= pos2){
                        int distance = pos1 - pos2; // fragility of l
                        // there is  other fragility of l in this simulation
                        HashMap<String, Integer> frag = plan.getTotSimFrag();
                        if(frag.containsKey(lname)){
                            int current = frag.get(lname);
                            frag.put(lname, current + distance);
                        }
                        // otherwise
                        else {
                            frag.put(lname, distance);
                        }
                    }
                    // set the position of l appears as precondition to 0
                    prePos.remove(lname);
                    //prePos.put(lname, 0);
                }
                // update position for l being postcondition
                postPos.put(lname, path.get(i).getCycNum());
                // update the count
                HashMap<String, Integer> postCount = plan.getTotSimEstab();

                if(postCount.containsKey(lname)){
                    int pcount = postCount.get(lname);
                    postCount.put(lname, pcount + 1);
                }else {
                    postCount.put(lname, 1);
                }
            }

            // get its precondition
            Literal[] pre = path.get(i).getPreCon();
            for(int j = 0; j <pre.length; j++){
                // update the position of  l being precondition
                String lname = pre[j].toString();
                prePos.put(lname, path.get(i).getCycNum());
            }
        }
        // after recording the position of each literal being pre- and post-condition, we calculate the fragility of
        // each literal
        for(Map.Entry<String, Integer> entry: prePos.entrySet())
        {
            // get the name of this literal
            String lname = entry.getKey();
            // get the position of l being precondition
            int pos1 = entry.getValue();
            // get the position of l being postcondition
            int pos2 = 0;
            if(postPos.containsKey(lname))
                pos2 = postPos.get(lname);
            // get the neg name
            String lnegname;
            if(lname.endsWith("+"))
                lnegname = lname.substring(0, lname.length()-2)+"-";
            else
                lnegname = lname.substring(0, lname.length()-2)+"+";
            // get the position of ~l being postcondition
            int pos3 = 0;
            if(postPos.containsKey(lnegname))
                pos3 = postPos.get(lnegname);
            if(pos3 <= pos2){
                int distance = pos1 - pos2;
                HashMap<String, Integer> frag = plan.getTotSimFrag();
                if(frag.containsKey(lname)){
                    int current = frag.get(lname);
                    frag.put(lname, current+distance);
                }else {
                    frag.put(lname, distance);
                }
            }
        }

        // update
        // simulation number updated
        int n = plan.getTotSimNum();
        plan.setTotSimNum(n + 1);
        // total length updated
        int l = plan.getTotSimLength();
        plan.setTotSimLength(l + path.size());

    }

//    private void update(PlanNode plan, ArrayList<SimStep> path, HashMap<String, Integer> postCount, HashMap<String, Integer> frag){
//        // number of simulation is increased
//        plan.simNum++;
//        // get the length of simulation
//        int length = path.size();
//        // total number of steps increases
//        plan.totStep += length;
//        // maximum number of steps update
//        if(plan.maxStep < length)
//            plan.maxStep = length;
//        // update the appearance of l as postcondition
//        for(Map.Entry<String, Integer> entry: postCount.entrySet()){
//            String key = entry.getKey();
//            if(plan.postSet.containsKey(key)){
//                int count = plan.postSet.get(key);
//                plan.postSet.put(key, count + entry.getValue());
//            }else{
//                plan.postSet.put(key, entry.getValue());
//            }
//        }
//        // update fragility info
//        for(Map.Entry<String, Integer> entry : frag.entrySet()){
//            // get the literal name
//            String key = entry.getKey();
//            // get its fragility
//            int distance = entry.getValue();
//
//            // get the preStat of the plan
//            preStat stat = plan.preSet.get(key);
//            // calculate proportion
//            //double proportion = ((double) distance) / path.size();
//            // if there is nothing in the map
//            if(stat == null){
//
//                // update maximum and total
//                preStat ps = new preStat();
//                ps.num = 1;
//                //ps.maxProportion = proportion;
//                //ps.totProportion = proportion;
//                ps.totFrigile = distance;
//                plan.preSet.put(key, ps);
//            }
//            else{
//                stat.num++;
//                // check maximum
//                //if(stat.maxProportion < proportion)
//                    //stat.maxProportion = proportion;
//                //stat.totProportion += proportion;
//                stat.totFrigile += distance;
//            }
//        }
//
//    }

//    private void calculation(PlanNode plan){
//        // the probability
//        HashMap<String, Double> postProb = new HashMap<>();
//        HashMap<String, preSimInfo> preProb = new HashMap<>();
//        //for each literal in plan's postSet
//        for(Map.Entry<String, Integer> entry : plan.postSet.entrySet()){
//            // get the name of the literal
//            String lname = entry.getKey();
//            // get the number of times it appears in the simulation as postcondition
//            int counts = entry.getValue();
//            // calculate the proportion of steps l is established
//            double prob = ((double) counts) / plan.totStep;
//            // put the result in the hashmap
//            postProb.put(lname, prob);
//        }
//        // for each literal in plan's preSet
//        for(Map.Entry<String, preStat> entry : plan.preSet.entrySet()){
//            // get the name of the literal
//            String lname = entry.getKey();
//            // get the precondition statics
//            preStat stat = entry.getValue();
//            // calculate the probability of this literal appears in simulation path as precondition
//            int counts = stat.num;
//            double prob = ((double) counts) / plan.simNum;
//            double max = stat.maxProportion;
//            double proportion = ((double) stat.totFrigile) / plan.totStep;
//
//            //double average = stat.totProportion / counts;
//            preProb.put(lname, new preSimInfo(prob, max, proportion));
//        }
//        /**
//         * calculate the average length of execution path
//         */
//        double aver = ((double) plan.totStep) / plan.simNum;
//        plan.setAverLength(aver);
//
//        plan.setProbPost(postProb);
//        plan.setProbPre(preProb);
//
//    }
}
