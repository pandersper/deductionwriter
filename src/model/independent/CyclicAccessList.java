package model.independent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * A list that is cyclic, holds it's own current position which can be iterated and that can export
 * a variable length section centered around its current position, the middle of the list.
 * 
 * @param <T>	The type contained in the list.
 */
public class CyclicAccessList<T> extends ArrayDeque<T> {
	

	/**
	 * New empty list.
	 */
	public CyclicAccessList() {
		super();
	}

	/**
	 * New list from a collection of items. The order is not determined since the order of collections is unknowm.
	 * 
	 * @param items	The items to contain in the list.
	 */
	public CyclicAccessList(Collection<? extends T> items) {
		super(items);
	}

	
	/**
	 * Inserts an element in the middle of list which is the current position.
	 * 
	 * @param e	The item to add.
	 */
	public void insert(T e) {
		this.addFirst(e);
	}
	
	/**
	 * Retreives the current item, the one in the middle.
	 * 
	 * @return	The middle element.
	 */
	public T getMiddle() {
		return this.getFirst();
	}

	/**
	 * Not used yet. Fetches the item s steps forward.
	 * 
	 * @param s	The number of steps forward.
	 * 
	 * @return	The element at thst position.
	 */
	public T getAhead(int s) {

		if (s > 0) {
			Iterator<T> it = this.iterator();
	
			int steps = s;
			
			T retur = null;
			
			while (it.hasNext() && steps > 0) {
				retur = it.next();
				steps--;
			}

			return retur;
		
		} else 	
			
			if (s < 0) {
			
				Iterator<T> it = this.descendingIterator();
				
				int steps = -s;
				
				T retur = null;
				
				while (it.hasNext() && steps > 0) {
					retur = it.next();
					steps--;
				}
	
				return retur;
				
			} else 
				return (s==0) ? this.getMiddle() : null;		
	}

	/**
	 * Returns an array of length (1+2*n) with the elements around the middle of the list.
	 * 
	 * @param n	The number of elements on one side of the middle.
	 * 
	 * @return	The array centered around the middle.
	 */
	public ArrayList<T> getCenterArray(int n) {
		
		n = n*n/n;

		ArrayList<T> center = new ArrayList<T>(1 + 2*n);
		
		for (int j = 0; j < 2*n+1; j++) 
			center.add(null);
		
		Iterator<T> it = this.iterator();
	
		int steps = 0;
		
		center.set(n,it.next());	// middle element
		
		while (it.hasNext() && steps < n) {
				center.set(n + 1 + steps,it.next());
				steps++;
		}
		
		it = this.descendingIterator();
		
		steps = 0;
					
		while (it.hasNext() && steps < n) {
				center.set(n - 1 - steps, it.next());
				steps++;
		}
				
		return center;
	}
	
	
	/**
	 * Removes the midde element which results the the next element in positive direction is the new middle.
	 * 
	 * @return	The removed middle.
	 */
	public T removeMiddle() {
		return this.removeFirst();
	}
	
	/**
	 * Move the middle of the list a certain number of steps.
	 * 
	 * @param s	The number of steps to move the middle.
	 * 
	 * @return	The new middle element.
	 */
	public T moveMiddle(int s) {
		
		while (s > 0) {
			this.addLast(this.removeFirst());
			s--;			
		}
		
		while (s < 0) {
			this.addFirst(this.removeLast());
			s++;			
		}

		assert(s == 0);
		
		return this.getMiddle();
	}
	
/*
	public static void main(String[] args) {
		
		String[] data = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","x","y","z"};

		int rounds = 0;

		CyclicAccessList<String> cycle = new CyclicAccessList<String>();
		
		while (rounds < data.length) {
			cycle.insert(data[rounds]);
			rounds++;
		}
		
		String output = "";
		
		ouput = cycle.getMiddle() + " : ";
		for(int i = 0; i < 7;i++) output += cycle.moveMiddle(1);
		for (int i = 0; i < 6;i++) output += cycle.getMiddle();
		for (int i = 0; i < 7;i++) 	output += cycle.moveMiddle(-1);
		
		output += "\n\n" + cycle.getMiddle() + " : ";
		for(int i = 0; i < 8;i++) output += cycle.moveMiddle(2);
		for (int i = 0; i < 4;i++) output += cycle.getMiddle();
		output += "<" + cycle.removeMiddle() + ">";
		output += "(" + cycle.getMiddle() + ")";
		for (int i = 0; i < 8;i++) output += cycle.moveMiddle(-2);
		
		cycle.insert("ö");

		output += "\n\n" + cycle.getMiddle() + " : ";
		for(int i = 0; i < 7;i++) output += cycle.moveMiddle(1);
		for (int i = 0; i < 6;i++) output += cycle.getMiddle();
		for (int i = 0; i < 7;i++) output += cycle.moveMiddle(-1);

		output += "\n\n" + cycle.getMiddle() + " : ";
		for(int i = 0; i < 5;i++) output += cycle.moveMiddle(3);
		for (int i = 0; i < 10;i++)	output += cycle.getMiddle();
		for (int i = 0; i < 5;i++) output += cycle.moveMiddle(-3);
		
		System.out.println(output);
	}
*/

}
 