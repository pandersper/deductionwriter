package view.abstraction;

import model.description.abstraction.Described;

/**
 * A canvas with a cursor that can be filled with a Described object and repainted.  
 * 
 * @see Described
 * @see model.description.DRectangle
 */
public interface CursoredCanvas {

	/**
	 * Fill this canvas's cursor and if demanded paints it's content. It also sets objects needed
	 * for erasing.  
	 *
	 * @param formal 	The described primitive that should be displayed by the cursor.
	 * @param paint		Wether to actually paint or just calculate new layout values.
	 * @param erased	The previously displayed but now removed described primitive. May be null.
	 */
	public void 		fillCursor(Described formal, boolean paint, Described erased);

	/**
	 * Returns the currently displayed described formal.
	 * 
	 * @return	The currently displayed described formal.
	 */
	public Described 	getDrawn();
	
	/**
	 * Increments this canvas's cursor and takes care of everything concerned with that task.
	 */
	public void			incrementCursor();

	/**
	 * Empties the cursor and sets erase object.
	 */
	public void 		emptyCursor();

	/**
	 * Repaints this canvas.
	 */
	public void 		repaint();
	
}
