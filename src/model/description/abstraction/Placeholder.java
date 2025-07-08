package model.description.abstraction;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

import model.description.DCursor;

/**
 * Used to join a described primitive with it's decoration elements: a handle, an offset point and it's bounds.
 */
/**
 * 
 */
public class Placeholder {

	
	public class Handle extends Ellipse2D.Double implements Comparable<Handle> {
	
		public int depth;

		public Handle(Point2D.Double p) {
			super(p.x-2.5, p.y-2.5, 5, 5);
		}
				
		public void moveTo(Point2D.Double p) {
			this.setFrame(p.x - this.width/2.0, p.y - this.height/2.0, this.width, this.height);
		}
		
		public int compareTo(Handle o) {
			return Integer.valueOf(this.depth).compareTo(o.depth);
		}
				
		public Handle clone() {

			Handle clone = new Handle(new Point2D.Double(this.getCenterX(), this.getCenterY()));
			
			clone.depth = this.depth;
			
			return clone;
		}
	}

	private Point2D.Double 	writepoint;
	private Handle 			handle;
	public 	int				id;					// same codepoint will occur many times
	
	private Handle 			subhandle;
	private Point2D.Double 	subhandleoffset;
	
	private Described 		described = null;
	private DCursor 		frame;				// location relative canvas origo (global)
	
	/**
	 * Instantiates a new placeholder for described formals.
	 *
	 * @param frame 	The frame that this placeholder holds and from which it's baseline and handle is derived.
	 */
	public Placeholder(DCursor frame) {
		
		this.id = this.hashCode();
		
		this.frame 		= frame;			
		this.writepoint = frame.getWritepoint();
		this.handle		= new Handle(frame.getLocalReferencepoint());	
		
		this.handle.moveTo(this.writepoint);		
	}	
	/**
	 * Instantiates a new placeholder for described formals and fills it with the primitive given as argument.
	 *
	 * @param primitive 	A primitive that this placeholder will be fitted for and contain. That is the primitive
	 * 						is also inserted and there is no need to call insert afterwards.
	 * 						Yet, it is only described primitives that is considered but perhaps composites could 
	 * 						useful as well.
	 */
	public Placeholder(Described primitive) {

		this.frame 		= primitive.description().cast();			
		this.writepoint = primitive.getWritepoint();
		this.handle		= new Handle(primitive.getLocalReference());	
	
		this.described 	= primitive;

		this.handle.moveTo(this.writepoint);				
	}	

	/**
	 * The rich cursor bounds of the bounding placeholder frame. 
	 *
	 * @return The rich rectangular bounds object. 
	 */
	public DCursor 		frame() { 
		
		return frame; 
	}
	/**
	 * The described formal, presumably a primitive, that is currently held by this placeholder.
	 *
	 * @return The described primitive or null if none is in place.
	 */
	public Described 	described() { 	
				
		return described; 
	}
	/**
 	 * The handle of the frame and eventually it's formal. The handle coincides with their both's respective
 	 * reference points, the write point where it all is written at. 
	 *
	 * @return 	The handle the user clicks when moving the placeholder around. The handles makes up the mouse
	 * 			interactive area.
	 */
	public Handle	 	handle() 	{ 
		
		return handle;
	}

	/**
	 * The local write point offset, an offset from device coordinates origo. The write point is called 
	 * reference point in java's and perhaps in  common typographical jargon and is at the start of the glyph's 
	 * base line.
	 * 
	 * @return	The reference point, the write point of the placeholder and perhaps it's described formal. Which
	 * 			points at the start of what is assumed as the baseline.
	 */
	public Point2D.Double 	getLocalReference() {
		
		if (described == null) return frame.getLocalReferencepoint();		
		else 
			if (described.getLocalReference().y != frame.getLocalReferencepoint().y) { 
			
				System.err.println("Cursor and glyph hasn't same offset point: " + (described.getLocalReference().y - frame.getLocalReferencepoint().y));
	
				return frame.getLocalReferencepoint();
				
			} else 
				return described.getLocalReference();
	}
	/**
	 * The offset.
	 *
	 * @return The description's offset from (0,0). 
	 */
	public Point2D.Double 	getGlobalReference() 	{

		if (described == null) return frame.getWritepoint();		
		else {

			if (described.getWritepoint().x != frame.getWritepoint().x) { 
				
				System.err.println("Cursor and glyph hasn't same writepoint: " + (described.getWritepoint().x - frame.getWritepoint().x));
	
				return frame.getWritepoint();
				
			} else 
				return described.getWritepoint();
		}
	}

