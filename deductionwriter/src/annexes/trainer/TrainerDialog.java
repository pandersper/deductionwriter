package annexes.trainer;	
import java.awt.Container;
import java.util.ArrayList;
import java.util.Collections;

import control.Shortcut;
import control.Toolbox;
import control.db.DeductionBase;
import model.independent.DoubleArray;
import model.logic.abstraction.Formal;
import view.components.dialogs.DefaultDialog;

/**
 * A dialog for loading new primitives into the trainer application.
 */
public class TrainerDialog extends DefaultDialog {

	private DeductionTrainer trainer;
	
	/**
	 * Instantiates a new trainer dialog.
	 *
	 * @param parent 	The main, parent application.
	 * @param base		The data base containing theorems and their parts.
	 */
	public TrainerDialog(DeductionTrainer trainer, DeductionBase base) {
		super(trainer.getFrame(), null);
				
		this.trainer = trainer;
	}

	/**
	 * Opens and initialises this dialog.
	 *
	 * @param opener The container to return control to.
	 */
	public void open(Container opener) {
 				
		updateMenu();
		
		list.setSelectedIndex(0);		
		
		Toolbox.switchContainer(this, opener);
	}
	
	/**
	 * Updates this dialog menu.
	 *
	 * @return The size of the menu.
	 */
	protected int updateMenu()  {

		ArrayList<String> fromdb = session.getBase().fetchNames("Primivestables");

		fromdb.removeAll(Collections.list(menu.elements()));

		if (! fromdb.isEmpty())
			menu.addAll(fromdb);

		return menu.size();
	}
	
	/**
	 * Loads a selected table in menu from the data base.
	 *
	 * @param name The name of the primitives tabel to fetch from data base.
	 */
	public void load(String name) {
				
		DoubleArray<Formal, Shortcut> bindings = session.getBase().fetchPrimitives(name);
		
		trainer.setPrimitives(Toolbox.describe(bindings));
		
		Toolbox.switchContainer(parent, this);
	}
	
	/**
	 * NOT IMPLEMENTED
	 *
	 * @param name Not used here.
	 */
	public void store(String name) {
		System.err.println("Not implemented method in " + this.getClass());
	}
	
	/**
	 * NOT IMPLEMENTED
	 *
	 * @param name Not used here.
	 */
	public void delete(String name) {
		System.err.println("Not implemented method in " + this.getClass());
	}

}
