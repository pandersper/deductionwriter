package model.description;

import java.util.ArrayList;

import control.Toolbox;
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

	/** The preliminary statement that is currently not added to the theorem. */
	private DStatement 				preliminarydescribed;	
	/** The statement chosen for editing or deletion. */
	private DStatement 				chosen;
	/** The editing aspect or cloak of the statement that is being edited. Null if no statement is edited. The preliminary statement is not 
	 * an edited statement but the common adding of new primitives to the statement. 
	 * */
	private DEditableStatement 		editing = null;

	
	/**
	 * Instantiates a new empty described theorem.
	 *
	 * @param name The name
	 */
	public DTheorem(String name) {
		super(name);

		this.preliminarydescribed = new DStatement();	

		this.setName(super.getName());
		super.preliminaryvalue = (Statement) this.preliminarydescribed;

	} 

	/**
	 * Constructs a new described theorem from the contents given as arguments.
	 *
	 * @param name 			The name of the theorem.
	 * @param sequences 	The sequences of described formal primitives constituting it's statemets.
	 * @param implications 	The implications between the statements, carrying the deduction forward in the theorem.
	 */
	public DTheorem(String name, ArrayList<ArrayList<Described>> sequences, ArrayList<ImplicationType> implications) {
		super(name, sequences, implications);

		ImplicationType type = implications.get(implications.size()-1);

		this.preliminarydescribed = (type == null) ? new DStatement(sequences.get(sequences.size()-1), null) : new DStatement();		

		this.setName(super.getName());
		super.preliminaryvalue = (Statement) this.preliminarydescribed;
	} 

	
	/**
	 * The name of the theorem.
	 * @return The name of the theorem.
	 */
	public String getName() { 
		return name; 
	}

	/**
	 * Sets the name of this described theorem.
	 *
	 * @param name The new name.
	 */
	public void setName(String name) {
		this.name = name + "D";	// To distinguish described from formal theorem.
	}

	/**
	 * Finalises the preliminary statement, adding it to the theorem together with an ending implication. 
	 *
	 * @param implication 	The deduction relation, the implication that the new statement qualify for.
	 * @return 				The finalised described statement.
	 */
	public DStatement finalisePreliminary(Described implication) {

		assert(preliminarydescribed.size() > 0);

		this.preliminarydescribed.addLast(implication);																		
		this.addLast(preliminarydescribed);

		DStatement newpreliminary = new DStatement();

		this.preliminarydescribed = newpreliminary;
		super.preliminaryvalue = (Statement) newpreliminary;

		return this.preliminarydescribed;
	}

	/**
	 * Returns the preliminary described statement.
	 *
	 * @return The preliminary described statement.
	 */
	public DStatement getPreliminary() {
		return preliminarydescribed;
	}
	
	
	/**
	 * This theorem's first described primitive.
	 *
	 * @return The first primitive and it's description.
	 */
	public Described firstFormal() {
		if (this.lengthInFormals() > 0)
			return this.size() != 0 ? this.getFirst().getFirst() : preliminarydescribed.getFirst();
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

		if (!this.preliminarydescribed.isEmpty()) 
			retur = this.preliminarydescribed.getLast();		// from preliminary
		else 
			if (!this.isEmpty()) 
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
		return this.size() != 0 ? this.getFirst() : preliminarydescribed;
	}
	
	/**
	 * This theorem's last described statement.
	 *
	 * @return The last statement and it's description.
	 */
	public DStatement lastStatement() {

		return (this.preliminarydescribed.size() != 0) ? this.preliminarydescribed : 
			   (this.size() > 0 ? this.getLast() : this.preliminarydescribed) ;
	}


	/**
	 * Append a primitive to the preliminary statement.
	 *
	 * @param added The added described primitive.
	 */
	public void appendPrimitive(Described added) {
		preliminarydescribed.addLast(added);
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

		if (preliminarydescribed.size() < 1) {						// must have new preliminary first

			if (this.size() > 0) {									// is possible

				this.preliminarydescribed = this.removeLast();		// so transfer
				super.preliminaryvalue = this.preliminarydescribed;

			} else 
				return  null; 										// or it is empty theorem
		} 															// must be one there to remove

		Described remove = preliminarydescribed.removeLast();

		return remove;
	}

	
	/**
	 * Delete and return the last described statement.
	 *
	 * @return The deleted described statement.
	 */
	public DStatement deleteLastStatement() {
		
		DStatement removed = this.preliminarydescribed;

		if (this.isEmptyTheorem()) return removed;

		DStatement empty = new DStatement();

		if (this.size() > 0) {

			empty.setLocation(this.preliminarydescribed.getLocation());

			this.preliminarydescribed = removed.isEmpty() ? this.removeLast() : empty;
		
		} else {
			
			empty.setLocation(Toolbox.DUMMYCURSOR.getLocation());

			this.preliminarydescribed = empty;
		}

		super.preliminaryvalue = (Statement) this.preliminarydescribed;

		return removed;		// perhaps zero size
	}	
	
	/**
	 * Delete a specific described statement.
	 *
	 * @param delete 	The described statement to be deleted from this theorem.
	 * @return 			True if removed otherwise false.
	 */
	public DStatement deleteStatement(DStatement delete) {

		if (this.isEmptyTheorem()) return null;

		DStatement removed = null;

		if (this.isEmpty() || delete == this.preliminarydescribed) {
			
			removed = this.preliminarydescribed;

			this.preliminarydescribed = new DStatement();
			super.preliminaryvalue = (Statement) new DStatement();

			DStatement before = this.isEmpty() ? null : this.getLast();
			
			if (chosen == removed) chosen = before;
			if (chosen != null) chosen.underline(true);
			
			return before;			
		
		}	// zero and singleton cases done. End case done.
		
		int before = super.indexOf(delete) - 1;

		removed = super.removeElement(delete);
				
		if (removed == null) System.err.println("Removal of not contained statement");

		if (removed == chosen) chosen = this.get(before);
		if (chosen != null) chosen.underline(true);
		
		return this.get(before);		
	}

	/**
	 * Gets the statement before the one given as parameter.
	 *
	 * @param after The statement after the one searched for.
	 * @return The statement before the one given as parameter or null if no such were found.
	 */
	public DStatement getPrevious(DStatement after) {
		
		int previousindex = this.indexOf(after);

		previousindex = (previousindex > 0) ? previousindex - 1 : 0;

		DStatement previous = (this.size() > 0) ? this.get(previousindex) : null;	

		return previous;
	}

		
	/**
	 * Change which statement that should be highlighted as chosen. Chosen for editing or deleting.
	 *
	 * @param forward 	Move forward, to the next statement, circularly in the theorem.
	 * @return 			The newly chosen statement.
	 */
	public DStatement moveChosen(boolean forward) {

		if (chosen == null) {	

			if (preliminarydescribed.size() != 0) chosen = preliminarydescribed;
			else 
				if (size() != 0) 
					chosen = this.getFirst();

			chosen.underline(true);

		} else {

			chosen.underline(false);

			int index = indexOf(chosen);

			int mod = size();

			if (forward)
				chosen = get((index+1) % mod);
			else
				chosen = get((index-1 + mod) % mod);	

			chosen.underline(true);
		}
		
		return chosen;
	}	

	/**
	 * Unchooses.
	 */
	public void clearChosen() {
		chosen = null;	
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
	 * @return	The editing aspect of the chosen statement if there are one that has been chosen.
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
	 * Checks if any of this theorem's statements is currently being edited.
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

		this.addLast(preliminarydescribed);

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

		if (Toolbox.DEBUGMINIMAL)
			System.out.println(this);
	}
																																					
}
