 package view.components;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import control.Toolbox;
import model.description.DEditableStatement;
import model.description.DPrimitive;
import model.description.DStatement;
import model.description.DTheorem;
import model.description.abstraction.Described;
import view.abstraction.CursoredCanvas;
import view.abstraction.DisplayTools;

/**
 * The canvas upon which to draw theorems.
 * 
 * The canvas is particularly 'view part of the application' but
 * <b>Remark:</b><i> it encapsulates and shields a core part of the model</i>
 * namely the {@see DTheorem} which is created in it and should be acceses
 * through either this {@see DisplayCanvas} which lays out and draws the theorem
 * or via the {@see Session} which stores all theorems, the theorems being
 * contained in their respective {@see Canvas}. 
 */
public class DisplayCanvas extends Canvas implements CursoredCanvas {

	
	private DTheorem				theorem;
	private Described				drawn = null,  erase = Toolbox.DUMMYCURSOR.clone(),  cursor = Toolbox.DUMMYCURSOR.clone();	
	private Rectangle				clear;
	private Set<DStatement>			altered = new HashSet<>();
	
	private Point					writepoint = new Point(0,0);
	
	private int						advance, lead, defaultadvance;
	
	private static int leftmargin = 5, rightmargin = 30, topmargin = 0;

	private boolean painting				= true;
	private boolean outofcursorchange 		= false;
	private boolean outofpreliminarychange 	= false;
	private boolean confirm 				= false;
	private boolean clearpreliminary		= false;

	
	/**
	 * Instantiates a new empty canvas for drawing fantastic math theorems upon.
	 * Every canvas holds one and only one theorem and is the only direct holder of that theorem.
	 * If no theorem is provided it creates an empty one.
	 */
 	public DisplayCanvas() {		
 		this("empty");
 	}
 	
	public DisplayCanvas(String name) {
 		this.theorem = new DTheorem(name);
 		this.reset();	
	}

	/**
	 * Instantiates a new canvas for drawing fantastic math theorems upon.
	 * Every canvas holds one and only one theorem and is the only direct holder of that theorem.
	 * @param theorem The theorem that is described by this canvas.
	 */
 	public DisplayCanvas(DTheorem theorem) {		
 		this.theorem = theorem;
 		this.reset();	
 	}

 	
	/** {@inheritDoc} */
 	public void fillCursor(Described formal, boolean paint, Described erased) {
		
		erase = erased != null ? erased : drawn;

		drawn = formal;
		
		drawn.setLocation(writepoint);	

		cursor.description().setBounds(drawn.description().getBounds());
		
		if (paint) {			
			this.setPaintMode(true, false, false);
			this.paint(this.getGraphics());
		}
	}
	

	/**
 	 * Sets the theorem to be drawn.
	 *
	 * @param theorem The new theorem.
	 */
	public void setAndDescribeTheorem(DTheorem theorem) {

		this.theorem = theorem;

		this.setWritepoint(startCursor());

		this.theorem.firstStatement().setLocation(writepoint.getLocation());
		if (!theorem.isEmptyTheorem())
			this.theorem.firstFormal().setLocation(writepoint.getLocation());
		
		this.redescribeAll();
	}

	

	/** 
	 * The theorem worked on in this canvas. Try to export the theorem only frmo its canvas. They are a couple.
	 * 
	 * @return The theorem beloning to this canvas and only this canvas. Sessions consists of many canvas-theorem pairs.
	 **/
	public DTheorem getTheorem() {
		return theorem;
	}
	
	/** 
	 * Returns the current cursor.
	 *
	 * @return 	The current cursor of the canvas.
	 */
 	public Described cursor() {
		return this.cursor;
	}

	/** {@inheritDoc} */
 	public void emptyCursor() {
		
		erase = drawn;

		drawn = null;
	}

	/**
	 * Set a new empty cursor after the last formal of the theorem
	 */
 	public void newCursor() {
		
 		Described last = !theorem.isEmptyTheorem() ? theorem.lastFormal() : startCursor();
 		
 		this.setWritepoint(last);

 		this.fillCursor(last, false, null);
 		
 		erase = last;
 	
 		if (!theorem.isEmptyTheorem()) incrementCursor();
 	}
	
	/** {@inheritDoc} */
 	public void incrementCursor() {

		advance = drawn.description().width;

		erase = drawn;

		drawn = null;

		if (writepoint.x + advance > this.getWidth() - rightmargin) 
			this.incrementRow();
		else 
			writepoint.x += advance;

		advance = defaultadvance;
				
		cursor.setLocation(writepoint);
		cursor.description().width = advance;
		
 	}