	public double 			getAdvance() {	
		
		if (described != null) 
			return described.getAdvance(); 
		else
			return frame.width;
	}

	/**
 	 * Fills this placeholder with a described formal and adjusts it's baseline to correct for differences
 	 * between frame-derived pre-reference and the described's glyph's reference point. 
	 *
	 * @param adjusted 	The new replacing formal.
	 */
	public void 	insert(Described adjusted) 	{ 
		
		// my convention: placeholder's and described's reference point is henceforth same	
		
		Point2D.Double writepoint = this.getGlobalReference();
		
		adjusted.setWritepoint(writepoint);					

		described = adjusted;	
	}	
	
	public void 	draw(Graphics2D g2d, boolean active) {
						
		if (described != null) 
			described.draw(g2d);

//		if (active) 
//			drawShadow(g2d);				
		
		drawBounds(g2d);					
		drawBaseline(g2d);
		drawHandle(g2d);
	}

	public void 	fill(Graphics g) {

		Graphics2D g2d = (Graphics2D) g.create();

		g2d.translate(writepoint.x, writepoint.y);
		
		g2d.setColor(new Color(0,128,0,64));
		
		g2d.fill(frame);
	}
		
	
	public void 	moveTo(Point2D.Double r) {
		
		this.writepoint = r;
		
		this.frame.setWritepoint(this.writepoint);
		
		if (this.described != null)
			this.described.setWritepoint(r);
		
		handle.moveTo(r);			//handle2.moveTo(Arithmetic.add(r,subhandleoffset));
	}
			
	
	public Handle 	getSubhandle() {
		return subhandle;
	}
	
	public Double 	getSubhandleoffset() {
		return subhandleoffset;
	}	

	public void 	setSubhandleoffset(Point2D.Double r) {
		
		Point2D.Double r0 = new Point2D.Double(handle.getCenterX(), handle.getCenterY());
		
		subhandleoffset = new Point2D.Double(r.x - r0.x, r.y - r0.y);
	}

	public void 	resizeAllToBaseline(double newbaseline) {

		frame.resizeToBaseline(newbaseline);
		
		if (described != null)
			described = described.scaledClone(newbaseline, true);

		// handle should be unchanged
	}
	
	/**
	 * Checks if this placeholder has a primitive or is empty.
	 *
	 * @return True iff empty.
	 */
	public boolean 	isEmpty() { 
	
		return described == null; 
	}


	public Placeholder clone() {

		Placeholder clone;
		
		if (this.described != null) 
			clone = new Placeholder(this.described.clone());
		else
			clone = new Placeholder(this.frame.clone());
		
		clone.handle.depth = this.handle.depth;
		
		clone.writepoint = (Point2D.Double) this.writepoint.clone();
		
		return clone;
	}
	
	
	private void drawBounds(Graphics2D g2d) {

		PathIterator pi = frame.getPathIterator(null);
		double[] coords = new double[4];

		int DR = 2;
		
		while (!pi.isDone()) {
			
			pi.currentSegment(coords);
			
			g2d.setColor(Color.yellow);
			g2d.fillOval((int) coords[0] - DR, (int) coords[1] - DR, 2*DR, 2*DR);

			g2d.setColor(Color.black);
			g2d.drawOval((int) coords[0] - DR, (int) coords[1] - DR, 2*DR, 2*DR);
			
			pi.next();
		}
	}
	
	private void drawShadow(Graphics2D g2d) {

		Rectangle shadow =  frame.getBounds();
		
		g2d.setColor(new Color(64,64,64,64));	
		
		shadow.grow(2, 2); 	g2d.draw(shadow);
	}
	
	private void drawBaseline(Graphics2D g2d) {
		 
		Point2D.Double reference = new Point2D.Double(handle().getCenterX(), handle().getCenterY());
						
		g2d.setColor(Color.magenta);
		
		g2d.drawLine((int)reference.x, (int) reference.y, (int) (reference.x + this.getAdvance()), (int) reference.getY());
	}
	
	private void drawHandle(Graphics2D g2d) {
		g2d.setColor(Color.green);
		g2d.fill(handle());
		g2d.setColor(Color.black);
		g2d.draw(handle());
	}

}