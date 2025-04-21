package annexes.picker;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import control.Session;
import control.Toolbox;
import control.db.DeductionBase;
import view.components.dialogs.DefaultDialog;

/**
 * A simple load, store and delete dialog for the DeductionPicker sub application. 
 */
public class PickerDialog extends DefaultDialog {

	/**
	 * Instantiates a new picker dialog.
	 *
	 * @param parent 	The parent frame and application to return control to.
	 * @param base 		The base containing theorems and their constituents.
	 */
	public PickerDialog(DeductionPicker parent, Session session) {
		super(parent, session);

		btnLoad.setText("Add");
	}

	
	/**
	 * Action performed. Main hub for control in this dialog: loading, storing and deleting.
	 * 
	 * @param e Event originating from the buttons.
	 */
	public void actionPerformed(ActionEvent e) {

		columnvalue = txfName.getText();

		boolean done;

		switch (e.getActionCommand()) {

		case "cancel":

			Toolbox.switchContainer(parent, this);

			break;

		case "load": load(columnvalue); break;

		case "delete": delete(columnvalue); break;

		case "store": store(columnvalue); break;

		default:

			break;
		}
	}

	
	/**
	 * Load table of primitives and add them to already selected.
	 *
	 * @param loaded 	Not used. The name of the table to fetch is chosen from the menu.
	 */
	public void load(String loaded) {

		if (!list.isSelectionEmpty()) {

			columnvalue = list.getSelectedValue();	

			((DeductionPicker) parent).addToSelected(columnvalue);
			((DeductionPicker) parent).updateOverview();

			Toolbox.switchContainer(parent, this);
		}
	}

	/**
	 * Store currently selected primitives into a table in the data base.
	 *
	 * @param stored 	Not used, the name of the table is selected from the list.
	 */
	public void store(String stored) {

		done = false;

		if (Toolbox.isOkName(columnvalue)) {	

			DeductionBase base = session.getBase();
			
			if (base.contains(columnvalue, "FormalsViews", "name")) {							

				int ok = JOptionPane.showConfirmDialog(parent, "Name exists, overwrite?");

				if (ok == JOptionPane.OK_OPTION) {

					base.delete("PrimitivesViews", "name", columnvalue);
					base.delete("Primitives", "tablename", columnvalue);			

					((DeductionPicker) parent).storeInBase(columnvalue);

					done = true;						

					System.out.println("Primitive table " + columnvalue + " is overwritten.");

				} else System.out.println("Skipping.");

			} else {

				((DeductionPicker) parent).storeInBase(columnvalue);

				done = true;

				System.out.println("Primitive table " + columnvalue + " is inserted.");					
			}

		} else JOptionPane.showMessageDialog(parent, "Bad naming, try again."); 

		updateMenu();

		if (done) Toolbox.switchContainer(parent, this);
	}

	/**
	 * Delete currently selected table of primitives.
	 *
	 * @param deleted 	Not used, the name of the table is selected from the list.
	 */
	public void delete(String deleted) {

		if (!list.isSelectionEmpty()) {
			
			DeductionBase base = session.getBase();
			
			columnvalue = list.getSelectedValue();
			done = false;

			if (base.contains(columnvalue, "PrimitivesViews", "name")) {						

				base.delete("PrimitivesViews", "name", columnvalue);
				base.delete("Primitives", "tablename", columnvalue);			

				System.out.println("Deleted view " + columnvalue + " and its primitives.");
			}

			updateMenu();	
		}
	}	
}
