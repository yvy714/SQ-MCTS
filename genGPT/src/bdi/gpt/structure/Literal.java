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

/**
 * @version 2.1
 */
public class Literal implements Comparable<Literal> {

	/**
	 * id
	 */
	final private String id;
	/**
	 * state
	 */
	private boolean state;
	final private boolean stochastic;
	final private boolean randomInit;


	/**
	 * constructor
	 * @param id the name of this literal
	 * @param state the value of this literal
	 * @param stochastic if it is stochastic
	 * @param randomInit if it is randomly initialised
	 */
	public Literal(String id, boolean state, boolean stochastic, boolean randomInit) {
		this.id = id;
		this.state = state;
		this.stochastic = stochastic;
		this.randomInit = randomInit;
	}

	/**
	 * method to get the name of this literal
	 * @return the name of this literal
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * method to get the state of this literal
	 * @return the state of this literal
	 */
	public boolean getState() {
		return state;
	}

	/**
	 * method to check if this literal is stochastic
	 * @return true, if it is stochastic; false, otherwise
	 */
	public boolean isStochastic() {
		return stochastic;
	}

	/**
	 * method to check if this literal is randomly initialised
	 * @return true, if it is randomly initialised; false, otherwise
	 */
	public boolean isRandomInit() {
		return randomInit;
	}

	/**
	 * method to set the current state of this literal
	 * @param state the current state
	 */
	public void setState(boolean state) {
		this.state = state;
	}

	/**
	 * method to flip the value of this literal
	 * @return true, if the current value is false; false, otherwise.
	 */
	public boolean flip(){
		this.state = !this.state;
		return this.state;
	}

	/**
	 * check if this literal equals to the given object
	 * @param o
	 * @return true, if they are the same thing; false, otherwise
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Literal literal = (Literal) o;
		return id.equals(literal.id);
	}

	/**
	 * override the compareto method
	 * @param o
	 * @return
	 */
	@Override
	public int compareTo(Literal o) {
		return this.getId().compareTo(o.getId());
	}

	/** write the literal*/
	public String toSimpleString()
	{
		return "(" + this.id + "," + this.state + ")";
	}

	@Override
	public Literal clone(){
		return new Literal(id, state, stochastic, randomInit);
	}
}
