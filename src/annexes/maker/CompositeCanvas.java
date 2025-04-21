package annexes.maker;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;

import control.Toolbox;
import model.description.DComposite;
import model.description.DPrimitive;
import model.description.DRectangle;
import model.description.abstraction.Described;
import model.description.abstraction.Placeholder;
import model.description.abstraction.Placeholder.Baseline;
import model.independent.CyclicList;
import view.abstraction.CursoredCanvas;
import view.components.DButton;

/**
 * The canvas of the composite maker which draws everything while they are edited. I keeps a current 
 * cursor among all the cursors and the buttons fills it int the same way as in the main application. 
 * It does not keep it's own set of constituents but instead gets it from the parent composite panel. 
 * It shows the rendering in an inset in the upper left corner and has a side panel displaying and 
 * choosing the described primitives in use. <br>
 * 
 * It keeps it's own static drawing helpers but probably they will be merged with other similar in 
 * {@link view.abstraction.DisplayTools} in the future.
 */
public class CompositeCanvas extends Canvas implements CursoredCanvas {

	/** Delta radius of the handles. */
	public static final int DR = 4;

	private CompositePanel 	parent;
	private Placeholder 	current;	
	private DRectangle 		inset;
	private Point			reference;

	private boolean clear = false;	
	
	/** Origo of the canvas. */
	public Point 	origo = new Point(0,0);
	/** If work surface is moving. */
	public  boolean moving;
	/** If current part is moving. */
	public  boolean movingpart;

	
	/**
	 * Instantiates a new composite canvas.
	 *
	 * @param parentcontainer The parent panel which keeps the constituents and corresponding buttons.
	 */
	public CompositeCanvas(CompositePanel parent) {

		this.setBackground(new Color(228, 222, 160));

		this.parent = parent;
	}

	
	/** {@inheritDoc} */
	public void fillCursor(Described fullsize, boolean paint, Described erased) {

			int codepoint 	= fullsize.value().getCodepoint();
			int baseline 	= computeBaseline(current.bounds(), codepoint);

			Described adjusted = new DPrimitive(codepoint, baseline);
			
			current.fill(adjusted); 
						
			DButton button = new DButton(adjusted);
			
			parent.updateButtons(button);			
	}
	
	
	/**
	 * Sets the described primitive component currently edited.
	 *
	 * @param current The placeholder containing the sub component to choose for editing.
	 */
	public void setCurrent(Placeholder current) {

		clear = true;	this.repaint();
		clear = false;	this.current = current;	
	}
	
	/**
	 * Returns the sub component currently edited. 
	 *
	 * @return The currently edited sub component, in it's placeholder.
	 */
	public Placeholder getCurrent() {
		return current;
	}
		
	/**
	 * Sets the inset image that displays the current all-components rendering.
	 *
	 * @param done The rendered composite mathematics symbol.
	 */
	public void setInset(DComposite done) {
		
		if (done != null) inset = done.description();

		this.repaint();
	}

