package model.description.abstraction;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;

import annexes.maker.CompositeCanvas;
import control.Toolbox;
import model.description.DPrimitive;

/**
 * Used to join a described primitive with it's decoration elements: a handle, an offset point and it's bounds.
 */
public class Placeholder {

	/**
	 * In principle a mutable struct with global variables to gather the
	 * variables describing a baseline and to improve readability.
	 * Global variables are bad programming but tolerated this time.
	 * 
	 * @see java.awt.FontMetrics
	 */
 	public class Baseline {	
		
 		/** Baseline x-coordinate. */
 		public int x;
 		/** Baseline y-coordinate. */
 		public int y;
 		/** Baseline length. */
 		public int  length;
 		
 		/** A new modifyable class of global variables. 
 		 * 
 		 *@param x x-coordinate of the baseline.
 		 *@param y y-coordinate of the baseline.
 		 *@param length Length of the baseline.
 		 *
 		 * @see java.awt.FontMetrics
 		 */
 		public Baseline(int x, int y, int length) {
 			this.x = x; this.y = y; this.length = length;
 		}
 		
 		/** A new modifyable class of global variables. 
 		 * 
 		 *@param referencepoint	Offset pointing to the beginning of the baseline.
 		 *@param length Length of the baseline. 
 		 *
 		 * @see java.awt.FontMetrics
 		 */
		public Baseline(Point referencepoint, int length) {
			this(referencepoint.x, referencepoint.y, length);
		}

		/**
		 * Point pointing at the beginning of the baseline.
		 * 
		 * @return The referencepoint.
		 * 
		 * @see java.awt.FontMetrics
		 */
		public Point getLocalReferencepoint() {
			return new Point(x, y);
		}	
		
		/**
		 * Translates this baseline.
		 * @param dx	The number of pixels to translate in x-direction.
		 * @param dy	The number of pixels to translate in y-direction.
		 */
		public void translate(int dx, int dy) {
			this.x += dx;
			this.y += dy;
		}
	}

	
	private Described 		 described;	
	private Ellipse2D.Double handle;	
	private Rectangle 		 frame;				// location relative canvas origo (global)
	private	Point			 prereference;	 
	private Baseline 		 baseline;			// location relative frame origo (local)	

	
	/**
	 * Instantiates a new placeholder for described formals.
	 *
	 * @param frame 	The frame that this placeholder holds and from which it's baseline and handle is derived.
	 */
	public Placeholder(Rectangle frame) {

		if (frame == null) return;
		
		this.described = null;														// glyph-dependent reference point later

		this.frame = frame;		
	
		Baseline defaultbaseline = computeDefaultBaseline(this.frame);				// rectangle-dependent reference point for now
		
		this.prereference = defaultbaseline.getLocalReferencepoint();			
		
		Point location = this.frame.getLocation();
				
		location.translate(this.prereference.x, this.prereference.y);
		
		this.baseline = new Baseline(location, defaultbaseline.length);				
		
		this.handle = makeHandle(this.frame);
	}

	
	/**
	 * The primitive.
	 *
	 * @return The described primitive.
	 */
	public Described 		described() { 
		return described; 
	}
	/**
	 * The offset.
	 *
	 * @return The description's offset from (0,0). 
	 */
	public Point 			offset() 	{ 
		return baseline.getLocalReferencepoint(); 
	}
	
	/**
	 * The baseline consisting of the reference point pointing at the beginning and the length of the baseline.
	 * 
	 * @return The baseline object.
	 * 
	 * @see Baseline
	 **/
	public Baseline			baseline() {
		return baseline;
	}
	
	/**
	 * The bounds of the bounding frame of the placeholder.
	 *
	 * @return The rectangular bounds.
	 */
	public Rectangle 		bounds() 	{ 
		return frame; 
	}
									
	/**
 	 * The handle of the description. The handle is cicular and situated att the upper left corner. 
	 *
	 * @return The handle used for moving the description around.
	 */
	public Ellipse2D.Double handle() 	{ 
		return handle; 
	}

