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

import bdi.gpt.structure.Literal;
import java.util.HashMap;

abstract class AbstractGenerator implements GPTGenerator {
    /** Default values */
    static final int def_seed = 100, def_num_tree = 20;


    /** environment */
    HashMap<String, Literal> environment;

    /**
     * Helper function to find, copy, set and return a literal
     * @param id The Literal's id as a string
     * @param state The desired state
     * @return The Literal produced
     */
    Literal produceLiteral(String id, boolean state){
        // Find and copy
        Literal workingLiteral = environment.get(id).clone();
        // Set state
        workingLiteral.setState(state);
        // Return
        return workingLiteral;
    }

}
