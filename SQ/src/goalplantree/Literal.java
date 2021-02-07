package goalplantree;

public class Literal implements Cloneable{

    /**
     * literal name
     */
    String name;
    /**
     * the state of this condition
     */
    boolean state;

    public Literal(String n, boolean s){
        this.name = n;
        this.state = s;
    }

    /**
     * @return the name of this literal
     */
    public String getName(){
        return this.name;
    }

    /**
     * @return the state of this literal
     */
    public boolean getState(){return this.state;}

    /**
     * check if this condition and the given condition are describing the same thing
     * @param o the given object
     * @return true if two condition are referring to the same literal; false, otherwise.
     */

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Literal literal = (Literal) o;

        return name.equals(literal.name) && (state == literal.state);
    }

    /**
     * @return the string representation of this literal
     */
    public String onPrintCondition(){
        return "(" + name + "," + state + ")";
    }

    /**
     * @return the clone of this condition
     */
    @Override
    public Literal clone(){
        return new Literal(name, state);
    }

    /**
     * @return the stringrization of this literal
     */
    @Override
    public String toString(){
        if(this.state){
            return this.name+"+";
        }else{
            return this.name+"-";
        }
    }

    public String toNegString(){
        if(this.state){
            return this.name+"-";
        }else {
            return this.name+"+";
        }
    }

}
