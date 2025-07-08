package control.statics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashMap;

import model.description.DCursor;
import model.description.DEditableStatement;
import model.description.DPrimitive;
import model.description.DRectangle;
import model.description.abstraction.Described;
import model.description.abstraction.Placeholder;
import model.description.abstraction.Placeholder.Handle;
import model.independent.CyclicMap;
import model.logic.Primitive;

/**
 * Class of static helper methods for drawing.
 */
public class PaintStatics {
	
	
	public static class Size2D extends Dimension2D {

		private double width, height;


		public Size2D(Rectangle2D.Double r) {
			this.width = r.width;
			this.height = r.height;
		}
		
		public Size2D(double width, double height) {
			this.width = width;
			this.height = height;
		}
		
		
		public double getWidth() {
			return this.width;
		}

		public double getHeight() {
			return this.height;
		}

		public void setSize(double width, double height) {
			this.width = width;
			this.height = height;
		}
	}	
		
	
	public static final Font 				FONT 		= new Font("Times", Font.PLAIN, 60);

	private static final Dimension 			GLYPHBOUNDS  = new Dimension(200, 400);
	public static final BufferedImage 		ANYPAINTABLE = new BufferedImage(GLYPHBOUNDS.width, GLYPHBOUNDS.height, BufferedImage.TYPE_INT_ARGB);	/** Commonly used font metrics. */
		/** Information about the font used **/
	public static final FontMetrics			FONTMETRICS			= ANYPAINTABLE.createGraphics().getFontMetrics(FONT);
	public static final FontRenderContext	FONTRENDERCONTEXT	= FONTMETRICS.getFontRenderContext();	
	
	public static final char 				DUMMYCHAR 	= 'D';

	public static final AffineTransform 	IDENTITY = AffineTransform.getTranslateInstance(0, 0);

	public static final double 				AVERAGEADVANCE 	= averageAdvance();
	public static final int 				AVERAGELEAD 	= FONTMETRICS.getDescent() + FONTMETRICS.getAscent() + FONTMETRICS.getLeading();

	public static final Rectangle			DUMMYBOUNDS 	= new Rectangle(0, -FONTMETRICS.getAscent(), (int) AVERAGEADVANCE, FONTMETRICS.getDescent() + FONTMETRICS.getAscent());
	
	public static final int[]				MARGINS			= { 10, 20, 10, 20 };
	private static final int				MAXROWS 		= 20;

	public static final int LEFT = 0, TOP = 1, RIGHT = 2, BOTTOM = 3; 

	public static final Point2D.Double		PAGESTART 	= new Point2D.Double(MARGINS[LEFT], 2*AVERAGELEAD);
	public static final Point2D.Double		PAGEEND 	= new Point2D.Double(MARGINS[RIGHT], MAXROWS*AVERAGELEAD + MARGINS[BOTTOM]);

	public static final DCursor 			DUMMYCURSOR = dummyCursor();

	public static final Color 				BACKGROUND 	= ViewStatics.floralwhite;
	public static final Color 				FOREGROUND 	= Color.black;
	
	/** The global collection of character to primitive bindings.  */
	public static final HashMap<Character, DPrimitive>  GLYPHDICTIONARY = new HashMap<Character, DPrimitive>();

