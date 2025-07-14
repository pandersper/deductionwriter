 package view.components;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Iterator;

import control.statics.Arithmetic;
import control.statics.PaintStatics;
import control.statics.PaintStatics.Size2D;
import control.statics.ViewStatics;
import model.description.DCursor;
import model.description.DEditableStatement;
import model.description.DRectangle;
import model.description.DStatement;
import model.description.DTheorem;
import model.description.abstraction.Described;
import view.abstraction.CursoredCanvas;

/**
 * The canvas upon which to draw theorems.
 * 
 * The canvas is particularly 'view part of the application' but <i> it also encapsulates and shields a core part of 
 * the model</i>, namely the {@see DTheorem} which is created in it and should be accessed through either this 
 * {@see DisplayCanvas} which lays out and draws the theorem or via the {@see Session} which stores all canvases.
 */
public class DisplayCanvas extends Canvas implements CursoredCanvas {
	
	/** 
	 * The standard cursor size of the application. 
	 */
	public static final Size2D CURSORSIZE = new Size2D(DRectangle.DUMMYRECTANGLE);
	
	private DTheorem			theorem;
	private Described			drawn  = null, erase  = null;	
	
	private DCursor				cursor 	= PaintStatics.DUMMYCURSOR.clone();
	private Point2D.Double		writepoint 	= cursor.getWritepoint();
	private double				advance		= cursor.getAdvance();

	private boolean 			painting = true;
	public boolean				degrowth = false;
	
	/**
	 * Instantiates a new canvas for drawing a fantastic math theorem upon. The canvas determines how the theorem's
	 * description is laid out. Every canvas holds one and only one theorem and is the only direct holder of that 
	 * theorem.
	 * 
	 * @param theorem The theorem that is described by this canvas.
	 */
 	public DisplayCanvas(DTheorem theorem) {

 		this.setSize(ViewStatics.cnvDspSize); 		

 		this.theorem = theorem; 		 		

 		this.reset();	 		
 	}
 	
 	/**
 	 * Resets this canvas to a cleared canvas with cursor at a page start position.
 	 */
	public void 	reset() {

		drawn = null;
		erase = null;
		
		cursor.setWritepoint((Point2D.Double) PaintStatics.PAGESTART.clone());

		this.setWritecursor(cursor);		
		this.repaint();
	}
 	
 	/**
	 * Sets the cursor's position, this canvas's write point to the given cursor's write point.
	 * 
	 * @param	movedto 			The described formal who's reference point should be the new write point.
	 * 								If this is null the writepoint is set to the startcursors. 
	 */
 	public void 	setWritecursor(DCursor movedto) {
						
 		Point2D.Double wp = movedto.getWritepoint();
 		
		this.writepoint.setLocation(wp.x, wp.y);
		
		this.advance = movedto.getAdvance();
	}
 	/**
	 * Sets the cursor's position, this canvas's write point.
	 * 
	 * @param	movedto 	If there is no offset (offset is null) the writepoint is set to this described formals 
	 * 						local reference point. 
	 * @param	origo		The position in the canvas where the current cursor has it's upper left corner.
	 * @param	offset		The offset, the cursors reference point, from the local origo.
	 */
	public void 	setWritecursor(DCursor movedto, Point2D.Double origo, Point2D.Double offset) {

		advance = movedto.getAdvance();

		if (offset == null) setWritecursor(movedto);
		else 
			writepoint.setLocation(Arithmetic.add(origo, offset));
	}

	/**
	 * Fills in a descibed formal in the cursor, erases the previous and calls repaint. 
	 * 
	 * @param formal 	The description that should be filled in.
	 * @param erased	The description that should be erased.
	 * @param paint		Boolean telling if the changes should be painted or just laid out.
	 */
	public void 	fillCursor(Described formal, Described erased, boolean paint) {

 		// erase the previous and clear current
 		erase = drawn;
 		drawn = null;	
		cursor.setErase();
 		if (paint) this.paint(this.getGraphics());
		
		// fill in the new
 		formal.setWritepoint(writepoint);
 		drawn = formal; 		
 		advance = drawn.getAdvance();

 		cursor.setFrame(drawn.description()); 			//dcursor.setFrame(drawn.description().getAscendingBounds());

 		this.repaint();
	}	

