package model.independent;

import java.util.Collection;
import java.util.LinkedList;

import control.Toolbox;

/**
 * A cyclic list with convenient access to iteration.
 * 
 * @param <T> Class of objects contained.
 */
public class CyclicList<T> extends LinkedList<T> {

	/** The position currently at. */
	protected int index = 0;
	
	/**
	 * Constructs a new cyclic list from the collection given, maintaining it's ordering.
	 * 
	 * @param collection	A collection objects of class T.
	 */
	public CyclicList(Collection<T> collection) {
		super(collection);
	}
	
	/**
	 * Constructs an empty cyclic list.
	 */
	public CyclicList() {
		super();
	}

	/**
	 * The element at the current iteration position. 
	 * 
	 * @return Current element is this list.
	 */
	public T current() {
		return this.get(index);
	}
	
	/**
	 * Iterates one step forward an returns that postition's element.
	 * 
	 * @return Next element in this cyclic list.
	 */
	public T next() {

		index = (index + 1) % this.size();
		
		return this.get(index);

	}

	/**
	 * Iterates one step backward an returns that postition's element.
	 * 
	 * @return Previous element in this cyclic list.
	 */
	public T previous() {

		index = (index - 1 + this.size()) % this.size();
		
		return this.get(index);
	}

	/**
	 * Remove a specific element from this cyclic list.
	 * 
	 * @param element 	The element to remove.
	 * 
	 * @return 			The element removed or null if nothing found to remove.
	 */
	public T removeElement(T element) {
		
		int index = this.indexOf(element);
		
		if (index != -1) {

			T removed = (T) super.remove(index);

			this.index = Toolbox.decreasePGE(this.index, index);
			
			return removed;			
		} else
			return null;
	}

	/**
	 * Inserts an element at the specified index.
	 * 
	 * @param index		The index where to insert the element.
	 * @param element	The element to insert.
	 * 
	 * @return	Wether insertion was succesful or not.
	 */
	public boolean insertElement(int index, T element) {

		if (index < 0 || index >= this.size()) {
			System.err.println("index out of bounds at CyclicList.insertElement");
			return false;
		}
		
		super.add(index, element);
		
		if (this.index >= index) this.index++;
		
		return true;
	}

	/**
	 * Checks if currently positioned at the first position.
	 * 
	 * @return Wether positioned at first element or not. Return true if one or less element is in list.
	 */
	public boolean currentIsFirst() {
		return index == 0;
	}
	
	/**
	 * Checks if currently positioned at the last positioned.
	 * 
	 * @return 	Wether positioned at the last element or not. Return true if one or less element is in list.
	 */
	public boolean currentIsLast() {
		return index == this.size() - 1;
	}

	/**
	 * Resets this cyclic lists iteration to the first element.
	 */
	public void reset() {
		index = 0;
	}
	
	/**
	 * Clears this list of all of it's elements.
	 */
	public void clear() {
		super.clear();
		index = 0;
	}

}