	/**
	 * Draw the blink of a cursor.
	 *
	 * @param g 		The graphics object with which to draw.
	 * @param cursor 	The cursor to blink. 
	 */
	public static void blinkCursor(Graphics g, DRectangle cursor) {

		double x = cursor.x, 		y = cursor.y;
		double w = cursor.width, 	h = cursor.height;

		Graphics2D g2d = (Graphics2D) g.create((int)x, (int)y, (int)w , (int)h);

		paintThickRectangle(g2d, 0, 0, w, h, 2, new Color(255,0,0,127));				//try { Thread.sleep(80); } catch (InterruptedException ie) { }
		///(DA53)
		paintThickRectangle(g2d, 0, 0, w, h, 2, ViewStatics.floralwhite);				//try { Thread.sleep(30); } catch (InterruptedException ie) { }

		g2d.dispose();
	}
	/**
	 * Clears the end of the current line starting after the described formal given as argument. 
	 * 
	 * @param g 	The graphics on which to draw.	 
	 * @param last	The formal that ends the line.
	 */
	public static void clearEndOfLine(Graphics g, Rectangle last) {
				
		Point end = last.getLocation();
				
		int rowheight = PaintStatics.AVERAGELEAD;
		
		Graphics gc = g.create(end.x + last.width, end.y, 1000, rowheight);					// global graphics object										///(BA0D)
			
		gc.setColor(PaintStatics.BACKGROUND);
		gc.fillRect(0, 0, 1000, rowheight);

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

		Rectangle surround = statement.bounds().getBounds();
		
		surround.grow(2, 2);
		
		g.setClip(surround);
		
		paintBox(surround, g, new Color(255, 0, 0, 127));		  		
	}
	/**
	 * Paint a box outline around a statement.
	 *
	 * @param r 	Rectangle bounds of a statement or just a box.
	 * @param g 	The graphics object to draw with 
	 * @param c 	The color of the box outline.
	 */
	public static void paintBox(Rectangle r, Graphics g, Color c) {

		Graphics gc = g.create(r.x, r.y, r.width , r.height);
		
		paintThickRectangle(gc, 0, 0, r.width, r.height, 3, c);
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
	public static void paintThickRectangle(Graphics g, double x, double y, double dx, double dy, int n, Color c) {
		
		Rectangle2D.Double outer = new Rectangle2D.Double(x,y,dx,dy);
		Area thick 		= new Area(outer);
		
		Rectangle2D.Double inner = new Rectangle2D.Double(x + n, y + n, dx - 2*n, dy - 2*n);
		Area innerarea 	= new Area(inner);
	
		thick.subtract(innerarea);
	
		Graphics2D g2d = (Graphics2D) g.create((int)x, (int)y, (int)dx, (int)dy);
	
		g2d.setColor(c);
		
		g2d.fill(thick);
		g2d.dispose();
	}
		
	
	public static BufferedImage		makeGlyph(int codepoint, double baseline, boolean transparent) {
		
		CharGauge gauge = new CharGauge(codepoint, PaintStatics.AVERAGEADVANCE);
	
		BufferedImage image = makeImage(gauge);
		
		drawGlyph(gauge, image, transparent);
	
		gauge = new CharGauge(codepoint, baseline);

		image = scaleImage(gauge, image);			// DONT SCALEIMAGE INSTEAD DRAWSTRING WITH CORRECT BASELINE AND FONT
		
		return image;
	}

	
	private static BufferedImage 	makeImage(CharGauge gauge) {
		
		double height 	= gauge.cursor.getHeight();
		double width 	= gauge.cursor.getWidth();
				
		BufferedImage image = new BufferedImage((int)width, (int)height, BufferedImage.TYPE_INT_ARGB);
		return image;
	}

	private static void 			drawGlyph(CharGauge gauge, BufferedImage image, boolean transparent) {

		Graphics2D g2d = image.createGraphics();
	
		if (transparent)
			g2d.setColor(new Color(0,0,0,0));
		else
			g2d.setColor(ViewStatics.floralwhite);
		
		g2d.fill(gauge.cursor);	
				
		g2d.setFont(FONT);
		g2d.setColor(Color.black);	
		
		if (gauge.codepoint != -1)
			g2d.drawString("" + (char) gauge.codepoint, (int) gauge.reference.x, (int) gauge.reference.y);		// characters are written upside down relative device coordinates
	}
	
	private static BufferedImage 	scaleImage(CharGauge gauge, BufferedImage image) {
	
		Rectangle2D.Double bounds = gauge.cursor;
			
		Image scaled = image.getScaledInstance((int)bounds.getWidth(), (int)bounds.getHeight(), Image.SCALE_SMOOTH);

		image = new BufferedImage(scaled.getWidth(null), scaled.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2d = image.createGraphics();
		
		g2d.drawImage(scaled, null, null);
		
		return image;
	}
	
	public static BufferedImage 	makeCompositeGlyph(CyclicMap<Handle, Placeholder> shapes) {
		
		Placeholder frameholder = Toolbox.findFrame(shapes);	/* Later: if subglyphs outside frame is allowed - determineBounds(shapes.values()); */
		
		Point2D.Double origo = new Point2D.Double(frameholder.getLocalReference().x, frameholder.getLocalReference().y);
		
		Rectangle2D.Double bounds = frameholder.described().description();							

		BufferedImage all = new BufferedImage((int)bounds.width, (int)bounds.height, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g2dc = all.createGraphics();
		
		g2dc.translate(origo.x, origo.y);
		
		Placeholder holder;
					
		for (Placeholder h : shapes.sortedValues())						
			h.described().draw(g2dc);
		
		return all;	
	}

	
	private static Rectangle2D.Double determineBounds(Collection<Placeholder> values) {

		double xmax = java.lang.Double.MAX_VALUE;
		double ymax = java.lang.Double.MAX_VALUE;
		double xmin = java.lang.Double.MIN_VALUE;
		double ymin = java.lang.Double.MIN_VALUE;
		
		for (Placeholder p : values) {
			
			Rectangle2D.Double r = p.frame();
			
			xmax = (xmax > r.x) ? xmax : r.x;
			ymax = (ymax > r.y) ? ymax : r.y;
			xmin = (xmin < r.x) ? xmin : r.x;
			ymin = (ymin < r.y) ? ymin : r.y;		
		}

		return new Rectangle2D.Double(xmin, ymin, (xmax - xmin), (ymax - ymin));
	}
	
	public static BufferedImage 	transparantSurrounding(BufferedImage centerpiece) {
		
		int width = centerpiece.getWidth();
		int height = centerpiece.getHeight();
		
		Rectangle boundary = new Rectangle(width,height);
	
		boundary.grow(width/2, height/2);
		boundary.translate(width/2, height/2);
		
		BufferedImage surrounding = new BufferedImage(boundary.width, boundary.height, BufferedImage.TYPE_INT_ARGB); 		
		 		
		Graphics2D g = (Graphics2D) surrounding.createGraphics();
		
		Color color = new Color(0,0,0,0);
	
		g.setColor(color); 		
		g.fill(boundary); 		
		
		color = new Color(6,6,6,6);
		
		g.setColor(color);		
		g.drawArc(boundary.x, boundary.y, boundary.width, boundary.height, 0, 360);
		 		
		return surrounding;	
	}

	
	private static double 			averageAdvance() {
		
		int sum = 0;
		
		int[] advances = FONTMETRICS.getWidths();
		
		for (int a : advances) sum += a;
		
		return (((double)sum)/advances.length);
	}	

	private final static DCursor 	dummyCursor() {
		
		model.description.DCursor dummy 	= new DCursor(Primitive.DUMMYFORMAL, (int)AVERAGEADVANCE);
				
		dummy.setWritepoint((Point2D.Double) PAGESTART.clone());
		
		return dummy;
	}
}
