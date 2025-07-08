package model.description.abstraction;

import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import control.statics.PaintStatics.Size2D;
import model.description.DPrimitive;
import model.description.DRectangle;
import model.logic.Primitive;
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
	/** {@inheritDoc} */
	protected DRectangle 	description;
	
	private boolean 		underlined = false;
	
	
	public void 		draw(Graphics g) {
		description.draw(g,underlined);
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

		return (this.codepoint < UTFMAX) ? this.codepoint == Primitive.DUMMYFORMAL.getCodepoint() : false;
	}
	
	// // //  CONTINUATION  TO DRectanle  - Code reuse has to stand back for interface semantics disciplin // // //
	/** {@inheritDoc} */
	public Formal		value() {
		return description.getValue();
	}
	
	public DRectangle 	description() {	
		return description;		
	}

	
	public Point2D.Double 		getLocalReference() {
		return description.getLocalReferencepoint();
	}
	
	public Point2D.Double 		getWritepoint() {
		return description.getWritepoint();
	}
	/** {@inheritDoc} */
	public void 				setWritepoint(Double writepoint) {

		description.setWritepoint(writepoint);
	}
	
	public void 				setErase() {
		description.setErase();
	}
	
	public double 				getAdvance() {
		return description.getAdvance();
	}
	/** {@inheritDoc} */
	public Rectangle2D.Double 	getBounds() {
		return (Rectangle2D.Double) description.getBounds2D();
	}

	// // //  CONTINUATION END  // // //

	
	public Size2D				getSize() {
		return new Size2D(description);
	}
	
	
	public BufferedImage 				getImage() {
		return description.getImage();
	}
	/** {@inheritDoc} */
	public String 						toString() {
	
		return "D[" + this.value().toString() + ", x=" + this.description.x + ", y=" + this.description.y + "]";
	}
	/** {@inheritDoc} */
	public abstract AbstractDescribed 	clone();
	

	public AbstractDescribed 			scaledClone(double baseline, boolean transparent) {
		
		DPrimitive clone =  new DPrimitive(this.getCodepoint(), baseline, transparent);

		clone.setWritepoint(this.getWritepoint());
	
		return (AbstractDescribed) clone;
	}

	/** {@inheritDoc} */
	public int 							hashCode() {
		return System.identityHashCode(this);
	}
	/** {@inheritDoc} */
	public boolean 	  					equals(Object other) {

		if (other instanceof Described)
			return (((Described) other).hashCode() == this.hashCode());
		else 
			return false;
	}
}

																																	