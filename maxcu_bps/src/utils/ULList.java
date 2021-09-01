package utils;

import java.util.ArrayList;

/**
 * Linked list with unique and ordered elements
 * @author Kevin
 *
 */
public class ULList <T extends Comparable<T>>{
	ListElement<T> first = new ListEnd<T>();
	
	private int lastIndex = -1;
	private ListContainer<T> lastAccessed = null;
	
	/**
	 * Creates an object of class ULList
	 */
	public ULList() {}

	/**
	 * Adds an element to the list if it does not exist yet!
	 * @param el
	 */
	public void add(T el) {
		first = first.add(el);
		
		//these values might be invalid now
		lastIndex = -1;
		lastAccessed = null;
	}
	
	/**
	 * Removes an element from the list if it was not in the list before
	 * @param el
	 */
	public void remove(T el) {
		first = first.remove(el);
		
		//these values might be invalid now
		lastIndex = -1;
		lastAccessed = null;
	}

	/**
	 * Returns the size of the list
	 * @return
	 */
	public int count() {
		return first.count(0);
	}

	/**
	 * Gets element at position i. This method is also optimized for traversing the list in for loops
	 * @param i
	 * @return
	 */
	public T get(int i) {
		//this is a performance tuning for traversing in for loops
		ListContainer<T> temp = null;
		if(lastAccessed != null && lastIndex >= 0 && i > lastIndex)
		{
			temp = lastAccessed.get(i - lastIndex);
		}
		else
		{
			temp = first.get(i);
		}
		
		lastAccessed = temp;
		lastIndex = i;
		return temp.getElement();
	}
	
	/**
	 * Gets the index of an element. Returns -1 if does not exist
	 * @param element
	 * @return
	 */
	public int getIndex(T element)
	{
		for(int i = 0; i < count(); i++)
		{
			if(get(i).compareTo(element)==0)
				return i;
		}
		return -1;
	}
	
	/**
	 * Returns all the lists elements in an ArrayList object
	 * @return
	 */
	public ArrayList<T> toArray()
	{
		int size = count();
		//counting is probably faster than just having the ArrayList object to adapt its size several times
		//also: I could code this myself. However, generic arrays are not allowed without significant workarounds
		//the user of this class can instead use ArrayList.toArray
		java.util.ArrayList<T> al = new ArrayList<>(size);
		
		first.addYourself(al);
		
		return al;
	}
}
