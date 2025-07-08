package model.logic;

import model.logic.abstraction.Formal;
import model.logic.abstraction.AbstractFormal;

/**
 * The most simple objects fullfilling the Formal interface. 
 * Primitive objects should be used as evanescent <b>values</b> and not as 
 * <b>objects with references</b>.
 * 
 * @see model.logic.abstraction.Formal
 */
public final class Primitive extends AbstractFormal {	
		
	
	public static final Formal DUMMYFORMAL = new Primitive(-1);

	
    private Primitive() {	
    	 
    	super.codepoint = 0;
    	super.name 		= "BLANK";
		this.type 		= FormalType.UNDEFINED;    
    }

	private Primitive(int codepoint) {	
 
		if (codepoint == -1) {
			
	    	this.codepoint 	= -1;
	    	this.name 		= "DUMMY";
	    	this.type 		= FormalType.OTHER;
			
		} else {
			
	    	boolean valid = Character.isValidCodePoint(codepoint);

	    	super.codepoint = codepoint;
	    	super.name 		= valid ?  Character.getName(codepoint) : "<" + codepoint + ">";
			this.type 		= FormalType.VARIABLE;    
			
		}		
    }
    
    /**
     * Produces static values of Primitives.
     * 
     * @param codepoint		The UTF codpoint that corresponds to this mathematics primitive.
     * 
     * @return 				The mathematics primitive as a value that is not referenceable.
     */
    public final static Primitive makeValue(int codepoint) {		
    	return new Primitive(codepoint);
    }
    
    /**
     * The java text description of this value. 
     * {@linkplain java.lang.Object#toString()}
     */
    public String toString() {
    	
    	return "P[" + super.toString() + "]";
    }
    
}
 
