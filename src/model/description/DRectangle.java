package model.description;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
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
 * A rectangle containg the graphical part (image) of the description of a formal expression. It also
 * is and should be <i>the only class for containers of the value</i> it describes and that so all along the described 
 * theorem and it's statements. The value could have other descriptions but then those should also be <i>DRectangles</i>.
 * That is: don't implement other image structures for formal values. So, the value semantics are contained, by static 
 * storage on the stack, in these description rectangles. 
 * 
 * The reference point is this glyph's origo and so painting it at it's referencepoint makes it rendered correctly along 
 * the indended base line. See {@link java.awt.FontMetrics} for info about how this works.
 * 
 * @see model.description.abstraction.Described#description()
 * @see model.description.abstraction.Described#value()
 */
public class DRectangle extends DCursor {
	
	
	public static final DRectangle DUMMYRECTANGLE = dummyDescription();
	
	
	private	BufferedImage	image, transparent;

	private Rectangle2D.Double	surrounding;			// offset vector plus size

	private boolean background = false;
	/** For later use: affine transform to further manipulate the glyph. */
	protected static AffineTransform	transform = null;								
	
		/**
	 * A default graphical description (glyph) of a formal mathematics primitive. The glyph is rendered by the 
	 * typographical standards of UTF.
	 * 
	 * @param primitive	The formal primitive to render an image for.
	 */
    public DRectangle(Formal primitive) {
		this(primitive, new CharGauge(primitive).advance, false);
	}
    /**
     * As previous constructor but scales rendering so as to fit onto a baseline.
     * 
     * @param primitive	The formal primitive to render an image for.
     * @param baseline	The length of the rendered glyph's baseline.
     * @param transparent TODO
     */
 	public DRectangle(Formal primitive, double baseline, boolean transparent) {
 		super(primitive, baseline);

 		this.background 	= transparent;
 		this.image 			= PaintStatics.makeGlyph(primitive.getCodepoint(), baseline, this.background);
 		this.transparent 	= PaintStatics.transparantSurrounding(this.image);			

 		this.surrounding 	= new Rectangle2D.Double(-width/2.0,-height/2.0 , 2*width, 2*height); 		
  	}
	
    public DRectangle(Composite value, Placeholder frame, BufferedImage fullglyph) {
    	super(frame);
    	
    	this.value 			= (Formal) value;
 		this.image 			= fullglyph;
 		
// 		int width  = this.image.getWidth();
// 		int height = this.image.getHeight();
// 		
 		this.surrounding 	= new Rectangle2D.Double(-width/2.0d, -height/2.0d , 2*width, 2*height); 		
 		this.transparent 	= PaintStatics.transparantSurrounding(this.image);			
	}

	    
	public void draw(Graphics g, boolean underlined) {	

		// the surrounding (outer)
		Graphics2D g2d = (Graphics2D) g.create((int)(x + surrounding.x), (int)(y + surrounding.y), (int)surrounding.width, (int)surrounding.height);

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
