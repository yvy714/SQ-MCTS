/*
 * Copyright 2020 Yuan Yao
 * Zhejiang University of Technology
 * Email: yaoyuan@zjut.edu.cn (yuanyao1990yy@icloud.com)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details
 *  <http://www.gnu.org/licenses/gpl-3.0.html>.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package bdi.gpt.generators;
import bdi.gpt.structure.*;
import java.util.*;

/**
 * @version 2.1
 * @author yuanyao
 *
 */
public class SynthGenerator extends AbstractGenerator {
	/** Default values */
	static final int def_depth = 3,	// maximum depth of the tree
						def_num_goal = 3, // number of subgoals in each plan
						def_num_plan = 3, // number of plans to achieve a goal
						def_num_action = 3, // number of actions in each plan
						def_num_var = 60, // the total number of environment variables
						def_num_selected = 30, // the number of variables selected for each intention
						def_num_literal = 1; // the number of literals in action's pre- and postcondition

	static final double def_lplan = 0.0d; // the probability of a plan being leave plan

	/** id of the tree */
	private int id;

	/** total number of goals in this goal plan tree */
	private int treeGoalCount;

	/** total number of plans in this goal plan tree */
	private int treePlanCount;

	/** total number of actions in this goal plan tree */
	private int treeActionCount;

	/** random generators */
	final private Random rm;

	/** depth of the tree */
	final private int tree_depth;

	/** number of trees */
	final private int num_tree;

	/** number of goals */
	final private int num_goal;

	/** number of plans */
	final private int num_plan;

	/** number of actions */
	final private int num_action;

	/** total number of environment variables */
	final private int num_var;

	/** the number of environment variables that can be used as post-condition of actions **/
	final private int num_sel;

	/** the number of literals appears in actions' pre- and post-condition **/
	final private int num_lit;

	/** probability of a plan being leave plan */
	final private double proPlan;

	/** the set of variables selected*/
	private ArrayList<Integer> selected_indexes;

	/** the set of irrelevant literals*/
	private ArrayList<Literal> is;

	/**
	 * constructor
	 * @param seed random seed
	 * @param tree_depth the depth of the tree
	 * @param num_tree the number of gpts
	 * @param num_goal the number of subgoals in each plan
	 * @param num_plan the number of plans to achieve a goal
	 * @param num_action the number of actions in a plan
	 * @param num_var the total number of environment variables
	 * @param num_sel the number of variables selected for each intention
	 * @param num_lit the number of literals in actions' postcondition
	 * @param lplan the probability of a plan being leave plan
	 */
	SynthGenerator(int seed, int tree_depth, int num_tree, int num_goal, int num_plan, int num_action, int num_var,
				   int num_sel, int num_lit, double lplan)
	{
		this.rm = new Random(seed);
		this.tree_depth = tree_depth;
		this.num_tree = num_tree;
		this.num_goal = num_goal;
		this.num_plan = num_plan;
		this.num_action = num_action;
		this.num_var = num_var;
		this.num_sel = num_sel;
		this.num_lit = num_lit;
		this.proPlan = lplan;
	}

	/**
	 * Generate the initial environment
	 * @return the generated environment*/
	public HashMap<String, Literal> genEnvironment(){
		environment = new HashMap<>();
		Literal workingLit;

		// generate goal literals, all of which are false initially
		for (int i = 0; i < num_tree; i++) {
			workingLit = new Literal("G-" + i, false, false, false);
			environment.put(workingLit.getId(), workingLit);
		}

		// generate all the  environment literals with their initial value, where the values are assigned randomly
		for (int i = 0; i < num_var; i++) {
			// generate random values 
			boolean v = rm.nextBoolean();
			// create the working literal
			workingLit = new Literal("EV-" + i, v, true, false);
			// add it to the environment map
			environment.put(workingLit.getId(), workingLit);
		}
		// return the environment
		return environment;
	}


