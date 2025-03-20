package view.components;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.KeyStroke;

import control.Shortcut;
import control.Toolbox;
import model.description.DComposite;
import model.description.abstraction.Described;
import view.PrimitivesPanel;
import view.abstraction.CursoredCanvas;

/**
 * Button used across the application for user input: 'a described primitive'. Not all input from keyboard
 * or other devices pass this button and some times it is used only as a sign. 
 */
public class DButton extends JButton {

	private Described  	formal;
	private KeyStroke 	key;    

	private int 		width,height;	

	/**
	 * An action that buttons use to fill in their formal's description into the canvas cursor. All by themselves. 
	 */
	public class DisplayAction extends AbstractAction {

		/**
		 * Instantiates a new display action.
		 *
		 * @param formal The primitive that it displays in the connected cursored canvas.
		 */
		public DisplayAction(Described formal) { this.putValue("formal", formal); }

		/**
		 * Sets the cursored canvas that the action display on.
		 *
		 * @param canvas the new canvas
		 */
		public void setCanvas(CursoredCanvas canvas) { this.putValue("canvas", canvas);	}
		
		/**
		 * Sets the component that should re-receive the focus after displaying.
		 *
		 * @param primitivesPanel The component (currently a PrimitivesPanel) to focus after displaying.
		 */
 		public void setFocusrestore(PrimitivesPanel primitivesPanel) { this.putValue("focusrestore", primitivesPanel); }   		

		/**
		 * Action performed.
		 *
		 * @param e The action event most often coming from the keyboard.
		 */
		public void actionPerformed(ActionEvent e) {

			CursoredCanvas canvas  = (CursoredCanvas) this.getValue("canvas");

			if (canvas != null) {

				PrimitivesPanel focus  = (PrimitivesPanel) this.getValue("focusrestore");
				Described 		formal = (Described) this.getValue("formal");

				Described newformal = formal.clone();
				
				canvas.fillCursor(newformal, true, null);
				
				if (newformal instanceof DComposite) 
					((DComposite) newformal).setCodepoint(formal.getCodepoint());
				
 				focus.restoreFocus();
			}
		}
	}

	/**
	 * Instantiates a new button for a described primitive.
	 *
	 * @param formal The primitive that this button serves as button for.
	 */
 	public DButton(Described formal) {

		DisplayAction displayaction = new DisplayAction(formal);

		this.formal = formal;
		this.setName(formal.getName());
		this.setAction(displayaction);
		this.makeIcons();
		this.setPreferredSize(new Dimension(width,height));  
	}

	/**
	 * Returns the described primitive this button displays or outputs in some way. 
	 *
	 * @return The primitive this button outputs, often to a cursored canvas. 
	 */
	public Described getDescribed() {
		return formal;
	}

	/**
	 * Retreives the display action of this button.
	 * @return This button's display action.
	 */
	public DisplayAction getDisplayAction() {
		return (DisplayAction) this.getAction();
	}

	/**
	 * Makes a key stroke from a binding. Essentially a conversion method.
	 *
	 * @param binding 	The binding between a codepoint and a keyboard key.
	 * @return 			The key stroke containg the binding.
	 */
	public KeyStroke makeKeyStroke(Shortcut binding) {

		this.key = KeyStroke.getKeyStroke(binding.keycode, binding.modifiers); 																///(A960)

		return this.key;
	}
	
	/**
	 * Returns the display action of this button.
	 *
	 * @return The action displaying the description of the primitive.
	 */
	private void makeIcons() {

		Icon icon, selected, pressed;

		icon = new ImageIcon(formal.description().getImage());

		this.setIcon(icon);

		width  = icon.getIconWidth();
		height = icon.getIconHeight();

		ImageProducer producer = formal.description().getImage().getSource();

		Toolbox.SelectedFilter selectedfilter = new Toolbox.SelectedFilter();
		selected = new ImageIcon(this.createImage(new FilteredImageSource(producer, selectedfilter)));
		this.setSelectedIcon(selected);

		Toolbox.PressedFilter pressedfilter = new Toolbox.PressedFilter();
		pressed = new ImageIcon(this.createImage(new FilteredImageSource(producer, pressedfilter)));
		this.setPressedIcon(pressed);
	}
}
