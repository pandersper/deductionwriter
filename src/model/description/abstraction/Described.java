package model.description.abstraction;

import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import control.statics.PaintStatics.Size2D;
import model.description.DRectangle;
import model.logic.abstraction.Formal;

/**
 * The expected functionality of a described formal mathematical primitive. Description is meant in a broad 
 * sense, everything that is used for perceiving the mathematics. The specifically graphical description is 
 * given by {@link DRectangle} and is exported by the method {@link #description()}
 * 
 * @see DRectangle 
 */
public interface Described extends Formal {

	
	/**
	 * The value part of this described formal. 
	 * 
	 * @return The value part. The formal value described.
	 */
	public Formal 		value();
	/**
	 * The graphical part of a formal's description. Remember that a lot other things could be included in the
	 * description of a piece of mathematics such as gradings and checks of different sorts.
	 * 
	 * @return	A functionally very enriched rectangle describing this formal. {@link DRectangle} is a very
	 * 			central class in this application, it is the center of nearly all graphics.
	 */
	public DRectangle 	description();
	
	/**
	 * This method and only this method and it's sub procedures should do all painting of this described formal.
	 * 
	 * @param g		A common graphics object used to draw with.
	 */
	public void 		draw(Graphics g);

	/**
	 * Marks this described for erasal but does no painting itself.
	 */
	public void 		setErase();
	
	/**
	 * Moves this glyph so that it's upper left corner is at {@link #getWritepoint()}.
	 * 
	 * @param location	Where the upper left corner should be.
	 */
	public void 				setWritepoint(Point2D.Double r);	
	/**
	 * The write point is the so called reference point, the start point of a glyphs base line. This is conventions 
	 * in typpography. See {@link java.awt.FontMetrics} for info about how this works.
	 * 
     * @return	What does it mean that a glyph is written to a point? It means that it's base line starts at the point 
     * 			that this method returns.
     * 
	 * @see java.awt.FontMetrics
	 */
	public Point2D.Double 		getWritepoint();	
	/**
	 * Returns the local referencepoint which points to the reference point, the start of the base line if the upper 
	 * left corner of the glyph is situated at origo. Consider that machine device coordinates always have origo in 
	 * upper left corner. This differ probably from common typography.
	 * 
	 * @return	The vector offset from the upper left corner to the glyph's base line's start point.
	 */
	public Point2D.Double 		getLocalReference();

	/**
	 * The rectangular bounds of the glyph, not including the transparent surrounding used for embellishments.
	 * 
	 * @return	The bounds of the glyph without it's transparent surrounding.
	 */
	public Rectangle2D.Double 	getBounds();
	/**
	 * The size of the graphical description's area.
	 * @return	Size of the boundary of the graphical description which nearly always coincides with the boundary 
	 * 			of it's image.
	 */
	public Size2D				getSize();
	/**
	 * The progress along a a line's baseline this glyph does when typing it. It often coincides with the width of
	 * the glyph's image width but it certainly does have to do so, concerning all serifs and for example integral
	 * limits. IThe designer of the glyph has decided this advance.
	 * 
	 * @return 	The advance made by the glyph when typing it.
	 */
	public double 				getAdvance();
	
	/**
	 * Set this glyph's underline parameter. Wether the rendering takes notice is not concerned. 
	 * 
	 * @param underline	Underline or not.
	 */
	public void 		underline(boolean underline);
	/**
	 * Tells wether this glyph should be underlined or not.
	 *
	 * @return  Wether this glyph should be underlined or not.
	 */
	public boolean 		isUnderlined();
	/**
	 * Checks wether this described formal is dummy, that is just a empty described formal used for replacal.
	 *
	 * @return Wether this is a dummy described formal or not.
	 */
	public boolean 		isDummy();

	/**
	 * In some exceptional case the image used for drawing has to be exported but ordinarily the description
	 * should drawn by calling it's draw method.
	 * 
	 * @return The image of the described's description.
	 */
	public BufferedImage 	getImage();
	
	/** {@inheritDoc} */
	public String 			toString();
	/**
	 * Deep cloning omitting only duplication of space demanding images.
	 * 
	 * @return	A fresh clone of this object.
	 */
	public Described 		clone();
	/**
	 * Returns a new identical copy of this described except for size and transparent surrounding.
	 * 
	 * @param baseline		The new base line of the clone.
	 * @param transparent	If the clone should have transparent background or not.
	 * 
	 * @return	The scaled clone.
	 */
	public Described 		scaledClone(double baseline, boolean transparent);
}
