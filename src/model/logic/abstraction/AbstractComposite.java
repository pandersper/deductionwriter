package model.logic.abstraction;

import java.util.ArrayList;
import java.util.Collection;

import control.Toolbox;
import model.description.abstraction.Placeholder.Baseline;
import model.description.abstraction.Described;
import model.description.abstraction.Placeholder;
import model.independent.CyclicList;
import model.independent.DoubleArray.Tuple;
import model.logic.Primitive;

/**
 * Provides the functionality of a composite. The only left to implement is the constructor.
 */
public abstract class AbstractComposite extends AbstractFormal {

	/** Iterable list of tuples of primitives description and it's baseline.  **/	
	protected CyclicList<Placeholder> constituents;
	
	/** The bounding and framing component that functions as a backdrop form the other components. */
	protected Placeholder 	frame;
	
	private Placeholder 	last;

	
	/**
	 * The component at the position iterated to. Remember that order is solely for navigation and
	 * is not semantically significant per se.
	 * 
	 * @return The component at the current position.
	 */
	public Described 	current() {		
		return this.constituents.current().described();
	}
	
	/**
	 * Iterates forward one step and returns that position's component. Remember that order is solely 
	 * for navigation and is not semantically significant per se.
	 * 
	 * @return The next component in this composite.
	 */
	public Described 	next() {
			return nextPlaceholder().described();
	}

	/**
	 * Iterates backward one step and returns that position's component.
	 * 
	 * @return The previous component in this composite.
	 */
	public Described 	previous() {
			return previousPlaceholder().described();
	}

	/**
	 * The component and it's baseline at the position iterated to, contained in a placeholder.
	 * Remember that order is solely for navigation and is not semantically significant per se.
	 * 
	 * @return The placeholder of the component at the current position.
	 * 
	 * @see Placeholder
	 */
	public Placeholder 	currentPlaceholder() {
		return this.constituents.current();	
	}

	/**
	 * Iterates forward one step and returns that position's placeholder. Remember that order is solely 
	 * for navigation and is not semantically significant per se.
	 * 
	 * @return The next placeholder in this composite.
	 *
	 * @see Placeholder
	 */
	public Placeholder 	nextPlaceholder() {

		last = this.constituents.next();

		if (last.described() == frame.described())
			last = this.constituents.next();

		return last;
	}

	/**
	 * Iterates backward one step and returns that position's placeholder.
	 * 
	 * @return The previous placeholder in this composite.
	 *
	 * @see Placeholder
	 */
	public Placeholder 	previousPlaceholder() {

		last = this.constituents.previous();

		if (last.described() == frame.described())
			last = this.constituents.previous();

		return last;
	}

	/**
	 * Returns the last positions placeholder.
	 * 
	 * @return The placeholder at the end of this composite.
	 *
	 * @see Placeholder
	 */
	public Placeholder 	lastPlaceholder() {
		return last != null ? last : nextPlaceholder();
	}
	
	
	/**
	 * Resets this composite that is clears it of its constituents.
	 */																																				/**(8E42)**																																				/**(5BB0)**/
 	public void reset() {
		this.constituents.reset();
	}
	
	
	/**
	 * Checks if this composite are currently positioned at it's bounding frame component.
	 * 
	 * @return Wether iteration is currently at the first component, the framing component.
	 */
	public boolean currentIsBounding() {
		return this.constituents.currentIsFirst();
	}

 	/**
 	 * Returns the bounding frame component of this described composite. The baseline of this baseline is always (0,0).
 	 *
 	 * @return The first and bounding framing components placeholder. 
 	 */
 	public Placeholder getFrame() {
		return frame;
	}

	
	/**
	 * Retreives the components and their placeholders. 
	 *
	 * @return The consituents of this composite.
	 */
	public CyclicList<Placeholder> getConstituents() {
		return constituents;
	}

	/**
	 * Checks wether this formal is dummy, that is just a empty formal used for replacal.
	 *
	 * @return Wether this is a dummy formal or not.
	 */
	public boolean isDummy() {
		return this.codepoint == Toolbox.DUMMY.getCodepoint();
	}
	
	/**
	 * Sets the codepoint of this composite. This will alway be home made since UTF standard only includes atomic glyphs not super.constituentss.
	 *
	 * @param codepoint The codepoint to adress to this composite.
	 */
	public void setCodepoint(int codepoint) {
		this.codepoint = codepoint;	
	}

	
	/** {@inheritDoc} */
	public String 		toString() {
		return "C[" + characterString(toFormals(constituents)) + "]";
	}

	/**
	 * Returns this composite's components.
	 *
	 * @return The sub components.
	 */
	public Formal[]		toArray() {
		return (Formal[]) toFormals(constituents).toArray();
	}

	/**
	 * Returns an array of clones of this composite's components. 
	 *
	 * @return the formal[]
	 */
 	protected Formal[]	toCloneArray() {

		ArrayList<Formal> clones = new ArrayList<Formal>();

		for (Formal primitive : toFormals(constituents))
			clones.add(Primitive.makeValue(primitive.getCodepoint()));

		return (Formal[]) clones.toArray();
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
	 * Returns only the described formals of the collection in preserved order. 
	 *
	 * @param layedout 	A collection of described primitives together with their baselines
	 *
	 * @return 			Only the described primitives, in a collection
	 * 
	 * @see Placeholder.Baseline
	 */
	public static Collection<? extends Described> 	toStandardDescriptions(Collection<Tuple<Baseline, Described>> layedout) {
	
		ArrayList<Described> ps = new ArrayList<Described>();
	
		for (Tuple<Baseline, Described> position : layedout)			
			ps.add(position.second());
	
		return ps;
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
	public static Collection<? extends Formal> 		toFormals(Iterable<Placeholder> primitives) {
	
		ArrayList<Formal> ps = new ArrayList<Formal>();
	
		for (Placeholder position : primitives)			
			ps.add(position.described().description().getValue());
	
		return ps;
	}
}
