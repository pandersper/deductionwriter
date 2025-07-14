package view.components.dialogs;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JOptionPane;

import control.session.Session;
import control.statics.Toolbox;
import control.statics.ViewStatics;
import view.abstraction.InitiableContainer;

/**
 * Dialog for loading a set of composites into the application.
 * 
 * @see DefaultDialog
 */
public class CompositesStore<C extends Container & InitiableContainer> extends DefaultDialog<C> {

	
	/**
	 * Instantiates a new composites loader.
	 *
	 * @param grandparent 	The grandparent container ince it is launched by the elder the controlpanel.
	 * @param base 			The base of theorems, primitives and composites.
	 */
	public CompositesStore(C grandparent, Session session) {
		super(grandparent, session);
	}
	
	/** {@inheritDoc} */
	protected int updateMenu()  {

		menu.clear();

		ArrayList<String> fromdb = session.getBase().fetchNames("Compositetables");

		fromdb.removeAll(Collections.list(menu.elements()));

		if (! fromdb.isEmpty())
			menu.addAll(fromdb);

		return menu.size();
	}

	
	/** {@inheritDoc} */
	public void load(String loaded) {

		if (!list.isSelectionEmpty()) {

			columnvalue = list.getSelectedValue();	

			session.loadComposites(columnvalue);			

			ViewStatics.switchContainer(elder, this);
		}
	}
	
	/** {@inheritDoc} */
	public void store(String name) {

		if (!list.isSelectionEmpty()) columnvalue = list.getSelectedValue();
		else {
			
			columnvalue = txfName.getText();

			while (!Toolbox.isOkName(columnvalue))
				JOptionPane.showMessageDialog(elder,"Bad naming, try something else.");
		}

		session.storeComposites(columnvalue);
			
		updateMenu();
		
		ViewStatics.switchContainer(elder, this);
	}
	
	/** {@inheritDoc} */
	public void delete(String deleted) {

		if (!list.isSelectionEmpty()) {

			columnvalue = list.getSelectedValue();

			session.deleteComposites(columnvalue);
						
			updateMenu();	
		}
	}
}
