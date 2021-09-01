package test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;
import maxcu.*;
import utils.*;

public class ULListTest {

	@Test
	public void test() {
		TimeSlot ts1 = new TimeSlot("ts1", 1, 1);
		TimeSlot ts1n = new TimeSlot("ts1", 1, 1);
		TimeSlot ts2 = new TimeSlot("ts2", 1, 1);
		TimeSlot ts3 = new TimeSlot("ts3", 1, 1);
		TimeSlot ts3l = new TimeSlot("ts3", 2, 1);
		TimeSlot ts4 = new TimeSlot("ts4", 1, 1);
		TimeSlot ts5 = new TimeSlot("ts5", 1, 1);
		
		ULList<TimeSlot> l = new ULList<TimeSlot>();
		assertTrue("There are not elements in the beginning", l.count() == 0);
		
		l.add(ts1);
		assertTrue("Adding first element failed", l.count() == 1);
		
		l.add(ts1n);
		assertTrue("Trying to add an existing element should not change the list", l.count() == 1);
		
		IndexOutOfBoundsException ex = null;
		try
		{
			l.get(2);
		}
		catch(IndexOutOfBoundsException e)
		{
			ex = e;
		}
		assertNotNull("Trying call get() on an index bigger than the list should throw an error!", ex);
		
		l.remove(ts5);
		assertTrue("Size of list must not change when trying to delete a non-existing item", l.count() == 1);
		
		l.add(ts5); l.add(ts3); l.add(ts2); l.add(ts3l); l.add(ts4);
		assertTrue("List did not order elements correctly! - get(0)", l.get(0) == ts1n); //here, the new reference ts1n is in
		assertTrue("List did not order elements correctly! - get(1)", l.get(1) == ts2);
		assertTrue("List did not order elements correctly! - get(2)", l.get(2) == ts3);
		assertTrue("List did not order elements correctly! - get(3)", l.get(3) == ts3l);
		assertTrue("List did not order elements correctly! - get(4)", l.get(4) == ts4);
		assertTrue("List did not order elements correctly! - get(5)", l.get(5) == ts5);
		assertTrue("Wrong adding elements", l.count() == 6);
		
		l.remove(ts1); //delete first
		assertTrue("Wrong ordering after deleting first - get(0)", l.get(0) == ts2);
		assertTrue("Wrong ordering after deleting first - get(1)", l.get(1) == ts3);
		assertTrue("Wrong ordering after deleting first - get(2)", l.get(2) == ts3l);
		assertTrue("Wrong ordering after deleting first - get(3)", l.get(3) == ts4);
		assertTrue("Wrong ordering after deleting first - get(4)", l.get(4) == ts5);
		assertTrue("Wrong size after deleting first", l.count() == 5);
		
		l.remove(ts5); //delete last
		assertTrue("Wrong ordering after deleting last - get(0)", l.get(0) == ts2);
		assertTrue("Wrong ordering after deleting last - get(1)", l.get(1) == ts3);
		assertTrue("Wrong ordering after deleting last - get(2)", l.get(2) == ts3l);
		assertTrue("Wrong ordering after deleting last - get(3)", l.get(3) == ts4);
		assertTrue("Wrong size after deleting last", l.count() == 4);
		
		l.remove(ts3); //delete in middle
		assertTrue("Wrong ordering after deleting in middle - get(0)", l.get(0) == ts2);
		assertTrue("Wrong ordering after deleting in middle - get(1)", l.get(1) == ts3l);
		assertTrue("Wrong ordering after deleting in middle - get(2)", l.get(2) == ts4);
		assertTrue("Wrong size after deleting in middle", l.count() == 3);
		
		ArrayList<TimeSlot> al = l.toArray();
		assertTrue("toArray returns array of wrong size", al.size() == 3);
		TimeSlot[] ar = new TimeSlot[al.size()];
		ar = al.toArray(ar);
		assertTrue("Wrong ordering in array! - [0]", ar[0] == ts2);
		assertTrue("Wrong ordering in array! - [1]", ar[1] == ts3l);
		assertTrue("Wrong ordering in array! - [2]", ar[2] == ts4);
		
		l.remove(ts2); l.remove(ts3l); l.remove(ts4);
		assertTrue("Deleting all failed", l.count() == 0);
		
		l.add(ts1);
		assertTrue("Adding first element failed (after list already has been used)", l.count() == 1);
	}
}