	/**
	 * A function for producing the top level goals for the GPTs
	 * @param index The index of the top-level goal being produced
	 * @return A Goal Node
	 */
	@Override
	public GoalNode genTopLevelGoal(int index) {
		// Set the generator id
		this.id = index;

		// Set the counters for this tree to 0
		this.treeGoalCount = 0;
		this.treePlanCount = 0;
		this.treeActionCount = 0;

		// Create the Goal Condition
		Literal gc = produceLiteral("G-" + id, true);

		// Randomly select n conditions that can be the post-condition of action in this gpt, where n is the number
		// of selected variables per gpt
		ArrayList<Literal> selected = selectVar(this.num_sel);

		// get the action literals
		ArrayList<Literal> actL = new ArrayList<>(selected.subList(0,this.num_sel * 2));

		// set the irrelevant literals for this gpt
		this.is = new ArrayList<>(selected.subList(this.num_sel * 2,selected.size()));
		// the goal-condition
		ArrayList<Literal> gcs = new ArrayList<>();
		gcs.add(new Literal("G-" + index, true, false, false));
		// create the top-level goal
		GoalNode tpg = createGoal(0, actL, new ArrayList<>(), gcs);
		// return top-level goal
		return tpg;
	}



	/**
	 * select m variables that can appear in actions' postcondition
	 * @return a list of literals, the first 2m of which are the literals that can be used as postcondition of actions in this gpt.
	 * While, the remaining literals cannot. We name these literals as irrelevant, and we only include the irrelevant literals
	 * that are currently true in the environment.
	 */
	private ArrayList<Literal> selectVar(int m){
		// randomly pick m different indexes
		this.selected_indexes = new ArrayList<>();
		// select indexes until it reaches the specified size m
		while (selected_indexes.size() < m){
			// generate a random integer
			int index = rm.nextInt(this.num_var);
			// check if the new integer number is already included
			if(!selected_indexes.contains(index)){
				// add it to the list if it is not
				selected_indexes.add(index);
			}
		}

		// return the corresponding literal in the current environment
		ArrayList<Literal> result = new ArrayList<>();

		// the set of literals that are not selected
		ArrayList<Literal> irr = new ArrayList<>();

		// check each literals
		for(int i = 0; i < this.num_var; i++){
			// if the index of this literal is selected
			if(this.selected_indexes.contains(i)){
				// add it (both positive literal and negative literal) to the selected list
				Literal sl = environment.get("EV-" + i);
				Literal nsl = sl.clone();
				nsl.flip();
				result.add(sl);
				result.add(nsl);
			}
			// otherwise, this literal is categorised as irrelevant
			else {
				// add it to the irrelevant list, we only care about the irrelevant literals that are true
				// in the current environment
				irr.add(environment.get("EV-" + i));
			}
		}
		// Merge these two lists and return the resulting list
		result.addAll(irr);
		return  result;
	}

	/**
	 * a function to recursively create and construct a goal and all its hierarchies below
	 * @param depth current depth
	 * @param as the set of literals could be used as postcondition of actions in this tree
	 * @param ps the possible precondition of the plans to achieve this goal (which are established by its preceding steps)
	 * @param gcs the goal-condition of this goal
	 * @return the constructed goal
	 */
	private GoalNode createGoal(int depth, ArrayList<Literal> as, ArrayList<Literal> ps, ArrayList<Literal> gcs){

		// create an empty goal
		GoalNode goalNode = new GoalNode("T" + this.id + "-G" + this.treeGoalCount++);

		// plans to achieve this goal
		ArrayList<PlanNode> plans = new ArrayList<>();

		// clone the set of irrelevant literals
		// we assume the number of literals in potential is greater than or equals to
		// the number of plans need to be generated
		ArrayList<Literal> potential = (ArrayList<Literal>) is.clone();

		// create p plans
		for(int i = 0; i < this.num_plan; i++){
			// a variable used to check if the current plan is a leave plan
			boolean leave = false;
			// number of environment literal selected for each plan
			int ne = 1;
			// if it reaches the maximum depth of the tree
			if(depth == this.tree_depth -1) {
				// then it is a leave plan
				leave = true;
			}
			// otherwise
			else {
				// create a random probability value
				double xp = rm.nextDouble();
				// if it is smaller than the probability of being a leave node, then the current plan is a leave plan
				if(xp <= this.proPlan){
					// set it as a leave plan
					leave = true;
					// to balance the difficulties in scheduling paths of different length
					// we increase the number of irrelevant precondition for a shallow leave plan
					ne = this.tree_depth - depth;
				}
			}
			// the set of precondition (which is established by earlier steps) for this plan
			ArrayList<Literal> prec = new ArrayList<>(ps);

			// if there are irrelevant precondition left in the set
			if(potential.size() > 0){
				// randomly pick ne irrelevant conditions for each plan
				for (int x = 0; x < ne; x++){
					// randomly select a purely environmental condition, i.e., the irrelevant literal
					int j = rm.nextInt(potential.size());
					// add it to the precondtion of this plan
					Literal tli = potential.get(j);
					prec.add(tli);
					// remove it from the set of possible environmental literals, such that it won't be selected again
					potential.remove(j);
				}
				// add the irrelevant conditions back (for other plans) except the first one, i.e., other plans may have
				// overlapping preconditions but not the same.
				for(int x = 1; x < prec.size(); x++){
					potential.add(prec.get(x));
				}
			}
			// create a plan
			PlanNode plan = createPlan(depth, as, prec, gcs, leave);
			// add it to the list of plans
			plans.add(plan);
		}
		// update the plans to achieve the goal
		goalNode.getPlans().addAll(plans);
		goalNode.getGoalConds().addAll(gcs);
		// return the goal node
		return goalNode;
	}


