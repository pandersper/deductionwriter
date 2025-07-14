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
 * The part of the description of a described formal that concerns local layout such as baseline length and 
 * writepoint (beginning of baseline). It separates the graphics and drawing part from the layout part together
 * with the class {@see DRectangle} that takes care of former. This is a semantic gift from separation of 
 * concerns and code readability, things that object oriented inheritance enforce. <br><br>
 * It also is the only encapsulator or the formals value. This is a implementation choice to keep things at 
 * one place. In the future these values could well figure in other contexts such as doing arithmetic with 
 * them.<br><br>
 * Although this object is for layout, rendering aspects currently present are underlying all their guages.
 * 
 * Remember that many of it's set values could be pre-calculated averages and could be real depending on if 
 * the cursor is filled with a glyph or not. See {@link FontMetrics}.
 */
public class DCursor extends Rectangle2D.Double {

	/**
	 * If this cursor should be erased or not. It is only a marker.
	 */
	protected 	boolean erase = false;

	/**
	 * The value carried by and represented by this cursor. Remember that values should reside and be accesssed 
	 * only here.
	 */
	protected 	Formal			value;	
	/**
	 * The typographical reference point, the write point of the glyph. See {@link FontMetrics}. Remember that it
	 * can be an pre calculated average and it could be real depending on if the cursor is filled with a glyph or not.
	 */
	protected	Point2D.Double	reference;			
	/**
	 * The advancement when typing this glyph's cursor.  See {@link FontMetrics}. Remember that it can be an pre 
	 * calculated average and it could be real depending on if the cursor is filled with a glyph or not.
	 */
	protected	double			advance;
	
	
 	/**
 	 * This is the simplest constructor of object of this class and uses only a rectangle to compute layouting. 
 	 * Remember that base line reference point and other things rely on current graphics configuration, primarily 
 	 * chosen font. It makes a guess of its future use by using an average glyph when computing this.
 	 * So <i>the write point will probably change</i> when it is filled.
 	 * 
 	 * @param frame	The frame that should fit an future glyph
 	 * 
 	 * @see CharGauge
 	 * @see PaintStatics.AVERAGE_ADVANCE
 	 */
    public DCursor(Rectangle2D.Double frame) {    	
    	
    	this.value = Primitive.DUMMYFORMAL;
    	
    	CharGauge gauge = new CharGauge(-1);
    	
    	Rectangle2D.Double unscaled = gauge.cursor;

    	double sx = unscaled.getWidth() / frame.width;
    	double sy = unscaled.getHeight() / frame.height;

    	double s = CharGauge.scaleDirection(sx, sy);
		
    	gauge.scale(1/s);
    	
		this.reference 	= gauge.reference;
    	this.advance 	= gauge.advance;
    	    	
    	Point2D.Double upperleft = new Point2D.Double(frame.x, frame.y);
    	
    	super.setFrame(upperleft, gauge.size);
 		
    	Point2D.Double referencepoint = Arithmetic.add(upperleft,reference);
    	
 		this.setWritepoint(referencepoint);
	}
	/**
	 * Creates an empty cursor from a placeholder frame. Since the place holder object and class is very rich,
	 * not just a rectangle it is merely a transfer of variables. The place holder could have a described formal
	 * in it's description part is not stored, only the value.
	 * 
	 * @param frame	The frame working as a preimage for this cursors size and layout.
	 */
	public DCursor(Placeholder frame) {
		
		if (!frame.isEmpty())
				this.value = frame.frame().value;
		
 		this.advance 		= frame.getAdvance(); 		
 		this.reference 		= frame.getLocalReference();
 		
 		super.setFrame(frame.frame());			

 		this.setWritepoint(this.reference);
	}
	/**
     * Constructs a cursor that that fits a formal with a certain base line length. This cursor depends on this 
     * applications current configuration regarding fonts and windows such as size and type. So the descriptive
     * aspect is still very much present implicitly.
     * 
     * @param primitive	The formal that this cursor should hold. Note that
     * @param baseline	The length of the rendered glyph's baseline.
     */
 	public DCursor(Formal primitive, double baseline) {

 		this.value 			= primitive;

 		CharGauge gauge = new CharGauge(primitive, baseline);
 		
 		this.advance 		= gauge.advance; 		
 		this.reference 		= gauge.reference;
 		
 		super.setFrame(new Point2D.Double(0,0), gauge.size);	// outside origo is now at the inside referencepoint, so that it is drawn 
 																// at the baseline start, att the referencepoint
 		this.setWritepoint(this.reference);						// redundant
 	}
	
	
 	/**
 	 * Draws this cursor outline onto whatever graphics object it is handed.
 	 * 
 	 * @param g2dc	A common graphics object.
 	 */
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
	
	/**
	 * Marks this cursor for erasal for future paint methods to take care of. Does nothing else.
	 */
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
	
    /**
     * Returns the primary variable of the cursor, the point which serves as its origo which in typography
     * is the beginning point of it's base line. See {@link java.awt.FontMetrics}.
     * 
     * @return	What does it mean that a glyph is written to a point? It means that it's base line starts at the point 
     * 			that this method returns.
     */
	public Point2D.Double 		getWritepoint() {
		return new Point2D.Double(x+reference.x, y+reference.y);
	}
	/**
	 * Sets this cursors write point
	 * 
	 * @param writepoint What does it mean that a glyph is written to a point? It means that it's base line starts at 
	 * 					 the point given as argument to this method.
	 */
	public void 				setWritepoint(Point2D.Double writepoint) {
		
		this.setFrame(Arithmetic.subtract(writepoint,reference), new Size2D(this));
	}
	
	/**
	 * This gives the cursor's local reference point relative to it's upper left corner, in common device coordinate manner.
	 * 'Reference point' is typographica nomenclature while I guess 'writepoint' is just common programming jargon.
	 * 
	 * @see java.awt.FontMetrics
	 *
	 * @return	The reference point of this cursor which could be a averaging guess of what it should contain and 
	 * 			could also be properly set to a specific glyph. These things are settled by the constructor only at 
	 * 			the time of writing.
	 */
	public Point2D.Double 		getLocalReferencepoint() {
		return reference;
	}
	/**
	 * Returns the length that this cursor advances when written.
	 * 
	 * @return 	The number of pixels that this cursor proceed.
	 *
	 * @see java.awt.FontMetrics
	 */
	public double 				getAdvance() {
		return advance;
	}
    /**
     * The bounds of the area from the base line and uppwards. So that descending parts falls outside of that area. 
     * It feel more intuitive to have a cursor that starts att the base although the glyph descends below it.
     * 
     * @return	The bounds of the area from the cursor's base line an uppwards (negative y-direction).
     */
	public Rectangle2D.Double 	getAscendingBounds() {
		
		Point2D.Double wp = this.getWritepoint();
		
		return new Rectangle2D.Double(wp.x - reference.x, wp.y - reference.y, width, reference.y);
	}
	
	/**
	 * Resizes this cursor to a fit a new baseline. <b>It uses it's current parameters as outset and does not revert
	 * to an averaging guess of a future glyph</b>.
	 * 
	 * @param newbaseline The new base line length.
	 */
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
		
		this.setFrame(x , y, width, height);		
	}	
	        	
	/**
	 * A full depth clone of this object.
	 */
	public DCursor clone() {	
		 
		DCursor clone = new DCursor(this.value, this.advance);
		
		clone.advance = this.advance;
		clone.reference = (Point2D.Double) this.reference;
		clone.erase = this.erase;

		clone.setFrame(this.getFrame());
		
		return clone;
    }	
}
