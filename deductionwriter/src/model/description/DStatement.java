package model.description;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;

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

	/** The start of the baseline this description follows. */
	private Point location;

	/**
	 * Indicates that this description should be highlighted with a box framing.
	 */
	public boolean boxed;			

	
	/**
	 * An empty described statement.
	 */
	public DStatement() {
		super();		
		commonConstructor();
	}	

	/**
	 * A described statement consisting of a collection of described formal mathematics primitives.
	 * 
	 * @param described 	The collection of described formal primitives.
	 * @param type 			The implication terminating this statement. 
	 */
	public DStatement(Collection<Described> described, ImplicationType type) {
		super(described, type);
		commonConstructor();
	}

	
	private void commonConstructor() {
		this.location = new Point(0,0);
		this.id = super.id + "D";		
	}

	
	/**
	 * Underline this statement, all its formals.
	 * 
	 * @param underlined If setting underlined or removing underlining.
	 */
	public void underline(boolean underlined) {
		
		for (Described d : this) d.underline(underlined);
	}

	/**
	 * Sets the location of this statement, which is its upper left corner. Often the same as its first 
	 * described formal's location.
	 * 
	 * @param location	The location point.
	 */
	public void setLocation(Point location) {  
		
    	this.location.setLocation(location.getLocation());	
	}	

	/**
	 * Returns this statement's location.
	 * 
	 * @return 	The statement's location.
	 */
	public Point getLocation() {
		return location.getLocation();
	}

	/**
	 * Retreives this description's bounding rectangle.
	 * 
	 * @return	The rectangular bounds of this statement's description.
	 */
 	public Rectangle getBounds() {

		int x = location.x, y = location.y;
		int width = 0; int height = 0;
		
		if (super.size() > 0) {
		
			Rectangle last = super.getLast().description().getBounds();
			
			Point uppercorner = new Point(last.x + last.width, last.y + last.height);

			width  = uppercorner.x - x; 	height = uppercorner.y - y;
		} 
		
		return new Rectangle(x, y, width, height);
	}

 	
	/** {@inheritDoc} */ 
	public String toString() {
		
		String output = "D[(" + location.x + "," + location.y + ") ";
		
		for (Described dp : this)
			output +=  dp.toString() + ":";
		
		output = output.substring(0, output.length()-1);
		
		output += "]";
		
		return output;
	}
}