 	/** Increment the writing point so as to point at the begining of the next row. */
	private void incrementRow() {

		writepoint.y += lead;
		writepoint.x = 0;
	}


	/** {@inheritDoc} */
	public Described getDrawn() {
		return drawn;
	}
																																				
 	/**
	 * Sets the cursor's position, this canvas's write point.
	 * 
	 * @param	movedto 			The described formal who's referencepoint should be the new write point.
	 * 								If this is null the writepoint is set to the startcursor's. 
	 */
 	public void setWritepoint(Described movedto) {
						
		Described write = movedto != null ? movedto : startCursor();
		
		Point to = write.getGlobalReference();
		
		writepoint.setLocation(to.x, to.y);
	}
	
 	/**
	 * Sets the cursor's position, this canvas's write point.
	 * 
	 * @param	movedto 	If there is no offset (offset is null) set the writepoint to this described formals referencepoint. 
	 * @param	localorigo	The position in the canvas where the current cursor has it's upper left corner.
	 * @param	offset		The offset, the cursors reference point, from the local origo.
	 */
	public void setWritepoint(Described movedto, Point localorigo, Point offset) {

		if (offset == null) setWritepoint(movedto);
		else {
			writepoint.setLocation(localorigo.x, localorigo.y);
			writepoint.translate(offset.x, offset.y);
		}
	}

	
	/**
	 * Adds the currently drawn primitive to the theorem in use, either at the end of the theorem or if
	 * a statement is edited into the current prompt of that statement.
	 */
	public void newPrimitive() {	
		
		if (this.isPrompting()) 
			theorem.insertPrimitive(drawn, theorem.getEditing().current());
		else 		
			theorem.appendPrimitive(drawn);
				
		this.setPaintMode(false, false, true);
 		this.paint(this.getGraphics());			// force is necessary - repaint paints 'as soon as possible'

		this.incrementCursor();
	}

	/**
 	 * Finalises and appends the work piece preliminary statement of the theorem to the theorem and creates a 
	 * new preliminary statement to work on. 
	 *
	 * @param implication The implication ending the finalised statement.
	 */
	public void newStatement(Described implication) {						
		
		theorem.finalisePreliminary(implication);
				
		erase = implication;
		
		this.setPaintMode(false, false, true);
		this.paint(this.getGraphics());																											///(1960)

		this.incrementCursor();
		this.setPaintMode(true, false, false);
		this.paint(this.getGraphics());
						
		theorem.getPreliminary().setLocation(writepoint);
	}

	
	/**
	 * Redescribes a statement of the theorem. Lays out the statement to fit this canvas.
	 *
	 * @param statement The statement to lay out and redescribe.
	 */
 	public void redescribeStatement(DStatement statement) {			/** DOES NOT RESTORE CURSOR **/

 		if (statement.isEmpty()) return;
 		
		Iterator<Described> it = statement.iterator();
		
		while (it.hasNext()) {
			this.fillCursor(it.next(), false, null);
			this.incrementCursor();
		}
		
		statement.setLocation(statement.getFirst().getLocation());			
	}

 	/**
 	 * Do a new layout of the whole theorem
 	 */
 	public void redescribeAll() {
 		
 		this.redescribeTail(theorem.firstStatement());
 		
 		if (Toolbox.DEBUGMINIMAL) System.err.println("Redescribed all");
 	}
 	
 	/**
 	 * Do a new layout of the ending part of the theorem, starting att statement given as argument. 
 	 * 
 	 * @param included	The statement that begins the tail to be re-layouted.
 	 */
 	public void redescribeTail(DStatement included) {
		
 		if (theorem.isEmptyTheorem()) return;
 		
 		if (included == null || included == theorem.getPreliminary() || theorem.size() == 0 ) {		// last, last, first
 			this.redescribePreliminary();
 			return;
 		} else {				// included != null && included != theorem.getPreliminary() && !theorem.size() == 0

 	 		if (Toolbox.DEBUGMINIMAL) System.err.println("Redescribing, starting at " + included.formalsString());

 			Point restore = writepoint.getLocation();

 			painting = false;
 			this.setWritepoint(included.getFirst()); 			
 			theorem.addLast(theorem.getPreliminary());

 			int start = theorem.indexOf(included);

 			Iterator<DStatement> it = theorem.listIterator(start);

 			while (it.hasNext()) 
 				this.redescribeStatement(it.next());

 			theorem.removeLast();
 			writepoint.setLocation(restore.getLocation());
 			painting = true;

 			return;
 		} 		
 	}
 	 	
