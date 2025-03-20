package annexes.trainer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import control.Shortcut;
import control.Toolbox;
import model.description.DComposite;
import model.description.DPrimitive;
import model.description.abstraction.Described;
import model.independent.DoubleArray;
import model.independent.DoubleArray.Tuple;
import view.components.DButton;

/**
 * A dialog for editing bindings before finally submitting them to the application.
 */
public class BindingsViewDialog extends JFrame {

	private DoubleArray<Described, Shortcut>		bindings  = new DoubleArray<Described, Shortcut>();
	private ArrayList<BindingContainer> 			containers = new ArrayList<BindingContainer>();
	
	private DeductionTrainer parent;
		
	/**
	 * Container gathering relevant things for choosing and altering bindings for described primitives. 
	 * It has buttons and text fields to adjust and correct values for the bindings. It's actionPerformed
	 * method updates the fields and then pass them on to the parent trainer module which does the actual
	 * removing and updating of the buttons, containing their described formals and bindings.
	 */
	public class BindingContainer extends Container implements ActionListener {

		/** The primitive whose binding should be altered. */
		public Described formal;
		
		/** The complete binding to alter. */
		public Shortcut shortcut;

		private JTextField txfCode, txfModifier, txfKey;
		private JTextField[] txfs;

		/**
		 * Instantiates a new binding container.
		 *
		 * @param formal 	The primitive bound to a keyboard key.
		 * @param shortcut 	The binding containing key and modifier masks.
		 */
		public BindingContainer(Described formal, Shortcut shortcut) {
			super();
			
			this.formal 	= formal;
			this.shortcut 	= shortcut;
			
			updateFields();
			
			txfCode.setActionCommand("keycode");
			txfModifier.setActionCommand("modifiers");
						
			txfs 	= new JTextField[] { txfCode, txfModifier, txfKey };

			this.setLayout(new GridLayout(1,5));

			DButton dpbutton = null;
			
			if (formal instanceof DPrimitive) dpbutton = new DButton(formal);
			if (formal instanceof DComposite) dpbutton = new DButton(formal);

			this.add(dpbutton);

			initTextFields();

			for (JTextField txf : txfs) this.add(txf);
				
			Action delete = new AbstractAction() {
				public void actionPerformed(ActionEvent e) { }		// done in parent.actionPerformed(e); 
			};
			delete.putValue("container", this);

			JButton btnDelete = new JButton("X");
			btnDelete.setAction(delete);						
			btnDelete.setActionCommand("delete");
			btnDelete.addActionListener(parent);
			btnDelete.setBackground(Color.black);
			
			Action catchit = new AbstractAction() {
				public void actionPerformed(ActionEvent e) { }		// done in parent.actionPerformed(e); 
			};
			catchit.putValue("container", this);
			
			JButton btnCatch = new JButton("catch");			
			btnCatch.setAction(catchit);
			btnCatch.setActionCommand("catch");
			btnCatch.addActionListener(parent);
			btnCatch.setBackground(Color.yellow);
			
			this.add(btnCatch);
			this.add(btnDelete);
			
			this.addKeyListener(parent);
		}


		private void initTextFields() {

			txfs[0].setMaximumSize(new Dimension(120, 10));
			txfs[0].addActionListener(this);

			for (int i = 1; i < 2; i++) {
				txfs[i].setEditable(false);	
				txfs[i].setMaximumSize(new Dimension(120, 10));
			}
		}

		/**
		 * Action performed. Updates value fields and textfields and then pass on the editing of the 
		 * buttons and primitives to parent deduction panel. By passing on a new binding event. 
		 *
		 * @param e The event originating from textfields or buttons of this container. Propagation
		 * 			to parent panel is done by a new event named "binding".
		 */
		public void actionPerformed(ActionEvent e) {
			
			try {

				JTextField source = (JTextField) e.getSource();
				
				if (e.getActionCommand() == "keycode") {
					
					this.shortcut.keycode = Integer.parseInt(source.getText());					
				}
				else
					this.shortcut.modifiers = Integer.parseInt(source.getText());
				
				txfKey.setText(Character.toString(Character.toUpperCase(this.shortcut.keycode)));

			} catch (NumberFormatException nfe ) {
				nfe.printStackTrace();
			}
			
			parent.actionPerformed(new ActionEvent(this, 0, "binding"));
		}

		/**
		 * Uppdate values in the fields in this container.
		 */
		public void updateFields() {

			int 	keycode 	= this.shortcut != null ? this.shortcut.keycode 							 	: -1;
			int 	modifiers 	= this.shortcut != null ? this.shortcut.modifiers 								: 0;	
			String 	keystring 	= this.shortcut != null ? Character.toString(Character.toUpperCase(keycode)) 	: "not set";

			txfCode 	= new JTextField("" + keycode);								
			txfModifier = new JTextField("" + InputEvent.getModifiersExText(modifiers));
			txfKey 		= new JTextField(keystring);
		}
	}

