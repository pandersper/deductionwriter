package model.description;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

import control.statics.Arithmetic;
import model.description.abstraction.Described;
import model.logic.Implication.ImplicationType;
import model.logic.Statement;

/**
 * The description of a mathematical statement. Description in a broad sense, not only renderable graphics. 
 * 
 * @see model.logic.Statement
 * @see model.logic.abstraction.Formal
 * @see model.description.abstraction.Described
 */
public class DStatement extends Statement {

	
	private Point2D.Double writepoint;
	
	/**
	 * An empty described statement.
	 */
	public DStatement() {
		super();
		this.writepoint = new Point2D.Double(0,0);
	}	
	/**
	 * A described statement consisting of a collection of described formal mathematics primitives.
	 * 
	 * @param described 	The collection of described formal primitives.
	 * @param type 			The implication terminating this statement. 
	 */
	public DStatement(LinkedList<Described> described, ImplicationType type) {
		super(described, type);
		this.writepoint = described.getFirst().getWritepoint();
	}
	
	
	public void draw(Graphics2D g) {
		
		for (Described described : this)
			described.draw(g);
	}
		
	/**
	 * Returns this statement's location.
	 * 
	 * @return 	The statement's location.
	 */
	public Point2D.Double getWritepoint() {
		return writepoint;
	}
	/**
	 * Sets the location of this statement, which is its upper left corner. Often the same as its first 
	 * described formal's location.
	 * 
	 * @param location	The location point.
	 */
	public void setWritepoint(Point2D.Double location) {  

		Point2D.Double offset = DRectangle.DUMMYRECTANGLE.clone().getLocalReferencepoint();
		
    	this.writepoint.setLocation(Arithmetic.add(location, offset));	
	}	
	/**
	 * Retreives this description's bounding rectangle.
	 * 
	 * @return	The rectangular bounds of this statement's description.
	 */
 	public Rectangle2D.Double getBounds() {

		double x = writepoint.x, y = writepoint.y;
		double width = 0, height = 0;
		
		if (super.size() > 0) {
		
			Rectangle2D.Double last = super.getLast().getBounds();
			
			Point2D.Double uppercorner = new Point2D.Double(last.x + last.width, last.y + last.height);

			width  = uppercorner.x - x; 	height = uppercorner.y - y;
		} 
		
		return new Rectangle2D.Double(x, y, width, height);
	}
 	
	/**
	 * Underline this statement, all its formals.
	 * 
	 * @param underlined If setting underlined or removing underlining.
	 */
	public void underline(boolean underlined) {
		
		for (Described d : this) d.underline(underlined);
	}
 	
	/** {@inheritDoc} */ 
	public String toString() {
		
		String output = "D[(" + writepoint.x + "," + writepoint.y + ") ";
		
		for (Described dp : this)
			output +=  dp.toString() + ":";
		
		output = output.substring(0, output.length()-1);
		
		output += "]";
		
		return output;
	}
}
