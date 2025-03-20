package view.abstraction;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.Set;

import control.Toolbox;
import model.description.DEditableStatement;
import model.description.DRectangle;
import model.description.DStatement;
import model.description.DTheorem;
import model.description.abstraction.Described;

/**
 * Class of static helper methods for drawing.
 */
public final class DisplayTools {
								
	/**
	 * Paint a described primitive.
	 *
	 * @param g 	The graphics on which to draw.
	 * @param dp 	The primitive who's description should be drawn.
	 * @param color	The color to underline with.
	 */
	public static void paintDescribed(Graphics g, Described dp, Color color) {
		
		if (dp.isDummy()) return;
		
		DRectangle r = dp.description();
		
		Graphics gc = g.create(r.x, r.y, r.width , r.height);				///(BA0D)
		
		gc.drawImage(r.getImage(), 0, 0, Color.white, null);		
			
		gc.setColor(color);
		gc.drawLine(r.getReference().x, r.getReference().y, r.getReference().x + r.getAdvance(), r.getReference().y);
		gc.fillOval(r.x, r.y, 3, 3);
		
		if (dp.isUnderlined()) {
			gc.setColor(Color.orange);
			gc.fillRect(0, r.height - 3, r.width, 1);
		}
		
		gc.dispose();		
	}

	/**
	 * Paint a described statement.
	 *
	 * @param g 	The graphics on which to draw.
	 * @param ds 	The statement who's description should be drawn.
	 */
	public static void paintStatement(Graphics g, DStatement ds) {
				
		for (Described d : ds) 
			paintDescribed(g, d, Color.black);
	
		if (ds.boxed) 
			paintBox(ds, g, Color.blue);	
	}

	/**
	 * Paint a described theorem.
	 *
	 * @param g 	The graphics on which to draw.
	 * @param dt 	The theorem who's description should be drawn.
	 */
	public static void paintTheorem(Graphics g, DTheorem dt) {

		for (DStatement ds : dt) 
			paintStatement(g, ds);

		paintStatement(g, dt.getPreliminary());
	}
	
	/**
	 * Paint the common cursor.
	 *
	 * @param g 		The graphics object which to draw with 
	 * @param described The described whos bounds outline the cursor.
	 * @param erase 	Wether cursor interior should be erased before it is drawn or draw around current glyph. 
	 */
	public static void paintCursor(Graphics g, Described described, boolean erase) { 

		Rectangle r = described.description();

		Graphics gc = g.create(r.x, r.y , r.width , r.height);

		if (erase)
			gc.setColor(Color.white);
		else
			gc.setColor(Color.lightGray);

		paintThickRectangle(gc, 0, 0, r.width, r.height, 1, gc.getColor());

		gc.dispose();
	}

	/**
	 * Paint a cursor with a particular color.
	 *
	 * @param g 		The graphics object which to draw with 
	 * @param cursor 	The rectangle outline of the cursor.
	 */
	public static void clearCursor(Graphics g, Described cursor) {

		Rectangle r = cursor.description().getBounds();

		Graphics gc = g.create(r.x, r.y , r.width , r.height);

		gc.setColor(Color.white);

		gc.fillRect(0, 0, r.width, r.height);

		gc.setColor(Color.lightGray);

		paintThickRectangle(gc, 0, 0, r.width, r.height, 1, gc.getColor());

		gc.dispose();
	}

	/**
	 * Draw the blink of a cursor.
	 *
	 * @param g 		The graphics object with which to draw.
	 * @param cursor 	The cursor to blink. 
	 */
	public static void blinkCursor(Graphics g, DRectangle cursor) {

		int x = cursor.x, 		y = cursor.y;
		int w = cursor.width, 	h = cursor.height;

		Graphics gc = g.create(x, y, w , h);

		paintThickRectangle(gc, 0, 0, w, h, 2, Color.red);			try { Thread.sleep(100); } catch (InterruptedException ie) { }
		///(DA53)
		paintThickRectangle(gc, 0, 0, w, h, 2, Color.white);		try { Thread.sleep(50); } catch (InterruptedException ie) { }

		gc.dispose();
	}