	/** 
	 * Proceeds the current cursor one step.  
	 */
	public void 	proceedCursor() {
		
 		if (drawn != null) {
				
 			// erase previous cursor
	 		cursor.setErase();
	 		if (this.getGraphics()!=null) this.paint(this.getGraphics());
	 		
	 		// move on one position or new row
			if (writepoint.x + 2*advance > ViewStatics.cnvDspSize.width - PaintStatics.MARGINS[PaintStatics.RIGHT])
				this.newRow();
			else 
				writepoint.x += advance;
					
			// set up new empty cursor
			cursor.setFrame(writepoint, CURSORSIZE);	
			cursor.setWritepoint(writepoint);
 		}
		
		erase = drawn;
		drawn = null;
	}

	/**
	 * Call the methods {@see #fillCursor(Described, Described, boolean)} and {@see #proceedCursor()}.
	 * 
	 * @param formal 	The description that should be filled in.
	 * @param erased	The description that should be erased.
	 * @param paint		Boolean telling if the changes should be painted or just laid out.
	 */
 	private void 	fillAndProceed(Described fillin, Described erase, boolean paint) {
 
 		this.fillCursor(fillin, erase, paint);
 		
 		proceedCursor();		
 	}

	/**
	 * Adds the currently drawn primitive to the theorem in use, either at the end of the theorem or if
	 * a statement is edited into the current prompt of that statement.
	 */
	public void 	newPrimitive() {	
		
		if (this.isPrompting()) 
			theorem.insertPrimitive(drawn, theorem.getEditing().current());
		else 
			theorem.appendPrimitive(drawn);

		proceedCursor();
	}
	/**
 	 * Finalises and appends the work piece preliminary statement to the theorem and creates a new preliminary
 	 * statement to work on. 
	 *
	 * @param implication The implication ending the finalised statement.
	 */
	public void 	newStatement(Described implication) {
		
		implication.setWritepoint(writepoint);
		
		theorem.finalisePreliminary(implication);
												
		proceedCursor();
	}
	
	/**
	 * Sets the theorem displayed by this canvas. There must alway be one so this method just overwrites the old.
	 */
	public void 	setTheorem(DTheorem theorem) {
		this.theorem = theorem;
	}
	/** 
	 * The theorem worked on in this canvas. Try to export the theorem only from it's canvas. They are a couple.
	 * 
	 * @return The theorem beloning to this canvas and only this canvas. Sessions consists of many canvas-theorem pairs.
	 */
	public DTheorem getTheorem() {
		return theorem;
	}
	
	/** {@inheritDoc} */
	public Described getDrawn() {
		return drawn;
	}
	/** {@inheritDoc} */
 	public void 	emptyCursor() {
 		
		erase = drawn;
		drawn = null;
	}
 	/** 
 	 * Opens an new cursor at the suitable position at the end of the theorem to continue write.
 	 */
 	public void 	newCursor() {
 		
 		Point2D.Double lastwrite = theorem.lastFormal().getWritepoint();

 		cursor.setFrame(PaintStatics.DUMMYCURSOR);
 		cursor.setWritepoint(lastwrite);

 		advance = cursor.getAdvance();
 		
 		writepoint.setLocation(lastwrite);
 		
 		proceedCursor(); 		
 	} 	 	
 	/** 
 	 * Increment the writing point so as to point at the begining of the next row. 
 	 */
	private void 	newRow() {
		writepoint.y += PaintStatics.AVERAGELEAD;
		writepoint.x = PaintStatics.PAGESTART.x;
	}
		
 	/**
 	 * Do a new layout of the whole theorem, relating to the current state of this canvas.
 	 */
 	public void 	describeTheorem() {

		if (!theorem.isEmptyTheorem()) {

	 		Described firstformal = theorem.firstFormal();
	 		
	 		cursor.setFrame(firstformal.description());
	 			 		
	 		this.setWritecursor(cursor);

			DStatement firststatement = theorem.firstStatement();

			firststatement.setWritepoint(writepoint);			
			firststatement.getFirst().setWritepoint((Point2D.Double) PaintStatics.PAGESTART.clone());
			
 			this.describeTheoremTail(firststatement);		
 			this.newCursor();
 			
 			drawn = theorem.lastFormal();
 		}		
 	}
	/**
 	 * Do a new layout of the ending part of the theorem, starting att statement given as argument. 
 	 * 
 	 * @param included	The statement that begins the tail to be re-layouted.
 	 */
 	public void 	describeTheoremTail(DStatement included) {
		
 		// empty statements can't have tail
 		if (included.isEmpty()) return; 				
 		
 		this.setWritecursor(included.getFirst().description()); 			
 		
 		Point2D.Double restore = writepoint;
 		
 		this.describeStatements(included);
 			
 		writepoint.setLocation(restore);
 		
 		return; 		
 	}
 	
