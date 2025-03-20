package view.components.dialogs;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JOptionPane;

import control.Toolbox;
import control.db.DeductionBase;
import view.DeductionFrame;
import view.abstraction.AbstractFrame;

/**
 * Dialog for loading a set of primitives into the application.
 * @see DefaultDialog
 */
public class CompositesLoader extends DefaultDialog {

	
	/**
	 * Instantiates a new composites loader.
	 *
	 * @param grandparent 	The grandparent container ince it is launched by the parent the controlpanel.
	 * @param base 			The base of theorems, primitives and composites.
	 */
	public CompositesLoader(AbstractFrame grandparent, DeductionBase base) {
		super(grandparent, base);
	}

	
	/** {@inheritDoc} */
	protected int updateMenu()  {

		menu.clear();

		ArrayList<String> fromdb = base.fetchNames("Compositetables");

		fromdb.removeAll(Collections.list(menu.elements()));

		if (! fromdb.isEmpty())
			menu.addAll(fromdb);

		return menu.size();
	}

	/** {@inheritDoc} */
	public void load(String loaded) {

		if (!list.isSelectionEmpty()) {

			columnvalue = list.getSelectedValue();	

			((DeductionFrame) parent).loadComposites(columnvalue);			

			Toolbox.switchContainer(parent, this);
		}
	}
	
	/** {@inheritDoc} */
	public void store(String name) {

		done = false;

		if (Toolbox.isOkName(name)) {	

			if (base.contains(name, "Compositetables", "tablename")) {							

				int ok = JOptionPane.showConfirmDialog(parent, "Name exists, overwrite?");

				if (ok == JOptionPane.OK_OPTION) {

					base.delete("Compositestables", "tablename", name);
					base.delete("Composites", "tablename", name);			

					((DeductionFrame) parent).storeComposites(name);

					done = true;															System.out.println("Composite table " + name + " is overwritten.");

				} else System.out.println("Skipping.");

			} else {

				((DeductionFrame) parent).storeComposites(name);

				done = true;																System.out.println("Composite table " + name + " is inserted.");					
			}

		} else JOptionPane.showMessageDialog(parent, "Bad naming, try again."); 

		updateMenu();

		if (done) Toolbox.switchContainer(parent, this);
	}
	
	/** {@inheritDoc} */
	public void delete(String deleted) {

		if (!list.isSelectionEmpty()) {

			columnvalue = list.getSelectedValue();

			if (base.contains(columnvalue, "Composites", "tablename")) {						
				base.delete("Composites", "tablename", columnvalue);
				System.out.println("Deleted table of composites: " + columnvalue + ".");
			}
			
			updateMenu();	
		}
	}

}