	/**
	 * Paint an edited statement aspect.
	 *
	 * @param g 			The graphics object with which to draw.
	 * @param statement 	The editable aspect of a statement.
	 * @param erase			The region to erase before drawing.
	 */
	public static void paintEditing(Graphics g, DEditableStatement statement, Described erase) {

		paintBox(statement.whole(), g, Color.red);		  		
	}

	/**
	 * Paint a box outline around a statement.
	 *
	 * @param ds 	The described statement.
	 * @param g 	The graphics object to draw with 
	 * @param c 	The color of the box outline.
	 */
	public static void paintBox(DStatement ds, Graphics g, Color c) {

		if (ds.size() > 0) {

			Rectangle dr = ds.getBounds();

			Graphics gc = g.create(dr.x, dr.y, dr.width , dr.height);

			paintThickRectangle(gc, 0, 0, dr.width, dr.height, 1, c);
		}	
	}

	/**
	 * Draws a horisontal line with y-coordinate at y.
	 * 
	 * @param g 	The graphics on which to draw.	 
	 * @param y		The y coordinate of the line.
	 */
	public static void paintBaseline(Graphics g, int y) {

		g.setColor(Color.blue);
		g.drawLine(0,y,1000,y);
	}
	
	/**
	 * Paints a thick rectangle.
	 *
	 * @param g 	The graphics on which to draw.
	 * @param x 	Location x coordinate.
	 * @param y 	Location y coordinate.
	 * @param dx 	Width in horisontal direction.
	 * @param dy 	The height in vertical direction.
	 * @param n 	How many pixel the bredth of the rectangles outline should be.
	 * @param c 	The color of th triangle.
	 */
	private static void paintThickRectangle(Graphics g, int x, int y, int dx, int dy, int n, Color c) {
		
		Rectangle outer = new Rectangle(x,y,dx,dy);
		Area thick 		= new Area(outer);
		
		Rectangle inner = new Rectangle(x + n, y + n, dx - 2*n, dy - 2*n);
		Area innerarea 	= new Area(inner);
	
		thick.subtract(innerarea);
	
		Graphics2D gc = (Graphics2D) g.create(x, y, dx, dy);
	
		gc.setColor(c);
		
		gc.fill(thick);
		gc.dispose();
	}

	/**
	 * Clears a rectangular area.
	 * 
	 * @param g 	The graphics on which to draw.
	 * @param r		The rectangle outlining the area.
	 */
	public static void clearRect(Graphics g, Rectangle r) {
				
		Graphics gc = g.create(r.x, r.y, r.width, r.height);					// global graphics object										///(BA0D)
			
		gc.setColor(Color.white);
		gc.fillRect(0, 0, r.width, r.height);
		gc.dispose();
	}

	/**
	 * Clears the line after the described formal given as argument. 
	 * 
	 * @param g 	The graphics on which to draw.	 
	 * @param last	The formal that ends the line.
	 */
	public static void clearEndOfLine(Graphics g, Described last) {
				
		Point end = last.getLocation();
		
		int rowheight = Toolbox.DUMMYCURSOR.description().height;
		
		Graphics gc = g.create(end.x + last.description().width, end.y, 1000, rowheight);					// global graphics object										///(BA0D)
			
		gc.setColor(Color.white);
		gc.fillRect(0, 0, 1000, rowheight);

		gc.dispose();
	}

	/**
	 * Adds areas of the described statements that are given as argument.
	 * 
	 * @param 	altered		A set of described statements.
	 * 
	 * @return				The total area of all the statements.
	 */
	public static Area area(Set<DStatement> altered) {

		altered.remove(null);

		Area a = new Area(new Rectangle());
				
		for (DStatement s : altered)
			a.add(new Area(s.getBounds()));
		
		return a;
	}

}
