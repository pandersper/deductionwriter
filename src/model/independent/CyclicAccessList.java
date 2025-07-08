package model.independent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class CyclicAccessList<T> extends ArrayDeque<T> {
	

	public CyclicAccessList() {
		super();
	}

	public CyclicAccessList(Collection<? extends T> display) {
		super(display);
	}

	
	public void insert(T e) {
		this.addFirst(e);
	}
	
	public T getMiddle() {
		return this.getFirst();
	}

	public T getMiddle(int i) {

		if (i > 0) {
			Iterator<T> it = this.iterator();
	
			int steps = i;
			
			T retur = null;
			
			while (it.hasNext() && steps > 0) {
				retur = it.next();
				steps--;
			}

			return retur;
		
		} else 	
			
			if (i < 0) {
			
				Iterator<T> it = this.descendingIterator();
				
				int steps = -i;
				
				T retur = null;
				
				while (it.hasNext() && steps > 0) {
					retur = it.next();
					steps--;
				}
	
				return retur;
				
			} else 
				return (i==0) ? this.getMiddle() : null;		
	}

	public ArrayList<T> getCenterArray(int i) {
		
		i = i*i/i;

		ArrayList<T> center = new ArrayList<T>(1 + 2*i);
		
		for (int j = 0; j < 2*i+1; j++) 
			center.add(null);
		
		Iterator<T> it = this.iterator();
	
		int steps = 0;
		
		center.set(i,it.next());	// middle element
		
		while (it.hasNext() && steps < i) {
				center.set(i + 1 + steps,it.next());
				steps++;
		}
		
		it = this.descendingIterator();
		
		steps = 0;
					
		while (it.hasNext() && steps < i) {
				center.set(i - 1 - steps, it.next());
				steps++;
		}
				
		return center;
	}
	
	
	public T removeMiddle() {
		return this.removeFirst();
	}
	
	public T moveMiddle(int offset) {
		
		while (offset > 0) {
			this.addLast(this.removeFirst());
			offset--;			
		}
		
		while (offset < 0) {
			this.addFirst(this.removeLast());
			offset++;			
		}

		assert(offset == 0);
		
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
 