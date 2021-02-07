package simulation;

import goalplantree.*;
import java.util.*;
import org.apache.commons.math3.util.CombinatoricsUtils;

public class ConflictsCalculator {

    Random rm = new Random();

    public ConflictsCalculator(){

    }

    public void calSimInfoGoal(GoalNode goal){
        // initialise the simulation information for this goal
        HashMap<String, Integer> gEstab = new HashMap<>(); // establish steps
        HashMap<String, Integer> gFlag = new HashMap<>(); // flag
        int gLength = 0; // total length
        int gSimNum = 0;// total number of simulation

        // get plans to achieve this goal
        PlanNode[] plans = goal.getPlans();
        // check each plan
        for(int i = 0; i <plans.length; i++){
            // Establishing step
            HashMap<String, Integer> estab = plans[i].getTotSimEstab();
            // for each literal in this map
            for(Map.Entry<String, Integer> entry : estab.entrySet()){
                String lname = entry.getKey();
                int count = entry.getValue();
                // if it is already in the set
                if(gEstab.containsKey(lname)){
                    int temp = gEstab.get(lname);
                    gEstab.put(lname, temp + count);
                }
                // otherwise
                else {
                    gEstab.put(lname, count);
                }
            }

            // Fragility
            HashMap<String, Integer> frag = plans[i].getTotSimFrag();
            // for each literal in this map
            for(Map.Entry<String, Integer> entry : frag.entrySet()){
                String lname = entry.getKey();
                int count = entry.getValue();
                // if it is already in the set
                if(gFlag.containsKey(lname)){
                    int temp = gFlag.get(lname);
                    gFlag.put(lname, temp + count);
                }
                // otherwise
                else {
                    gFlag.put(lname, count);
                }
            }

            // length
            gLength = gLength + plans[i].getTotSimLength();
            // simulaiton number
            gSimNum = gSimNum + plans[i].getTotSimNum();
        }

        goal.setTotSimEstab(gEstab);
        goal.setTotSimFrag(gFlag);
        goal.setTotSimLength(gLength);
        goal.setTotSimNum(gSimNum);
    }

