package maxcu;

import java.util.ArrayList;

/**
 * An element of the ULList containing an object
 * @author Kevin
 *
 * @param <T>
 */
class ListContainer <T extends Comparable<T>> implements ListElement<T>{
	private T element;
	private ListElement<T> next;
	
	ListContainer(T el, ListElement<T> next)
	{
		this.element = el;
		this.next = next;
	}
	
	@Override
	public ListElement<T> add(T el) {
		int cmp = element.compareTo(el);

		if(cmp == 0)
		{
			this.element = el;
			return this;
		}
		if(cmp < 0)
		{
			this.next = next.add(el);
			return this;
		}
		
		ListContainer<T> newLC = new ListContainer<T>(el, this);
		return newLC;
	}
	
	@Override
	public ListElement<T> remove(T el) {
		int cmp = element.compareTo(el);
		
		if(cmp == 0)
			return next; // remove this from list
		if(cmp < 0)
		{
			this.next = next.remove(el);
			return this;
		}
		// Element not in list
		return this;
	}
	@Override
	public int count(int counter) {
		return next.count(++counter);
	}
	@Override
	public ListContainer<T> get(int i) {
		if(i == 0)
			return this;
		
		return next.get(--i);
	}

	@Override
	public void addYourself(ArrayList<T> al) {
		al.add(element);
		next.addYourself(al);
	}

	public T getElement()
	{
		return element;
	}
}