 	/**
 	 * Do a new layout of the preliminary statement.
 	 */
  	public void redescribePreliminary() {

  		DStatement redescribe = theorem.getPreliminary();
  		
  		if (redescribe.isEmpty()) return;
  		
 		Point restore = writepoint.getLocation();
 		Point write = redescribe.getLocation();
 		
 		writepoint.setLocation(write);
 		this.fillCursor(redescribe.getFirst(), false, null);
 		this.incrementCursor();
 		
 		this.redescribeStatement(redescribe);
 		
 		writepoint.setLocation(restore);
 		this.fillCursor(redescribe.getLast(), false, null);
 		this.incrementCursor();
	}

 	/**
 	 * Resets this canvas to a cleared canvas with cursor a start position.
 	 */
	public void reset() {

 		Described startcursor = DisplayCanvas.startCursor();

 		this.setWritepoint(startcursor);
		
		defaultadvance	= startcursor.description().width;
		lead			= startcursor.description().height;
		advance 		= defaultadvance;
			
		this.repaint();
	}

  	
  	/**
  	 * Add a statement's bounding area to the area that should be updated because it has been altered.
  	 * 
  	 * @param altered	The statement which also should be updated.
  	 */
	public void unionAltered(DStatement altered) {
		this.altered.add(altered);
	}

	/**
	 * Toggle editing mode. Starts a new editing aspect or closes, tidies up and leaves the existing editing aspect.
	 */
	public void toggleEditingMode() {

		DStatement chosen = theorem.getChosen();
		DEditableStatement editing = theorem.getEditing();

		if (editing == null) {										// start new editing aspect of chosen statement 

			if (chosen != null && chosen.size() > 0) {				// someting to edit

				editing = theorem.edit();							// new aspect of chosen

				Described first = editing.current();
				
				this.setWritepoint(first);
				this.fillCursor(first, true, null);
			} 												
			
		} else {								// edited statement is larger than two
												// finish up and close editing aspect, make chosen statement and canvas ok to leave
			if (editing.current().isDummy()) 				
				editing.deleteCurrent();												// perhaps delete dummy 
			else 
				if (this.isPrompting() && editing.current() != drawn && drawn != null)	// or insert wysiwyg
					editing.replaceCurrent(drawn);										
			
			editing.togglePrompting();													// close prompt, dummy already removed

			DStatement previous = theorem.getPrevious(editing.whole());					// perhaphs null

			this.redescribeTail(previous);												// handles all cases

			theorem.leaveEditing();
		
			this.setWritepoint(theorem.lastFormal());							// reset cursor
			this.fillCursor(theorem.lastFormal(), false, editing.current());			
			this.incrementCursor();

			editing = null;																// editing no more
		}		
	}

	/**
	 * Checks if the editing aspect exists and are prompting for a new primitive to insert.
	 *
	 * @return true, if is prompting and editing. Otherwise false.
	 */
	public boolean isPrompting() {

		DEditableStatement editing = theorem.getEditing();

		return editing != null ? editing.prompting : false;
	}
	
	// the usual suspects

	/** {@inheritDoc} */
	public void update(Graphics g) {	
		
		if (outofpreliminarychange) { 
						
			if (theorem.isEdited()) 
				redescribeTail(theorem.getEditing().whole());
			else
				redescribeAll();
				
			erase = theorem.lastFormal();
			
			super.update(g);																													///(266G)
		} 		
	}

	/** {@inheritDoc} */
	public void repaint() {		
		super.repaint();
		
		if (theorem != null) {
			
			Rectangle bounds = new Rectangle();

			if (theorem.isEdited()) {
						
				bounds = theorem.getEditing().whole().getBounds();																			///(DE2G)
				
			} else {

				cursor.setLocation(writepoint.getLocation());
				
				if (outofpreliminarychange) 
					bounds = new Rectangle(0, writepoint.y, this.getWidth(), writepoint.y + lead);											///(040C)
				else 
					if (outofcursorchange) 
						bounds = theorem.getPreliminary().getBounds();  																	///(GE23)
					else 
						bounds = cursor.description();  																					///(7AG9)
			}
	
			super.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
		}
	}

