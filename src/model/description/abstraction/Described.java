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

	public DRectangle 	description();
	
	
	public void 		draw(Graphics g);

	public void 		setErase();
	
	/**
	 * Moves this glyph so that it's upper left corner is at {@link #getWritepoint()}.
	 * 
	 * @param location	Where the upper left corner should be.
	 */
	public void 				setWritepoint(Point2D.Double r);	
		
	public Point2D.Double 		getWritepoint();	

	public Point2D.Double 		getLocalReference();

	
	public Rectangle2D.Double 	getBounds();
	
	public Size2D				getSize();
	
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


	public BufferedImage 	getImage();
	
	/** {@inheritDoc} */
	public String 			toString();
	/**
	 * Deep cloning omitting only duplication of images.
	 * 
	 * @return	A fresh clone of this object.
	 */
	public Described 		clone();
	
	public Described 		scaledClone(double baseline, boolean transparent);
}
