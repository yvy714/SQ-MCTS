package goalplantree;


public class ActionNode extends TreeNode {

    /**
     * ActionTemplates: an action contains its precondition, incondition and postcondition
     */
    /**
     * precondition
     */
    final private Literal[] prec;
    /**
     * postcondition
     */
    final private Literal[] postc;


    /**
     * constructor
     * @param name name of the action
     */
    public ActionNode(String name){
        super(name);
        this.prec = new Literal[0];
        this.postc = new Literal[0];
    }

    /**
     * constructor
     * @param name name of the action
     * @param precondition the precondition of this action
     * @param postcondition the postcondition of this action
     */
    public ActionNode(String name, Literal[] precondition, Literal[] postcondition){
        super(name);
        this.prec = precondition == null ? new Literal[0] : precondition;
        this.postc = postcondition == null ? new Literal[0] : postcondition;
    }

    /**
     * @return the precondition of this action
     */
    public Literal[] getPrec(){
            return this.prec;
    }

    /**
     * @return the postcondition of this action
     */
    public Literal[] getPostc(){
            return this.postc;
    }


    public String onPrintNode(int num){
        String result = "Action:[type = " + name +
                        "; status = " + status +
                        "; prec = {";
        if(getPrec().length > 0){
            result += getPrec()[0].onPrintCondition();
            for(int i = 1; i < getPrec().length; i++){
                result += ";" + getPrec()[i].onPrintCondition();
            }
        }

        result += "}; postc = {";
        if(getPostc().length > 0){
            result += getPostc()[0].onPrintCondition();
            for(int i = 1; i < getPostc().length; i++){
                result += ";" + getPostc()[i].onPrintCondition();
            }
        }
        result += "}]";
        return result;
    }
}
