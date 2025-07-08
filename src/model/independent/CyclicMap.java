package model.independent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;

import control.statics.Toolbox;
import model.description.abstraction.AbstractDComposite;
import model.description.abstraction.Described;
import model.description.abstraction.Placeholder;

/**
 * A cyclic list with convenient access to iteration.
 * 
 * @param <V> Class of objects contained.
 */
public class CyclicMap<K,V> extends IdentityHashMap<K,V> {

	/** The position currently at. */
	protected int index = 0;
	
	private int sortsize = 0;
	
	protected LinkedList<K> keys = new LinkedList<K>();		
	
	/**
	 * Constructs a new cyclic list from the collection given, maintaining it's ordering.
	 * 
	 * @param collection	A collection objects of class T.
	 */
	public CyclicMap(Collection<Map.Entry<K,V>> collection) {
		super();
		
		for (Entry<K,V> pair : collection) {
			super.put(pair.getKey(), pair.getValue());
			keys.add(pair.getKey());
		}
		
		keys.sort(null);
	}
	
	/**
	 * Constructs an empty cyclic list.
	 */
	public CyclicMap() {
		super();
	}


	
	public V put(K key, V value) {

		keys.add(key);
		
		return super.put(key, value);		
	}
	
	
	/**
	 * The element at the current iteration position. 
	 * 
	 * @return Current element is this list.
	 */
	public V current() {
		return this.get(keys.get(index));
	}
	/**
	 * Iterates one step forward an returns that postition's element.
	 * 
	 * @return Next element in this cyclic list.
	 */
	public V next() {

		index = (index + 1) % this.size();
		
		return this.get(keys.get(index));

	}
	/**
	 * Iterates one step backward an returns that postition's element.
	 * 
	 * @return Previous element in this cyclic list.
	 */
	public V previous() {

		index = (index - 1 + this.size()) % this.size();
		
		return this.get(keys.get(index));
	}
	
	/**
	 * Remove a specific element from this cyclic list.
	 * 
	 * @param key 	The element to remove.
	 * 
	 * @return 			The element removed or null if nothing found to remove.
	 */
	public V removeElement(K key) {
						
			boolean mapok = super.containsKey(key);

			if (mapok) {
				
				int oldindex = keys.indexOf(key);
								
				boolean listok = oldindex != -1;
				
				if (listok) {

					V removed = (V) super.remove(key);

					keys.remove(key);
					
					this.index = Toolbox.decreasePGE(this.index, oldindex);

					return removed;

				} else
					return null;
			}
			
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
	public boolean insertElement(int index, K key, V element) {

		if (index < 0 || index >= this.size()) {
			System.err.println("index out of bounds at CyclicList.insertElement");
			return false;
		}
		
		super.put(key, element);
		
		keys.add(index, key);
		
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
		return index == keys.size() - 1;
	}
	
	
	public Collection<K> sortedKeys() {
		
		if (sortsize == this.size())	// already sorted
			return keys;
		else {

			PriorityQueue<K> sorted = new PriorityQueue<K>(keys);
		
			keys = new LinkedList<K>(sorted);	
		
			sortsize = this.size();
			
			return sorted;
		}
	}

	public Collection<V> sortedValues() {
		
		PriorityQueue<K> ordering = new PriorityQueue<K>(keys);
		
		ArrayList<V> values = new ArrayList<V>();
		
		for (K k : ordering) 
			values.add(this.get(k));
	
		return values;
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
		keys.clear();
		index = 0;
	}

	/**
	 * Replaces a constituent of a composite.
	 * 
	 * @param composite		The composite which should have a constituent replaced.
	 * @param replace		The constituent to replace.
	 * @param replaced		The constituent to become.
	 */
	public static void replaceComponent(AbstractDComposite composite, Described replace, Described replaced) {

		for(Placeholder holder : composite.getConstituents().values()) 			
			if (holder.described() == replaced)
				holder.insert(replace);
	}

}
