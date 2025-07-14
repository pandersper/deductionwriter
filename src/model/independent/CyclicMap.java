package model.independent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;

import control.statics.Toolbox;

/**
 * A map that can be cycled through in the order that the keys and values have been added to it.
 * 
 * @param <V> Class of objects contained.
 */
public class CyclicMap<K,V> extends IdentityHashMap<K,V> {

	/** The position currently at. */
	protected int index = 0;
	
	private int sortsize = 0;
	
	protected LinkedList<K> keys = new LinkedList<K>();		
	
	
	/**
	 * Constructs a new cyclic map from the collection given and also ordered in the same way as that collection. 
	 * That is one has to maintain the ordering oneself and be able to insert according to that. This will probably
	 * change soon.
	 * 
	 * NOT IMPLEMENTED YET: This ordering is not decided yet so a collection orderedanyhow could be given for the 
	 * moment. The implementation is now a based on a hash map wich must have orderable keys secure to collision 
	 * detection but that is not used as ordering but the ordering exported by {@see sortedKeys} is the order of 
	 * addition and insertion.
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
	 * Constructs an empty cyclic map.
	 */
	public CyclicMap() {
		super();
	}

	
	/**
	 * Insert a new key and value pair into this map. Appending the key last in the cycle ordering.
	 *
	 *@param key
	 *@param value
	 */
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
	 * @return Next element in this cyclic map.
	 */
	public V next() {

		index = (index + 1) % this.size();
		
		return this.get(keys.get(index));

	}
	/**
	 * Iterates one step backward an returns that postition's element.
	 * 
	 * @return Previous element in this cyclic map.
	 */
	public V previous() {

		index = (index - 1 + this.size()) % this.size();
		
		return this.get(keys.get(index));
	}
	
	/**
	 * Removes a specific element from this cyclic list.
	 * 
	 * @param key 	The key of the element value to remove.
	 * 
	 * @return	The element removed or null if nothing found to remove.
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
	 * Inserts an element at the specified index. This indexation is wholy guaranteed by the programmer. Underlying 
	 * implementation is not arrayed.
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
	
	/**
	 * The keys of this map sorted according to their hashing function.
	 * 	
	 * @return	The ordered keys ordered anyhow tolerated by {@see IdentityHashMap}, the ordering is not checked yet. 
	 */
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

	/**
	 * The values of this map sorted according to their key's hashing function.
	 * 	
	 * @return	The ordered keys ordered anyhow tolerated by {@see IdentityHashMap}, the ordering is not checked yet. 
	 */
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
}