	/**
	 * a method to create and construct a plan
	 * @param depth the depth of the plan that is located in the hierarchy
	 * @param as the list of literals that can be selected as postcondition of actions
	 * @param prec the precondition of the plan
	 * @param gcs the goal-condition this plan is going to achieve
	 * @param leave a boolean value indicating if this plan is a leave plan
	 * @return a constructed plan node
	 */
	private PlanNode createPlan(int depth, ArrayList<Literal> as, ArrayList<Literal> prec, ArrayList<Literal> gcs, boolean leave){

		// create an empty plan node
		PlanNode planNode = new PlanNode("T" + this.id + "-P" + this.treePlanCount++);
		// create the empty plan body
		ArrayList<Node> planbody = new ArrayList<>();

		// get the number of steps in a plan
		int stepnum;
		// if it is a lave plan then the plan-body only contains actions
		if(leave)
			stepnum = this.num_action;
		// otherwise, it contains both actions and subgoals
		else
			stepnum = this.num_action + this.num_goal;

		// create the list of execution steps, assuming all these steps are actions that have pre- and post-condition
		ArrayList<ActionNode> steps = createPlanBody(stepnum, prec, gcs, as);
		// randomly assign types to each step, i.e., if this step is an action or a subgoal
		ArrayList<Boolean> types = assignPosition(stepnum);
		// calculate the safe conditions for subgoals
		ArrayList<Literal> safeC = safeCondition(steps, as);
		// calculate the postcondition of this plan
		ArrayList<Literal> postc = calPlanPostc(steps, types);

		// create each action and subgoal
		for(int i = 0; i < types.size(); i++){
			// if it is an action
			if(types.get(i)){
				ActionNode actionNode = new ActionNode("T" + this.id + "-A" + this.treeActionCount++,
						steps.get(i).getPreC(), steps.get(i).getPostC());
				planbody.add(actionNode);
			}
			// if it is a subgoal
			else{
				// we need to transform an action to a subgoal
				// the precondition of this action becomes the definite precondition required by the plans to achieve it
				// the post-condition of this action becomes the goal-condition of this subgoal
				GoalNode subgoal = createGoal(depth+1, safeC, steps.get(i).getPreC(), steps.get(i).getPostC());
				planbody.add(subgoal);
			}
		}

		// update the precondition, postcondition and the planbody of this plan
		planNode.getPlanBody().addAll(planbody);
		planNode.getPre().addAll(prec);
		planNode.getPost().addAll(postc);

		return planNode;
	}


