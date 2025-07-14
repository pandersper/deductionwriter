package model.description;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import control.statics.CharGauge;
import control.statics.PaintStatics;
import model.description.abstraction.Placeholder;
import model.logic.Composite;
import model.logic.Primitive;
import model.logic.abstraction.Formal;

/**
 * A rectangle containg the graphical part (image) of the description of a formal expression. <br><br>
 * It's inherited class is also the sole container of the formal values. A value could have other descriptions but 
 * that should also be objects of this class. That is: don't implement other image structures for formal values but
 * think that they could be used as stack values. <br><br>
 * 
 * The reference point is this glyph's origo and so painting it at it's referencepoint makes it rendered correctly 
 * along  the indended base line. See {@link java.awt.FontMetrics} for info about how this works.
 * 
 * @see model.description.abstraction.Described#description()
 * @see model.description.abstraction.Described#value()
 */
public class DRectangle extends DCursor {
	
	/**
	 * To be used when empty positions are temporarily needed when editing.
	 */
	public static final DRectangle DUMMYRECTANGLE = dummyDescription();

	private	BufferedImage		image, transparent;
	private Rectangle2D.Double	surrounding;			// offset vector plus size

	private boolean background = false;
	
	/**
	 * A default graphical description (glyph) of a formal mathematics primitive. The glyph is rendered by java
	 * according to the standards of UTF.
	 * 
	 * @param primitive	The formal primitive to render an image for.
	 */
    public DRectangle(Formal primitive) {
		this(primitive, new CharGauge(primitive).advance, false);
	}
    /**
     * As previous constructor but scales rendering to fit onto a baseline of given length.
     * 
     * @param primitive	The formal primitive to render an image for.
     * @param baseline	The length of the rendered glyph's baseline.
     * @param transparent If the background should be transparent or not.
     */
 	public DRectangle(Formal primitive, double baseline, boolean transparent) {
 		super(primitive, baseline);

 		this.background 	= transparent;
 		this.image 			= PaintStatics.makeGlyph(primitive.getCodepoint(), baseline, this.background);
 		this.transparent 	= PaintStatics.transparantSurrounding(this.image);			

 		this.surrounding 	= new Rectangle2D.Double(-width/2.0,-height/2.0 , 2*width, 2*height); 		
  	}
	
    /**
     * Constructor adapted for composites that which needs a prerendered total image description preview
     * to hand over to button icons for example. So that it's constituents don't have to be draw every
     * time by components.
     * 
     * CONSIDER: Should frame size be corrected to the image size or be assumed as correct?
     * 
     * @param value	The value this description represents. TO BE REMOVED.
     * @param frame	The frame placeholder carrying the described formal that this drectangle describes.
     * @param fullglyph The image that this drectangle should display.
     */
    public DRectangle(Composite value, Placeholder frame, BufferedImage fullglyph) {
    	super(frame);
    	
    	this.value 			= (Formal) value;
 		this.surrounding 	= new Rectangle2D.Double(-width/2.0d, -height/2.0d , 2*width, 2*height); 		

 		this.image 			= fullglyph;	
 		this.transparent 	= PaintStatics.transparantSurrounding(this.image);			
	}

	 /**
	  * Draws this graphical description. 
	  *    
	  * @param g			The common graphics object.
	  * @param underlined	Sets marker for if the glyph should be underlined.
	  */
	public void draw(Graphics g, boolean underlined) {	

		// the surrounding (outer)
		Graphics2D g2d = (Graphics2D) g.create((int) (x + surrounding.x), (int) (y + surrounding.y), 
											   (int) surrounding.width, (int) surrounding.height);

		if (erase) {	
			g2d.setColor(PaintStatics.BACKGROUND);
			g2d.fill(this);	
			erase = false;
		}
		
		g2d.drawImage(transparent, 0, 0, null, null);
		
		// the glyph (inner)
		g2d = (Graphics2D) g.create((int) x, (int) y, (int) width, (int) height);

		g2d.drawImage(image, 0, 0, null, null);		
							
		underline(underlined, g2d, reference, advance);
		
		g2d.dispose();		
	}
	
	/**
	 * The image of this description. It is the main functionality of these objects but they also carry a lot of gauge
	 * an positional variables.
	 * 
	 * @return The image of this description.
	 */
	public BufferedImage getImage() {
		return image;
	}
	
	/**
	 * Deep clone except for images.
	 */
	public DRectangle clone() {	
 
		DRectangle clone = new DRectangle(value, advance, background);
    	    	
		clone.image 		= this.image;
		clone.transparent 	= this.transparent;
		clone.advance 		= this.advance;
		clone.reference 	= (Point2D.Double) this.reference.clone();
	
		clone.setFrame(this.getFrame());
		
		return clone;
    }
	
	/**
	 * Returns a new object carying only the cursor part of this description. A new downcasted clone.
	 * 
	 * @return A new downcast clone of this description.
	 */
	public DCursor cast() {	
		 
		DCursor cast = new DCursor(value, advance);
    			
       	cast.setWritepoint(this.getWritepoint());
    	
    	return cast;
    }

	
	private static void underline(boolean underlined, Graphics2D gc, Point2D.Double reference, double advance) {

		if (underlined) {
			gc.setColor(Color.green);
			
			gc.fillRect((int) reference.x, (int) reference.y + 1, (int) advance, 1);
		}
	}	

	private final static DRectangle dummyDescription() {
		
		DRectangle dummy 	= new DRectangle(Primitive.makeValue(-1), (int)PaintStatics.AVERAGEADVANCE, false);
		
		dummy.value 		= Primitive.DUMMYFORMAL;

		Rectangle bounds 	= PaintStatics.DUMMYBOUNDS.getBounds();

		dummy.image 		= new BufferedImage(bounds.width, bounds.height,BufferedImage.TYPE_INT_ARGB);
		dummy.transparent 	= PaintStatics.transparantSurrounding(dummy.image);
		dummy.surrounding 	= new Rectangle2D.Double(-bounds.width/2.0d, -bounds.height/2.0d,bounds.width,bounds.height);	// ERRONIOUS		
		 		
		return dummy;
	}
}
