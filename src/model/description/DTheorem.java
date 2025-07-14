package model.description;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.LinkedList;

import control.statics.DebugStatics;
import control.statics.PaintStatics;
import control.statics.Toolbox;
import model.description.abstraction.Described;
import model.independent.CyclicList;
import model.logic.Implication.ImplicationType;
import model.logic.Statement;
import model.logic.Theorem;

/**
 * The objects of class DTheorem is the main workpiece for DeductionWriter. It is the described aspect of a formal mathematical 
 * writing and it's formal non-described value part is implemented by {@link Theorem}. The user is editing and describing this
 * described theorem which is displayed on the {@link view.components.DisplayCanvas} and stored in the {@link control.db.DeductionBase}<br><br>
 * 
 * The edited content of this application is modelled by an as strict as possible division between formal value and more loosely
 * defined description of the mathematics. Hence the model contains {@link model.logic.Theorem}, {@link model.logic.Statement}, 
 * {@link model.logic.Primitive} where the last class is for atomary formal objects implementing the {@see model.logic.abstraction.Formal} 
 * interface. And on the description side corresponding classes {@link DTheorem}, {@link DStatement}, {@link DPrimitive} is found. 
 * The latter three is for unique objects with reference semantics and the former three is for value semantics with static access only.<br><br>
 * 
 * <i>The implementation is somewhat pragmatic</i> in that the Theorem class which belongs to the static-access value part of the
 * modell is a {@link CyclicList} of {@link DStatement}s which belong to the description side and have reference access. This is a 
 * convinient implementation choice which should not be exploited and are as good as possible hidden. The value part shoud only 
 * export static values and the description part works like common object oriented programming. <br><br>
 * 
 * Remark: Statements are interconnected by implications but these are for now part of the satements as it's final formal. With a 
 * terminating implication statements are considered closed.<br><br>
 * 
 * The word <i>description is used in a broad sense</i> so other than graphical representations may very well be includable.
 * 
 * @see CyclicList
 */
public class DTheorem extends Theorem {

	private DStatement 				preliminary;	
	private DStatement 				chosen;
	private DEditableStatement 		editing = null;

	
	/**
	 * Instantiates a new empty described theorem.
	 *
	 * @param name A name for it.
	 */
	public DTheorem(String name) {
		super(name);

		this.preliminary = new DStatement();	

		this.setName(super.getName());
		super.preliminaryvalue = (Statement) this.preliminary;

	} 
	/**
	 * Constructs a new described theorem from the contents given as arguments.
	 *
	 * @param name 			The name of the theorem.
	 * @param sequences 	The sequences of described formal primitives constituting it's statements.
	 * @param implications 	The implications between the statements, carrying the deduction forward in the theorem.
	 */
	public DTheorem(String name, ArrayList<LinkedList<Described>> sequences, ArrayList<ImplicationType> implications) {
		super(name, sequences, implications);

		ImplicationType type = implications.get(implications.size()-1);

		this.preliminary = (type == null) ? new DStatement(sequences.get(sequences.size()-1), null) : new DStatement();		

		this.setName(super.getName());
		super.preliminaryvalue = (Statement) this.preliminary;
	} 	
	
	/**
	 * Finalises the preliminary statement, adding it to the theorem together with an ending implication. 
	 *
	 * @param implication 	The deduction relation, the implication that the new statement qualify for.
	 * @return 				The finalised described statement.
	 */
	public DStatement finalisePreliminary(Described implication) {

		assert(preliminary.size() > 0);

		preliminary.setWritepoint(preliminary.getFirst().getWritepoint());		
		preliminary.addLast(implication);																		
		addLast(preliminary);

		DStatement newpreliminary = new DStatement();

		this.preliminary = newpreliminary;
		super.preliminaryvalue = (Statement) newpreliminary;

		return this.preliminary;
	}

	/**
	 * Draw this theorem, that is draws each of it's statements.
	 * 
	 * @param g The common grphics object.
	 */
	public void draw(Graphics2D g) {
		Graphics2D g2c = (Graphics2D) g;
		
		for (DStatement statement : this)
			statement.draw(g2c);
	}

	/**
	 * The name of the theorem.
	 * 
	 * @return The name of the theorem.
	 */
	public String getName() { 
		return name; 
	}
	/**
	 * Returns the preliminary described statement.
	 *
	 * @return The preliminary described statement.
	 */
	public DStatement getPreliminary() {
		return preliminary;
	}
	
	/**
	 * This theorem's first described primitive.
	 *
	 * @return The first primitive and it's description.
	 */
	public Described firstFormal() {
		if (lengthInFormals() > 0)
			return size() != 0 ? this.getFirst().getFirst() : preliminary.getFirst();
		else 
			return null;
	}
	/**
	 * This theorem's last described primitive.
	 *
	 * @return The last primitive and it's description.
	 */
	public Described lastFormal() {

		Described retur;

		if (!preliminary.isEmpty()) 
			retur = preliminary.getLast();		// from preliminary
		else 
			if (!isEmpty()) 
				retur = this.getLast().getLast(); 				// from deduction
			else retur = null;			 						// all empty

		return retur;
	}
	/**
	 * This theorem's first described statement.
	 *
	 * @return The first statement and it's description. 
	 */
	public DStatement firstStatement() {
		return size() != 0 ? getFirst() : preliminary;
	}


