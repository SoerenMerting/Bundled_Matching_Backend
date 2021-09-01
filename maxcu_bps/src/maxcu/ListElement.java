package maxcu;

/**
 * The interface for the composite design pattern used in ULList
 * @author Kevin
 *
 * @param <T>
 */
interface ListElement <T extends Comparable<T>>{
	ListElement<T> add(T el);
	ListElement<T> remove(T el);
	int count(int counter);
	ListContainer<T> get(int i);
	void addYourself(java.util.ArrayList<T> al);
}
