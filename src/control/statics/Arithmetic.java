package control.statics;

import java.awt.geom.Point2D;

public class Arithmetic {

	
	public static Point2D.Double 	add(Point2D.Double r0, Point2D.Double r1) {
		return new Point2D.Double(r0.x + r1.x, r0.y + r1.y);
	}

	public static Point2D.Double 	subtract(Point2D.Double r1, Point2D.Double r0) {		
		return new Point2D.Double(r1.x - r0.x, r1.y - r0.y);
	}

	public static Point2D.Double 	neg(Point2D.Double r) {
	
		Point2D.Double s = (Point2D.Double) r.clone();
	
		s.x = -s.x;
		s.y = -s.y;
	
		return s;
	}

	private static void 			translate(Point2D.Double r, Point2D.Double dr) {
		r.x += dr.x;
		r.y += dr.y;
	}


}
