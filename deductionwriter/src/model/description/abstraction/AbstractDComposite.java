package model.description.abstraction;

import java.awt.Point;
import java.awt.Rectangle;

import control.Toolbox;
import model.description.DRectangle;
import model.description.abstraction.Placeholder.Baseline;
import model.logic.Composite;
import model.logic.abstraction.AbstractComposite;
import model.logic.abstraction.Formal;

/**
 * An abstract base class for described composites. Extending this class, you must provide
 * graphical rendering {@link renderAndMount()} and constructors.
 * 
 * @see Described
 */
public abstract class AbstractDComposite extends AbstractComposite implements Described {

	private boolean underlined = false;

	/** The graphical description, the image description of this piece of mathematics. */
	protected DRectangle 	description;
	
	/**
	 * Renders a graphical representation for this described composite.
	 * 
	 * @return The bounding area of the graphical description.
	 */
	public abstract Rectangle renderAndMount();

	
	/** {@inheritDoc} */	
	public DRectangle 	description() {
		return description;
	}

	/** {@inheritDoc} */
	public Formal 		value() {
		return (description != null) ? description.getValue() : null;
	}
	
	
	/** {@inheritDoc} */
	public Point 		getLocalReference() {		
		return description.getReference();
	}

	/** {@inheritDoc} */																																			
	public Point 		getGlobalReference() {

		Point offset = description.getReference();
		Point upperleft = description.getLocation();
		upperleft.translate(offset.x, offset.y);
		
		return upperleft;
	}

	/** {@inheritDoc} */																																			
	public Point 		getLocation() {
		return this.description.getLocation();
	}

	/** {@inheritDoc} */																																			
	public void 		setLocation(Point location) {
			
		this.description.setLocation(location);
		this.description.translate(Toolbox.negate(this.description.getReference()));
	}
	
	
	/** {@inheritDoc} */																																			
	public void 	underline(boolean underline) {
		this.underlined = underline;
	}

	/** {@inheritDoc} */																																			
	public boolean 	isUnderlined() {
		return underlined;
	}
	
	
	/**
	 * Used to construct a textual representation to make able storing it in the data base.
	 *
	 * @return The string representation of all baselines, in the order it was added. 
	 */
	public String baselinesString() {

		Baseline frame =  this.frame.baseline();

		String output = "";

		for (Placeholder component : super.constituents) {

			Baseline base = component.baseline();

			output += base.x +  ":" + base.y +  ":" + base.length + " ";
		}

		output = output.substring(0, output.length() - 1);

		return output;
	}
	/**
	 * Used to construct a textual representation to make able storing it in the data base.
	 *
	 * @return The string representations of all codepoints, in the order the were added.
	 */
	public String codepointsString() {

		String output = "";

		output += ((Composite) this.value()).codepointsString(); 	

		return output;
	}

	
	/** {@inheritDoc} */	
	public Described clone() {
		return null;
	}

	/** {@inheritDoc} */	
	public String toString() {

		return "D[" + this.allString() + "]";

	}
	
	/** {@inheritDoc} */	
	public int hashCode() {
		return (int) this.codepoint;
	}	


	private String allString() {

		String output = "";

		for (Placeholder component : super.constituents) {

			Baseline xyb = component.baseline();

			output += (char) component.described().getCodepoint() + " @" + xyb.x +  ":" + xyb.y +  ":" + xyb.length + " ";
		}

		output = output.substring(0, output.length() - 1);

		return output;
	}
	
}
