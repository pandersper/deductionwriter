package model.logic;

import model.logic.abstraction.AbstractFormal;
import model.logic.abstraction.Formal;

/**
 * Special class for the three elementary deductions.
 * @see ImplicationType
 */
public class Implication extends AbstractFormal {
	
	private ImplicationType implicationtype;   
	
	
	/**
	 *  A deduction of the given type.
	 * @param implicationtype The implication type of three possible.
	 */
	private Implication(ImplicationType implicationtype) {
    
		this.implicationtype = implicationtype;

    	switch (implicationtype) {
    	
			case LEFT:
				super.codepoint = 0x21d0;
				break;
			case RIGHT:
				super.codepoint = 0x21d2;
				break;
			case EQUIV:
				super.codepoint = 0x21d4;
				break;
			default:
				System.err.println("Unknown implication type!");
				break;
    	}
    	
    	super.name = Character.getName(super.codepoint);
    	super.type = FormalType.ARROW;
	}

    /**
     * Produces static values of Implications.
	 * @param type 	The implication type of three possible.
     * 
     * @return 		An implication value.
     */
	public static Implication makeValue(ImplicationType type) {
		return new Implication(type);
	}

	
	/**
	 * The type of this implication.
	 *
	 * @return		The type of this implication. 
	 */
	public ImplicationType getImplicationType() { 
    	return implicationtype; 
    }

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return "I[" + super.toString() + "]";
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object other) {
		return (other instanceof Implication) ? this.getCodepoint() == ((Formal) other).getCodepoint() : false;
	}

	
	/**
	 * The three elementary implications or arrows. <a>&#x21D0;</a>, <a>&#x21D2;</a>, <a>&#x21D4;</a>.
	 */
	public static enum ImplicationType { 
		/** Equivalence */ 
		EQUIV, 
		/** Right, forward implication */
		RIGHT, 
		/** Left, backward implication */
		LEFT 
	}

	/** Equivalence */
	public final static Implication EQUIV  = new Implication(ImplicationType.EQUIV);
	/** Right, forward implication */
	public final static Implication RIGHT  = new Implication(ImplicationType.RIGHT);
	/** Left, backward implication */
	public final static Implication LEFT   = new Implication(ImplicationType.LEFT);

}