	/**
	 * Given the number of execution steps in this plan, the precondition of this plan, the goal-condition this plan
	 * is going to achieve, a list of conditions that can be used as postcondition of actions in this plan, (assuming
	 * this plan is a leave plan) return a sequence of actions that are consistent with the given pre and goal-condition
	 * @param stepNum the number of execution steps in this plan
	 * @param prec the precondition of this plan
	 * @param gcs the goal-condition this plan is going to achieve
	 * @param as a list of conditions that can be used as postcondition of actions in this plan
	 * @return a sequence of actions steps
	 */
	private ArrayList<ActionNode> createPlanBody(int stepNum, ArrayList<Literal> prec, ArrayList<Literal> gcs, ArrayList<Literal> as){

		// create an empty list of actions
		ArrayList<ActionNode> steps = new ArrayList<>();

		// the current states, copied from the precondition of this plan
		ArrayList<Literal> current = new ArrayList<>();
		for(int i = 0; i < prec.size(); i++){
			current.add(prec.get(i));
		}

		// possible action literals copied from as, we also ensure that there is no action make the current state true
		// i.e., it is meaningless if an action has c1 as its postcondition and one of its preceding step also makes c1
		// true (no step between them make c1 false)
		ArrayList<Literal> actionLiteral = new ArrayList<>(as);


		// start creating n actions
		for(int i = 0; i < stepNum; i++){
			// create an empty list to store the precondition of the action
			ArrayList<Literal> precondition = new ArrayList<>();

			// the precondition of the first step is the same as the precondition of this plan
			if(i == 0){
				precondition = prec;
			}
			// if it is not the first action
			else{
				// randomly select m literal from the set of current state, m is the number of literals in an action's
				// pre- and post-condition specified by the user
				while (precondition.size() < this.num_lit){
					// randomly select a literal from the current state
					int sx = rm.nextInt(current.size());
					// ignore the precondition that already appears in its precondition
					while(precondition.contains(current.get(sx))){
						sx = rm.nextInt(current.size());
					}
					// add it to the set of preconditions
					precondition.add(current.get(sx));
				}
			}

			// create an empty list to store the post-condition of the action
			ArrayList<Literal> postcondition = new ArrayList<>();
			// if this step is the last action, then it has the goal-condition as its postcondition
			if(i == stepNum - 1){
				postcondition = gcs;
			}
			// otherwise
			else{
				// the post-conditions are randomly generated
				while(postcondition.size() < this.num_lit){
					// randomly select a postcondition
					int index = rm.nextInt(actionLiteral.size());
					Literal p = actionLiteral.get(index);
					// ensure there is no redundant post-conditions
					while(postcondition.contains(p)){
						index = rm.nextInt(actionLiteral.size());
						p = actionLiteral.get(index);
					}
					// add it to the list of post-condition
					postcondition.add(p);
					// update the current state
					updateCurrentLiterals(current, p);
					// update the set of action literal
					updateActionLiterals(actionLiteral, p);
				}
			}
			// generate an action with specified pre- and post-condition
			ActionNode action = new ActionNode("", precondition, postcondition);
			steps.add(action);
		}

		return steps;
	}

	/**
	 * update the current state based on a literal l. If a literal l(its negation) is in ls, then remove it.
	 * Add l in the tail of this list
	 * @param ls the list of literals that are true in the current state
	 * @param l the new literal
	 */
	private void updateCurrentLiterals(ArrayList<Literal> ls, Literal l){
		// check each literal in the list
		for(int i = 0; i < ls.size(); i++){
			// remove all literal that has the same name with the new literal
			if(ls.get(i).getId().equals(l.getId())){
				ls.remove(i);
				// once this literal is found, jump out the loop
				break;
			}
		}
		// add the new literal to the list
		ls.add(l);
	}





	/**
	 * update the list of action literals. If a literal l is achieved, then remove l from ls and add its negation to ls
	 * @param ls the list of conditions that can be used as the post-condition of actions
	 * @param l the new literal
	 */
	private void updateActionLiterals(ArrayList<Literal> ls, Literal l){

		int index = -1;
		// check each literal
		for(int i = 0; i < ls.size(); i++){
			// find the literal that has the same name with the new literal
			if(ls.get(i).getId().equals(l.getId())){
				// if they are exactly the same
				if(ls.get(i).getState() == l.getState()){
					// if its negation has not been found yet
					if(index == -1)
						index = i;
					// remove it from the list
					ls.remove(i);
					break;
				}
				// if its negation was found
				else{
					index = - 2;
				}
			}
		}
		// continue looking for its negation, if we haven't find it in the first run
		if(index != -2){
			for(int i = index; i < ls.size(); i++){
				if(ls.get(i).getState() == l.getState()){
					index = -2;
					break;
				}
			}
		}
		// if its negation is not included in the current list, then we add it to the list
		if(index != -2) {
			ls.add(new Literal(l.getId(), !l.getState(), l.isStochastic(), l.isRandomInit()));
		}
	}

