package view.components.dialogs;
import java.awt.Container;

import control.db.DeductionBase;
import control.session.Session;
import control.statics.ViewStatics;
import view.abstraction.InitiableContainer;

/**
 * Dialog for loading a set of primitives into the application.
 * @see DefaultDialog
 */
public class PrimitivesLoader<C extends Container & InitiableContainer> extends DefaultDialog<C> {
	
	
 	/**
	  * Instantiates a new primitives loader.
	  *
	  * @param grandparent The grandparent container ince it is launched by the elder the controlpanel.
	  * @param session The base of theorems.
	  */
	public PrimitivesLoader(C grandparent, Session session) {
		super(grandparent, session);
		
		super.remove("store");
		super.remove("delete");
		super.remove("input");
	 }

	/** {@inheritDoc} */
	public void load(String loaded) {
 		
		if (!list.isSelectionEmpty()) {
			
			columnvalue = list.getSelectedValue();	
			
			session.loadPrimitives(columnvalue);			

			ViewStatics.switchContainer(elder, this);
		}
 	}

	/** Not used. */
	public void store(String stored) {
		System.err.println("Empty method called in: " + this.getClass());
	}

	/** Not used. */
	public void delete(String deleted) {

		DeductionBase base = session.getBase();
		
		if (!list.isSelectionEmpty()) {

			columnvalue = list.getSelectedValue();

			if (base.contains(columnvalue, "Primitives", "tablename")) {						
	
				base.delete(columnvalue, "Primitives", "name");
				
				System.out.println("Deleted primitives belonging to " + columnvalue + ".");
			}
			
			updateMenu();	
		}
	}
	
}
