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
import java.util.ArrayList;
import java.util.HashMap;

import bdi.gpt.structure.GoalNode;
import bdi.gpt.structure.Literal;

/**
 * @version 2.1
 */
class Main
{
	/**
	 * The main function, takes a set of commandline arguments
	 */
	public static void main(String[] args)
	{

		// Shared parameters with their default values
		int seed = AbstractGenerator.def_seed, num_tree = AbstractGenerator.def_num_tree;
		// default path
		String path ="gpt.xml";

		 //Synth parameters with their default values
		int sy_depth = SynthGenerator.def_depth,	// the maximum depth
				sy_num_goal = SynthGenerator.def_num_goal, // the number of subgoals per plan
				sy_num_plan = SynthGenerator.def_num_plan, // the number of plans for each goal
				sy_num_action = SynthGenerator.def_num_action, // the number of actions in each plan
				sy_num_var = SynthGenerator.def_num_var, // the total number of environmental variables
				sy_num_lit = SynthGenerator.def_num_literal, // the number of literals appear in actions' pre- and post-condition
    				sy_num_selected = SynthGenerator.def_num_selected; // the number of variables selected for each gpt

		double sy_prob_lplan = SynthGenerator.def_lplan; // the probability of a plan being leave plan

		// Miconic parameters with their default values
		int mi_num_floor = MiconicGenerator.def_floors, mi_num_pass = num_tree;

		// Logistics parameters with their default values
		int lo_num_stops = LogiGenerator.def_stops, lo_cargo_space = LogiGenerator.def_cargo_space, lo_num_parcels = num_tree;

		double lo_shortcut_factor = LogiGenerator.def_init_shortcut_factor;

		// Block World parameters with their default value
		int bw_num_blocks = BlockWorldGenerator.def_num_block, bw_height = BlockWorldGenerator.def_height;


		GPTGenerator gen;

		if (args.length == 0){
			String help = "\n"+
					"HELP:\n" +
					"synth\n The pure synthetic generator\n" +
					"miconic\n The Miconic-N generator\n" +
					"block\n The Block World generator\n" +
					"logi\n The Logistics generator";

			System.out.println(help);
			return;
		}


		switch(args[0]){
			case "synth": // Synthetic Generator
				{
					String help = "\n" +
						"HELP:\n" +
						"-s\n Random seed. If the value is not specified, 100 is default \n" +
						"-d\n Maximum depth of the goal-plan tree. If the value is not specified, 3 is default.\n" +
						"-g\n Number of subgoals in each plan (except the leaf plan). If the value is not specified, 1 is default.\n" +
						"-p\n Number of plans to achieve each goal. If the value is not specified, 2 is default.\n" +
						"-a\n Number of actions in each plan. If the value is not specified, 1 is default.\n" +
						"-v\n Number of environment variables. If the value is not specified, 60 is default.\n" +
						"-t\n Number of goal-plan trees. If the value is not specified, 1 is default.\n" +
						"-e\n Number of selected literals. If the value is not specified, 30 is default.\n" +
						"-l\n Number of literals per action. If the value is not specified, 1 is default.\n" +
							"-x\n Probability of a plan being leave plan. If the value is not specified, 0 is default\n" +
						"-f\n The output file path to which the set of goal-plan tree is saved. If the value is not specified, gpt.xml is default.\n";

					// parser for the input parameters
					int i = 1;
					String arg;
					// check each flag
					while(i < args.length && args[i].startsWith("-"))
					{
						arg = args[i++];
						// parser to find out the flags
						if(arg.length() != 2)
						{
							System.out.println(arg + " is not a valid flag");
							System.out.println(help);
							System.exit(1);
						}
						// get the flag value
						char flag = arg.charAt(1);
						switch (flag)
						{
							case 'h': // help
								System.out.println(help);
								System.exit(0);

							case 's': // seed
								try
								{
									seed = Integer.parseInt(args[i++]);break;
								}catch(Exception e)
								{
									System.out.println("Seed must be an integer");
									System.exit(1);
								}
							case 'd': // depth
								try
								{
									sy_depth = Integer.parseInt(args[i++]);break;
								}catch(Exception e)
								{
									System.out.println("Depth must be an integer");
									System.exit(1);
								}
							case 'g': // number of goals
								try
								{
									sy_num_goal = Integer.parseInt(args[i++]);break;
								}catch(Exception e)
								{
									System.out.println("The number of goals in each plan must be an integer");
									System.exit(1);
								}
							case 'p': // number of plans
								try
								{
									sy_num_plan = Integer.parseInt(args[i++]);break;
								}catch(Exception e)
								{
									System.out.println("The number of plans to achieve each goal must be an integer");
									System.exit(1);
								}
							case 'a': // number of actions
								try
								{
									sy_num_action = Integer.parseInt(args[i++]);break;
								}catch(Exception e)
								{
									System.out.println("The number of actions in each plan must be an integer");
									System.exit(1);
								}
							case 'v': // number of variables
								try
								{
									sy_num_var = Integer.parseInt(args[i++]);break;
								}catch(Exception e)
								{
									System.out.println("The number of environment variables must be an integer");
									System.exit(1);
								}
							case 'e': // selected number of variables
								try
								{
									sy_num_selected = Integer.parseInt(args[i++]);break;
								}catch(Exception e)
								{
									System.out.println("The number of selected environment variables must be an integer");
									System.exit(1);
								}
							case 'l': // number of literal per action
								try
								{
									sy_num_lit = Integer.parseInt(args[i++]);break;
								}catch(Exception e)
								{
									System.out.println("The number literals per action must be an integer");
									System.exit(1);
								}
							case 't': // number of trees
								try
								{
									num_tree = Integer.parseInt(args[i++]);break;
								}catch(Exception e)
								{
									System.out.println("The number of goal-plan tree must be an integer");
									System.exit(1);
								}
							case 'x': // probability of a plan being leave plan
								try{
									sy_prob_lplan = Double.parseDouble(args[i++]);
									if (sy_prob_lplan < 0 || sy_prob_lplan > 1){
										throw (new IllegalArgumentException());
									}
									break;
								}catch (Exception e){

								}
							case 'f': // path
								path = args[i++];break;

							default:
								System.out.println(arg + " is not a valid flag");
								System.out.println(help);
								System.exit(1);
						}
					}


					// check the value of the input arguments
					if(sy_depth <= 0)
					{
						System.out.println("Depth must be greater than 0");
						System.exit(1);
					}
					if(sy_num_goal <= 0)
					{
						System.out.println("Maximum number of goals must be greater than 0");
						System.exit(1);
					}
					if(sy_num_plan <= 0)
					{
						System.out.println("Maximum number of plans must be greater than 0");
						System.exit(1);
					}
					if(sy_num_action < 0)
					{
						System.out.println("Maximum number of actions must be greater than 0");
						System.exit(1);
					}
					if(sy_num_var <= 0)
					{
						System.out.println("Total number of variables must be greater than 0");
						System.exit(1);
					}
					// need to find more
					if(sy_num_selected <= 0)
					{
						System.out.println("The number of selected variables must be greater than 0");
						System.exit(1);
					}
					if(sy_num_selected > sy_num_var)
					{
						System.out.println("The number of selected variables must be less than or equal to the total number of variables");
						System.exit(1);
					}
					if(sy_num_lit <= 0){
						System.out.println("The number of literals per action must be greather than 0");
						System.exit(1);
					}
					if(num_tree <= 0)
					{
						System.out.println("Total number of goal-plan tree must be greater than 0");
						System.exit(1);
					}
					if(sy_prob_lplan < 0 || sy_prob_lplan > 1){
						System.out.println("probability of a plan being leave plan must be between 0 and 1");
						System.exit(1);
					}

					System.out.println("seed: " + seed);
					System.out.println("depth: " + sy_depth);
					System.out.println("tree: " + num_tree);
					System.out.println("goals: " + sy_num_goal);
					System.out.println("plans: " + sy_num_plan);
					System.out.println("actions: " + sy_num_action);
					System.out.println("var: " + sy_num_var);
					System.out.println("selected: " + sy_num_selected);
					System.out.println("literals: " + sy_num_lit);
					System.out.println("lplan: " + sy_prob_lplan);

					gen = new SynthGenerator(seed, sy_depth, num_tree, sy_num_goal, sy_num_plan, sy_num_action,
							sy_num_var, sy_num_selected, sy_num_lit, sy_prob_lplan);

					break;
				}
			case "miconic": // Miconic-N Generator
			{
				String help = "\n" +
						"HELP:\n" +
						"-s\n Random seed. If the value is not specified, 100 is default \n" +
						"-p\n Number of passengers(goals) to be used in the GPT. If the value is not specified, 10 is default.\n" +
						"-l\n Number of floors to be used in the GPT. If the value is not specified, 10 is default.\n" +
						"-f\n The output file path to which the set of goal-plan tree is saved. If the value is not specified, gpt.xml is default.\n";

				// parser for the input parameters
				int i = 1;
				String arg;
				while(i < args.length && args[i].startsWith("-"))
				{
					arg = args[i++];
					if(arg.length() != 2)
					{
						System.out.println(arg + " is not a valid flag");
						System.out.println(help);

						System.exit(1);
					}
					char flag = arg.charAt(1);
					switch (flag)
					{
						case 'h': // help
							System.out.println(help);
							System.exit(1);

						case 's': // seed
							try
							{
								seed = Integer.parseInt(args[i++]);break;
							}catch(Exception e)
							{
								System.out.println("Seed must be an integer");
								System.exit(1);
							}
						case 'p': // passengers
							try
							{
								mi_num_pass = Integer.parseInt(args[i++]);
								num_tree = mi_num_pass;break;
							}catch(Exception e)
							{
								System.out.println("Passengers must be an integer");
								System.exit(1);
							}
						case 'l': // number of floors
							try
							{
								mi_num_floor = Integer.parseInt(args[i++]);break;
							}catch(Exception e)
							{
								System.out.println("The number of floors must be an integer");
								System.exit(1);
							}
						case 'f': // path
							path = args[i++];break;
						default:
							System.out.println(arg + " is not a valid flag");
							System.out.println(help);
							System.exit(1);
					}
				}

				// check the value of the input arguments
				if(mi_num_pass <= 0)
				{
					System.out.println("Passengers must be greater than 0");
					System.exit(1);
				}
				if(mi_num_floor <= 1)
				{
					System.out.println("Maximum number of floors must be greater than 1");
					System.exit(1);
				}

				gen = new MiconicGenerator(seed, mi_num_floor, mi_num_pass);
				break;
			}
			case "logi": // Logistics Generator
			{
				String help = "\n" +
						"HELP:\n" +
						"-s\n Random seed. If the value is not specified, 100 is default \n" +
						"-p\n Number of parcels(goals) to be used in the GPT. If the value is not specified, 10 is default.\n" +
						"-t\n Number of stops to be used in the GPT. If the value is not specified, 10 is default.\n" +
						"-a\n Number of parcels that the transport can carry. If the value is not specified, 2 is default.\n" +
						"-f\n The output file path to which the set of goal-plan tree is saved. If the value is not specified, gpt.xml is default.\n";

				// parser for the input parameters
				int i = 1;
				String arg;
				while(i < args.length && args[i].startsWith("-"))
				{
					arg = args[i++];
					if(arg.length() != 2)
					{
						System.out.println(arg + " is not a valid flag");
						System.out.println(help);

						System.exit(1);
					}
					char flag = arg.charAt(1);
					switch (flag)
					{
						case 'h': // help
							System.out.println(help);
							System.exit(0);

						case 's': // seed
							try
							{
								seed = Integer.parseInt(args[i++]);break;
							}catch(Exception e)
							{
								System.out.println("Seed must be an integer");
								System.exit(1);
							}
						case 'p': // passengers
							try
							{
								lo_num_parcels = Integer.parseInt(args[i++]);
								num_tree = lo_num_parcels;break;
							}catch(Exception e)
							{
								System.out.println("PArcels must be an integer");
								System.exit(1);
							}
						case 't': // number of stops
							try
							{
								lo_num_stops = Integer.parseInt(args[i++]);break;
							}catch(Exception e)
							{
								System.out.println("The number of stops must be an integer");
								System.exit(1);
							}
						case 'a': // cargo space
							try
							{
								lo_cargo_space = Integer.parseInt(args[i++]);break;
							}catch(Exception e)
							{
								System.out.println("The cargo space must be an integer");
								System.exit(1);
							}
						case 'c': // shortcut factor
							try
							{
								lo_shortcut_factor = Double.parseDouble(args[i++]);break;
							}catch(Exception e)
							{
								System.out.println("The shortcut factor of stops must be a double");
								System.exit(1);
							}
						case 'f': // path
							path = args[i++];break;
						default:
							System.out.println(arg + " is not a valid flag");
							System.out.println(help);
							System.exit(1);
					}
				}

				// check the value of the input arguments
				if(lo_num_parcels <= 0)
				{
					System.out.println("Parcels must be greater than 0");
					System.exit(1);
				}
				if(lo_num_stops <= 4)
				{
					System.out.println("Number of stops must be greater than 4");
					System.exit(1);
				}
				if(lo_cargo_space < 1)
				{
					System.out.println("Cargo space must be greater than 0");
					System.exit(1);
				}
				if(lo_shortcut_factor < 0 || lo_shortcut_factor > 1){
					System.out.println("Shortcut factor must be between 0 and 1");
					System.exit(1);
				}

				gen = new LogiGenerator(seed, lo_num_stops, lo_num_parcels, lo_cargo_space, lo_shortcut_factor);
				break;
			}
			case "block": // BlockWord Generator
			{
				String help = "\n" +
						"HELP:\n" +
						"-s\n Random seed. If the value is not specified, 100 is default \n" +
						"-t\n Number of trees to be used in the GPT. If the value is not specified, 10 is default.\n" +
						"-b\n Number of blocks to be used in the GPT. If the value is not specified, 10 is default.\n" +
						"-e\n height of the towers to be made in the GPT. If the value is not specified, 4 is default.\n" +
						"-f\n The output file path to which the set of goal-plan tree is saved. If the value is not specified, gpt.xml is default.\n";

				// parser for the input parameters
				int i = 1;
				String arg;
				while(i < args.length && args[i].startsWith("-"))
				{
					arg = args[i++];
					if(arg.length() != 2)
					{
						System.out.println(arg + " is not a valid flag");
						System.out.println(help);

						System.exit(1);
					}
					char flag = arg.charAt(1);
					switch (flag)
					{
						case 'h': // help
							System.out.println(help);
							System.exit(1);

						case 's': // seed
							try
							{
								seed = Integer.parseInt(args[i++]);break;
							}catch(Exception e)
							{
								System.out.println("Seed must be an integer");
								System.exit(1);
							}
						case 't': // trees
							try
							{
								num_tree = Integer.parseInt(args[i++]);break;
							}catch(Exception e)
							{
								System.out.println("The number of trees must be an integer");
								System.exit(1);
							}
						case 'b': // number of blocks
							try
							{
								bw_num_blocks = Integer.parseInt(args[i++]);break;
							}catch(Exception e)
							{
								System.out.println("The number of floors must be an integer");
								System.exit(1);
							}
						case 'e': // number of blocks
							try
							{
								bw_height = Integer.parseInt(args[i++]);break;
							}catch(Exception e)
							{
								System.out.println("The height must be an integer");
								System.exit(1);
							}
						case 'f': // path
							path = args[i++];break;
						default:
							System.out.println(arg + " is not a valid flag");
							System.out.println(help);
							System.exit(1);
					}
				}

				// check the value of the input arguments
				if(num_tree <= 0)
				{
					System.out.println("Trees must be greater than 0");
					System.exit(1);
				}
				if (bw_height < 2){
					System.out.println("Height must be greater than 1");
					System.exit(1);
				}
				if(bw_num_blocks < bw_height)
				{
					System.out.println("The number of blocks must be at least height.");
					System.exit(1);
				}

				gen = new BlockWorldGenerator(seed, bw_num_blocks, bw_height);
				break;
			}
			default:
				String help = "\n"+
					"HELP:\n" +
							"synth\n The pure synthetic generator\n" +
							"miconic\n The Miconic-N generator\n" +
							"block\n The Block World generator\n" +
							"logi\n The Logistics generator";

				System.out.println(help);
				return;
		}

		// generate the environment
		HashMap<String, Literal> environment = gen.genEnvironment();
		// generate the tree
		ArrayList<GoalNode> goalForests = new ArrayList<>();

		System.out.println(num_tree);
		for (int k = 0; k < num_tree; k++)
		{
			goalForests.add(gen.genTopLevelGoal(k));
		}

		// write the set of goal plan tree to an XML file
		XMLWriter wxf = new XMLWriter();
		wxf.CreateXML(environment, goalForests, path);
	}

	
}
