package view;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.Color;

import javax.swing.border.BevelBorder;
import javax.swing.JButton;
import javax.swing.border.SoftBevelBorder;

import model.logic.Implication;
import view.abstraction.TraversablePanel;
import view.components.DisplayCanvas;
import view.components.ViewConstants;
import model.description.abstraction.Described;
import model.description.abstraction.Placeholder;
import model.description.DComposite;
import model.description.DEditableStatement;
import model.description.DPrimitive;
import model.description.DStatement;
import control.Toolbox;


public class NavigatePanel extends TraversablePanel {

	private Implication toggled;
	private JButton minimalbutton;

	/**
	 * Panel for navigation in deductions.
	 */
	public NavigatePanel(DeductionFrame parent) {

		this.parent = parent;
		
		makeButtons();
	}


	
	
	public void setImplication(Implication implication) {
		toggled = implication;
		this.getCanvas().fillCursor(new DPrimitive(implication),true,null);
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

	
	public void navigate(boolean forward) {

		DisplayCanvas current = this.getCanvas();
		
		if (!this.getTheorem().isEdited()) { /// (C3F0)
		
			if (!this.getTheorem().isEmptyTheorem()) {
				current.unionAltered(this.getTheorem().getChosen());
				this.getTheorem().moveChosen(forward);
				current.unionAltered(this.getTheorem().getChosen());
			}

		} else { /// (45FB)

			DEditableStatement edit = this.getTheorem().getEditing();

			if (current.isPrompting()) { /// (0614)
				edit.togglePrompting();
				current.redescribeTail(edit.whole());
				current.setPaintMode(false, false, false);
				current.paint(current.getGraphics());

			}

			Described position = null, movedto;

			Point localorigo = null, offset = null;

			if (edit.isSublevel()) {

				position = edit.current();
				localorigo = position.getGlobalReference();

				assert (position instanceof DComposite);

				Placeholder component = forward ? ((DComposite) position).nextPlaceholder()
						: ((DComposite) position).previousPlaceholder();

				movedto = component.described();
				offset = component.baseline().getLocalReferencepoint();

			} else
				movedto = forward ? edit.next() : edit.previous();

			current.setWritepoint(movedto, localorigo, offset); /// (81A1)
			current.fillCursor(movedto, false, movedto);
			current.setPaintMode(true, false, false);
			current.paint(current.getGraphics());
			current.setPaintMode(false, false, false);
			current.paint(current.getGraphics());
		}
	}

	
	public void dropPrimitive() {

		DisplayCanvas current = this.getCanvas();

		if (current.getDrawn() != null) {

			current.emptyCursor();

		} else if (!this.getTheorem().isEmptyTheorem()) {

			Described removed = this.getTheorem().removeLastPrimitive();

			current.setWritepoint(removed);
			current.fillCursor(removed, false, null);
		}

		return;
	}
	
	public void newStatement() {

		if (this.getCanvas().getDrawn()==null) return;
		if (this.getTheorem().getPreliminary().size() == 0)	return;

		Implication implication = toggled;

		this.getCanvas().newStatement(new DPrimitive(implication));
	}

	public void dropStatement() {

		DisplayCanvas current = this.getCanvas();

		Rectangle bounds = this.getTheorem().lastStatement().getBounds().union(current.cursor().description());

		DStatement dropped = this.getTheorem().deleteLastStatement();

		Described last = dropped.isEmpty() ? this.getTheorem().removeLastPrimitive() : dropped.getFirst();
		
		current.setClearArea(bounds);
		current.paint(current.getGraphics());

		Described reset = last != null ? last : DisplayCanvas.startCursor();

		current.setWritepoint(reset);
		current.fillCursor(reset, true, reset);
		current.setPaintMode(false, true, false);
	}

	
	public void ordinaryDelete() {
		
		if (this.getTheorem().getChosen() != null) {

			DisplayCanvas current = this.getCanvas();

			DStatement before = this.getTheorem().deleteStatement(this.getTheorem().getChosen());						
						
			current.redescribeTail(before);
			current.newCursor();
		} 
	}

	public void editDelete(DEditableStatement edit) {

		Described remove = edit.current();																									///(83D2)

		DStatement previous = this.getTheorem().getPrevious(edit.whole()); 																			///(9A5A)

		DisplayCanvas current = this.getCanvas();

		if (edit.isSingleton()) {																											///(04A2)

			if (remove.isDummy()) {
				
				this.getTheorem().deleteStatement(edit.whole());																						///(3895)

				current.toggleEditingMode();																									///(C717)
				current.redescribeTail(null);		
				current.newCursor();

				return;																														///(19E5)
				
			} else 	 
				edit.replaceCurrent(Toolbox.DUMMYCURSOR.clone());																						///(E7A0)

		} else 																																///(2CFF)
			edit.deleteCurrent();	
		
		current.redescribeTail(previous);																										///(F209)
		current.setWritepoint(edit.current());																							///(5B71)
		current.setPaintMode(false, false, false);																							///(G0A3)
		current.paint(current.getGraphics());

	}

	public void insertDummy(DEditableStatement edit) {

		Described dummy = Toolbox.DUMMYCURSOR.clone();

		dummy.description().setLocation(edit.current().description().getLocation());

		edit.insertBeforeCurrent(dummy);
	}

		
	private void makeButtons() {

		setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));

		JButton dummy1 = new JButton("");
		
		JButton btnBackward = new JButton("<<");			btnBackward.setActionCommand("backward");
		JButton btnForward 	= new JButton(">>");			btnForward.setActionCommand("forward");
		JButton btnNext 	= new JButton("next");			btnNext.setActionCommand("next");
		JButton btnEditUp 	= new JButton("  out of  ");	btnEditUp.setActionCommand("edit up");
		JButton btnEditDown = new JButton("into");			btnEditDown.setActionCommand("edit down");
		JButton btnInsert 	= new JButton("insert");		btnInsert.setActionCommand("insert");
		JButton btnDelete 	= new JButton("delete");		btnDelete.setActionCommand("delete");

		JButton[] buttons = new JButton[] { btnDelete, btnBackward, btnForward, btnInsert, dummy1, btnEditUp, btnEditDown, btnNext };


		setLayout(new GridLayout(2, 4, ViewConstants.btnhgap, ViewConstants.btnvgap));

		for (JButton button : buttons) {
			button.setMargin(ViewConstants.btnInset);
			button.setBorder(new BevelBorder(BevelBorder.RAISED));
			button.setBackground(ViewConstants.btnBkgr);
			button.setFont(ViewConstants.btnFontBold);
			add(button);
		}

		btnNext.setBackground(Color.green);

		dummy1.setEnabled(false);

		minimalbutton = btnEditUp;
	}
}