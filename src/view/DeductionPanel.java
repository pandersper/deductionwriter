package view;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import control.DeductionWriter.CustomKeyboardFocusManager;
import control.Toolbox;
import model.description.DComposite;
import model.description.DEditableStatement;
import model.description.DPrimitive;
import model.description.DStatement;
import model.description.DTheorem;
import model.description.abstraction.Described;
import model.description.abstraction.Placeholder;
import model.logic.Implication;
import model.logic.Implication.ImplicationType;
import view.abstraction.TraversablePanel;

/**
 * This panel is used for editing the theorem.
 */
public class DeductionPanel extends TraversablePanel implements ActionListener {

	private DTheorem 		theorem;	
		
	/**
	 * Instantiates a new deduction panel.
	 *
	 * @param display The canvas that draws the theorem.
	 * @param primitives Panel containing the primitives used in the theorem.
	 */
	public DeductionPanel(DisplayCanvas display, PrimitivesPanel primitives) {

		this.pnlPrimitives = primitives;
		this.canvas = display;
				
		makeLayoutAndComponents();
	}

	/**
	 * Manages most of the control functionality in this class. 
	 */
 	public void actionPerformed(ActionEvent e) {
				
		switch (e.getActionCommand()) {		
			
			case "done": 			newStatement();		break;
			case "drop primitive": 	dropPrimitive(); 	break;
			case "drop statement": 	dropStatement(); 	break;				
			case "forward":			navigate(true);		break;
			case "backward": 		navigate(false); 	break;

			case "edit down":
				
				if (theorem.getChosen() != null) { 																							///(G2CC)
					
					if (theorem.isEdited()) {																								///(E0D9)
						
						DEditableStatement edit = theorem.getEditing();
						
						Described descend = edit.descend();
						
						if (descend != null) {																								///(1B8G)
							
							Placeholder last = ((DComposite)descend).lastPlaceholder();
							
							canvas.setWritepoint(last.described(), descend.getGlobalReference(), last.baseline().getLocalReferencepoint());					
							canvas.fillCursor(last.described(), true, descend);

						} else 	
							canvas.setPaintMode(true, false, true);																			///(AA1G)
					} else {
						canvas.toggleEditingMode();																							///(A207)
						canvas.setPaintMode(false, false, false);
					}
				}
				
				break;

			case "edit up":
				
				if (theorem.isEdited()) { 																									///(968E)
					
					DEditableStatement edit = theorem.getEditing();

					Described ascend = edit.ascend();
					
					if (ascend != null)	{																									///(C018)
						canvas.setWritepoint(ascend);
						canvas.fillCursor(ascend, true, null);

					} else {	
						canvas.toggleEditingMode();																							///(8F71)
						canvas.setPaintMode(false, false, false);
					}
				} 
																																			///(D4A2)
				break;

			case "insert":																													///(25CA)
				
				DEditableStatement edit = theorem.getEditing();

				if (theorem.isEdited() && !edit.current().isDummy()) { 	

					insertDummy(edit);

					edit.togglePrompting();																									///(D748)

					canvas.redescribeTail(edit.whole());	
					
					canvas.setPaintMode(false, false, false);

				} else { return; } 																											///(24BB)

				break;
				
			case "delete":	
				
				if (canvas.isPrompting()) break;

				if (theorem.isEdited()) 																										///(4BBB)
					editDelete(theorem.getEditing());
				else 																														///(59FG)
					ordinaryDelete();
				
				canvas.setPaintMode(false, false, false);
				break;
			
			default:
				break;
		}

		canvas.paint(canvas.getGraphics());

		theorem.printout();
		
		pnlPrimitives.restoreFocus();
		
	}
		
	/**
	 * Sets the current work piece theorem.
	 *
	 * @param dtheorem The described theorem to work on.
	 */
 	public void setTheorem(DTheorem dtheorem) {

 		this.theorem = dtheorem;

 		if (this.theorem != null)
 			theorem.clearChosen();
 	}
 	
	/**
	 * Sets the default component to focus when receiving focus.
	 */
 	public void setDefaultComponent() {

 		this.defaultfocus = btnDone;
 	}
	
 	/** {@inheritDoc} */
 	public void setFocusTraversal(CustomKeyboardFocusManager manager) {

 		this.manager = manager;	
 		this.setFocusable(true);
 		this.setFocusCycleRoot(true);

 		this.defaultfocus = this.getParent();
 	}
	
 	/** {@inheritDoc} */
 	public Component[][] focusCycleNodes() {
 		return new Component[][] { new Component[] { this.getParent() } }; 
 	}

 	
	private void insertDummy(DEditableStatement edit) {

		Described dummy = Toolbox.DUMMYCURSOR.clone();
		
		dummy.description().setLocation(edit.current().description().getLocation());

		edit.insertBeforeCurrent(dummy);  
	}

