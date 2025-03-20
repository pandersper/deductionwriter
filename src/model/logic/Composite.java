package model.logic;

import java.util.Arrays;
import java.util.Collection;

import model.description.DComposite;
import model.description.DPrimitive;
import model.description.abstraction.Described;
import model.description.abstraction.Placeholder;
import model.independent.CyclicList;
import model.logic.abstraction.AbstractComposite;
import model.logic.abstraction.Formal;

/**
 * The class Composite is for compositions of formals. Since it is a formal it can contain values of its own class. 
 * 
 * <br><br><i>Not implemented: composites of composites are only planned for yet.</i>
 */
public class Composite extends AbstractComposite {


	private Composite(Collection<? extends Formal> constituents) {

		if (constituents.size() == 1) System.err.println("Composite from one formal?");

		super.constituents 	= emptyLayout(constituents);		
		super.name 			= "C[" + characterString(toFormals(super.constituents)) + "]";
		super.type 			= FormalType.COMPOSITE;
		super.codepoint 	= -1;
	}

	/**
	 * Factory method for producing cursorial value objects of class (type) Composite. 
	 * @param subformals	The collection of formals that make up this composite's components. 
	 * @param codepoint		The codepoint that should be associated with this Composite value. It may not 
	 * 						conflict with UTF codepoints but are otherwise decided by the user.
	 * 						
	 * <br><i>Not yet implemented: This condition of being a UTF codepoint are not checked yet.</i>
	 * 
	 * @return A new composite value.
	 */
 	public static Composite makeValue(Collection<? extends Formal> subformals, int codepoint) {

		Composite composite =  new Composite(subformals);
	
		composite.codepoint = codepoint;
		
		return composite;
	}
	
	/**
	 * Constructs a string of characters from this composite's components codepoints in navigation order. 
	 *
	 * @return A string representation of the constituting formals in navigation order.
	 */
	public String codepointsString() {

		String codepoints = "";

		for (Formal primitive : toFormals(constituents)) 		
			codepoints += primitive.getCodepoint() + " ";

		codepoints = codepoints.substring(0, codepoints.length() - 1);

		return codepoints;

	}

	/**
	 * Clone.
	 *
	 * @return the composite
	 */
	protected Composite clone() {
		return Composite.makeValue(Arrays.asList(this.toCloneArray()), this.codepoint);
	}
	
	
	private static CyclicList<Placeholder> emptyLayout(Collection<? extends Formal> constituents) {
		
		CyclicList<Placeholder> notlayedout = new CyclicList<Placeholder>();
		
		for (Formal formal : constituents) {
			
			Placeholder zeromontage = null;
			Described described = null;
			
			if (formal instanceof Primitive) 
				described = new DPrimitive(formal);
			else	
				if (formal instanceof Composite) 
					described = new DComposite(formal);

	
			if (described != null) {

				Placeholder holder = new Placeholder(described.description().getBounds());

				holder.fill(described);
				zeromontage = holder;
			}
			else
				System.err.println("Unknown Described type in emptyLayout.");

			notlayedout.add(zeromontage);
		}
		
		return notlayedout;
	}
}
