package control.statics;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import control.statics.PaintStatics.Size2D;
import model.logic.abstraction.Formal;

public final class CharGauge {

	public int codepoint;
	
	public double 	advance, averageadvance, 
					descent, ascent,
					variation, scale;
 
	public final Point2D.Double 	reference;
	public final Rectangle2D.Double cursor;
	public final Size2D 			size;
	

	public CharGauge(Formal primitive, double newadvance) {
		this(primitive.getCodepoint(), newadvance);
	}

	public CharGauge(Formal primitive) {
		this(primitive.getCodepoint());
	}
	
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

	
	public static double scaleDirection(double sx, double sy, double s0) {
		
		double s = s0;
		
		if (sx == sy) s = sx;
		
		if (sx <= 1 && sy > 1) s = sy;
	
		if (sy <= 1 && sx > 1) s = sx;
	
		if (sx > 1 && sx > 1) s = (sx > sy) ? sx : sy;
		
		if (sx < 1 && sx < 1) s = (sx > sy) ? (1/sy) : (1/sx);
		
		return s;
	}
	
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