	private void newStatement() {

		if (theorem.getPreliminary().size() == 0) return;
		
		Implication implication = lstImplication.getItemAt(lstImplication.getSelectedIndex());
				
		canvas.newStatement(new DPrimitive(implication));	
	}

	private void dropStatement() {

		Rectangle bounds = theorem.lastStatement().getBounds().union(canvas.cursor().description());

		DStatement dropped = theorem.deleteLastStatement();		
		
		Described last = dropped.isEmpty() ? theorem.removeLastPrimitive() : dropped.getFirst();

		canvas.setClearArea(bounds);
		canvas.paint(canvas.getGraphics());

		Described reset = last != null ? last : DisplayCanvas.startCursor();
		
		canvas.setWritepoint(reset);
		canvas.fillCursor(reset, true, reset);
		canvas.setPaintMode(false, true, false);
	}

	private void dropPrimitive() {
		
		if (canvas.getDrawn() != null) {

			canvas.emptyCursor();
						
		} else if (!theorem.isEmptyTheorem()) {	
			 
			Described removed = theorem.removeLastPrimitive();
					
			canvas.setWritepoint(removed);
			canvas.fillCursor(removed, false, null);			
		}
		
		return;
	}

	private void navigate(boolean forward) {

		if (!theorem.isEdited()) {																											///(C3F0)

			if (!theorem.isEmptyTheorem()) {				
					canvas.unionAltered(theorem.getChosen());
					theorem.moveChosen(forward);
					canvas.unionAltered(theorem.getChosen());
			}
			
		} else {																															///(45FB)
			
			DEditableStatement edit = theorem.getEditing();	
						
			if (canvas.isPrompting()) {																										///(0614)
				edit.togglePrompting();	
				canvas.redescribeTail(edit.whole());
				canvas.setPaintMode(false, false, false);
				canvas.paint(canvas.getGraphics());
				
			}

			Described position = null, movedto;

			Point localorigo = null, offset = null;

			if (edit.isSublevel()) {

				position = edit.current();
				localorigo = position.getGlobalReference();

				assert(position instanceof DComposite);
				
				Placeholder component = forward ? ((DComposite) position).nextPlaceholder() : 
					  ((DComposite) position).previousPlaceholder();

				movedto = component.described();
				offset = component.baseline().getLocalReferencepoint();

			} else				
				movedto = forward ? edit.next() : edit.previous();

						
			canvas.setWritepoint(movedto, localorigo, offset);																									///(81A1)
			canvas.fillCursor(movedto, false, movedto);
			canvas.setPaintMode(true, false, false);
			canvas.paint(canvas.getGraphics());
			canvas.setPaintMode(false, false, false);
			canvas.paint(canvas.getGraphics());
		}
	}

	private void editDelete(DEditableStatement edit) {

		Described remove = edit.current();																												///(83D2)

		DStatement previous = theorem.getPrevious(edit.whole()); 																			///(9A5A)
		
		if (edit.isSingleton()) {																											///(04A2)

			if (remove.isDummy()) {
				
				theorem.deleteStatement(edit.whole());																						///(3895)

				canvas.toggleEditingMode();																									///(C717)
				canvas.redescribeTail(null);		
				canvas.newCursor();

				return;																														///(19E5)
				
			} else 	 
				edit.replaceCurrent(Toolbox.DUMMYCURSOR.clone());																						///(E7A0)

		} else 																																///(2CFF)
			edit.deleteCurrent();	
		
		canvas.redescribeTail(previous);																										///(F209)
		canvas.setWritepoint(edit.current());																							///(5B71)
		canvas.setPaintMode(false, false, false);																							///(G0A3)
		canvas.paint(canvas.getGraphics());

	}

	private void ordinaryDelete() {
		
		if (theorem.getChosen() != null) {
			
			DStatement before = theorem.deleteStatement(theorem.getChosen());						
						
			canvas.redescribeTail(before);
			canvas.newCursor();
		} 
	}

	/* * AWT AND SWING * */
	
	private DisplayCanvas 	canvas;	
	
	private PrimitivesPanel pnlPrimitives;	

	private JLabel			lblNavigate, lblImplication, lblDone; 	
	private JButton 								btnBackward, btnForward, 
							btnDone, 				btnEditUp, 	 btnEditDown,
							btnDropP, btnDropS, 	btnInsert, 	 btnDelete;

	private JComboBox<Implication> lstImplication = new JComboBox<Implication>(
			new Implication[] { Implication.makeValue(ImplicationType.LEFT),
								Implication.makeValue(ImplicationType.RIGHT),
								Implication.makeValue(ImplicationType.EQUIV) }
	);
	
