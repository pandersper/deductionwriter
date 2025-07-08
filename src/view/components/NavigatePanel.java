package view.components;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.Color;

import javax.swing.border.BevelBorder;
import javax.swing.JButton;
import javax.swing.border.SoftBevelBorder;

import model.logic.Implication;
import view.DeductionFrame;
import view.SidePanel;
import view.abstraction.TraversablePanel;
import model.description.abstraction.Described;
import model.description.abstraction.Placeholder;
import model.description.DComposite;
import model.description.DEditableStatement;
import model.description.DPrimitive;
import model.description.DCursor;
import model.description.DStatement;
import model.description.DTheorem;
import control.statics.ViewStatics;
import control.statics.ViewStatics.Mode;


public class NavigatePanel extends TraversablePanel {

	private Implication toggled;
	private JButton minimalbutton;

	/**
	 * Panel for navigation in deductions.
	 */
	public NavigatePanel(DeductionFrame parent) {

		this.parent = parent;
		
		this.setMaximumSize(ViewStatics.navpnlsize);

		makeButtons();
	}

	
	public void navigate(boolean forward) {

		DisplayCanvas current = this.getCanvas();
		
		DTheorem theorem = current.getTheorem();
				
		if (!theorem.isEdited()) { 							// common case outside edit
			
			if (!theorem.isEmptyTheorem()) {			
				theorem.moveChosen(forward);
			}
			
		} else { 											// in editing mode

			DEditableStatement edit = theorem.getEditing();

			if (current.isPrompting()) { 					// just passing on from prompting
				
				edit.togglePrompting();
				
				current.describeTheoremTail(edit.whole());	
			}

			Described position = null, movedto;

			Point2D.Double localorigo = null, offset = null;

			if (edit.isSublevel()) {						// in a composite glyph

				position = edit.current();
			
				localorigo = position.getWritepoint();		assert (position instanceof DComposite);

				Placeholder component = forward ? ((DComposite) position).nextPlaceholder()
												: ((DComposite) position).previousPlaceholder();

				movedto = component.described();
				offset  = movedto.getLocalReference();

			} else																// common edit navigation
				movedto = forward ? edit.next() : edit.previous();

			current.setWritecursor(movedto.description(), localorigo, offset); 	// next cursor in all cases
			current.fillCursor(movedto, position, !Mode.PAINT);
		}
	}
		
	
	public void dropPrimitive() {

		DisplayCanvas canvas = this.getCanvas();

		canvas.degrowth = true;
		
		DTheorem theorem = canvas.getTheorem();
		
		if (canvas.getDrawn() != null) {

			canvas.emptyCursor();

		} else 
			if (!theorem.isEmptyTheorem()) {
				
				canvas.newCursor();

				Described removed = theorem.removeLastPrimitive();
					
				canvas.fillCursor(removed, null, Mode.PAINT);
		}

		return;
	}
	
	public void dropStatement() {

		DisplayCanvas canvas = this.getCanvas();

		canvas.degrowth = true;

		DTheorem 	theorem = canvas.getTheorem();
		DStatement 	dropped = theorem.deleteLastStatement();

		Described last = dropped.isEmpty() ? theorem.removeLastPrimitive() : dropped.getFirst();
		
		if (last == null) canvas.reset();
		else {		
			
			DCursor lastcursor = last.description();

			lastcursor.setErase();
			canvas.paint(canvas.getGraphics());
			
			canvas.setWritecursor(lastcursor);		
			canvas.fillCursor(last, null, Mode.PAINT);
			canvas.newCursor();
			canvas.repaint();
		}
	}

	public void newStatement() {

		DisplayCanvas canvas = this.getCanvas();
				
		Implication implication = toggled;

		canvas.newStatement(new DPrimitive(implication));
	}

	
	public void ordinaryDelete() {
		
		DisplayCanvas canvas = this.getCanvas();	

		DTheorem theorem = canvas.getTheorem();				
		DStatement delete = theorem.getChosen();
		
		if (delete != null) {

			DStatement chosen = theorem.deleteStatement(delete);				
			
			if (chosen == theorem.firstStatement()) {
				canvas.reset();
				canvas.describeStatement(chosen);
			}
							
			canvas.describeTheoremTail(chosen);
		} 
		
		canvas.newCursor();
	}

	public void editDelete(DEditableStatement edit) {

		Described remove = edit.current();																									///(83D2)

		DisplayCanvas canvas = this.getCanvas();

		DTheorem theorem = canvas.getTheorem();
		
		if (edit.isSingleton()) {																											///(04A2)

			if (remove.isDummy()) {
				
				theorem.deleteStatement(edit.whole());																						///(3895)

				canvas.toggleEditingMode();																									///(C717)
				canvas.describeTheoremTail(null);		
				canvas.newCursor();

				return;																														///(19E5)
				
			} else 	 
				edit.replaceCurrent(DPrimitive.DUMMY.clone());																			///(E7A0)

		} else 																																///(2CFF)
			edit.deleteCurrent();	
		
		DStatement previous = theorem.getPreviousStatement(edit.whole()); 																			///(9A5A)

		canvas.describeTheoremTail(previous);																									///(F209)
		canvas.setWritecursor(edit.current().description());																								///(5B71)

		return;
	}
	
	public void insertDummy(DEditableStatement edit) {

		Described dummy = DPrimitive.DUMMY.clone();

		dummy.setWritepoint(edit.current().getWritepoint());

		edit.insertBeforeCurrent(dummy);
	}

	
	public void setImplication(Implication implication) {
		
		toggled = implication;

		this.getCanvas().fillCursor(new DPrimitive(implication),null,Mode.PAINT);
	}
	
	public Rectangle getMinimalButtonBounds() {
		return minimalbutton.getBounds();
	}

	public void registerButtons(SidePanel listener) {

		Component[] components = this.getComponents();

		for (Component button : components)
			if (button instanceof JButton)
				((JButton)button).addActionListener(listener);
	}

		
	private void makeButtons() {

		this.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));

		JButton dummy1 = new JButton("");
		
		JButton btnBackward = new JButton("<<");			btnBackward.setActionCommand("backward");
		JButton btnForward 	= new JButton(">>");			btnForward.setActionCommand("forward");
		JButton btnNext 	= new JButton("next");			btnNext.setActionCommand("next");
		JButton btnEditUp 	= new JButton("  out of  ");	btnEditUp.setActionCommand("edit up");
		JButton btnEditDown = new JButton("into");			btnEditDown.setActionCommand("edit down");
		JButton btnInsert 	= new JButton("insert");		btnInsert.setActionCommand("insert");
		JButton btnDelete 	= new JButton("delete");		btnDelete.setActionCommand("delete");

		JButton[] buttons = new JButton[] { btnDelete, btnBackward, btnForward, btnInsert, dummy1, btnEditUp, btnEditDown, btnNext };

		this.setLayout(new GridLayout(2, 4, ViewStatics.btnhgap, ViewStatics.btnvgap));

		for (JButton button : buttons) {
			button.setMargin(ViewStatics.btnInset);
			button.setBackground(ViewStatics.btnBkgr);
			button.setFont(ViewStatics.btnFontBold);
			add(button);
		}

		btnNext.setBackground(Color.green);

		dummy1.setEnabled(false);

		this.minimalbutton = btnEditUp;
	}
}