package environment;

import agent.AbstractAgent;
import goalplantree.ActionNode;
import goalplantree.GoalNode;
import goalplantree.Literal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractEnvironment {

    /**
     * environment states are literal name and value pairs
     */
    Map<String, Boolean> states;
    /**
     * the list of agents that are situated in this environment
     */
    ArrayList<AbstractAgent> agents;
    /**
     * the list of changed variables at current cycle
     */
    ArrayList<Literal> nStates;
    /**
     * new goals for each agent at current cycle
     */
    HashMap<String, ArrayList<GoalNode>> nGoals = new HashMap<>();

    /**
     * constructor
     */
    public AbstractEnvironment(){
        init(null, null);
    }

    /**
     * constructor
     * @param literals the literals in the initial environment
     * @param mean the mean for all poisson processes
     */
    public AbstractEnvironment(ArrayList<Literal> literals, double mean){
        ArrayList<Double> means = new ArrayList<>();
        for(int i = 0; i < literals.size(); i++){
            means.add(mean);
        }
        init(literals, means);
    }

    /**
     * constructor
     * @param literals the literals in the initial environment
     */
    public AbstractEnvironment(ArrayList<Literal> literals){
        this(literals, 0);
    }

    /**
     * constructor
     * @param literals the literals in the initial environment
     * @param means poisson means for each literal
     */
    public AbstractEnvironment(ArrayList<Literal> literals, ArrayList<Double> means){
        init(literals, means);
    }

    /**
     * initialisation
     * @param literals a list of all literals in the environment
     * @param means the corresponding poisson means
     */
    private void init(ArrayList<Literal> literals, ArrayList<Double> means){
        states = new HashMap<>();
        agents = new ArrayList<>();

        if(literals != null && means != null && literals.size() == means.size()){
            for(int i = 0; i < literals.size(); i++){
                // add states
                states.put(literals.get(i).getName(), literals.get(i).getState());
            }
        }
    }

    /**
     * allocate new goals to a particular agent
     * @param goals
     */
    public void postGoals(String id, ArrayList<GoalNode> goals){
        nGoals.put(id, goals);
    }

    /**
     * add a new agent to the environment
     * @param agent
     */
    public void addAgent(AbstractAgent agent){
        this.agents.add(agent);
    }

    /**
     * remove an agent at with a specified index
     * @param index
     */
    public void removeAgentAt(int index){
        agents.remove(index);
    }

    /**
     * remove a specified agent
     * @param id
     */
    public void removeAgent(String id){
        for(int i = 0; i < agents.size(); i++){
            if(agents.get(i).name.equals(id))
                agents.remove(i);
                return;
        }
    }

    /**
     * @param id name of the agent
     * @return the specified agent. null, if there is no such agent.
     */
    public AbstractAgent findAgent(String id){
        for(AbstractAgent a : agents){
            if(a.name.equals(id))
                return a;
        }
        return null;
    }


    /**
     * all the agents in this environment run one cycle
     */
    public boolean run(){
        // new states are initialised
        nStates = new ArrayList<>();
        // a value indicates if all agents stops executing
        boolean stoped = true;
        // for all the agents, run one cycle
        for(AbstractAgent a: agents){

            // agent senses the environment
            a.sense(this);
            // adopt the new goals
            ArrayList<GoalNode> ng = nGoals.remove(a.name);
            a.adoptGoals(ng);

            // agent deliberates the best choice at the current cycle
            boolean executable = a.deliberate();

            System.out.println("executable: "+ executable);
            // if there are executable intentions
            if(executable){
                stoped = false;
                // agent execute the action
                // get the action
                ActionNode act = a.execute(this);

                System.out.println(act == null? "null": act.getName());

                // if an action is selected for execution
                if(act != null){
                    // apply the action to the environment and get the execution result
                    boolean result = executeAction(act);

                    // the agent update its intentions according to the execution results
                    if(result)
                        a.exeSucceed(); // succeeds
                    else
                        a.exeFail();    // fails
                }
                // if a null is return, report the error
                else {
                    System.err.println("null cannot be executed");
                    System.exit(0);
                }

            }
        }
        // the environment changes after all agents executed their actions
        //envChange();

        return !stoped;
    }

    /**
     * execute an action to it
     * @param action
     * @return true if execution is successful; false, otherwise
     */
    private boolean executeAction(ActionNode action){
        // if action is null, return true
        if(action == null)
            return true;
        // get its precondition
        Literal[] prec = action.getPrec();
        // if the precondition holds
        if(evaluate(prec)){
            // get its postcondition
            Literal[] post = action.getPostc();
            apply(post);
            // all the postconditions are added to the changed literals
            for(Literal l : post){
                nStates.add(l);
            }
            return true;
        }else {
            // return false, if it does not hold
            return false;
        }
    }

    /**
     * check if a given literal holds in this environment
     * @param literal a given literal
     * @return true, if it does hold; false, otherwise.
     */
    private boolean evaluate(Literal literal){
        // get the literal name
        String name = literal.getName();
        // check if the value of this literal equals to the value in the environment
        if(states.get(name) == literal.getState())
            return true;
        else
            return false;
    }

    /**
     * check if a given list of literals hold in the environment
     * @param literals a list of literals
     * @return true, if all literal hold; false, otherwise.
     */
    private boolean evaluate(Literal[] literals){
        // if null is given, return true
        if(literals == null)
            return true;
        // check each literal
        for(Literal l : literals){
            // if one of the literals does not hold, return false
            if(!evaluate(l))
                return false;
        }
        // return true if all the literals hold
        return true;
    }

    /**
     * apply a given literal to the current environment
     * @param literal the given literal
     */
    private void apply(Literal literal){
        if(literal == null)
            return;
        // apply the literal
        states.put(literal.getName(), literal.getState());
    }

    /**
     * apply a list of literals to the environment
     * @param literals
     */
    private void apply(Literal[] literals){
        if(literals == null)
            return;
        // apply all literals
        for(Literal l : literals){
            apply(l);
        }
    }

    public ArrayList<Literal> getStates(){
        if(nStates == null)
            return new ArrayList<>();
        else
            return nStates;
    }

    public abstract ArrayList<Literal> getStates(HashMap<String, Double> visions);


    /**
     * @return the string representation of belief base
     */
    public String onPrint(){
        String result = "Environment States = {";
        for(Map.Entry<String, Boolean> b : states.entrySet()){
            result += "(" + b.getKey() + "," + b.getValue() + ");";
        }
        result += " }.\n";
        result += "Poisson Processes = {";
        result += " }.";
        return result;
    }




}
