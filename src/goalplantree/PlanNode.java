package goalplantree;

public class PlanNode extends TreeNode {

    /**
     * precondition
     */
    final private Literal[] prec;

    /**
     * plan body
     */
    final private TreeNode[] body;


    /**
     * initialisation with an empty plan with no precondition
     * @param name name of the plan
     */
    public PlanNode(String name){
        super(name);
        this.prec = new Literal[0];
        this.body = new TreeNode[0];
    }

    /**
     * initialisation
     * @param name name of the plan
     * @param preconditions precondition of the plan
     * @param nodes planbody of the plan
     */
    public PlanNode(String name, Literal[] preconditions, TreeNode[] nodes){
        super(name);
        this.prec = preconditions == null ? new Literal[0] : preconditions;
        this.body = nodes == null ? new TreeNode[0] : nodes;
        init();
    }

    private void init(){
        // set the parent-child relationship
        for(int i = 0; i < this.body.length; i++){
            body[i].setParent(this);
        }
        // set the next step relationship
        for(int i = 0; i < this.body.length-1; i++){
            body[i].setNext(body[i+1]);
        }
    }


    /**
     * @return the precondition of this plan
     */
    public Literal[] getPrec(){
        return this.prec;
    }


    /**
     * @return the plan body of this plan
     */
    public TreeNode[] getPlanbody(){
        return body;
    }

    /**
     * @return if this plan is empty
     */
    public boolean isEmpty(){
        return getPlanbody().length == 0;
    }






    @Override
    public String onPrintNode(int num){
        String result = "Plan:[type = " + name +
                        "; status = " + status +
                        "; prec = {";
        for(int i = 0; i < prec.length; i++){
            result += "(" + prec[i].getName() + "," + prec[i].getState() + ");";
        }
        result += "}; planbody = {";

        if(!isEmpty()){
            result += (getPlanbody()[0].getName());
        }
        for(int i = 1; i < getPlanbody().length; i++){
            result += (";" + getPlanbody()[i].getName());
        }
        result += "}]";


        for(int i = 0; i < getPlanbody().length; i++){
            result += "\n";
            for(int j = 0; j < num + 1; j++){
                result += indent;
            }
            result += getPlanbody()[i].onPrintNode(num + 1);
        }

        return result;
    }


}