    public void calIntention(GoalPlanTree gpt){
        // initialise the information
        gpt.averSimEstab.clear();
        gpt.averSimFrag.clear();
        gpt.averSimLength = 0;
        // Hash table to record the location of actions
        HashMap<String, Integer> posActionPost = new HashMap<>();


        // get the current intention of this gpt
        ArrayList<TreeNode> intention = gpt.getIntention();
        // check for each step
        for(int i = 0; i < intention.size(); i++){

            //System.out.print(intention.get(i).getName() + ";");

            // if this step is an action
            if(intention.get(i) instanceof ActionNode){
                // cast it to an action
                ActionNode action = (ActionNode) intention.get(i);
                // get its precondition, precondition only related to fragility
                Literal[] prec = action.getPrec();

                for(int j = 0; j < prec.length; j++){
                    String lname = prec[j].toString();

                    // fragility
                    // if this literal hasn't been established before
                    // i.e., it is not in the map, or its corresponding value is 0
                    if(!gpt.averSimEstab.containsKey(lname) || gpt.averSimEstab.get(lname) == 0){
                        // it becomes the current average depth + 1
                        gpt.averSimFrag.put(lname, gpt.averSimLength + 1);
                    }
                    // if it is potentially established, then we consider the best case, the average fragility only increased by 1
                    else {
                        // if there is a step establishes this action
                        if(posActionPost.containsKey(lname)){
                            // value of fragile
                            double fValue = 0;
                            // get its position
                            int position = posActionPost.get(lname);
                            for(int x = position; x < i; x++){
                                if(intention.get(x) instanceof ActionNode){
                                    fValue = fValue + 1;
                                }else if (intention.get(x) instanceof GoalNode){
                                    GoalNode gl = (GoalNode) intention.get(x);
                                    double aLength = ((double) gl.getTotSimLength()) / gl.getTotSimNum();
                                    fValue += aLength;
                                }
                            }

                            if(gpt.averSimFrag.containsKey(lname)){
                                gpt.averSimFrag.put(lname, gpt.averSimFrag.get(lname) + fValue);
                            }
                            else {
                                gpt.averSimFrag.put(lname, fValue);
                            }
                            // update position
                            posActionPost.put(lname, i);
                        }
                        // if there is no such action
                        else {
                            if(gpt.averSimFrag.containsKey(lname)){
                                gpt.averSimFrag.put(lname, gpt.averSimFrag.get(lname) + 1);
                            }else {
                                gpt.averSimFrag.put(lname, 1d);
                            }
                        }
                    }
                }

                // get its postcondition, postcondiiton only related to establishment
                Literal[] postc = action.getPostc();
                for(int j = 0; j < postc.length; j++){
                    String lname = postc[j].toString();
                    // get its negation
                    String lnegname;
                    if(lname.endsWith("+"))
                        lnegname = lname.substring(0, lname.length()-2)+"-";
                    else
                        lnegname = lname.substring(0, lname.length()-2)+"+";

                    // establishment increased by 1
                    if(gpt.averSimEstab.containsKey(lname)){
                        gpt.averSimEstab.put(lname, gpt.averSimEstab.get(lname) + 1);
                    }else {
                        gpt.averSimEstab.put(lname, 1d);
                    }

                    // update position
                    posActionPost.put(lname, i);
                    posActionPost.remove(lnegname);
                }
                // length
                gpt.averSimLength++;
            }
            // if this step is a subgoal
            else if(intention.get(i) instanceof GoalNode){
                // cast it to a goal
                GoalNode goal = (GoalNode) intention.get(i);
                // summarise simulation information from its plans
                calSimInfoGoal(goal);
                // get the total number of simulation
                double tSimNum = goal.getTotSimNum();
                // get the total number of steps;
                double tSimLength = goal.getTotSimLength();


                // establishment
                HashMap<String, Integer> estab = goal.getTotSimEstab();
                for(Map.Entry<String, Integer> entry : estab.entrySet()){
                    // get the name of literal
                    String lname = entry.getKey();
                    // get its negation
                    String lnegname;
                    if(lname.endsWith("+"))
                        lnegname = lname.substring(0, lname.length()-2)+"-";
                    else
                        lnegname = lname.substring(0, lname.length()-2)+"+";
                    // get the total number of time this literal has been established for this goal
                    double count = entry.getValue();
                    // get the average establishment per step
                    double aEstab = count / tSimNum;
                    // add it to the map
                    if(gpt.averSimEstab.containsKey(lname)){
                        // if it is already in the map, the average is added by the number of the average establishement
                        gpt.averSimEstab.put(lname, gpt.averSimEstab.get(lname) + aEstab);
                    }
                    // otherwise, add the number of average establishment
                    else {
                        gpt.averSimEstab.put(lname, aEstab);
                    }
                    // remove it from the position set
                    posActionPost.remove(lname);
                    posActionPost.remove(lnegname);
                }
                // fragility
                HashMap<String, Integer> frag = goal.getTotSimFrag();
                for(Map.Entry<String, Integer> entry : frag.entrySet()){
                    // get the name of this literal
                    String lname = entry.getKey();
                    // get the total number of steps being fragile
                    double count = entry.getValue();
                    // get the average fragile per step
                    double aFrag = count / tSimNum;
                    // add it to the map
                    if(gpt.averSimFrag.containsKey(lname)){
                        // if it is already in the map, then the value is increased by adding the average fragility
                        gpt.averSimFrag.put(lname, gpt.averSimFrag.get(lname) + aFrag);
                    }
                    // otherwise, only the number of average fragility
                    else {
                        gpt.averSimFrag.put(lname, aFrag);
                    }
                }
                // average length
                double aLength = tSimLength / tSimNum;
                gpt.averSimLength = gpt.averSimLength + aLength;
            }
            // if it is a complete plan
            else if(intention.get(i) instanceof PlanNode){
                // cast it to a plan
                PlanNode planNode = (PlanNode) intention.get(i);
                // get the information for this plan
                // tot simulation
                double tsimnum = planNode.getTotSimNum();
                // tot length
                double tlength = planNode.getTotSimLength();
                // tot fragile steps
                HashMap<String, Integer> tfrag = planNode.getTotSimFrag();
                // tot establishment steps
                HashMap<String, Integer> testab = planNode.getTotSimEstab();

                // fragility
                for(Map.Entry<String, Integer> entry : tfrag.entrySet()){
                    String lname = entry.getKey();
                    double count = entry.getValue();
                    gpt.averSimFrag.put(lname, count/tsimnum);
                }
                // establishment
                for(Map.Entry<String, Integer> entry : testab.entrySet()){
                    String lname = entry.getKey();
                    double count = entry.getValue();
                    gpt.averSimEstab.put(lname, count/tsimnum);
                }
                gpt.averSimLength = tlength / tsimnum;
            }
        }
        //System.out.println();

    }