	/** {@inheritDoc} */
	public void paint(Graphics g) {
    	
     	if (painting) {

     	  	Graphics2D gcopy = (Graphics2D) g.create();
     	   
    		if (theorem == null) {
    			
    			return;

    		} else {											///(3G90)
        			
    			if (!theorem.isEdited()) DisplayTools.clearEndOfLine(gcopy, cursor);

    			if (clear != null) {
    				gcopy.clip(clear);
    				DisplayTools.clearRect(gcopy, clear);
    				clear = null;
    				return;
    			}
    			
    			if (!outofcursorchange) {						// only in cursor																					///(683F)
    				    				
    	    		if (drawn != null) {

    	    			if (erase != null) {
    	    				
    	    				gcopy.setClip(drawn.description().union(erase.description()));		// backwards

        					DisplayTools.paintCursor(gcopy, erase, true);									
    	    				DisplayTools.paintDescribed(gcopy, drawn, Color.orange);												
    	    				DisplayTools.paintCursor(gcopy, drawn, false);									
    	    			
    	    			} else {

    	    				gcopy.setClip(drawn.description());

        					DisplayTools.paintDescribed(gcopy, drawn, Color.orange);												
        					DisplayTools.paintCursor(gcopy, drawn, false);									
    	    			}

    					if (confirm) {	  										///(E83F)

    						DisplayTools.blinkCursor(gcopy, drawn.description());
    						erase = drawn;
    						confirm = false;
    					}  
        				    
    				} else {					 								// proceeded so erase previous cursor																							///(05B8)

    					gcopy.setClip(erase.description().union(cursor.description()));

    					DisplayTools.paintCursor(gcopy, erase, true);									
    					DisplayTools.paintDescribed(gcopy, erase, Color.green);												
    
    					DisplayTools.clearCursor(gcopy, cursor);
    				}    				
  	    		
    			} else {														///(AA7D)
    				
    				if (!outofpreliminarychange) {								// only in preliminary 																								///(5403)    			

    					DStatement preliminary = theorem.getPreliminary();
    					Rectangle bounds = preliminary.getBounds().union(cursor.description());
    					
    					gcopy.setClip(bounds);
    					
    					DisplayTools.paintStatement(gcopy, preliminary);
    					
    				} else {													// whole canvas	///(27GB)

    					gcopy.setClip(null);
    					
    					DisplayTools.clearRect(gcopy, this.getBounds());
    	    			DisplayTools.paintTheorem(gcopy, theorem);
    					
    		    		if (theorem.isEdited()) {								///(7DB0)

    		    			DEditableStatement edited = theorem.getEditing();
    		    			
    		    			gcopy.setClip(edited.getBounds());

    		    			DisplayTools.paintEditing(gcopy, edited, null);
    		    			DisplayTools.paintCursor(gcopy, edited.current(), false);

    		    		}
    		    		
    					outofpreliminarychange = false;							// done
    				}
    				
    				outofcursorchange = false;									// done
    			}   			    			    			

    		}  		

    		DisplayTools.paintCursor(gcopy, cursor, false);									

			if (!altered.isEmpty()) {
				
				gcopy.setClip(DisplayTools.area(altered));
				
				for (DStatement s : altered) DisplayTools.paintStatement(gcopy, s);

				altered.clear();
			}																		///(34CD)	

			gcopy.dispose();
    	}
  	}

	// the remedy
	
	/**
	 * Sets which parts of the theorem that needs painting and similar. 
	 * 
	 * @param cursorchanged			The cursor has changed.
	 * @param preliminarychanged	The preliminary statement has changed.
	 * @param confirmcursor			The cursor content should be confirmed by red blinking.
	 */
 	public void setPaintMode(boolean cursorchanged, boolean preliminarychanged, boolean confirmcursor) {
		
		confirm = confirmcursor;			

		outofpreliminarychange  = confirm ? false : !(cursorchanged || preliminarychanged);																	///(66ED)
		outofcursorchange 		= confirm ? false : (preliminarychanged || outofpreliminarychange);		
		clearpreliminary		= preliminarychanged;
	}

 	
 	/**
 	 * Sets the area that should be cleared before painting.
 	 * 
 	 * @param cleared	The area that should be cleared. null is a sound value if no clearing should be made.
 	 */
	public void setClearArea(Rectangle cleared) {
		clear = cleared;
	}

	/**
	 * The start cursor of the canvas page.
	 * 
	 * @return	A dummy formal positioned at the very start of the page, where the first glyph should be drawn.
	 */
	public static Described startCursor() {
		
		Described start = new DPrimitive(Toolbox.DUMMY);
		
		start.setLocation(Toolbox.PAGESTART);
		
		return start;
	}
}
