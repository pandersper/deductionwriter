package model.description.abstraction;

import java.awt.Point;

import control.Toolbox;
import model.description.DRectangle;
import model.logic.abstraction.AbstractFormal;
import model.logic.abstraction.Formal;				// interface Formal is doubly implemented by AbstractDComposite and AbstractDescribed

/**
 * Provides the basic functionality for classes that meet the {@link model.description.abstraction.Described} interface.
 * Constructors and clone is not provided. 
 * 
 * @see Described
 */
public abstract class AbstractDescribed extends AbstractFormal implements Described {

	/** Baseline length is the basis for scale computation for now. Don't know typography more than so. */
	protected float 		scale = 1.0f;
	private boolean 		underlined = false;

	/** {@inheritDoc} */
	protected DRectangle 	description;

	
	/** {@inheritDoc} */
	public Formal		value() {
		return description.getValue();
	}

	/** {@inheritDoc} */
	public DRectangle 	description() {
		return description;
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
	public Point		getLocation() {
		return description.getLocation();
	}
	
	/** {@inheritDoc} */
	public void 		setLocation(Point location) {
		this.description.setLocation(location.getLocation());
		this.description.translate(Toolbox.negate(this.description.getReference()));
	}

	
	/** {@inheritDoc} */
	public void 		underline(boolean underline) {
		this.underlined = underline;
	}

	/** {@inheritDoc} */
	public boolean 		isUnderlined() {
		return underlined;
	}

	/** {@inheritDoc} */
	public boolean 		isDummy() {

		int UTFMAX = 10000;

		// Composites can't be dummies.		
		return (this.codepoint < UTFMAX) ? this.codepoint == Toolbox.DUMMYCURSOR.getCodepoint() : false;
	}

	
	/** {@inheritDoc} */
	public String 		toString() {
	
		return "D[" + this.value().toString() + ", x=" + this.description.x + ", y=" + this.description.y + "]";
	}

	/** {@inheritDoc} */
	public abstract AbstractDescribed clone();
	
	/** {@inheritDoc} */
	public int 			hashCode() {
		return System.identityHashCode(this);
	}

	/** {@inheritDoc} */
	public boolean 	  	equals(Object other) {

		if (other instanceof Described)
			return (((Described) other).hashCode() == this.hashCode());
		else 
			return false;
	}
}

																																	