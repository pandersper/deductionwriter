package model.logic;

import java.util.ArrayList;
import java.util.LinkedList;

import model.description.DStatement;
import model.description.abstraction.Described;
import model.independent.CyclicList;
import model.logic.Implication.ImplicationType;

/** NOW GIT IS WORKING **/

/**
  * The objects of class Theorem is the formal part of a mathematics theorem which the user is editing and describing and 
 * which is displayed and stored. <br><br>
 * 
 * It is implemented as an easily iterable {@see CyclicList} of {@see DStatement}s which are part of the non formal, non value 
 * side of the model. They contain and can easily be downcasted to {@see Statement} objects that are meant to be used as static 
 * values. This is unproblematic since the downcast is a static value.<br><br>
 * 
 * <i>The implementation is somewhat pragmatic</i> in that this Theorem class which belongs to the static-access value part of the
 * modell is a {@see CyclicList} of {@see DStatement}s which belong to the description side and have reference access. This is a 
 * convinient implementation choice which should not be exploited and are as good as possible hidden. The value part shoud only 
 * export static values and the description part works like common object oriented programming.<br><br> 
 * 
 * With a terminating implication a statement are considered closed.<br><br>
 * 
 * Again, the word description is used in a broad sense so other than graphical representations should be includable.<br><br>
 * 
 * @see CyclicList
 * @see DStatement	The description part of a statement but that also contain the formal part, which object of this class exports.
 * @see model.description.DTheorem	The description part of the theorem which also contains it, or rather it's value.
 */
public class Theorem extends CyclicList<DStatement> {

	/** The statement currently worked on, to be added to the theorem when ready. */
	protected Statement preliminaryvalue;	

	/** The name of the theorem. */
	protected String name;				


	/**
	 * Instantiates a new empty theorem.
	 *
	 * @param name The name of the theorem
	 */
	protected Theorem(String name) {
		super(new LinkedList<DStatement>());

		this.preliminaryvalue = new Statement();
		this.name = name;
	}

	/**
	 * Instantiates a new theorem with the content given as parameters.
	 *
	 * @param name 			The name of the theorem.
	 * @param sequences 	List of lists (statements) of described formals (primitives) which are used to construct this theorem. 
	 * @param implications 	The implications used beteween statements, given in same order as the list of statements that they terminate.
	 */
	protected Theorem(String name, ArrayList<ArrayList<Described>> sequences, ArrayList<ImplicationType> implications) {
		this(name);

		int N = sequences.size();

		DStatement statement;

		for (int i = 0; i < N; i++) { 		
			statement = new DStatement(sequences.get(i), implications.get(i));	
			this.add(statement);
		}    	

		ImplicationType type = implications.get(implications.size()-1);

		preliminaryvalue = (type == null) ? (Statement) this.removeLast() : new Statement(); 

	}


	/**
	 * The theorem's length in primitives.
	 * @return The number of primitives in the theorem.
	 */
	public int lengthInFormals() {

		int length = this.preliminaryvalue.size();

		for (Statement s : this)
			length += s.size();

		return length;
	}

	/**
	 * The theorem's length in statements.
	 * @return The number of statements in the theorem.
	 */
	public int lengthInStatements() {
		int length = this.preliminaryvalue.isEmpty() ? 0 : 1;

		length += this.size();

		return length;
	}

	/**
	 * Checks if this theorem is the empty theorem.
	 * @return Wether this theorem is empty or not.
	 */
	public boolean isEmptyTheorem() {

		return this.lengthInFormals() == 0;					
	}

	/**
	 * The name of the theorem.
	 * @return The name of the theorem.
	 */
	public String getName() { 
		return name; 
	}

	/**
	 * Sets the name of the theorem.
	 *
	 * @param name The new name.
	 */
	public void setName(String name) {
		this.name = name;
	}


	public int consistencyNumber() {
		
		int sum = 0;
		
		for (Statement statement : this) {
			sum += statement.consistencyNumber();
		}
		
		return sum;
	}

}
