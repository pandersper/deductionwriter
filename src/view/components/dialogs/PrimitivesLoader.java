package view.components.dialogs;
import control.Toolbox;
import control.db.DeductionBase;
import view.abstraction.AbstractFrame;

/**
 * Dialog for loading a set of primitives into the application.
 * @see DefaultDialog
 */
public class PrimitivesLoader extends DefaultDialog {
	
	
 	/**
	  * Instantiates a new primitives loader.
	  *
	  * @param grandparent The grandparent container ince it is launched by the parent the controlpanel.
	  * @param base The base of theorems.
	  */
	public PrimitivesLoader(AbstractFrame grandparent, DeductionBase base) {
		super(grandparent, base);
		
		super.remove("store");
		super.remove("delete");
		super.remove("input");
	 }

	
	/** {@inheritDoc} */
	public void load(String loaded) {
 		
		if (!list.isSelectionEmpty()) {
			
			columnvalue = list.getSelectedValue();	
			
			AbstractFrame frame = (AbstractFrame) parent;

			frame.loadPrimitives(columnvalue);			

			Toolbox.switchContainer(parent, this);
		}
 	}

	/** Not used. */
	public void store(String stored) {
		System.err.println("Empty method called in: " + this.getClass());
	}

	/** Not used. */
	public void delete(String deleted) {

		if (!list.isSelectionEmpty()) {

			columnvalue = list.getSelectedValue();

			if (base.contains(columnvalue, "Primitives", "tablename")) {						
	
				base.delete("Primitives", "name", columnvalue);
				
				System.out.println("Deleted primitives belonging to " + columnvalue + ".");
			}
			
			updateMenu();	
		}
	}
	
}
