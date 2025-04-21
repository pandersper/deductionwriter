 package annexes.maker;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.border.EmptyBorder;

import control.Toolbox;
import model.description.DComposite;
import model.description.abstraction.Described;
import view.DeductionFrame;
import view.GlyphsPanel;
import view.components.DButton;
import view.components.DisplayCanvas;
import view.components.DButton.DisplayAction;

/**
 * CompositeMaker is a sub application of DeductionWriter where the user puts together
 * a composite mathematics symbol from primitives and then exports it to the main application.
 */
public class CompositeMaker extends JFrame implements ActionListener {

	private CompositePanel 	compositespanel;
	private DeductionFrame 	deductionframe;	
	private GlyphsPanel primitivespanel;
	private DisplayCanvas 	makercanvas;

	private JFrame 			primitivesmenu = new JFrame();

	private JButton quit;

	private DComposite done;

	/**
	 * Instantiates a new composite maker.
	 *
	 * @param frame The parent frame to return control to.
	 */
 	public CompositeMaker(DeductionFrame frame) {

		this.deductionframe = frame; 

		setBounds(100, 100, 500, 500);

		compositespanel = new CompositePanel(frame);
		compositespanel.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(compositespanel); 

		compositespanel.addListeners(this);			
	}

	/**
	 * As usual, this is the main hub of control, directed by button clicks.
	 * 
	 * @param ae The action event originating from buttons.
	 */
	public void actionPerformed(ActionEvent ae) {

		switch (ae.getActionCommand()) {

			case ("render"):
	
				done = compositespanel.requestComposite();								
	
				compositespanel.getCanvas().setInset(done);
	
				break;
	
			case ("quit"):
	
				if (done != null) {				
	
	//				primitivespanel.makeButton(done, null);	
					
					done = null;					
				}
	
				quit();		
	
				break;
	
			case ("to maker"): 		Toolbox.switchContainer(this, primitivesmenu); break;
	
			case ("to primitives"): Toolbox.switchContainer(primitivesmenu, this); break;
	
			case ("bounds"):	compositespanel.glyphToBounds(); break;
	
			case ("glyph"): 	compositespanel.boundsToGlyph(); break;
	
			case ("delete"):	compositespanel.deleteCurrent(); break;
	
			case ("clear"): 	compositespanel.clearAll(); break;
	
			case ("new"): 		compositespanel.toggleEditing(); break;
	
			case ("next"):		compositespanel.forward(); break;
	
			default: break;
		}
	}

	/**
	 * Sets the primitives panel.
	 *
	 * @param panel the new primitives panel
	 */
	public void setGlyphsPanel(GlyphsPanel panel) {

		this.primitivespanel = panel;
		this.primitivespanel.doLayout();

		primitivesmenu.setLayout(new BorderLayout());
		primitivesmenu.add(this.primitivespanel, BorderLayout.CENTER);

		quit = new JButton(new AbstractAction() {

			public void actionPerformed(ActionEvent ae) {

				Toolbox.switchContainer(CompositeMaker.this, primitivesmenu);
				primitivesmenu.remove(quit);
			}
		});

		quit.setText("quit");
		quit.setPreferredSize(new Dimension(30,25));
		quit.setMinimumSize(new Dimension(30,25));

		primitivesmenu.add(quit, BorderLayout.SOUTH);
		primitivesmenu.pack();

		changeActions(true);
	}
	
	/**
	 * Makes new or removes actions in all buttons used in CompositeMaker sub application. 
	 * The buttona are transfered from and to the main application. The same buttons are used
	 * everywhere so their actions have to be changed. 
	 *
	 * @param to Transfering to or from the main application that is leaving versus entering.
	 */
	public void changeActions(boolean to) {

		Collection<DButton> buttons = primitivespanel.getButtons();

		if (to) {

			for (DButton button : buttons) {

				DisplayAction action = button.getDisplayAction();
				makercanvas = (DisplayCanvas) action.getValue("canvas");
				action.putValue("canvas", compositespanel.getCanvas());

				button.setActionCommand("to maker");
				button.addActionListener(this);
			}	

		} else {						// from

			for (DButton button : buttons) {

				DisplayAction action = button.getDisplayAction();
				action.putValue("canvas", makercanvas);

				button.setActionCommand("");
				button.removeActionListener(this);
			}		
		}
	}

	/**
	 * Initialises this composite maker frame.
	 * 
	 * @param described	If composite it sets it up for further editing and if primitive it works 
	 * 					as the bounding frame for a new composite.
	 */
	public void initialise(Described described) {

		compositespanel.clearAll();
				
		if (described instanceof DComposite) {
			
			DComposite composite = (DComposite) described;
			
			compositespanel.setupComposite(composite);
			compositespanel.updateButtons(null);

		} else {
			
			compositespanel.setBoundingCursor(described.clone());
			compositespanel.updateButtons(null);
		}

	}
	
	private void quit() {

		changeActions(false);	

		Toolbox.switchContainer(deductionframe, this);

//		primitivesmenu.remove(primitivespanel);
//
//		deductionframe.redoLayout();
//		deductionframe.revalidate();
	}

}
