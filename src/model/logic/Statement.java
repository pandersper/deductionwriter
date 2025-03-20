 package model.logic;

import java.util.Collection;

import model.description.DComposite;
import model.description.DPrimitive;
import model.description.abstraction.Described;
import model.independent.CyclicList;
import model.logic.Implication.ImplicationType;
import model.logic.abstraction.Formal;

/**
 * A statement about a collection of mathematical primitives. The order of
 * the primitives are not concerned with. <br>
 * 
 * Compare for example with an integral expression that could be considered a true or 
 * sound statement without including for example an equality sign or other relation. <br><br>
 * 
 * The semantics is also supposed to be value-based although this implementation 
 * consists of descriptions of values that are ordinary objects with a self determining 
 * reference. <br><br>
 * 
 * Nothing this class exports or instantiates should be treated as other than values. It is completely
 * cursorial.<br><br>
 * 
 * REMARK: This class subclass {@link model.description.DStatement} is not a value but a description object
 * of a statement value and classes further extending that are not values either. <br><br>
 * 
 * Compare {@link model.logic.Statement} and {@link model.description.DStatement}.
 **/
public class Statement extends CyclicList<Described> {		

	/** Identifcation number for this statement. Used for storage in the database. */
	protected String id;
	
	/** 
	 * Constructors is meant to be called by the {@see #makeValue()} and {@see #makeValue(Collection<Described>, ImplicationType)} 
	 * but are exported to extending classes as well. Se these methods for description.
	 * {@see #makeValue()}
	 **/
	protected Statement() {
 		super();
 		
		id = Double.toString(Math.random()).substring(2);
    }
	
	/** 
	 * Constructors is meant to be called by the {@see #makeValue()} and {@see #makeValue(Collection<Described>, ImplicationType)} 
	 * but are exported to extending classes as well. Se these methods for description.
	 *  
	 *  @param described	The described formals that make up this statement.
	 *  @param type			The type of implication that ends this statement.
	 *  
	 * {@see #makeValue(Collection<Described>, ImplicationType)}
	 **/
	protected Statement(Collection<Described> described, ImplicationType type) {
		this();

		this.addAll(described);
		
		if (type != null)
			this.addLast(new DPrimitive(Implication.makeValue(type)));
	}
 
	/**
 	 * Tells if this statement has an Implication that closes it. 
 	 * 
 	 * @return	Wether this statement is closed or not.
	 */
	public final boolean isClosed() {
		return this.getLast() instanceof Implication;
	}

	/**
	 * Returns a unique identification to be able to store and retreive these values.
	 * @return	The identification number of this collection of values as a string. 
	 */
	public final String getID() {
		return this.id;
	}

	/**
	 * Tells what implication that is associated with this statement.
	 * @return	Returns the following integers: <i>3 - equivalence, 
	 * 											   2 - right implication, 
	 * 											   1 - left implication and 
	 * 											  -1 - no implication associated yet.</i>
	 */
	public final int implicationID() {

		if (this.isClosed()) {

				Formal last = super.getLast().value();
				
				if (last instanceof Implication) {
					ImplicationType type = ((Implication) last).getImplicationType();
					return (type == ImplicationType.EQUIV) ? 3 : ((type == ImplicationType.RIGHT) ? 2 : 1);
				} else
					return -1;			
		} else
			return -1;
	}

	/**
	 * Another string representation used for storing its primitives in a database.
	 * @return String of its primitive's UTF codepoints as characters.
	 */
	public String formalsString() {
		
		String output = "";

		Described implication = null; 
		
		if (this.isClosed())
			implication = this.removeLast();
		
		for (Described described : this) 			

			if (described instanceof DComposite)
				output += described.getCodepoint() + ":";	
			else
				output += (char) described.getCodepoint() + ":";

		if (this.isClosed())
			this.addLast(implication);
		
		return output.substring(0, output.length() - 1);
	}
	
}
 
