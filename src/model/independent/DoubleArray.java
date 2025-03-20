package model.independent;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import model.independent.DoubleArray.Tuple;

/**
 * Array of tuples to be used as a sortable bijection, that is a mapping that maintains the correspondance between 
 * the tuples's first and second coordinate. 
 * 
 * @param <S>	The domain value.
 * @param <T>	The range value.
 */
public class DoubleArray<S extends Comparable<? super S>, T extends Comparable<? super T>> extends Vector<Tuple<S,T>> {

	private class FirstComparator implements Comparator<Tuple<S,T>> { 
		public int compare(Tuple<S,T> o1, Tuple<S,T> o2) { return o1.first().compareTo(o2.first()); }
	}

	private class SecondComparator implements Comparator<Tuple<S,T>> { 
		public int compare(Tuple<S,T> o1, Tuple<S,T> o2) { return o1.second().compareTo(o2.second()); }
	}
	
	/** Compares first coordinate of tuple. */
	private final FirstComparator firstComparator = new FirstComparator();
	/** Compares second coordinate of tuple. */
	private final SecondComparator secondComparator = new SecondComparator();
	
	
	/**
	 * A pair of values.
	 * 
	 * @param <S>	First element is of type S.
	 * @param <T>	Second element is of type T.
	 */
	public record Tuple<S,T> (S first, T second) {
	
		/** First element. 
		 * @return The first element. 
		 */
		public S first() { return first; }
		
		/** Second element. 
		 * @return The second element. 
		 */
		public T second() { return second; }
	}

	
	/**
	 * Find and return the tuple having a specific first coordinate.
	 * 
	 * @param s	The value of the first coordinate of the tuple to search for.
	 * 
	 * @return	The tuple with first coordinate s.
	 */
	public Tuple<S,T> getByFirst(S s) {
		
		Iterator<Tuple<S,T>> it = this.iterator();
		Tuple<S,T> candidate;
		
		while (it.hasNext()) {

			candidate = it.next();

			if (candidate.first().compareTo(s) == 0)
				return candidate;
		}

		return null;
	}

	/**
	 * Find and return tuple having a specific second coordinate.
	 * 
	 * @param t	The value of the second coordinate to find.
	 * 
	 * @return The tuple with first coordinate t.
	 */
	public Tuple<S,T> getBySecond(T t) {
		
		Iterator<Tuple<S,T>> it = this.iterator();
		Tuple<S,T> candidate;
		
		while (it.hasNext()) {
			
			candidate = it.next();

			if (candidate.second().compareTo(t) == 0)
				return candidate;
		}

		return null;
	}

	/**
	 * Sort the list's pairs with respect to their first element.
	 */
	public void sortByFirst() {
		this.sort(firstComparator);
	}

	/**
	 * Sort the list's pairs with respect to their second element.
	 */
	public void sortBySecond() {
		this.sort(secondComparator);
	}

	/**
	 * Find and update tuple having a specific first coordinate.
	 * 
	 * @param s Domain value used for finding.
	 * @param t Range value to update.
	 * 
	 * @return The inserted new tuple consisting of the found together with the updated value. 
	 */
	public Tuple<S, T> updateByFirst(S s, T t) {

		Iterator<Tuple<S,T>> it = this.iterator();

		Tuple<S,T> candidate;
		
		int index = 0;
		
		while (it.hasNext()) {
			
			candidate = it.next();
			
			if (candidate.first().compareTo(s) == 0) {
				
				candidate = new Tuple<S, T>(s,t);
				
				this.set(index, candidate);
				
				return candidate;
			}	
			
			index++;
		}
		
		return null;
	}

	/**
	 * Find and update tuple having a specific second coordinate.
	 * 
	 * @param s Domain value to update.
	 * @param t Range value used for finding.
	 * 
	 * @return The inserted new tuple consisting of the updated together with the found value. 
	 */
	public Tuple<S, T> updateBySecond(S s, T t) {

		Iterator<Tuple<S,T>> it = this.iterator();

		Tuple<S,T> candidate;
		
		int index = 0;
		
		while (it.hasNext()) {
			
			candidate = it.next();

			if (candidate.second().compareTo(t) == 0) {
				
				candidate = new Tuple<S, T>(s,t);
				
				this.set(index, candidate);

				return candidate;
			}
			
			index++;
		}

		return null;
	}

	/**
	 * Remove the correspondance with a specific first coordinate.
	 * 
	 * @param s 	The element to indicate which pair to remove.
	 * 
	 * @return		The pair removed.
	 */
	public Tuple<S, T> removeByFirst(S s) {

		Tuple<S,T> found = this.getByFirst(s);

		this.remove(found);
		
		return found;
	}

	
	/**
	 * The domain of this bijection.

	 * @return A collection of all elements in the domain.
	 */
	public Collection<S> domain() {
		
		LinkedList<S> domain = new LinkedList<S>();
		
		for (Tuple<S,T> pair : this) 			
			domain.add(pair.first());

		return domain;
	}

	/**
	 * The range of this bijection.
	 * @return	A collection of all the elements in the range.
	 */
	public Collection<T> range() {
		
		LinkedList<T> range = new LinkedList<T>();
		
		for (Tuple<S,T> pair : this) 			
			range.add(pair.second());

		return range;
	}

}
