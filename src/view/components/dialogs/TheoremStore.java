package view.components.dialogs;


import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;

import control.db.DeductionBase;
import control.session.Session;
import control.statics.ViewStatics;
import view.abstraction.InitiableContainer;

/**
 * Dialog taking care of theorem storage using a data base.
 * 
 * @see DeductionBase
 */
public class TheoremStore<C extends Container & InitiableContainer> extends DefaultDialog<C> {		

	
	/**
	 * Instantiates a new theorem store.
	 * 
	 * @param elder 	The frame that opens this dialogue.
	 * @param base		The base from wich to fetch theorems.
	 */
	public TheoremStore(C parent, Session session) {
		super(parent, session);		
		this.remove("input");
		this.remove("store");
		this.remove("storelabel");
	}
	
	/** {@inheritDoc} */
	public void 	initialise() {
		updateMenu();
		list.setSelectedIndex(0);
	}
 
	/**
	 * Empty and refill menu items from the data base.
 	 *
 	 * @return The size of the menu.
 	 */
	protected int 	updateMenu()  {

		menu.clear();
		
		ArrayList<String> fromdb = session.getBase().fetchTheoremNames();
		fromdb.removeAll(Collections.list(menu.elements()));

		if (! fromdb.isEmpty())
			menu.addAll(fromdb);

		return menu.size();
	}
	/**
	 * Insert names into the menu.
	 *
	 * @param names The names of the items.
	 * @return the The size of the menu.
	 */
	public int 		insertNames(ArrayList<String> names) {
		
		for (String name : names) 
			menu.addElement(name);
		
		return menu.size();
	}
	
	/** {@inheritDoc} */
	public void actionPerformed(ActionEvent e) {
						
		columnvalue = txfName.getText();
		
		switch (e.getActionCommand()) {
				
			case "cancel": ViewStatics.switchContainer(elder, this); break;

			case "load": load(columnvalue); break;

			case "store": store(columnvalue); break;
				
			case "delete": delete(columnvalue); break;

			default: break;
		}
	}	

	
	/** {@inheritDoc} */
	public void 	load(String loaded) {

		if (!list.isSelectionEmpty()) {
			
			columnvalue = list.getSelectedValue();	

			session.openWork(columnvalue);
						
			ViewStatics.switchContainer(elder, this);
		}
	}

	/** {@inheritDoc} */
	public void 	delete(String deleted) {
		
		if (!list.isSelectionEmpty()) {

			columnvalue = list.getSelectedValue();

			DeductionBase base = session.getBase();
			
			boolean occupied = base.contains(columnvalue, "Theorems", "name");

			if (occupied) {						
				base.delete(columnvalue, "Theorems", "name");
				base.delete(columnvalue, "Statements", "theorem");			
			}
			
			updateMenu();	
		}
	}

	/** Not used **/
	public void 	store(String name) {
		// not used		
	}

}