	/**
	 * Sets a new origo to be the center of the rectangle frame given.
	 * 
	 * @param centeredframe	The frame that should be centered after the change of origo.
	 */
 	public void setOrigo(Rectangle centeredframe) {
		
		origo = new Point((getWidth() - centeredframe.width)/2, (getHeight() - centeredframe.height)/2);  	
	}

 	
	/** {@inheritDoc} */
	public void paint(Graphics g) {

		Graphics2D gcopy = (Graphics2D) g; 		

		CyclicList<Placeholder> subprimitives = parent.getConstituents();

		Placeholder draw;

		if (!clear) {

			gcopy.translate(origo.x, origo.y);

			draw = subprimitives.get(0);
			
			drawBounds(gcopy, draw.bounds(), DR);
			decorateBounds(gcopy, draw.bounds());
			
			for (int i = 0; i < subprimitives.size(); i++) {

				draw = subprimitives.get(i);
				
				drawBounds(gcopy, draw.bounds(), DR);

				if (!draw.isEmpty()) 
					drawSubprimitive(gcopy, draw);
			}

			if (inset != null) drawInset(gcopy, origo, inset);

			if (!moving && !movingpart) highlightBounds(gcopy, current);

		} else	
			gcopy.clearRect(0,  0, this.getWidth(), this.getHeight());

		gcopy.dispose();
	}
	
	
	/**
	 * Compute baseline length for a given character fitted in a particular bonding rectangle.
	 * This is not true to typography but instead done so that the whole glyph should be contained
	 * by using this baseline. That is, it uses much more space than is customary.
	 *
	 * @param bounds 	The bounds that should contain the glyph.
	 * @param codepoint The codepoint of the glyph.
	 * 
	 * @return 			The length of the baseline onto which to place the glyph.
	 */
	public static int computeBaseline(Rectangle bounds, int codepoint) {

		BufferedImage image = Toolbox.makeGlyph(codepoint, Toolbox.advance((char) codepoint));

		int unitwidth  = image.getWidth();
		int unitheight = image.getHeight();

		float xscale = ((float) bounds.width) / unitwidth; 
		float yscale = ((float) bounds.height) / unitheight; 

		int width, height;

		if (xscale < yscale) 																	
			width = (bounds.width < unitwidth) ? bounds.width : (int) (xscale * unitwidth);		// keep or use x-scaling or	
		else 																					// zoom most in y direction	
			width = (int) (yscale * unitwidth);	  												// must use y-scaling

		return width;
	}
	
	
	private static void drawSubprimitive(Graphics2D gcopy, Placeholder holder) {

		Described subprimitive = holder.described();

		Point location = holder.offset();
		Point imageoffset = holder.prereference();		
		
		gcopy.drawImage(subprimitive.description().getImage(), location.x - imageoffset.x, location.y - imageoffset.y, Color.white, null);		

		Baseline baseline = holder.baseline();
				
		Point preref 	= holder.prereference();
		Point ref 		= subprimitive.getLocalReference();
		Point deltaref 	= new Point(ref.x - preref.x, ref.y - preref.y);
				
		int length = subprimitive.description().width;

		gcopy.setColor(Color.magenta);
		gcopy.drawLine(baseline.x + deltaref.x, baseline.y + deltaref.y, baseline.x + deltaref.x + length, baseline.y + deltaref.y);
}
	
 	private static void decorateBounds(Graphics2D gcopy, Rectangle bounds) {

		gcopy.setColor(Color.black);
		gcopy.draw(bounds);

		Ellipse2D.Double circle = new Ellipse2D.Double(bounds.x - (DR+1), bounds.y - (DR+1), 2*(DR+1), 2*(DR+1));

		gcopy.setColor(Color.green);
		gcopy.fill(circle);
		gcopy.setColor(Color.black);
		gcopy.draw(circle);			
	}
	
	private static void highlightBounds(Graphics2D gcopy, Placeholder holder) {

		Rectangle shadow =  holder.bounds().getBounds();
		
		gcopy.setColor(Color.lightGray);	
		shadow.grow(1, 1); 		gcopy.draw(shadow);
		shadow.grow(-2, -2);	gcopy.draw(shadow);
		
		gcopy.setColor(Color.red);
		gcopy.fill(holder.handle());

		gcopy.setColor(Color.black);
		gcopy.draw(holder.handle());
				
		Baseline base = holder.baseline();

		gcopy.setColor(Color.cyan);
		gcopy.drawLine(base.x, base.y, base.x + base.length, base.y);
	}
	
	private static void drawBounds(Graphics2D gcopy, Rectangle bounds, int radius) {

		gcopy.draw(bounds);

		PathIterator pi = bounds.getPathIterator(null);
		double[] coords = new double[4];

		while (!pi.isDone()) {
			
			pi.currentSegment(coords);
			
			gcopy.setColor(Color.yellow);
			gcopy.fillOval((int) coords[0] - radius, (int) coords[1] - radius, 2*radius, 2*radius);

			gcopy.setColor(Color.black);
			gcopy.drawOval((int) coords[0] - radius, (int) coords[1] - radius, 2*radius, 2*radius);
			
			pi.next();
		}

		Ellipse2D.Double circle = new Ellipse2D.Double(coords[0] - (radius+1), coords[1] - (radius+1), 2*(radius+1), 2*(radius+1));

		gcopy.setColor(Color.green);
		gcopy.fill(circle);
		gcopy.setColor(Color.black);
		gcopy.draw(circle);
	}

	private static void drawInset(Graphics2D gcopy, Point origo, DRectangle inset) {
		gcopy.translate(-origo.x, -origo.y);
		gcopy.drawImage(inset.getImage(), 0, 0, null, null);
		gcopy.translate(origo.x, origo.y);
	}

	/**
	 * Not used. 
	 */
	public void incrementCursor() {
		System.err.println("Not implemented in " + this.getClass());
	}

	/**
	 * Not used. 
	 */
	public void emptyCursor() {
		System.err.println("Not implemented in " + this.getClass());
	}

	/**
	 * Not used. 
	 */
	public Described getDrawn() {
		System.err.println("Not implemented in " + this.getClass());
		return null;
	}
}	
