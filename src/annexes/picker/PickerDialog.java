package annexes.picker;

import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import control.db.DeductionBase;
import control.session.Session;
import control.statics.Toolbox;
import control.statics.ViewStatics;
import view.abstraction.InitiableContainer;
import view.components.dialogs.DefaultDialog;

/**
 * A simple load, store and delete dialog for the DeductionPickerOld sub application. 
 */
public class PickerDialog<C extends Container & InitiableContainer>  extends DefaultDialog<C> {

	/**
	 * Instantiates a new picker dialog.
	 *
	 * @param elder 	The elder frame and application to return control to.
	 * @param base 		The base containing theorems and their constituents.
	 */
	public PickerDialog(DeductionPicker<C> parent, Session session) {
		super(((C)parent), session);
		btnLoad.setText("Add");
	}

	
	/**
	 * Action performed. Main hub for control in this dialog: loading, storing and deleting.
	 * 
	 * @param e Event originating from the buttons.
	 */
	public void actionPerformed(ActionEvent e) {

		columnvalue = txfName.getText();

		switch (e.getActionCommand()) {

		case "cancel":

			ViewStatics.switchContainer(elder, this);

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

			if (elder instanceof DeductionPicker) {
				((DeductionPicker<?>) elder).addToSelected(columnvalue);
				((DeductionPicker<?>) elder).updateOverview();
			}
			
			ViewStatics.switchContainer(elder, this);
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
			
			if (base.contains(columnvalue, "Theoremnames", "name")) {							

				int ok = JOptionPane.showConfirmDialog(elder, "Name exists, overwrite?");

				if (ok == JOptionPane.OK_OPTION) {

					base.delete(columnvalue, "Primitivestables", "name");
					base.delete(columnvalue, "Primitives", "tablename");			

					((DeductionPicker<?>) elder).storeInBase(columnvalue);

					done = true;						

					System.out.println("Primitive table " + columnvalue + " is overwritten.");

				} else System.out.println("Skipping.");

			} else {

				((DeductionPicker<?>) elder).storeInBase(columnvalue);

				done = true;

				System.out.println("Primitive table " + columnvalue + " is inserted.");					
			}

		} else JOptionPane.showMessageDialog(elder, "Bad naming, try again."); 

		updateMenu();

		if (done) ViewStatics.switchContainer(elder, this);
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

				base.delete(columnvalue, "PrimitivesViews", "name");
				base.delete(columnvalue, "Primitives", "tablename");			

				System.out.println("Deleted view " + columnvalue + " and its primitives.");
			}

			updateMenu();	
		}
	}	
}
