package model.description;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import control.statics.Arithmetic;
import control.statics.CharGauge;
import control.statics.PaintStatics;
import control.statics.PaintStatics.Size2D;
import model.description.abstraction.Placeholder;
import model.logic.Primitive;
import model.logic.abstraction.Formal;

/**
 */
public class DCursor extends Rectangle2D.Double {

	
	protected 	boolean erase = false;

	protected 	Formal			value;	
	protected	Point2D.Double	reference;		// stored in db	
	protected	double			advance;
	
	
	/**
     * As previous constructor but scales rendering so as to fit onto a baseline.
     * 
     * @param primitive	The formal primitive to render an image for.
     * @param baseline	The length of the rendered glyph's baseline.
     */
 	public DCursor(Formal primitive, double baseline) {

 		this.value 			= primitive;

 		CharGauge gauge = new CharGauge(primitive, baseline);
 		
 		this.advance 		= gauge.advance; 		
 		this.reference 		= gauge.reference;
 		
 		super.setFrame(new Point2D.Double(0,0), gauge.size);	// outside origo is now at the inside referencepoint, so that it is drawn 
 																// at the baseline start, att the referencepoint
 		this.setWritepoint(this.reference);	// redundant
 	}

    public DCursor(Rectangle2D.Double frame) {    	
    	
    	this.value = Primitive.DUMMYFORMAL;
    	
    	CharGauge gauge = new CharGauge(-1);
    	
    	Rectangle2D.Double unscaled = gauge.cursor;

    	double sx = unscaled.getWidth() / frame.width;
    	double sy = unscaled.getHeight() / frame.height;

    	double s = CharGauge.scaleDirection(sx, sy, -1);
		
    	gauge.scale(1/s);
    	
		this.reference 	= gauge.reference;
    	this.advance 	= gauge.advance;
    	    	
    	Point2D.Double upperleft = new Point2D.Double(frame.x, frame.y);
    	
    	super.setFrame(upperleft, gauge.size);
 		
    	Point2D.Double referencepoint = Arithmetic.add(upperleft,reference);
    	
 		this.setWritepoint(referencepoint);
	}
	
	public DCursor(Placeholder frame) {
		
		if (!frame.isEmpty())
				this.value = frame.frame().value;
		
 		this.advance 		= frame.getAdvance(); 		
 		this.reference 		= frame.getLocalReference();
 		
 		super.setFrame(frame.frame());			

 		this.setWritepoint(this.reference);
	}
	
	
	public void 	draw(Graphics2D g2dc) {

		Color color;
		
		if (erase) {
			color = PaintStatics.BACKGROUND;
			erase = false;
		} else 
			color = PaintStatics.FOREGROUND;
		
		g2dc.setColor(color);		
		g2dc.draw(this);
	}
	
	public void 	setErase() {
		erase = true;
	}

	/**
	 * The value that this object describes graphically.
	 * 
	 * @return A formal mathematical primitive value.
	 */
	public Formal 				getValue() {
    	return value;
    }
	
    
	public Point2D.Double 		getWritepoint() {
		return new Point2D.Double(x+reference.x, y+reference.y);
	}

	public void 				setWritepoint(Point2D.Double writepoint) {
		
		this.setFrame(Arithmetic.subtract(writepoint,reference), new Size2D(this));
	}

	public Point2D.Double 		getLocalReferencepoint() {
		return reference;
	}
	
	/**
	 * Return the length that this advances the cursor.
	 * 
	 * @return 	The number of pixels that this glyph proceed the cursor.
	 *
	 * @see java.awt.FontMetrics
	 */
	public double 				getAdvance() {
		return advance;
	}
    /**
     * Remember taht this rectangle grows in negative y direction, having location point att
     * glyph's start of baseline, referense point. Because glyphs are standing upside down reative device coordinates. 
     * @return
     */
	public Rectangle2D.Double 	getAscendingBounds() {
		
		Point2D.Double wp = this.getWritepoint();
		
		return new Rectangle2D.Double(wp.x - reference.x, wp.y - reference.y, width, reference.y);
	}

	public void resizeToBaseline(double newbaseline) {

		Point2D.Double writepoint = this.getWritepoint();
		
		double preadvance = advance, prewidth = width, preheight = height;

		double preascent 		= Math.abs(reference.y);		
		double ascentproportion = preascent / height;
		double change 			= newbaseline / advance;
		
		advance *= change;
		width *= change;
		height *= change;

		double dh = height - preheight;
		
		y -= ascentproportion * dh;
		reference.y += ascentproportion * dh;
		
		this.setFrame(x,y,width,height);		
	}	
	        	
	public DCursor clone() {	
		 
		DCursor clone = new DCursor(this.value, this.advance);
		
		clone.advance = this.advance;
		clone.reference = (Point2D.Double) this.reference;
		clone.erase = this.erase;

		clone.setFrame(this.getFrame());
		
		return clone;
    }	
}
