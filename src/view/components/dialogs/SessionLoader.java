package view.components.dialogs;
import java.util.ArrayList;
import java.util.Collections;


import javax.swing.JOptionPane;
import java.awt.Container;

import control.db.DeductionBase;
import control.session.Session;
import control.statics.Toolbox;
import control.statics.ViewStatics;
import view.DeductionFrame;
import view.abstraction.InitiableContainer;

/**
 * Dialog for loading a set of primitives into the application.
 * @see DefaultDialog
 */
public class SessionLoader<C extends Container & InitiableContainer> extends DefaultDialog<C> {
	
	
 	/**
	  * Instantiates a new primitives loader.
	  *
	  * @param elder The grandparent container ince it is launched by the elder the controlpanel.
	  * @param session The base of theorems.
	  */
	public SessionLoader(C elder, Session session) {
		super(elder, session);		
	 }

	
	protected int updateMenu()  {

		menu.clear();
		
		ArrayList<String> fromdb = session.getBase().fetchNames("Sessions");
		
		fromdb.removeAll(Collections.list(menu.elements()));

		if (! fromdb.isEmpty())
			menu.addAll(fromdb);

		return menu.size();
	}

	/** {@inheritDoc} */
	public void load(String loaded) {
 		
		if (!list.isSelectionEmpty()) {
			
			columnvalue = list.getSelectedValue();	
			
			session.fetchWorklist(columnvalue);

			ViewStatics.switchContainer(elder, this);
		}
 	}

	/** Not used. */
	public void store(String stored) {		
		
		if (!list.isSelectionEmpty()) columnvalue = list.getSelectedValue();			
		else {
			
			columnvalue = txfName.getText();

			while (!Toolbox.isOkName(columnvalue))
				JOptionPane.showMessageDialog(elder,"Bad naming, try something else.");
		}

		((DeductionFrame) elder).storeSession(columnvalue);

		updateMenu();
		
		ViewStatics.switchContainer(elder, this);
	}

	/** Not used. */
	public void delete(String deleted) {

		DeductionBase base = session.getBase();
		
		if (!list.isSelectionEmpty()) {

			columnvalue = list.getSelectedValue();

			if (base.contains(columnvalue, "Sessions", "name")) {						
	
				base.delete(columnvalue, "Sessions", "name");
				
				System.out.println("Deleted session " + columnvalue + ".");
			}
			
			updateMenu();	
		}
	}
	
}
