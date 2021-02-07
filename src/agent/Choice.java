package agent;

public class Choice {
    /** intention choice*/
    public final int intentionChoice;
    /** plan choice*/
    public final int planChoice;

    /**
     * constructor
     * @param ic
     * @param pc
     */
    public Choice(int ic, int pc){
        intentionChoice = ic;
        planChoice = pc;
    }

    public Choice(int ic){
        this(ic, -1);
    }

    /**
     * @return true, if this is a plan choice; false, otherwise.
     */
    public boolean isPlanSelection(){
        if(intentionChoice >= 0 && planChoice >= 0)
            return true;
        else
            return false;
    }

    /**
     * @return true, if it is an intention choice; false, otherwise.
     */
    public boolean isActionExecution(){
        if(intentionChoice >= 0 && planChoice <0)
            return true;
        else
            return false;
    }



}
