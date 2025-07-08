package model.description.abstraction;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import control.statics.DebugStatics;
import control.statics.PaintStatics.Size2D;
import model.description.DRectangle;
import model.description.abstraction.Placeholder.Handle;
import model.independent.CyclicMap;
import model.logic.Composite;
import model.logic.abstraction.Formal;

/**
 * An abstract base class for described composites. Extending this class, you must provide
 * graphical rendering {@link renderAndMount()} and constructors.
 * 
 * @see Described
 */
public abstract class AbstractDComposite extends Composite implements Described {
	
	/** Iterable list o tuples of primitives description and it's baseline.  **/	
	protected CyclicMap<Handle, Placeholder> constituents;	
	/** The bounding and framing component that functions as a backdrop form the other components. */
	protected Placeholder 	frameholder, current;	/** The graphical description, the image description of this piece of mathematics. */
	protected DRectangle 	description;

	private boolean underlined = false;

	
	/**
	 * Retreives the components and their placeholders. 
	 *
	 * @return The consituents of this composite.
	 */
	public CyclicMap<Handle, Placeholder> getConstituents() {
		return this.constituents;
	}
		
	
	public void 		draw(Graphics g) {

		Rectangle2D.Double bounds = this.frameholder.described().description();

		Graphics2D g2d = (Graphics2D) g.create(); 
		
		Point2D.Double writepoint = this.getWritepoint();
		
		g2d.translate(writepoint.x,  writepoint.y);
		
		for (Placeholder holder : constituents.sortedValues()) 
			holder.described().draw(g2d);	
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
		// skip the frame itself
		return (current.described() != frameholder.described()) ? current : constituents.next();		
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

		current = constituents.next();

		if (current.described() == frameholder.described())
			current = constituents.next();

		return current;
	}
	/**
	 * Iterates backward one step and returns that position's placeholder.
	 * 
	 * @return The previous placeholder in this composite.
	 *
	 * @see Placeholder
	 */
	public Placeholder 	previousPlaceholder() {

		current = constituents.previous();

		if (current.described() == frameholder.described())
			current = constituents.previous();

		return current;
	}
		
	/** {@inheritDoc} */																																			
	public void 		underline(boolean underline) {
		this.underlined = underline;
	}
	/** {@inheritDoc} */																																			
	public boolean 		isUnderlined() {
		return underlined;
	}
	

	// // //  CONTINUATION  TO DRectangle  - Code reuse has to stand back for interface semantics discipline // // //
	/** {@inheritDoc} */
	public Formal 			value() {
		return description.getValue();
	}		
	/** {@inheritDoc} */
	public DRectangle 		description() {
		return description;
	}
	/** {@inheritDoc} */
	public Point2D.Double 	getLocalReference() {
		
		return description.getLocalReferencepoint();
	}
	/** {@inheritDoc} */																																			
	public Point2D.Double 	getWritepoint() {

		return description.getWritepoint();
	}
	/** {@inheritDoc} */																																			
	public void 			setWritepoint(Point2D.Double location) {
		
		this.description.setWritepoint(location);
	}

	public void 			setErase() {
		description.setErase();
	}
	
	// // //  CONTINUATION END  // // //
	
 	/**
 	 * Returns the bounding frame component of this described composite. The baseline of this baseline is always (0,0).
 	 *
 	 * @return The first and bounding framing components placeholder. 
 	 */
 	public Placeholder 			getFrame() {
		return frameholder;
	}	
 	
	public double 				getAdvance() {
		return frameholder.getAdvance();
	}

	public Rectangle2D.Double 	getBounds() {
		return frameholder.frame();
	}

	public Size2D				getSize() {
		return new Size2D(frameholder.frame());
	}


	public BufferedImage getImage() {
				
		return description.getImage();

	}
/** {@inheritDoc} */	
	public Described 	clone() {
		return null;
	}
	/** {@inheritDoc} */	
	public String 		toString() {

		return "D[" + DebugStatics.allString(this) + "]";

	}
	/** {@inheritDoc} */	
	public int 			hashCode() {
		return (int) this.codepoint;
	}	
}
