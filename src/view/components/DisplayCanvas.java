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
import control.statics.ViewStatics.Mode;
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
 * The canvas is particularly 'view part of the application' but
 * <b>Remark:</b><i> it encapsulates and shields a core part of the model</i>
 * namely the {@see DTheorem} which is created in it and should be accessed
 * through either this {@see DisplayCanvas} which lays out and draws the theorem
 * or via the {@see Session} which stores all canvases.
 */
public class DisplayCanvas extends Canvas implements CursoredCanvas {
	
	
	public static final Size2D CURSORSIZE = new Size2D(DRectangle.DUMMYRECTANGLE);
	
	private DTheorem			theorem;
	private Described			drawn  = null, erase  = null;	
	
	private DCursor				dcursor 	= PaintStatics.DUMMYCURSOR.clone();
	private Point2D.Double		writepoint 	= dcursor.getWritepoint();
	private double				advance		= dcursor.getAdvance();

	private boolean 			painting = true;
	public boolean				degrowth = false;
	
	/**
	 * Instantiates a new canvas for drawing fantastic math theorems upon.
	 * Every canvas holds one and only one theorem and is the only direct holder of that theorem.
	 * 
	 * @param theorem The theorem that is described by this canvas.
	 */
 	public DisplayCanvas(DTheorem theorem) {

 		this.setSize(ViewStatics.canvasdimension); 		

 		this.theorem = theorem; 		 		

 		this.reset();	 		
 	}
 	
 	/**
 	 * Resets this canvas to a cleared canvas with cursor a start position.
 	 */
	public void 	reset() {

		drawn = null;
		erase = null;
		
		dcursor.setWritepoint((Point2D.Double) PaintStatics.PAGESTART.clone());

		this.setWritecursor(dcursor);		
		this.repaint();
	}
 	
 	/**
	 * Sets the cursor's position, this canvas's write point.
	 * 
	 * @param	movedto 			The described formal who's referencepoint should be the new write point.
	 * 								If this is null the writepoint is set to the startcursor's. 
	 */
 	public void 	setWritecursor(DCursor movedto) {
						
 		Point2D.Double wp = movedto.getWritepoint();
 		
		this.writepoint.setLocation(wp.x, wp.y);
		
		this.advance = movedto.getAdvance();
	}
 	/**
	 * Sets the cursor's position, this canvas's write point.
	 * 
	 * @param	movedto 	If there is no offset (offset is null) set the writepoint to this described formals referencepoint. 
	 * @param	origo	The position in the canvas where the current cursor has it's upper left corner.
	 * @param	offset		The offset, the cursors reference point, from the local origo.
	 */
	public void 	setWritecursor(DCursor movedto, Point2D.Double origo, Point2D.Double offset) {

		advance = movedto.getAdvance();

		if (offset == null) setWritecursor(movedto);
		else 
			writepoint.setLocation(Arithmetic.add(origo, offset));
	}

	public void 	fillCursor(Described formal, Described erased, boolean paint) {

 		// erase the previous and clear current
 		erase = drawn;
 		drawn = null;	
		dcursor.setErase();
 		if (paint) this.paint(this.getGraphics());
		
		// fill in the new
 		formal.setWritepoint(writepoint);
 		drawn = formal; 		
 		advance = drawn.getAdvance();

 		dcursor.setFrame(drawn.description()); 		//dcursor.setFrame(drawn.description().getAscendingBounds());

 		this.repaint();
	}	

	public void 	proceedCursor() {
		
 		if (drawn != null) {
				
 			// erase previous cursor
	 		dcursor.setErase();
	 		if (this.getGraphics()!=null) this.paint(this.getGraphics());
	 		
	 		// move on one position or new row
			if (writepoint.x + 2*advance > ViewStatics.canvasdimension.width - PaintStatics.MARGINS[PaintStatics.RIGHT])
				this.newRow();
			else 
				writepoint.x += advance;
					
			// set up new empty cursor
			dcursor.setFrame(writepoint, CURSORSIZE);	
			dcursor.setWritepoint(writepoint);
 		}
		
		erase = drawn;
		drawn = null;
	}

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
 	 * Finalises and appends the work piece preliminary statement of the theorem to the theorem and creates a 
	 * new preliminary statement to work on. 
	 *
	 * @param implication The implication ending the finalised statement.
	 */
	public void 	newStatement(Described implication) {
		
		implication.setWritepoint(writepoint);
		
		theorem.finalisePreliminary(implication);
												
		proceedCursor();
	}
	
	
	public void 	setTheorem(DTheorem theorem) {
		this.theorem = theorem;
	}
	/** 
	 * The theorem worked on in this canvas. Try to export the theorem only frmo its canvas. They are a couple.
	 * 
	 * @return The theorem beloning to this canvas and only this canvas. Sessions consists of many canvas-theorem pairs.
	 **/
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
 	
 	public void 	newCursor() {
 		
 		Point2D.Double lastwrite = theorem.lastFormal().getWritepoint();

 		dcursor.setFrame(PaintStatics.DUMMYCURSOR);
 		dcursor.setWritepoint(lastwrite);

 		advance = dcursor.getAdvance();
 		
 		writepoint.setLocation(lastwrite);
 		
 		proceedCursor(); 		
 	} 	 	
 	/** Increment the writing point so as to point at the begining of the next row. */
	private void 	newRow() {
		writepoint.y += PaintStatics.AVERAGELEAD;
		writepoint.x = PaintStatics.PAGESTART.x;
	}
		
 	/**
 	 * Do a new layout of the whole theorem
 	 */
 	public void 	describeTheorem() {

		if (!theorem.isEmptyTheorem()) {

	 		Described firstformal = theorem.firstFormal();
	 		
	 		dcursor.setFrame(firstformal.description());
	 			 		
	 		this.setWritecursor(dcursor);

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
	 * @param statement The statement to lay out and redescribe.
	 */
 	public void 	describeStatement(DStatement statement) {			/** DOES NOT RESTORE CURSOR **/

 		if (statement.isEmpty()) return;
 		
		Iterator<Described> it = statement.iterator();
		
		while (it.hasNext()) 
			this.fillAndProceed(it.next(), null, !Mode.PAINT);
		
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
				this.fillCursor(first, null, Mode.PAINT);
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
			this.fillAndProceed(last, editing.current(), !Mode.PAINT);			

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
	
	
	public void update(Graphics g) {		

		Graphics2D g2dc = (Graphics2D) g.create();

		if (degrowth) {
			degrowth = false;
			PaintStatics.clearEndOfLine(g2dc,dcursor.getBounds());
		}

		paintBaseline(g2dc);

		theorem.draw(g2dc);			
		theorem.getPreliminary().draw(g2dc);
				
		this.paint(g2dc); 
	}
		
	public void paint(Graphics g) {
    	
		Graphics2D g2dc = (Graphics2D) g.create();
		
		if (painting && drawn != null) { 			
			paintDrawnAndErase(g2dc);
			paintCursor(g2dc);						
			//paintEditing(g2dc);
		}
  	}

	
	private void 	paintCursor(Graphics2D g2dc) {
		dcursor.draw(g2dc);
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