package view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;

import control.DeductionWriter.CustomKeyboardFocusManager;
import control.statics.ViewStatics;
import model.description.DComposite;
import model.description.DEditableStatement;
import model.description.DTheorem;
import model.description.abstraction.Described;
import model.description.abstraction.Placeholder;
import model.logic.Implication;
import view.abstraction.TraversablePanel;
import view.components.ConcludePanel;
import view.components.DisplayCanvas;
import view.components.GlyphsPanel;
import view.components.NavigatePanel;
import view.components.ConcludePanel.ImplicationToggle;
import view.components.ConcludePanel.ToggleGroup;

/**
 * This panel is the full control panel of the application, editing and navigating the theoreom in the currently 
 * selected tab.
 */
public class SidePanel extends TraversablePanel implements ActionListener {


	private NavigatePanel	pnlNavigate;
	private ConcludePanel	pnlConclude;
	private GlyphsPanel		pnlGlyphs;
	
	private ToggleGroup 	toggles;

	
	/**
	 * Instantiates a new deduction panel.
	 *
	 * @param original The canvas that draws the theorem.
	 * @param glyphs Panel containing the primitives used in the theorem.
	 */
 	public SidePanel(DeductionFrame parent, GlyphsPanel glyphs, NavigatePanel navigation, ConcludePanel conclude) {

 		this.parent = parent;
		this.pnlGlyphs = glyphs;
 		this.pnlNavigate = navigation;
 		this.pnlConclude = conclude;

 		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.setPreferredSize(new Dimension(350, 3*ViewStatics.a4height));

		navigation.registerButtonsListener(this);
		conclude.registerToggleButtonsListener(this);

		pnlConclude.addKeyListener(this.pnlGlyphs);
	}
 	
	/**
	 * Manages most of the control functionality in this class. 
	 */
 	public void actionPerformed(ActionEvent e) {

 		DisplayCanvas canvas = this.getCanvas();
 		
 		DTheorem theorem = canvas.getTheorem();
 		
		switch (e.getActionCommand()) {		
			
			case "done": 			pnlNavigate.newStatement();		break;
			case "next": 			pnlGlyphs.newGlyph();			break;
			case "drop primitive": 	pnlNavigate.dropFormal(); 		break;
			case "drop statement": 	pnlNavigate.dropStatement(); 	break;				
			case "forward":			pnlNavigate.navigate(true);		break;
			case "backward": 		pnlNavigate.navigate(false); 	break;

			case "edit down":
				
				if (theorem.getChosen() != null) {
				
					if (theorem.isEdited()) {
						
						DEditableStatement edit = theorem.getEditing();
						
						Described composite = edit.descend();
						
						if (composite != null) {
							
							Placeholder placeholder = ((DComposite)composite).currentPlaceholder();
							
							Described primitive = placeholder.described();
							
							canvas.setWritecursor(primitive.description(), composite.getWritepoint(), primitive.getLocalReference());					
							canvas.fillCursor(primitive, composite, ViewStatics.PAINT);

						} else {}
					} else 
						canvas.toggleEditingMode();	
				}
				
				break;

			case "edit up":
				
				if (theorem.isEdited()) {
					
					DEditableStatement edit = theorem.getEditing();

					Described ascend = edit.ascend();
					
					if (ascend != null)	{
						canvas.setWritecursor(ascend.description());
						canvas.fillCursor(ascend, null, ViewStatics.PAINT);

					} else 
						canvas.toggleEditingMode();
				} 

				break;

			case "insert":
				
				DEditableStatement edit = theorem.getEditing();

				if (theorem.isEdited() && !edit.current().isDummy()) { 	

					edit.insertDummy();
					edit.togglePrompting();

					canvas.describeTheoremTail(edit.whole());	
					
				} else { break; } 

				break;
				
			case "delete":	
				
				if (canvas.isPrompting()) break;

				if (theorem.isEdited()) 
					pnlNavigate.editDelete(theorem.getEditing());
				else 
					pnlNavigate.ordinaryDelete();
				
				break;
			
			case "toggle":
				
				ImplicationToggle sender = (ImplicationToggle) e.getSource();
				
				Implication change = toggles.chooseToggle(sender.getImplication());
				
				pnlNavigate.setImplication(change);
				
				break;
				
			default:
				break;
		}

		canvas.repaint();

		getTheorem().printout();
		
		pnlGlyphs.restoreFocus();
	}	
 	 	
	/**
	 * Sets the default component to focus when receiving focus.
	 */
 	public void setDefaultComponent() {
 		this.defaultfocus = pnlConclude.getDefaultComponent();
 	}

 	/**
 	 * Sets the shared toggle buttons group.
 	 *  
 	 * @param toggles The group of toggle buttons to switch between implications.
 	 */
 	public void setToggles(ConcludePanel.ToggleGroup toggles) {
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