 	/**
	  * Instantiates a new bindings view dialog.
	  *
	  * @param parent The main (parent) frame of the sub application.
	  */
	public BindingsViewDialog(DeductionTrainer parent) {

		this.bindings = new DoubleArray<Described, Shortcut>();
		this.parent = parent;

		buttonslayout.setAlignment(FlowLayout.LEADING);
		buttonslayout.setHgap(5);
		buttonslayout.setVgap(5);
		
		buttonPanel.setLayout(buttonslayout);

		contentPanel.setBorder(new TitledBorder("Keyboard key to primitive binding"));
		contentPanel.setPreferredSize(new Dimension(800, 800));
		contentPanel.setLayout(contentlayout);

		btnShortcuts.addActionListener(parent);
		btnShortcuts.setActionCommand("insert-bindings");
		buttonPanel.add(btnShortcuts);
		
		btnWriter.addActionListener(parent);
		btnWriter.setActionCommand("return-to-writer");
		buttonPanel.add(btnWriter);
		
		btnTrainer.addActionListener(parent);
		btnTrainer.setActionCommand("return-to-trainer");
		buttonPanel.add(btnTrainer);

		this.getContentPane().add(contentPanel, BorderLayout.CENTER);		
		this.add(buttonPanel, BorderLayout.NORTH);	
		
		buttonPanel.revalidate();
	}

	/**
	 * Returns the map of bound described primitives.
	 *
	 * @return The maping of described primitives to their bindings.
	 */
	public DoubleArray<Described, Shortcut> getBindings() {
		return bindings;
	}
	
	/**
	 * Update the bindings map with the updated ones given as argument.
	 *
	 * @param updated The updated bindings and their respective described primitives.
	 */
	public void updateBindings(DoubleArray<Described, Shortcut> updated) {
		
		ArrayList<Tuple<Described, Shortcut>> newbindings = new ArrayList<Tuple<Described, Shortcut>>();
		
		for (Tuple<Described, Shortcut> pair : updated) {
			
			Tuple<Described, Shortcut> found = bindings.getByFirst(pair.first());

			if (found == null)
				newbindings.add(pair);
			else 
				if (pair.second() != null)
					found.second().setKeyAndModifier(pair.second());
		}
	
		bindings.addAll(newbindings);

		bindings.sortByFirst();
	}
	
	/**
	 * Reset the map of bindings to the one given.
	 *
	 * @param bindings The bindings and their described primitives that should be used henceforth.
	 */
	public void resetBindings(DoubleArray<Described, Shortcut> bindings) {
			
		this.bindings.clear();
		
		this.bindings.addAll(bindings);

		this.bindings.sortByFirst();
	}

	/**
	 * Clears all the bindings's short-cuts to be able to re-assign them freely.
	 */
	public void clearShortcuts() {

		for (int i = 0; i < this.bindings.size(); i++) 
			this.bindings.updateByFirst(this.bindings.get(i).first(), null);
	}
	
	/**
	 * Remove a binding from this sub application view.
	 *
	 * @param container The binding and relevant information.
	 * @return The removed pair of binding and bound primitive.
	 */
	public Tuple<Described, Shortcut> removeBinding(BindingContainer container) {

		Tuple<Described, Shortcut> removed = bindings.removeByFirst(container.formal);
		
		containers.remove(container);
		
		contentPanel.remove(container);
		
		contentPanel.invalidate();
		
		return removed;
	}

	/** Called from actionPerformed method */
	public void setCatching() {
		contentPanel.setBackground(Color.yellow);
	}
	
	/**
	 * String that describes which keyboard keys that are in current use.
	 *
	 * @return The string of currently used keyboard keys.
	 */
	public String occupiedString() {

		Shortcut binding;
		String bitoken = "", output = "";
		
		for (Tuple<Described, Shortcut> pair : this.bindings) {

			if (pair.second().keycode != -1) {
			
				binding = pair.second();

				bitoken = "[" + (char) (int) binding.keycode + InputEvent.getModifiersExText(binding.modifiers) + "] "; 

				output += bitoken;
			
			} else output += "";
		}
		
		return output;
	}

	
	private final JPanel contentPanel 	= new JPanel();
	private final JPanel buttonPanel 	= new JPanel();

	private final JButton btnShortcuts  = new JButton("store bindings");
	private final JButton btnWriter  	= new JButton("to writer");
	private final JButton btnTrainer  	= new JButton("to trainer");
	
	private FlowLayout 	contentlayout 	= new FlowLayout(FlowLayout.LEFT);
	private FlowLayout 	buttonslayout 	= new FlowLayout(FlowLayout.CENTER);
	
	/**
	 * Open this dialog view.
	 *
	 * @param opener The container opening this view and to which it should return after initialisation is done.
	 */
	public void open(Container opener) {

		contentPanel.removeAll();

		this.makeContainers();
		
		for (BindingContainer c : containers) 
			contentPanel.add(c);

		Toolbox.switchContainer(this, opener);
		this.pack();
	}

	private void makeContainers() {

		// drop and redo all
		containers = new ArrayList<BindingContainer>();	
		
		bindings.sortByFirst();
		
		for (Tuple<Described, Shortcut> pair : bindings) {
			
			BindingContainer container = new BindingContainer(pair.first(), pair.second());
						
			containers.add(container);
		}
	}

} 