	private JLabel[] labels;
	private JButton[] buttons;	

	
	private void makeOverallLayout(GridBagLayout gridbag) {

    	gridbag.columnWidths 	= new int[] {100, 100, 100, 0};
		gridbag.rowHeights 		= new int[] {15, 40, 40, 0, 0};
		gridbag.columnWeights 	= new double[] {0.0, 0.0, 0.0, 0.0};
		gridbag.rowWeights 		= new double[] {0.0, 0.0, 0.0, 0.0, 0.0};		
    }

	private void makeLayoutAndComponents() {
				
		setAutoscrolls(true);
		setBorder(null);

		GridBagLayout gridbag = new GridBagLayout();
	
		makeOverallLayout(gridbag);
		setLayout(gridbag);

		makeAndAddLabels();
		layoutAndAddList();
		makeAndAddButtons();
		
	}

	private void makeAndAddButtons() {

		btnBackward = new JButton("Backward");			btnBackward.setActionCommand("backward");
		btnForward 	= new JButton("Forward");			btnForward.setActionCommand("forward");
		btnDone 	= new JButton("Done");				btnDone.setActionCommand("done");
		btnEditUp 	= new JButton("Edit up");			btnEditUp.setActionCommand("edit up");
		btnEditDown = new JButton("Edit down");			btnEditDown.setActionCommand("edit down");
		btnDropP 	= new JButton("Drop primitive");	btnDropP.setActionCommand("drop primitive");
		btnDropS 	= new JButton("Drop statement");	btnDropS.setActionCommand("drop statement");
		btnInsert 	= new JButton("Insert");			btnInsert.setActionCommand("insert");
		btnDelete 	= new JButton("Delete");			btnDelete.setActionCommand("delete");

		buttons = new JButton[] { btnBackward, btnForward, btnEditDown, btnEditUp, btnDone, btnDropP, btnDropS, btnInsert, btnDelete };

		for (JButton button : buttons)
			button.addActionListener(this);	

		GridBagConstraints gbc_btn = new GridBagConstraints();
		
		gbc_btn.fill = GridBagConstraints.HORIZONTAL;
		gbc_btn.insets = new Insets(5, 5, 5, 5);
		gbc_btn.anchor = GridBagConstraints.NORTHWEST;
		
		gbc_btn.gridx = 2;	gbc_btn.gridy = 1;		add(btnBackward, gbc_btn);
		gbc_btn.gridx = 3;	gbc_btn.gridy = 1;		add(btnForward, gbc_btn);		
		
		gbc_btn.gridx = 1;	gbc_btn.gridy = 2;		add(btnDone, gbc_btn);
		gbc_btn.gridx = 2;	gbc_btn.gridy = 2;		add(btnEditUp, gbc_btn);
		gbc_btn.gridx = 3;	gbc_btn.gridy = 2;		add(btnEditDown, gbc_btn);
		
		gbc_btn.gridx = 0;	gbc_btn.gridy = 3;		add(btnDropP, gbc_btn);		
		gbc_btn.gridx = 1;	gbc_btn.gridy = 3;		add(btnDropS, gbc_btn);		
		gbc_btn.gridx = 2;	gbc_btn.gridy = 3;		add(btnInsert, gbc_btn);
		gbc_btn.gridx = 3;	gbc_btn.gridy = 3;		add(btnDelete, gbc_btn);
	}
	
	private void layoutAndAddList() {

		lstImplication.setSelectedIndex(2);
		lstImplication.setName("lstImplication");
		lstImplication.addKeyListener(pnlPrimitives);
		
		GridBagConstraints gbc_choImplication = new GridBagConstraints();
		gbc_choImplication.insets = new Insets(2, 2, 5, 5);
		gbc_choImplication.anchor = GridBagConstraints.NORTHWEST;
																	gbc_choImplication.gridx = 0;
																	gbc_choImplication.gridy = 1;
		
		add(lstImplication, gbc_choImplication);
	}
	
	private void makeAndAddLabels() {

		lblNavigate 	= new JLabel("Navigate and edit:");
		lblImplication 	= new JLabel("Ending implication:");
		lblDone 		= new JLabel("Statement finished:");

		labels = new JLabel[] { lblNavigate, lblImplication, lblDone };

		for (JLabel label : labels) {	
			label.setAlignmentX(Component.LEFT_ALIGNMENT);
			label.setVerticalAlignment(SwingConstants.BOTTOM);
		}

		GridBagConstraints gbc_lbl = new GridBagConstraints();
		gbc_lbl.insets = new Insets(2, 2, 5, 5);
		gbc_lbl.anchor = GridBagConstraints.NORTHWEST;
			
		gbc_lbl.gridx = 0;	gbc_lbl.gridy = 0; 		add(lblImplication, gbc_lbl);	
		gbc_lbl.gridx = 2;	gbc_lbl.gridy = 0;		add(lblNavigate, gbc_lbl);
		gbc_lbl.gridx = 0;	gbc_lbl.gridy = 2;		gbc_lbl.ipadx = 10; add(lblDone, gbc_lbl);
	}

}
