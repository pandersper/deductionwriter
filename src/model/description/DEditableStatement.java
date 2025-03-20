package model.description;

import java.awt.Shape;
import java.util.Stack;

import model.description.abstraction.Described;

/**
 * An editing-aspect or cloak of a described statement. Provides functionality for editing and navigating a 
 * particular one of a theorem's statements. Statements added to a theorem are otherwise often considered 
 * fixed but sometimes one wants to go back and edit for example the description part. 
 */
public class DEditableStatement {

	private DStatement 		statement;	
	private int 			index;
	private int 			size;

	private Stack<DComposite> lowest = new Stack<DComposite>();

	/** If this editable statement is asking for input. */
	public boolean 			prompting = false;
	
	
	/**
	 * Instantiates a new editing aspect for the provided described statement.
	 *
	 * @param statement The statement to be edited.
	 */
	public DEditableStatement(DStatement statement) {
		
		this.statement = statement;
		
		this.size = statement.isClosed() ? statement.size()-1 : statement.size();
					
		index = 0;
	}
	
	
	/**
	 * The current state of the edited statement.
	 *
	 * @return The described statement that is edited.
	 */
	public DStatement whole() { 
		return statement; 
	}

	/**
	 * Return the outlining boundary of this statment's all formals.
	 * 
	 * @return	The shape of the bounding outline. For now a rectangle.
	 */
	public Shape getBounds() {
		 return statement.getBounds();
	 }

	
	/**
	 * The described formal currently navigated to.
	 *
	 * @return The described formal currently at focus.
	 */
	public Described current() {
		return lowest.size() == 0 ? statement.get(index) : lowest.peek();
	}
	
	/**
	 * Circularly steps to the next (or first) described formal in this statement and then returns it.
	 *
	 * @return The next described formal, also setting the cursor to it.
	 */
	public Described next() {

		if (lowest.size() == 0) {
			
			index = (index + 1) % size;

			return statement.get(index);
		
		} else 								// something in lowest											
			return lowest.peek().next();					
	}
	
	/**
	 * Circularly backs to the previous (or last) described primitive in this statement and returns it.
	 *
	 * @return The previous described primitive, also setting the cursor to it.
	 */ 	
	public Described previous() {

		if (lowest.size() == 0) {
			
			index = (index - 1 + size) % size;

			return statement.get(index);
		
		} else 								// something in lowest											
			return lowest.peek().previous();					
	}

	/**
	 * Descends into the current formal provided that it is a composite, otherwise it does nothing.
	 * 
	 * @return The composite descended into or null if current is a primitive.
	 */
	public Described descend() {

		Described into = this.current();
		
		if (into instanceof DPrimitive)	return null;	// can't descend into primitive
		else
			if (into instanceof DComposite) {
				
				DComposite composite = (DComposite) into;
				
				lowest.push(composite);	

				return composite;
			} 

		System.err.println("Shoud not be reached in DEditableStatement:descend");
		return null;
	}

	/**
	 * Ascends up from the composite the cursor in is or does nothing if it is already on
	 * topmost level.
	 * 
	 * @return The ascended to or null if already at top level.
	 */
 	public DComposite ascend() {
		return (lowest.size() == 0) ? null : lowest.pop();		
	}

 	
	/**
	 * Replaces the described formal at the current position.
	 *
	 * @param replacing	Replacing The described formal to replace current with.
	 * 
	 * @return 			The replaced described formal.
	 */
	public Described replaceCurrent(Described replacing) {
		
		if (statement.size() == 0) System.err.println("replace on empty");

		return statement.set(index, replacing);
	}

	/**
	 * Delete the current described formal.
	 *
	 * @return 	The deleted described formal.
	 */
	public Described deleteCurrent() {										

		if (statement.size() == 0) System.err.println("deletion on empty");
		
		Described removed = statement.remove(index);
		
		size--;
		
		index = (size > 0) ? (size + index) % size : 0;
		
		return removed;
	}
	
	/**
	 * Insert a described primitive at the current position, shifting every following described 
	 * formal a step forward.
	 *
	 * @param inserted	The inserted described formal.
	 * @return int 		The new size of this statement.
	 */
	public int insertBeforeCurrent(Described inserted) {
		
		if (index == 0) 
			statement.addFirst(inserted);
		else
			statement.insertElement(index, inserted);
		
		size++;
		
		return statement.size();
	}

	
	/**
	 * Checks if this statement is empty.
	 *
	 * @return 	True, if is empty
	 */
	public boolean isEmpty() {
		return size == 0;
	}
	
	/**
	 * Checks if this statement contains only one last formal.
	 *
	 * @return 	True if singleton, otherwise false.
	 */
	public boolean isSingleton() {
		return size == 1;
	}

	/**
	 * Confirms that the current position is <b>inside a composite</b>.
	 *
	 * @return 	True if inside a composite, otherwise false.
	 */
	public boolean isSublevel() {
		return lowest.size() > 0;
	}
	
	/**
	 * Confirms that the current position is <b>at a composite</b>.
	 *
	 * @return 	True if inside a composite, otherwise false.
	 */
	public boolean currentIsComposite() {
		return (this.current() instanceof DComposite);
	}
	
	
	/**
	 * Turn of asking for input.
	 */
	public void togglePrompting() {

 		prompting = !prompting;

			if (statement.current().isDummy()) 
				this.deleteCurrent();	
	
	 }

}