    /**
     * @param gpts the set of current intentions
     * @param m the number of goals to achieve
     * @return the probability of achieving m goals without causing any potential conflicts given the set of intentions
     */
    public double probConfGPT(GoalPlanTree[] gpts, int m, double gamma){
        double result = 1;

        // generate all possible combinations
        String[] a = new String[gpts.length];
        for(int i = 0; i < a.length; i++){
            a[i] = i + "";
        }
        ArrayList<String> combs = combine(a, m);

        // for each combination find the maximum probability
        for(int i = 0; i < combs.size(); i++){
            // get the indexes
            String[] indexes = combs.get(i).split(" ");

            // generate a list of intentions
            ArrayList<GoalPlanTree> sgpts = new ArrayList<>();
            for(int j = 0; j < indexes.length; j++){
                // get the index and cast it to an integer
                try{
                    int index = Integer.parseInt(indexes[j]);
                    sgpts.add(gpts[index]);
                }catch (Exception e){
                    System.out.println("Casting Error: fail to cast a string to an integer");
                }
            }

            // for the list of gpt, calculate its probability of being conflicts free
            double prob = probConfInts(sgpts);



            // compare the maximum
            if(result > prob)
                result = prob;

            if(result > gamma)
                break;
        }



        return 1 - result;
    }



    /**
     * @param gpts the set of current intentions
     * @param m the number of goals to achieve
     * @return the probability of achieving m goals without causing any potential conflicts given the set of intentions
     */
    public double probConfGPT2(GoalPlanTree[] gpts, int m, double gamma){
        double result = 1;

        int times = gpts.length - m + 1;



        for(int i = 0; i < times; i++){
            ArrayList<GoalPlanTree> sgpts = new ArrayList<>();
            //
            ArrayList<Integer> selected = new ArrayList<>();
            //
            for(int j = 0; j < gpts.length; j++){
                selected.add(j);
            }
            while (selected.size() > m){
                selected.remove(rm.nextInt(selected.size()));
            }

            for(int j = 0; j < selected.size(); j++){
                sgpts.add(gpts[j]);
            }

            // for the list of gpt, calculate its probability of being conflicts free
            double prob = probConfInts(sgpts);

            // compare the maximum
            if(result > prob)
                result = prob;

            if(result > gamma)
                break;
        }

        return 1 - result;
    }

    public double probConfInts(ArrayList<GoalPlanTree> gpts){

        double result = 1;
        // if there is only one intention or less, then no conflicts will happen
        if(gpts.size() <= 1)
            return 1;
        // we need at least two intentions to start comparison
        for(int i = 0; i < gpts.size() - 1; i++){
            for(int j = i + 1; j < gpts.size(); j++){
                // get the probability of conflicts between these two intentions
                double conf = probConf2Ints(gpts.get(i), gpts.get(j));

                // the probability of conflicts free
                result = result * (1 - conf);
            }
        }
        // return the probability of conflicts
        return 1 - result;
    }

