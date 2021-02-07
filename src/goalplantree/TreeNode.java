package goalplantree;

import java.util.HashMap;

public abstract class TreeNode {

    /**
     * definition of the state
     */
    public enum Status{
        // enumerate status
        SUCCESS("success"),
        FAILURE("failure"),
        ACTIVE("active"),
        DEFAULT("default");

        private String name;
        private Status(String name){
            this.name = name;
        }
        public boolean isDone(){
            return this == SUCCESS || this == FAILURE;
        }

        @Override
        public String toString(){
            return this.name;
        }
    };

    /**
     * the parent node
     */
    protected TreeNode parent;

    /**
     * the name of this node
     */
    protected String name;

    /**
     * the execution status
     */
    protected Status status;
    /**
     * the next step of this node
     */
    protected TreeNode next;

    /**
     * indentation
     */
    final String indent = "    ";


    /**
     * sim info
     */
    /**
     * the total number of fragile steps in all simulation execution
     */
    private HashMap<String, Integer> totSimFrag = new HashMap<>();
    /**
     * the total number of steps establishing a literal l
     */
    private HashMap<String, Integer> totSimEstab = new HashMap<>();
    /**
     * the total length of execution path
     */
    private int totSimLength = 0;
    /**
     * the total number of simulation from this node
     */
    private int totSimNum = 0;

    /**
     * constructor
     * @param name name of this node
     */
    public TreeNode(String name){
        // name
        this.name = name;
        this.status = Status.DEFAULT;
    }

    /**
     * @return the name of this node
     */
    public String getName(){
        return this.name;
    }

    /**
     * @return the parent of this node
     */
    public TreeNode getParent(){
        return this.parent;
    }

    public void setParent(TreeNode node){
        this.parent = node;
    }

    /**
     * @return the current status
     */
    public Status getStatus(){
        return this.status;
    }

    /**
     *
     */
    public void setStatus(Status state){
        this.status = state;
    }

    /**
     * @return the next step in the gpt
     */
    public TreeNode getNext(){
        return this.next;
    }

    /**
     * set the next goal/action
     * @param node
     */
    public void setNext(TreeNode node){
        this.next = node;
    }

    /**
     * @return
     */
    public HashMap<String, Integer> getTotSimEstab(){
        return this.totSimEstab;
    }

    public void setTotSimEstab(HashMap<String, Integer> estab){
        this.totSimEstab = estab;
    }

    /**
     * @return
     */
    public HashMap<String, Integer> getTotSimFrag(){
        return this.totSimFrag;
    }

    public void setTotSimFrag(HashMap<String, Integer> frag){
        this.totSimFrag = frag;
    }

    /**
     * @return the average length of execution path from this node
     */
    public int getTotSimLength(){
        return this.totSimLength;
    }

    public void setTotSimLength(int l){
        this.totSimLength = l;
    }

    public int getTotSimNum(){
        return this.totSimNum;
    }

    public void setTotSimNum(int n){
        this.totSimNum = n;
    }


    /**
     * print the tree node
     * @param num
     * @return
     */
    public abstract String onPrintNode(int num);


}
