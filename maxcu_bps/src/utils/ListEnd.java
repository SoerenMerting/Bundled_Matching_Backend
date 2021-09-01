package utils;

import java.util.ArrayList;

/**
 * The "leaf" or "end" of the list in ULList
 * @author Kevin
 *
 * @param <T>
 */
class ListEnd <T extends Comparable<T>> implements ListElement<T>{	
	
	@Override
	public ListElement<T> add(T el) {
		ListContainer<T> newLC = new ListContainer<T>(el, this);
		return newLC;
	}

	@Override
	public ListElement<T> remove(T el) {
		return this;
	}

	@Override
	public int count(int counter) {
		return counter;
	}

	@Override
	public ListContainer<T> get(int i) {
		throw new IndexOutOfBoundsException();
	}

	@Override
	public void addYourself(ArrayList<T> al) {
		// do nothing
		return;
	}
	
}
