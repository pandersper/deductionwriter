package control.statics;

import java.awt.geom.Point2D;

import model.description.abstraction.AbstractDComposite;
import model.description.abstraction.Placeholder;
import model.logic.Composite;

/**
 * Class for global static members that is used for debugging.
 */
public final class DebugStatics {
	
	/** Verbosity of debugging - verbose level low on/off. */
	public static final boolean DEBUGVERBOSE = false;
	/** Verbosity of debugging - minimal level low on/off. */
	public static final boolean DEBUGMINIMAL = false;
	
	/**
	 * Used to construct a textual representation to make able storing it in the data base.
	 * @param composite
	 *
	 * @return The string representations of all codepoints, in the order the were added.
	 */
	public static String 	codepointsString(AbstractDComposite composite) {
	
		String output = "";
	
		output += ((Composite) composite.value()).codepointsString(); 	
	
		return output;
	}

	/**
	 * String debug representation of a composite.
	 * 
	 * @param composite	The composite to make a string of.
	 */
	public static String 	allString(AbstractDComposite composite) {
	
		String output = "";
	
		for (Placeholder constituent : composite.getConstituents().values()) {
	
			Point2D.Double location = constituent.described().getWritepoint();
	
			output += (char) constituent.described().getCodepoint() + " @" + location.x +  ":" + location.y +  ":" + constituent.getAdvance() + " ";
		}
	
		output = output.substring(0, output.length() - 1);
	
		return output;
	}
	/**
	 * Used to construct a textual representation to make able storing it in the data base.
	 * @param composite
	 *
	 * @return The string representation of all baselines, in the order it was added. 
	 */
	public static String 	baselinesString(AbstractDComposite composite) {
	
		String output = "";
	
		for (Placeholder constituent : composite.getConstituents().values()) {
	
			Point2D.Double location = constituent.described().getWritepoint();
			
			output += location.x +  ":" + location.y +  ":" + constituent.getAdvance() + " ";
		}
	
		output = output.substring(0, output.length() - 1);
	
		return output;
	}
	/**
	 * For debugging purpose. 
	 * 
	 * @param condition	Condition wether to output messages or not. Output error message otherwise.
	 * @param minimum	Message on minimal verbosity level.
	 * @param verbose	Message on high verbosity level.
	 * @param error		Message to output if condition is not met.
	 */
	public static void 		output(boolean condition, String minimum, String verbose, String error) {
				
		if (condition) {
			if (DebugStatics.DEBUGMINIMAL) System.out.println(minimum);
			if (DebugStatics.DEBUGVERBOSE) System.out.println(verbose);
		} else if (error != null) 
			System.err.println(error);
	}

}