	/**
	 * assign the types to each execution steps
	 * @param stepNum the total number of execution steps
	 * @return
	 */
	private ArrayList<Boolean> assignPosition(int stepNum){
		// create a new empty list, the element of which are all initially true
		ArrayList<Boolean> positions = new ArrayList<>();
		for(int i = 0; i < stepNum; i++){
			positions.add(true);
		}
		// if this list contains sub-goals
		if(stepNum != this.num_action){
			// the position of subgoal
			ArrayList<Integer> goal_pos = new ArrayList<>();
			// randomly generate m subgoals
			while (goal_pos.size() < this.num_goal){
				// note that the first and the last step are definitely actions
				int index = rm.nextInt(stepNum-2);
				if(!goal_pos.contains(index+1)){
					goal_pos.add(index+1);
					positions.set(index+1,false);
				}
			}
		}
		return positions;
	}

	/**
	 * Given a set of conditions that can be used as the postcondition of actions, a list of execution steps
	 * (i.e., actions for simplicity), calculate the set of conditions that can be safely executed with the list of actions
	 * @param steps a list of actions
	 * @param conds a set of conditions that can be used as postcondition of actions
	 * @return a set of conditions that will not conflicts with
	 */
	private ArrayList<Literal> safeCondition(ArrayList<ActionNode> steps, ArrayList<Literal> conds){
		// clone the current action literals
		ArrayList<Literal> actionLiteral = (ArrayList<Literal>) conds.clone();

		// remove all the conditions in the action literal that conflict with the precondition of the first step
		removeConflicting(actionLiteral, steps.get(0).getPreC());

		// remove all the conditions in the action literal that will potentially conflict with the postcondition of each step
		for(int i = 0; i < steps.size(); i++){
			removeConflicting(actionLiteral, steps.get(i).getPostC());
		}
		return actionLiteral;
	}

	/**
	 * remove all the literals in the first list that will conflict with the literals in the second
	 * @param ls a list of literals
	 * @param l another list of literals
	 */
	private void removeConflicting(ArrayList<Literal> ls, ArrayList<Literal> l){
		// get the size of the first list
		int index = ls.size();
		// for each literal in the second list
		for(int j = 0; j < l.size(); j++){
			// remove literals in the first list that have the same name to avoid conflicts
			for(int i = 0; i < ls.size(); i++){

				if(ls.get(i).getId().equals(l.get(j).getId())) {
					ls.remove(i);
					index = i;
					break;
				}
			}
			//
			for(int i = index; i < ls.size(); i++){
				if(ls.get(i).getId().equals(l.get(j).getId())) {
					ls.remove(i);
					break;
				}
			}
		}
	}


	/**
	 * Given a list of execution steps, and their corresponding types, calculate the
	 * @param actions the list of execution steps
	 * @param types their corresponding execution types
	 * @return the post-condition of this sequence
	 */
	private ArrayList<Literal> calPlanPostc(ArrayList<ActionNode> actions, ArrayList<Boolean> types){
		// return empty list, if the size of actions equals to 0, or the number of actions and their corresponding types
		// do not match
		if(actions.size() == 0 ||  actions.size() != types.size())
			return new ArrayList<>();

		ArrayList<Literal> postc = new ArrayList<>();

		// check each steps, we only consider the post-condition of actions
		for(int i = 0; i < actions.size(); i++){
			// if it is an action
			if(types.get(i)){
				// get its postcondition
				ArrayList<Literal> post = actions.get(i).getPostC();
				// update the postcondition
				for(int j = 0; j < post.size(); j++){
					updateCurrentLiterals(postc, post.get(j));
				}
			}
		}

		return postc;
	}
}
