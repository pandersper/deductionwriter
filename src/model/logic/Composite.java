package model.logic;

import java.util.List;

import model.description.abstraction.Placeholder;
import model.logic.abstraction.AbstractComposite;
import model.logic.abstraction.Formal;

/**
 * The class Composite is for compositions of formals. Since it is a formal it can contain values of its own class. 
 * 
 * <br><br><i>Not implemented: composites of composites are only planned for yet.</i>
 */
public class Composite extends AbstractComposite {


	private Composite(List<? extends Formal> constituents) {

		if (constituents.size() == 1) System.err.println("Composite from one formal?");

		super.composition.addAll(constituents);

		super.codepoint 	= -1;
		super.name 			= "C[" + characterString(constituents) + "]";
		super.type 			= FormalType.COMPOSITE;
		
	}
	
	protected Composite() {
	}	
		

	public static String 					makeCompositeName(Iterable<Placeholder> components) {
	
		String sequence = "";
	
		for (Placeholder component : components) 			
			sequence += (char) component.described().getCodepoint() + ",";
	
		sequence = sequence.substring(0, sequence.length()-1);
	
		return "[" + sequence + "]";
	}

	/**
	 * Factory method for producing cursorial value objects of class (type) Composite. 
	 * 
	 * @param subformals	The collection of formals that make up this composite's components. 
	 * @param codepoint		The codepoint that should be associated with this Composite value. It may not 
	 * 						conflict with UTF codepoints but are otherwise decided by the user.
	 * 						
	 * <br><i>Not yet implemented: This condition of being a UTF codepoint are not checked yet.</i>
	 * 
	 * @return A new composite value.
	 */
 	public static Composite makeValue(List<? extends Formal> subformals, int codepoint) {

		Composite composite =  new Composite(subformals);
	
		composite.codepoint = codepoint;
		
		return composite;
	}
}