    public double probConf2Ints(GoalPlanTree gpt1, GoalPlanTree gpt2){

        //double result = 1;

        double max = 0;
        //double min = 1;
        //double tot = 0;
        //int count = 0;

        // calculate QSI, if it hasn't been calculated yet
        if(gpt1.averSimLength == 0) {
            calIntention(gpt1);
        }
        // similarly, calculate QSI for the second intention
        if(gpt2.averSimLength == 0){
            calIntention(gpt2);
        }

        // get the QSI for the first intention
        HashMap<String, Double> frag1 = gpt1.averSimFrag;
        HashMap<String, Double> estab1 = gpt1.averSimEstab;
        double length1 = gpt1.averSimLength;
//        System.out.println("frag1: " + frag1.size());
//        System.out.println("estab1: " + estab1.size());
//        System.out.println("length1: " + length1);

        // get the QSI for the second intention
        HashMap<String, Double> frag2 = gpt2.averSimFrag;
        HashMap<String, Double> estab2 = gpt2.averSimEstab;
        double length2 = gpt2.averSimLength;
//        System.out.println("frag2: " + frag2.size());
//        System.out.println("estab2: " + estab2.size());
//        System.out.println("length2: " + length2);

        // for each fragile step in the first intention
        for(Map.Entry<String, Double> entry : frag1.entrySet()){
            // get the name
            String lname = entry.getKey();
            // get its negation
            String lnegname;
            if(lname.endsWith("+"))
                lnegname = lname.substring(0, lname.length()-2)+"-";
            else
                lnegname = lname.substring(0, lname.length()-2)+"+";
            // if there is a step in the second intention will potentially destroy the dependency link of l in the first intention
            if(estab2.containsKey(lnegname)){
                double fragValue = frag1.get(lname);
                double estabValue = estab2.get(lnegname);
                // conflicts
                double conflicts = probConfLiteral(length1, length2, fragValue, estabValue);

                if(conflicts > max)
                    max = conflicts;
            }
            // otherwise, ignore it
        }

        // similarly, for each fragile literal in the second intention
        for(Map.Entry<String, Double> entry : frag2.entrySet()){
            // get the name
            String lname = entry.getKey();
            // get its negation
            String lnegname;
            if(lname.endsWith("+"))
                lnegname = lname.substring(0, lname.length()-2)+"-";
            else
                lnegname = lname.substring(0, lname.length()-2)+"+";
            // if there is a step in the first intention will potentially destroy the dependency link of l in the second intention
            if(estab1.containsKey(lnegname)){
                double fragValue = frag2.get(lname);
                double estabValue = estab1.get(lnegname);
                // conflicts
                double conflicts = probConfLiteral(length2, length1, fragValue, estabValue);

                if(conflicts > max)
                    max = conflicts;
            }
        }

        return max;
    }


    public double probConfLiteral(double l1, double l2, double frag, double estab){

        int lj = (int) Math.round(l2);
        int fi = (int) Math.round(frag);
        int ej = (int) Math.round(estab);

        if(lj - ej < fi )
            return 1;


        double v1 = CombinatoricsUtils.binomialCoefficientDouble(lj-ej, fi);
        double v3 = CombinatoricsUtils.binomialCoefficientDouble(lj, fi);

        return 1 - v1 / v3;

    }

    private ArrayList<String> combine(String[] a, int num) {
        ArrayList<String> list = new ArrayList<String>();
        StringBuffer sb = new StringBuffer();
        String[] b = new String[a.length];

        for (int i = 0; i < b.length; i++) {
            if (i < num) {
                b[i] = "1";
            } else
                b[i] = "0";
        }

        int point = 0;
        int nextPoint = 0;
        int count = 0;
        int sum = 0;
        String temp = "1";
        while (true) {

            for (int i = b.length - 1; i >= b.length - num; i--) {
                if (b[i].equals("1"))
                    sum += 1;
            }

            for (int i = 0; i < b.length; i++) {
                if (b[i].equals("1")) {
                    point = i;
                    sb.append(a[point]);
                    sb.append(" ");
                    count++;
                    if (count == num)
                        break;
                }
            }

            list.add(sb.toString());


            if (sum == num) {
                break;
            }
            sum = 0;


            for (int i = 0; i < b.length - 1; i++) {
                if (b[i].equals("1") && b[i + 1].equals("0")) {
                    point = i;
                    nextPoint = i + 1;
                    b[point] = "0";
                    b[nextPoint] = "1";
                    break;
                }
            }

            for (int i = 0; i < point - 1; i++)
                for (int j = i; j < point - 1; j++) {
                    if (b[i].equals("0")) {
                        temp = b[i];
                        b[i] = b[j + 1];
                        b[j + 1] = temp;
                    }
                }

            sb.setLength(0);
            count = 0;
        }
        //
        return list;
    }



}
