package model.logic.abstraction;

import control.Toolbox;
import model.description.DComposite;
import model.description.DPrimitive;
import model.logic.Composite;
import model.logic.Primitive;

/**
 * Provides the straightforward functionality of a formal. Formals should be treated as values so 
 * constructors are hidden and should be replaced with static factory methods like 
 * {@link model.logic.Primitive#makeValue(int)} in classes that extend it.
 * 
 * If you extend this class, the semantics of the protected fields described in this documentation must be 
 * cared for in your constructor. No automatic things happen.
 */
public abstract class AbstractFormal implements Formal  {	
	
	 /** The UTF codepoint representation of this formal. **/
	protected int codepoint; 
	
	/** The text name of this formal, often same as the name in the UTF standard. **/
	protected String name;
	
	/** The type of this mathematical entity. */
	protected FormalType type = FormalType.UNDEFINED;	
	
	
	/** {@inheritDoc} */	
 	public int 		  getCodepoint() {
		return codepoint;
	}

	/** {@inheritDoc} */	
 	public FormalType getType() {
 		return type;
 	}

	/** {@inheritDoc} */	
    public String 	  getName() { 
    	return name; 
    }

    
	/** {@inheritDoc} */	
	public String 	  toString() {
		return (char) codepoint + "";
	}

	/** {@inheritDoc} */	
	public boolean 	  equals(Object other) {

		if (other instanceof Primitive || other instanceof DPrimitive)
			return (((AbstractFormal) other).getCodepoint() == this.codepoint);

		if (other instanceof Composite || other instanceof DComposite)
			return (((AbstractFormal) other).getCodepoint() == this.codepoint);

		return false;		
	}

	/** {@inheritDoc} */	
	public int 		  compareTo(Formal f) {

		if (Toolbox.DEBUGVERBOSE) System.out.println("AbstractPrimitive comparison");

		Integer thisbox = (Integer) codepoint;
		Integer otherbox = (Integer) f.getCodepoint();							
		
		return thisbox.compareTo(otherbox);

	} 	// FOR USE IN DOUBLE ARRAY ONLY

	/** {@inheritDoc} */	
 	public int 		  hashCode() {
		return codepoint;	
		
	} // FOR USE IN DOUBLE ARRAY ONLY

}
