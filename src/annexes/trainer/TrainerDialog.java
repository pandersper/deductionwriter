package annexes.trainer;	
import java.awt.Container;
import java.util.ArrayList;
import java.util.Collections;

import control.session.Session;
import control.session.Shortcut;
import control.statics.Toolbox;
import control.statics.ViewStatics;
import model.independent.DoubleArray;
import model.logic.abstraction.Formal;
import view.abstraction.InitiableContainer;
import view.components.dialogs.DefaultDialog;

/**
 * A dialog for loading new primitives into the trainer application.
 */
public class TrainerDialog<C extends Container & InitiableContainer>  extends DefaultDialog<C>{

	private DeductionTrainer trainer;
	
	/**
	 * Instantiates a new trainer dialog.
	 * @param session TODO
	 * @param elder 	The main, elder application.
	 */
	public TrainerDialog(DeductionTrainer trainer, Session session) {
		super((C)trainer.getFrame(), session);
				
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
		
		ViewStatics.switchContainer(this, opener);
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
		
		ViewStatics.switchContainer(elder, this);
	}
	/**
	 * Not implemented yet.
	 */
	public void store(String name) {
		System.err.println("Not implemented method in " + this.getClass());
	}
	/**
	 * Not implemented yet.
	 */
	public void delete(String name) {
		System.err.println("Not implemented method in " + this.getClass());
	}

}
