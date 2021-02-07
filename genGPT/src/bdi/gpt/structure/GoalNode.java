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

package bdi.gpt.structure;
import java.util.ArrayList;

/**
 * @version 2.1
 */
public class GoalNode extends Node 
{
	// Goal -> GoalName {Plans}{Goal-Condition}
	/** associated plans */
	final private ArrayList<PlanNode> plans;

	/** goalConds-condition */
	final private ArrayList<Literal> goalConds;


	/**
	 * constructor
	 * @param name the name of the goal
	 * @param plan the set of associated plans
	 * @param goalConds the goal-condition
	 */
	public GoalNode(String name, ArrayList<PlanNode> plan, ArrayList<Literal> goalConds)
	{
		super(name);
		this.plans = plan;
		this.goalConds = goalConds;
	}

	/**
	 * constructor for empty goal
	 * @param name
	 */
	public GoalNode(String name){
		super(name);
		this.plans = new ArrayList<>();
		this.goalConds = new ArrayList<>();
	}
	
	/** method to return the plans to achieve this goalConds*/
	public ArrayList<PlanNode> getPlans()
	{
		return this.plans;
	}

	/** method to return the plans to achieve this goalConds*/
	public ArrayList<Literal> getGoalConds()
	{
		return this.goalConds;
	}
}
