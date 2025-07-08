package model.logic.abstraction;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import model.description.abstraction.Placeholder;
import model.logic.Primitive;

/**
 * Provides the functionality of a composite. The only left to implement is the constructor.
 * 
 * REMARK ON STRANGE IMPLEMENTATION DECISION: This class carries  <i>a lot of descriptive 
 * functionality</i> in the form of a {@see CyclicList} of {@see PlaceHolder}s on behalf of 
 * its abstract super class {@see AbstractDComposite} just for the sake of code readability 
 * which it never use but only hides away. The most prominent risk is to confuse the 
 * <i>instance composite value</i> variable in the class {@see DComposite} with the the 
 * class {@see Composite} it inherits, that contains another {@see CyclicList} of 
 * {@see PlaceHolder}s that also most often represents the same composite matheamtics value.<br>
 * This is strange and could well be semantically aligned later but makes source files much
 * more maintainable now. 
 */
public abstract class AbstractComposite extends AbstractFormal {

	
	protected LinkedList<Formal> composition = new LinkedList<Formal>();


	public LinkedList<Formal> getComposition() { 
		return composition;
	}
	
	/**
	 * Sets the codepoint of this composite. This will alway be home made since UTF standard only includes atomic glyphs not super.constituentss.
	 *
	 * @param codepoint The codepoint to adress to this composite.
	 */
	public void setCodepoint(int codepoint) {
		this.codepoint = codepoint;	
	}
	/**
	 * Constructs a string of characters from this composite's components codepoints in navigation order. 
	 *
	 * @return A string representation of the constituting formals in navigation order.
	 */
	public String codepointsString() {

		String codepoints = "";

		for (Formal primitive : composition) 		
			codepoints += primitive.getCodepoint() + " ";

		codepoints = codepoints.substring(0, codepoints.length() - 1);

		return codepoints;

	}
	/**
	 * Checks wether this formal is dummy, that is just a empty formal used for replacal.
	 *
	 * @return Wether this is a dummy formal or not.
	 */
	public boolean isDummy() {
		return this.codepoint == Primitive.DUMMYFORMAL.getCodepoint();
	}
	
	/** {@inheritDoc} */
	public String 		toString() {
		return "C[" + this.codepoint + "]";
	}

	/**
	 * A string of characters derived from the components codepoints.
	 *
	 * @param 	subformals The subformals
	 *
	 * @return 	The string
	 * 
	 * Not implemented yet: only works for primitive components. Composites of composites are dealt with later.
	 */
	public static String 	 						characterString(Iterable<? extends Formal> subformals) {

		String cps = "";

		for (Formal formal : subformals) 		
			cps += (char) formal.getCodepoint() + " ";

		cps = cps.substring(0, cps.length() - 1);

		return cps;
	}
	/**
	 * Returns only the undescribed formals of the collection in preserved order. 
	 *
	 * @param primitives 	A collection of formals together with their placeholders
	 * 
	 * @return 				Only the undescribed formals, in a collection in preserved order
	 * 
	 * @see Placeholder
	 */
	public static List<? extends Formal> 			toFormals(Iterable<Placeholder> primitives) {
	
		ArrayList<Formal> ps = new ArrayList<Formal>();
	
		for (Placeholder position : primitives)			
			ps.add(position.described().value());
	
		return ps;
	}
}