	/**
	 * The pre-calculated referencepoint that considers the frame before the placeholder is filled with a described formal
	 * and it's deciding glyph. The pre-reference is later adjusted to the filled in description.
	 * 
	 * @return	A pre-calculated reference point for the current frame. 
	 */
	public Point 			prereference() {
		return prereference;
	}

	
	/**
 	 * Fills this placeholder with a described formal and adjusts it's baseline to correct for differences
 	 * between frame-derived pre-reference and the described's glyph's reference point. 
	 *
	 * @param formal 	The new replacing formal.
	 */
	public void fill(Described formal) 	{ 
	
		described = formal;
		
		int y = this.baseline.y;
		
		Point r1 = this.prereference;												// rectangle-dependent reference point
		Point r2 = this.described.getLocalReference();								// glyph-dependent reference point
		
		Point dr = new Point(r2.x - r1.x, r2.y - r1.y);															

		this.baseline.translate(dr.x, dr.y);										// difference is now zero

		int length = this.baseline.length;
		
		this.baseline.length = described.description().width;
		
		if (Toolbox.DEBUGMINIMAL)
			System.out.println("Baseline translated : (" + dr.x + "," + dr.y +") \t Length changed : " + (length - this.baseline.length) + "\t" + "y-direction : " + y + "->" + this.baseline.y);
	}

	
	/**
	 * Inserts a new frame of this placeholder and recomputes all dependent states of the placeholder.
	 * 
	 * @param newframe	The rectangle that should make up the new frame. <i>Remark: the bounds are not copied but the 
	 * 					actual rectangle given as argument is used (copy-by-reference).</i>
	 */
	public void setFrame(Rectangle newframe) {
		
		this.frame = newframe;

		this.handle = this.makeHandle(this.frame);
		
		int length;
		
		if (this.described == null) { 

			length = CompositeCanvas.computeBaseline(this.frame, Toolbox.DUMMY.getCodepoint());

			Described adjusteddummy = new DPrimitive(Toolbox.DUMMY.getCodepoint(), length);

			this.prereference = adjusteddummy.description().getReference();				// rectangle-dependent reference point for now
		
		} else {
			
			length = this.described.description().width;

			this.prereference = this.described.description().getReference();				// rectangle-dependent reference point for now			
		}

		Point offset = newframe.getLocation();										
		offset.translate(this.prereference.x, this.prereference.y);			

		this.baseline = new Baseline(offset, length);				
		
		this.handle = makeHandle(this.frame);		
	}
	
	/**
	 * Checks if this placeholder has a primitive or is empty.
	 *
	 * @return True iff empty.
	 */
	public boolean isEmpty() { 
	
		return described == null; 
	}
	
	/**
	 * Trims the frame rectangle to the bounds of the image of the described primitive.
	 */
	public void trimBounds() {
		
		Point location = this.frame.getLocation();
		
		frame = described.description().getBounds();
		
		frame.setLocation(location);
		
		this.prereference = described.description().getReference();
		
		this.updateHandle();
	}
	
	/**
	 * Recomputes the handle for the description. Used after altering bounds.
	 */
	public void updateHandle() {
		this.handle = makeHandle(this.frame);
	}
	
	/**
	 * Translates bounds and handle, that is all that concerns the described primitive.
	 *
	 * @param dragging the dragging
	 */
	public void translate(Point dragging) {
			
		frame.translate(dragging.x, dragging.y);

		handle.x += dragging.x;
		handle.y += dragging.y;		
		
		baseline.x += dragging.x;
		baseline.y += dragging.y;
	}	

	
	private static Ellipse2D.Double 		makeHandle(Rectangle rectangle) {
	
		int DR = CompositeCanvas.DR;		

		int x = rectangle.x;
		int y = rectangle.y;
		
		Ellipse2D.Double circle = new Ellipse2D.Double(x - (DR+1), y - (DR+1), 2*(DR+1), 2*(DR+1));
	
		return circle;
	}

	private static Placeholder.Baseline 	computeDefaultBaseline(Rectangle frame) {

		Placeholder staticclass = new Placeholder(null);
		
		int length = CompositeCanvas.computeBaseline(frame, Toolbox.DUMMY.getCodepoint());

		Described adjusteddummy = new DPrimitive(Toolbox.DUMMY.getCodepoint(), length);
		
		Point offset =  adjusteddummy.description().getReference();			
				
		return staticclass.new Baseline(offset.x, offset.y, length);
	}
}