	/**
	 * Append a primitive to the preliminary statement.
	 *
	 * @param added The added described primitive.
	 */
	public void appendPrimitive(Described added) {
		preliminary.addLast(added);
	}
	/**
	 * Insert a described primitive into this theorem.
	 *
	 * @param insert 	The described primitive to insert.
	 * @param after 	The described primitive after the inserted.
	 */
	public void insertPrimitive(Described insert, Described after) {

		if (after == null)
			return;

		DStatement grown = Toolbox.findStatement(after, this);

		if (grown != null) 
			grown.add(grown.indexOf(after), insert);
		else
			System.out.println("no such described primitive to insert before");

	}
	/**
	 * Removes and returns the last primitive of the prliminary statement.
	 *
	 * @return The removed description and primitive.
	 */
	public Described removeLastPrimitive() {

		if (preliminary.size() < 1) {								// must have new preliminary first

			if (this.size() > 0) {									// is possible

				this.preliminary = this.removeLast();				// so transfer
				super.preliminaryvalue = this.preliminary;

			} else 
				return  null; 										// or it is empty theorem
		} 															// must be one there to remove

		Described remove = preliminary.removeLast();

		return remove;
	}
	
	/**
	 * Delete and return the last described statement.
	 *
	 * @return The deleted described statement.
	 */
	public DStatement deleteLastStatement() {
		
		DStatement removed = this.preliminary;

		if (this.isEmptyTheorem()) return removed;

		DStatement empty = new DStatement();

		if (this.size() > 0) {

			empty.setWritepoint(this.preliminary.getWritepoint());

			this.preliminary = removed.isEmpty() ? this.removeLast() : empty;
		
		} else {
			
			empty.setWritepoint(PaintStatics.PAGESTART);

			this.preliminary = empty;
		}

		super.preliminaryvalue = (Statement) this.preliminary;

		return removed;		// perhaps zero size
	}	
	/**
	 * Delete a specific described statement.
	 *
	 * @param delete 	The described statement to be deleted from this theorem.
	 * 
	 * @return True if removed otherwise false.
	 */
	public DStatement deleteStatement(DStatement delete) {

		if (this.isEmptyTheorem()) return null;

		if (this.isEmpty() || delete == this.preliminary) {
			
			this.preliminary = new DStatement();
			super.preliminaryvalue = (Statement) new DStatement();

			chosen = this.preliminary;

			chosen.underline(true);			
			
			return chosen;			
		
		}	// zero and singleton cases done. End case done.
		
		super.removeElement(delete);

		int newchosen = super.indexOf(delete) - 1;

		chosen = (this.isEmpty()) ? this.preliminary : 
				  (newchosen > 0) ? this.get(newchosen) : this.get(0);		// could be empty now
		
		chosen.underline(true);		
		
		return chosen;				
	}
	/**
	 * Gets the statement before the one given as parameter.
	 *
	 * @param after The statement after the one searched for.
	 * 
	 * @return The statement before the one given as parameter or null if no such were found.
	 */
	public DStatement getPreviousStatement(DStatement after) {
		
		int previousindex = this.indexOf(after);

		previousindex = (previousindex > 0) ? previousindex - 1 : 0;

		DStatement previous = (this.size() > 0) ? this.get(previousindex) : null;	

		return previous;
	}
		
	/**
	 * Change which statement that should be highlighted as chosen. Chosen for editing or deleting.
	 *
	 * @param forward 	Move forward, to the next statement, circularly in the theorem.
	 * 
	 * @return 			The newly chosen statement.
	 */
	public DStatement moveChosen(boolean forward) {

		if (chosen == null) {	

			if (preliminary.size() != 0) chosen = preliminary;
			else 
				if (size() != 0) 
					chosen = this.getLast();

			chosen.underline(true);									// not null - not empty theorem

		} else {
			
			chosen.underline(false);							

			if (preliminary.size() != 0) this.addLast(preliminary);	// trick

			int index = indexOf(chosen);
			int mod = size();
			
			if (forward)
				chosen = get((index+1) % mod);
			else
				chosen = get((index-1 + mod) % mod);	

			chosen.underline(true);
			
			if (preliminary.size()!=0) this.removeLast();			// trick correction
		}
		
		return chosen;
	}	
	/**
	 * Returns the chosen statement.
	 *
	 * @return The statement currently chosen.
	 */
	public DStatement getChosen() {
		return chosen;
	}
	
	/**
	 * Starts an editing aspect of the chosen described statement if there are one.
	 * 
	 * @return	The editing aspect of the chosen statement if there are one that has been chosen.
	 * 
	 * @see DEditableStatement
	 */
	public DEditableStatement edit() {

		if (chosen != null)
			editing = new DEditableStatement(chosen);

		return editing;
	}
	/**
	 * Returns the encapsulated statement that is currently chosen and is being edited, enclosed in it's encapsulation.
	 *
	 * @see DEditableStatement
	 * 
	 * @return The described statement chosen for editing enclosed in it's encapsulation, an object of the DEditableStatement class.
	 */
	public DEditableStatement getEditing() {
		return editing;
	}
	/**
	 * Checks if this theorem's statements is currently being edited.
	 *
	 * @return Wether some statement is being edited.
	 */
	public boolean isEdited() {
		return editing != null;
	}
	/**
	 * Deletes the editing aspect.
	 */
	public void leaveEditing() {
		editing = null;		
	}
		
	/** {@inheritDoc} */
	public String toString() {

		String output = "{" + super.getName() + ": ";

		this.addLast(preliminary);

		for (DStatement ds : this) {

			output += ds.toString() + " :: ";
		}

		output = output.substring(0, output.length() - 4);

		output += "}";

		this.removeLast();

		return output;
	}

	/**
	 * Prints a simple text description to sys.out.
	 */
	public void printout() {

		if (DebugStatics.DEBUGMINIMAL)
			System.out.println(this);
	}																																					
}
