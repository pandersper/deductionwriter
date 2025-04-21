package model.logic;

import model.logic.abstraction.AbstractFormal;

/**
 * The most simple objects fullfilling the Formal interface. 
 * Primitive objects should be used as evanescent <b>values</b> and not as 
 * <b>objects with references</b>.
 * @see model.logic.abstraction.Formal
 */
public final class Primitive extends AbstractFormal {	
		
	
    private Primitive(int codepoint) {	
 
    	super.codepoint = codepoint;
    	
		if (Character.isUnicodeIdentifierPart(codepoint))
			super.name = Character.getName(codepoint); else
			super.name = "<" + codepoint + ">";
    
    }
    
    /**
     * Produces static values of Primitives.
     * @param codepoint	The UTF codpoint that corresponds to this mathematics primitive.
     * @return The mathematics primitive as a value that is not referenceable.
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
 
