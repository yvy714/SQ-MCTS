package mcts;

public class Statistic {

    /**
     * the number of times this node has been visited
     */
    public int nVisits;
    /**
     * the total simulation values
     */
    public double totValue;
    /**
     * the total squared simulation values
     */
    public double totSquare;
    /**
     * best simulation result;
     */
    public double best;

    public Statistic(){
        nVisits = 0;
        totValue = 0;
        totSquare = 0;
        best = 0;
    }

    /**
     * update the statistics by adding a
     * @param value
     */
    public void addValue(double value){
        nVisits++;
        totValue += value;
        totSquare += (value*value);
        if(value > best)
            best = value;
    }

    public void addValue(double value, int times){
        nVisits+= times;
        totValue += (value * times);
        totSquare += ((value*value) * times);
        if(value > best)
            best = value;
    }


}
