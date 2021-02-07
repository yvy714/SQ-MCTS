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
public class ActionNode extends Node{
	// Action -> ActionName {pre-conditions}{post-conditions}
	/** precondition */
	final private ArrayList<Literal> prec;
	
	/** post-condition */
	final private ArrayList<Literal> postc;

	/**
	 * constructor for dummy action
	 * @param name name of the action
	 */
	public ActionNode(String name){
		super(name);
		this.prec = new ArrayList<>();
		this.postc = new ArrayList<>();
	}

	/**
	 * constructor
	 * @param name name of the action
	 * @param precondition precondition of the action
	 * @param postcondition postcondition of the action
	 */
	public ActionNode(String name, ArrayList<Literal> precondition, ArrayList<Literal> postcondition)
	{
		super(name);
		this.prec = precondition;
		this.postc = postcondition;
	}
	
	/** method to return the precondition of this action */
	public ArrayList<Literal> getPreC()
	{
		return this.prec;
	}
	
	/** method to return the postcondition of this action */
	public ArrayList<Literal> getPostC()
	{
		return this.postc;
	}
	
}
