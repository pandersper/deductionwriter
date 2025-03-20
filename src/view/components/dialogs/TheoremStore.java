package view.components.dialogs;


import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JOptionPane;

import control.Toolbox;
import control.db.DeductionBase;
import model.description.DTheorem;
import view.DeductionFrame;

/**
 * Dialog taking care of theorem storage using a data base.
 * 
 * @see DeductionBase
 */
public class TheoremStore extends DefaultDialog {		

	
	/**
	 * Instantiates a new theorem store.
	 * 
	 * @param parent 	The frame that opens this dialogue.
	 * @param base		The base from wich to fetch theorems.
	 */
	public TheoremStore(DeductionFrame parent, DeductionBase base) {
		super(parent, base);		
	}

	
	/**
	 * Insert names into the menu.
	 *
	 * @param names The names of the items.
	 * @return the The size of the menu.
	 */
	public int insertNames(ArrayList<String> names) {
		
		for (String name : names) 
			menu.addElement(name);
		
		return menu.size();
	}
	
 	/**
 	 * Empty and refill menu items from the data base.
	  *
	  * @return The size of the menu.
	  */
	protected int updateMenu()  {

		menu.clear();
		
		ArrayList<String> fromdb = base.fetchTheoremNames();
		fromdb.removeAll(Collections.list(menu.elements()));

		if (! fromdb.isEmpty())
			menu.addAll(fromdb);

		return menu.size();
	}

	
	/** {@inheritDoc} */
	public void actionPerformed(ActionEvent e) {
						
		columnvalue = txfName.getText();
		
		switch (e.getActionCommand()) {
				
			case "cancel": Toolbox.switchContainer(parent, this); break;

			case "load": load(columnvalue); break;

			case "store": store(columnvalue); break;
				
			case "delete": delete(columnvalue); break;

			default: break;
		}
	}
	
	
	/** {@inheritDoc} */
	public void initialise() {
		updateMenu();
		list.setSelectedIndex(0);
	}
	
	/** {@inheritDoc} */
	public void load(String loaded) {

		if (!list.isSelectionEmpty()) {
			
			columnvalue = list.getSelectedValue();	

			DTheorem theorem = base.fetchTheorem(columnvalue);
			DeductionFrame frame = (DeductionFrame) parent;
			
			frame.setAndDescribeTheorem(theorem);
			frame.getCanvas().newCursor();
			
			Toolbox.switchContainer(parent, this);
		}
	}

	/** {@inheritDoc} */
	public void store(String stored) {
		
		String theoremname = txfName.getText();
		
		DTheorem theorem = ((DeductionFrame) parent).currentTheorem();
		
		theorem.setName(theoremname);
		
		if (Toolbox.isOkName(theorem.getName())) {	
				
			if (base.contains(theorem.getName(), "Theorems", "name")) {							

				boolean overwrite = JOptionPane.showConfirmDialog(parent , "Theorem exists, overwrite?") == JOptionPane.OK_OPTION;

				if (overwrite) 					
					done = base.insert(theorem, overwrite) != -1;
				else System.out.println("Skipping.");
			
			} else 				
				done = base.insert(theorem, false) != -1;
	
		} else JOptionPane.showMessageDialog(parent, "Bad naming, try again.");
		
		updateMenu();
		
		if (done) Toolbox.switchContainer(parent, this);
	}

	/** {@inheritDoc} */
	public void delete(String deleted) {
		
		if (!list.isSelectionEmpty()) {

			columnvalue = list.getSelectedValue();

			if (base.contains(columnvalue, "Theorems", "name")) {						
				base.delete("Theorems", "name", columnvalue);
				base.delete("Statements", "theorem", columnvalue);			
				System.out.println("Deleted theorem " + columnvalue + " but not its primitives.");
			}
			
			updateMenu();	
		}
	}
}
