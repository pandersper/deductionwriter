package view;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import control.DeductionWriter.CustomKeyboardFocusManager;
import model.description.DComposite;
import model.description.DEditableStatement;
import model.description.abstraction.Described;
import model.description.abstraction.Placeholder;
import model.logic.Implication;
import view.ConcludePanel.ImplicationToggle;
import view.ConcludePanel.ToggleGroup;
import view.abstraction.TraversablePanel;
import view.components.DisplayCanvas;

/**
 * This panel is used for editing the theorem.
 */
public class SidePanel extends TraversablePanel implements ActionListener {


	private NavigatePanel	pnlNavigate;
	private ConcludePanel	pnlConclude;
	private GlyphsPanel		pnlGlyphs;
	
	private ToggleGroup 	toggles;
	

	/**
	 * Instantiates a new deduction panel.
	 *
	 * @param canvas The canvas that draws the theorem.
	 * @param glyphs Panel containing the primitives used in the theorem.
	 */
 	public SidePanel(DeductionFrame parent, GlyphsPanel glyphs, NavigatePanel navigation, ConcludePanel conclude) {

 		this.parent = parent;
		this.pnlGlyphs = glyphs;
 		this.pnlNavigate = navigation;
 		this.pnlConclude = conclude;
				
		pnlConclude.addKeyListener(this.pnlGlyphs);
		
	}
 	
	/**
	 * Manages most of the control functionality in this class. 
	 */
 	public void actionPerformed(ActionEvent e) {

 		DisplayCanvas canvas = this.getCanvas();
 		
		switch (e.getActionCommand()) {		
			
			case "done": 			pnlNavigate.newStatement();		break;
			case "next": 			pnlGlyphs.newGlyph();			break;
			case "drop primitive": 	pnlNavigate.dropPrimitive(); 	break;
			case "drop statement": 	pnlNavigate.dropStatement(); 	break;				
			case "forward":			pnlNavigate.navigate(true);		break;
			case "backward": 		pnlNavigate.navigate(false); 	break;

			case "edit down":
				
				if (getTheorem().getChosen() != null) { 																							///(G2CC)
					
					if (getTheorem().isEdited()) {																								///(E0D9)
						
						DEditableStatement edit = getTheorem().getEditing();
						
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
				
				if (getTheorem().isEdited()) { 																									///(968E)
					
					DEditableStatement edit = getTheorem().getEditing();

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
				
				DEditableStatement edit = getTheorem().getEditing();

				if (getTheorem().isEdited() && !edit.current().isDummy()) { 	

					pnlNavigate.insertDummy(edit);

					edit.togglePrompting();																									///(D748)

					canvas.redescribeTail(edit.whole());	
					
					canvas.setPaintMode(false, false, false);

				} else { return; } 																											///(24BB)

				break;
				
			case "delete":	
				
				if (canvas.isPrompting()) break;

				if (getTheorem().isEdited()) 																										///(4BBB)
					pnlNavigate.editDelete(getTheorem().getEditing());
				else 																														///(59FG)
					pnlNavigate.ordinaryDelete();
				
				canvas.setPaintMode(false, false, false);
				
				break;
			
			case "toggle":
				
				ImplicationToggle sender = (ImplicationToggle) e.getSource();
				
				Implication change = toggles.chooseToggle(sender.getImplication());
				
				pnlNavigate.setImplication(change);
				
				break;
				
			default:
				break;
		}

		canvas.paint(canvas.getGraphics());

		getTheorem().printout();
		
		pnlGlyphs.restoreFocus();
		
	}
	
 	 	
	/**
	 * Sets the default component to focus when receiving focus.
	 */
 	public void setDefaultComponent() {
 		this.defaultfocus = pnlConclude.getDefaultComponent();
 	}

 	public void shareToggles(ConcludePanel.ToggleGroup toggles) {
		this.toggles = toggles;	
	}

 	
 	/** {@inheritDoc} */
 	public void setFocusTraversal(CustomKeyboardFocusManager manager) {

 		this.manager = manager;	
 		this.setFocusable(true);
 		this.setFocusCycleRoot(false);

 		this.defaultfocus = this.getParent();
 	}
	
 	/** {@inheritDoc} */
 	public Component[][] focusCycleNodes() {
 		return new Component[][] { new Component[] { this.getParent() } }; 
 	}
	
}