	private void 	describeStatements(DStatement included) {
	
		theorem.addLast(theorem.getPreliminary());

		int start = theorem.indexOf(included);

		Iterator<DStatement> it = theorem.listIterator(start);

		painting = false;
		
		while (it.hasNext()) 
			this.describeStatement(it.next());
	
		painting = true;

		theorem.removeLast();
	}	 	
	/**
	 * Redescribes a statement of the theorem. Lays out the statement to fit this canvas.
	 * 
	 * REMARK: Does not restore cursor, just leaves it at the end.
	 *
	 * @param statement The statement to lay out and redescribe.
	 */
 	public void 	describeStatement(DStatement statement) {			

 		if (statement.isEmpty()) return;
 		
		Iterator<Described> it = statement.iterator();
		
		while (it.hasNext()) 
			this.fillAndProceed(it.next(), null, !ViewStatics.PAINT);
		
		statement.setWritepoint(statement.getFirst().getWritepoint());			
	}

	/**
	 * Toggle editing mode. Starts a new editing aspect or closes, tidies up and leaves the existing editing aspect.
	 */
	public void 	toggleEditingMode() {

		DStatement 			chosen 	= theorem.getChosen();
		DEditableStatement 	editing = theorem.getEditing();

		if (editing == null) {								// start new editing aspect of chosen statement 

			if (chosen != null && chosen.size() > 0) {				// someting to edit

				editing = theorem.edit();							// new aspect of chosen

				Described first = editing.current();
				
				this.setWritecursor(first.description());
				this.fillCursor(first, null, ViewStatics.PAINT);
			} 												
			
		} else {										// edited statement is larger than two
														// finish up and close editing aspect, make chosen statement and canvas ok to leave
			if (editing.current().isDummy()) 				
				editing.deleteCurrent();												// perhaps delete dummy 
			else 
				if (this.isPrompting() && editing.current() != drawn && drawn != null)	// or insert wysiwyg
					editing.replaceCurrent(drawn);										
			
			editing.togglePrompting();													// close prompt, dummy already removed

			DStatement previous = theorem.getPreviousStatement(editing.whole());		// perhaphs null

			this.describeTheoremTail(previous);											// handles all cases

			theorem.leaveEditing();
			
			Described last = theorem.lastFormal();
		
			this.setWritecursor(last.description());									// reset cursor
			this.fillAndProceed(last, editing.current(), !ViewStatics.PAINT);			

			editing = null;																// editing no more
		}		
	}
	/**
	 * Checks if the editing aspect exists and are prompting for a new primitive to insert.
	 *
	 * @return true, if is prompting and editing. Otherwise false.
	 */
	public boolean 	isPrompting() {

		DEditableStatement editing = theorem.getEditing();

		return editing != null ? editing.prompting : false;
	}
	
	/**
	 * This update implementation erases apropriately, paints the common base line, redraw the whole theorem and it's 
	 * preliminary statement. Then calls paint for the last finishing things to paint.
	 */
	public void update(Graphics g) {		

		Graphics2D g2dc = (Graphics2D) g.create();

		if (degrowth) {
			degrowth = false;
			PaintStatics.clearEndOfLine(g2dc,cursor.getBounds());
		}

		paintBaseline(g2dc);

		theorem.draw(g2dc);			
		theorem.getPreliminary().draw(g2dc);
				
		this.paint(g2dc); 
	}
		
	/**
	 * The paint method is now very simple. It erases only once, draw the lst filled in formal and it's cursor.
	 */
	public void paint(Graphics g) {
    	
		Graphics2D g2dc = (Graphics2D) g.create();
		
		if (painting) { 			
			paintDrawnAndErase(g2dc);
			paintCursor(g2dc);						
			//paintEditing(g2dc);
		}
  	}

	
	private void 	paintCursor(Graphics2D g2dc) {
		cursor.draw(g2dc);
	}

	private void 	paintDrawnAndErase(Graphics2D g2d) {
		
		if (drawn!=null) drawn.draw(g2d);
		else
			if (erase!=null) {
				erase.setErase();
				erase.draw(g2d);
			}
	}

	private void 	paintEditing(Graphics2D g2dc) {
		if (theorem.isEdited()) 
			theorem.getEditing().draw(g2dc);
	}

	private void 	paintBaseline(Graphics2D gcopy) {

		Point2D.Double start = PaintStatics.PAGESTART;
		
		gcopy.setColor(Color.black);
		gcopy.drawLine((int)start.x, (int)start.y, (int)(start.x + this.getWidth()), (int)start.y);
	}
}