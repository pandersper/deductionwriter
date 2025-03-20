package model.description.abstraction;

import java.awt.Point;

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
	 * The graphical, image part of this description.
	 * 
	 * @return 	The graphical part. An image embedded into a frame rectangle wich also provides quite a lot 
	 * 		 	of usefull functionality. 
	 * 
	 * @see DRectangle
	 */
	public DRectangle 	description();
	
	/**
	 * The reference point that points to the baseline of this glyph. I accordance with typographical conventions.
	 * The point is given in relation to the glyph's origo, the upper left corner. 
	 *
	 *  @return The glyph-local location of the glyph's referencepoint, it's baseline start locally. 
	 *
	 * @see java.awt.FontMetrics 
	 */	
	public Point 		getLocalReference();

	/**
	 * The reference point that globaly points to the baseline of this glyph, in relation to the canvas origo. The canvas
	 * upon which it has been drawn.
	 *  
	 *  @return The canvas-global location of the glyph's referencepoint, it's baseline start globally. 
	 *  
	 * @see java.awt.FontMetrics 
	 */	
	public Point		getGlobalReference();
	
	/**
	 * Returns the coordinates of this glyph's upper left corner.
	 * 
	 * @return 	The location of this glyph's description.
	 */
	public Point 		getLocation();

	/**
	 * Moves this glyph so that it's upper left corner is at {@link #getLocation()}.
	 * 
	 * @param location	Where the upper left corner should be.
	 */
	public void 		setLocation(Point location);	

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

	/** {@inheritDoc} */
	public String 		toString();

	/**
	 * Deep cloning omitting only duplication of images.
	 * 
	 * @return	A fresh clone of this object.
	 */
	public Described 	clone();

}
