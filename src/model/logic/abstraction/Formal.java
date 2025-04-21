package model.logic.abstraction;

/**
 * Formal representation of a mathematical 'primitive', an 'atomary entity', an undivisible mathematical 'thing' . 
 */
public interface Formal extends Comparable<Formal> {
    
	/**
	 * The UTF codepoint representation of this formal.
	 * 
	 * @return	The UTF-16 code point.
	 */
 	public int 			getCodepoint();

 	/**
 	 * The type, that is category of this mathematical entity.

 	 * @return	The type or category of this formal.
 	 */
 	public FormalType 	getType();
 	
 	/**
 	 * The text name of this formal, often same as the name in the UTF standard.
 	 * 
 	 * @return	This formals name.
 	 */
    public String 		getName();
    
	/**
	 * Tests for equality with value semantics, not by comparing references. Equal, not same.
	 * 
	 * @see java.lang.Object#equals(Object)
	 * @see java.lang.Integer#equals(Object)
	 */
	public boolean 	equals(Object o);

	/**
	 * A single UTF char derived from the codepoint embedded into a string of length one.
	 */
	public String 	toString();
	
	
	public default long concistencyNumber() {
	    	return (long) getCodepoint();
	    }
	
	/**
	 * Enumeration of the allowed types (categories) of mathematics that can be represented in this application. 
	 */
	public static enum FormalType { 
		
		/** Variables of all sorts. */
		VARIABLE(0), 
		/** Operators operating on sets. */
		OPERATOR(1), 
		/** Sets of all sorts. */
		SET(2), 
		/** Never changing math values. */
		CONSTANT(3), 
		/** For things not discoverd yet. */
		OTHER(4),
		/** Deduction symbols used between statements.  */
		ARROW(5), 
		/** Composition of different formals mentioned here. */
		COMPOSITE(6), 
		/** Formals not assigned or defined yet. */
		UNDEFINED(-1); 

		FormalType(int type) { }
			
		/**
		 * Returns the currently defined types, the categories this software currently is modelling.
		 * 
		 * @return 	A constant array of the types of mathematics currently in use. 
		 */
		public final static FormalType[] definedValues() { 
			return new FormalType[] { VARIABLE, OPERATOR, SET, CONSTANT, OTHER, ARROW }; 
		}
	}
}
