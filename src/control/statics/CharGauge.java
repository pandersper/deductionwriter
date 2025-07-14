package control.statics;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import control.statics.PaintStatics.Size2D;
import model.logic.abstraction.Formal;

/**
 * A class for acquiring and collecting a certain symbol's all gauges and measures. It got a lot of global variables
 * for the moment and som of them are not final. Since it is a collection of measures it is not a big deal to avoid
 * fiddling with them. All of this depends on the application currently chosen font, font type and font size.
 * 
 *  @see PaintStatics
 */
public final class CharGauge {

	private double 	averageadvance, 
	descent, ascent,
	variation, scale;	
	
	/**
	 * The codepoint of the gluph to make a gauge for.
	 */
	public int codepoint;
	/**
	 * The advance of the glyph.
	 */
	public double 	advance;

	/**
	 * The reference point of this glyph. See {@link FontMetrics}.
	 */
	public final Point2D.Double 	reference;
	/**
	 * The bounds of this glyph
	 */
	public final Rectangle2D.Double cursor;
	/**
	 * The size of this glyph
	 */
	public final Size2D 			size;
	
	
	/**
	 * Constructs gauge information for a certain formal's description to fit a certain base line length.
	 * 
	 * @param primitive		The primitive who's description is concerned.
	 * @param newadvance	The base line length and advancement of the glyph.
	 */
	public CharGauge(Formal primitive, double newadvance) {
		this(primitive.getCodepoint(), newadvance);
	}
	/**
	 * Constructs gauge information for a certain formal's description to fit an average baseline length.
	 * 
	 * @param primitive		The primitive who'e description is concerned.
	 */
	public CharGauge(Formal primitive) {
		this(primitive.getCodepoint());
	}
	
	/**
	 * Constructs gauge information for a certain formal's description to fit a certain base line length.
	 * 
	 * @param codepoint		The codepoint of the primitive who's description is concerned.
	 * @param newadvance	The base line length and advancement of the glyph.
	 */
	public CharGauge(int codepoint, double newadvance) {

		this.codepoint = codepoint;
		
		double averageadvance = PaintStatics.AVERAGEADVANCE;

		descent = PaintStatics.FONTMETRICS.getDescent();
		ascent  = PaintStatics.FONTMETRICS.getAscent();
		
		if (codepoint == -1) {
			
			scale = newadvance / averageadvance;
			advance = averageadvance;
			variation = 1;
			
		} else {
				
			double unscaledadvance 	= PaintStatics.FONTMETRICS.charWidth(codepoint);
			
			scale = (Toolbox.tolerance(newadvance, unscaledadvance)) ? 1 : newadvance / averageadvance;	
			advance = unscaledadvance;
			variation = unscaledadvance / averageadvance;			
		}

		descent *= scale * variation;
		ascent  *= scale * variation;
		advance *= scale;

		reference 	= new Point2D.Double(0, ascent);
		cursor 		= new Rectangle2D.Double(0, 0, advance, descent + ascent);
		size 		= new Size2D(cursor);
	}
	
	/**
	 * Constructs gauge information for a certain formal's description to fit an average base line length.
	 * 
	 * @param codepoint		The codepoint of the primitive who's description is concerned.
	 */
	public CharGauge(int codepoint) {

		this.codepoint = codepoint;

		double averageadvance = PaintStatics.AVERAGEADVANCE;

		scale = 1;

		advance = (codepoint == -1) ? PaintStatics.AVERAGEADVANCE :
									  PaintStatics.FONTMETRICS.charWidth(codepoint);
		variation = advance / averageadvance;			

		descent = variation * PaintStatics.FONTMETRICS.getDescent();
		ascent  = variation * PaintStatics.FONTMETRICS.getAscent();

		reference 	= new Point2D.Double(0, ascent);
		cursor 		= new Rectangle2D.Double(0, 0, advance, descent + ascent);
		size 		= new Size2D(cursor);
	}

	
	/**
	 * Determines the scale to be used to not scale out of a rectangle with different sides.
	 *
	 * @param sx	Scaling factor in x direction.
	 * @param sy	Scaling factor in y direction;
	 * 
	 * @return		The scaling factor to fit best with respect to width of the triangle without 
	 * 				getting outside of the box.
	 */
	public static double scaleDirection(double sx, double sy) {
		
		double s = -1;
		
		if (sx == sy) s = sx;										// square
			
		if (sx <= 1 && sy > 1) s = sy;								// one is decreasing
	
		if (sy <= 1 && sx > 1) s = sx;								// one is decreasing
	
		if (sx > 1 && sx > 1) s = (sx > sy) ? sx : sy;				// both are increasing
		
		if (sx < 1 && sx < 1) s = (sx > sy) ? (1/sy) : (1/sx);		// both are decreasing
		
		return s;
	}
	
	/**
	 * Scales all measures with a scaling factor.
	 * 
	 * @param s	The scaling factor.
	 */
	public void scale(double s) {		

		descent *= s;
		ascent  *= s;
		advance *= s;
		
		reference.x *= s;		
		reference.y *= s;		

		cursor.x *= s;
		cursor.y *= s;
		cursor.width *= s;
		cursor.height *= s;

		size.setSize(cursor.width, cursor.height);
	}
}