package view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;

import control.DeductionWriter.CustomKeyboardFocusManager;
import control.statics.ViewStatics;
import control.statics.ViewStatics.Mode;
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

		navigation.registerButtons(this);
		conclude.registerButtons(this);

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
			case "drop primitive": 	pnlNavigate.dropPrimitive(); 	break;
			case "drop statement": 	pnlNavigate.dropStatement(); 	break;				
			case "forward":			pnlNavigate.navigate(true);		break;
			case "backward": 		pnlNavigate.navigate(false); 	break;

			case "edit down":
				
				if (theorem.getChosen() != null) { 																							///(G2CC)
				
					if (theorem.isEdited()) {																								///(E0D9)
						
						DEditableStatement edit = theorem.getEditing();
						
						Described composite = edit.descend();
						
						if (composite != null) {																								///(1B8G)
							
							Placeholder placeholder = ((DComposite)composite).currentPlaceholder();
							
							Described primitive = placeholder.described();
							
							canvas.setWritecursor(primitive.description(), composite.getWritepoint(), primitive.getLocalReference());					
							canvas.fillCursor(primitive, composite, Mode.PAINT);

						} else {}
					} else 
						canvas.toggleEditingMode();																							///(A207)
				}
				
				break;

			case "edit up":
				
				if (theorem.isEdited()) { 																									///(968E)
					
					DEditableStatement edit = theorem.getEditing();

					Described ascend = edit.ascend();
					
					if (ascend != null)	{																									///(C018)
						canvas.setWritecursor(ascend.description());
						canvas.fillCursor(ascend, null, Mode.PAINT);

					} else 
						canvas.toggleEditingMode();						///(8F71)
				} 
																																			///(D4A2)
				break;

			case "insert":																													///(25CA)
				
				DEditableStatement edit = theorem.getEditing();

				if (theorem.isEdited() && !edit.current().isDummy()) { 	

					edit.insertDummy();
					edit.togglePrompting();																									///(D748)

					canvas.describeTheoremTail(edit.whole());	
					
				} else { break; } 																											///(24BB)

				break;
				
			case "delete":	
				
				if (canvas.isPrompting()) break;

				if (theorem.isEdited()) 																								///(4BBB)
					pnlNavigate.editDelete(theorem.getEditing());
				else 																														///(59FG)
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
