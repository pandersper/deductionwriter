package model.description;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import control.Toolbox;
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
public class DRectangle extends Rectangle {

	/** This glyphs holds a copy of the formal value it describes. */
	private Formal	 		value;	
	/** The glyph image of the description. */
	private	BufferedImage	image;
	/** The advancing length in pixels when typing this mathematics. */
	private	int				advance;
	/** Point pointing to the start of the base line. */
	private	Point			reference;		// STORED IN DB.

	/** For later use: affine transform to further manipulate the glyph. */
	protected static AffineTransform	transform = null;								


    /**
     * As previous constructor but scales rendering so as to fit onto a baseline.
     * 
     * @param primitive	The formal primitive to render an image for.
     * @param baseline	The length of the rendered glyph's baseline.
     */
 	public DRectangle(Formal primitive, int baseline) {

 		this.value 		= primitive;
 		this.advance 	= baseline;

 		this.image 		= render(baseline, primitive.getCodepoint());
 		this.reference 	= referencePoint(primitive.getCodepoint(), this.image);

 		int height 		= image.getHeight(null);
 		int width  		= this.advance;

 		super.setLocation(Toolbox.negate(this.reference));
 		super.setSize(new Dimension(width, height));
 	}
 	
	/**
	 * A default graphical description (glyph) of a formal mathematics primitive. The glyph is rendered by the 
	 * typographical standards of UTF.
	 * 
	 * @param primitive	The formal primitive to render an image for.
	 */
    public DRectangle(Formal primitive) {
		this(primitive, Toolbox.advance((char) primitive.getCodepoint()));
	}
    
	/**
	 * An empty graphical description.
	 */
    public DRectangle() {
    	this(Toolbox.DUMMY);
    }

    
	/**
	 * The value that this object describes graphically.
	 * 
	 * @return A formal mathematical primitive value.
	 */
	public Formal getValue() {
    	return value;
    }
	
	/**
	 * This graphical description as an buffered image.
	 * 
	 * @return The image with this rectangle's bounds depicting the glyph.
	 */
    public BufferedImage getImage() { 
    	return image; 
    }

    
    /**
     * Set the location of this description - it's upper left corner.
     */
    public void setLocation(Point location) {
    	super.setLocation(location);
    }
    
	/**
	 * Translates this descriptions image by the vector given. The vector is given as a point.
	 * 
	 * @param vector	Translates this description by a the vector. 
	 */
    public void translate(Point vector) {
    	super.translate(vector.x,  vector.y);
    }
    
    
	/**
	 * Return the length that this advances the cursor.
	 * 
	 * @return 	The number of pixels that this glyph proceed the cursor.
	 *
	 * @see java.awt.FontMetrics
	 */
	public int getAdvance() {
		return advance;
	}

	/**
	 * Gives the typographical reference point of this glyph, that points to the beginning of it's base line.
	 * 
	 * @return 	The first point of this glyph's baseline.
	 * 
	 * @see java.awt.FontMetrics
	 */
	public Point getReference() {
		return reference;
	}

	    
    /**
     * Provides the possibility to replace this descriptions image. Will probably be removed.
     * 
     * @param image		The image of this description frame. This method exist for the sake of
     * 					describing composites since they are often rerendered when constructed.
     * 					It should not be used by primitives which are rendered according standards.
     */
	public void setImage(BufferedImage image) {
		this.image = image;		
	}
		
	/**
	 * Tells wether this description is rendered or not in its current state.
	 * 
	 * @return True or false wether rendered or not.
	 */
	public boolean isRendered() {	
		return image != null;
	}
	
	
	/**
	 * Deep clone except for images.
	 */
	public DRectangle clone() {	
 
		DRectangle clone = new DRectangle(value, getAdvance());
    	
		clone.image = this.image;
    	clone.transform = (transform != null) ? (AffineTransform) this.transform.clone() : null;

    	clone.setLocation(this.getLocation());
    	
    	return clone;
    }

	
	/**
	 * Computes the reference point of a glyph. For now only the image are considerd when deciding the
	 * baseline's offset. Later more exotic fonts could demand that codepoint is taken into account.
	 * 
	 * @param codepoint	The codepoint of the symbol represented. *not in use yet*
	 * @param image		The glyph of the math symbol.
	 * 
	 * @return 			Point pointing at the referencepoint, the beginning of the baseline.
	 */
    public static Point referencePoint(int codepoint, BufferedImage image) {

    	int y = image.getHeight();
    	
    	double scaling = y / ((double) Toolbox.FONTMETRICS.getHeight()); 
    	
    	y -= (int) (scaling * Toolbox.FONTMETRICS.getDescent());
    	
    	return new Point(0,y);
	}

	private static BufferedImage render(int baseline, int codepoint) {
		
		BufferedImage image = Toolbox.makeGlyph(codepoint, Toolbox.advance((char) codepoint));
		
		int renderedheight = image.getHeight();
		
		int unitwidth  = image.getWidth(null);
		int unitheight = image.getHeight(null);
		
		float xscale = ((float) baseline) / unitwidth; 
		float yscale = ((float) renderedheight) / unitheight; 

		int width  = (xscale <= yscale) ? baseline : -1;
		int height = (width == -1) ? renderedheight : -1;
		
   		Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);

   		Graphics2D g = (Graphics2D) image.getGraphics();
   		
   		g.drawImage(scaled, transform, null);
   		
   		return image.getSubimage(0, 0, scaled.getWidth(null), scaled.getHeight(null));
	}

}
