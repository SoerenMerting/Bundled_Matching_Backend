package test;

import static org.junit.Assert.*;

import org.junit.Test;

import maxcu.*;

public class TimeSlotTest {

	@Test
	public void testTimeSlotCompare()
	{
		TimeSlot ts1 = new TimeSlot("ts1", 1, 3);
		TimeSlot ts2 = new TimeSlot("ts2", 1, 3);
		TimeSlot ts3 = new TimeSlot("ts2", 2, 3);
		TimeSlot ts4 = new TimeSlot("ts1", 1, 4);
		TimeSlot ts5 = new TimeSlot("ts1", 1, 3);
		
		assertTrue("Same reference should evaluate to equal!", ts1.compareTo(ts1) == 0);
		assertTrue(ts1 + " and " + ts2 + " are different", ts1.compareTo(ts2) != 0);
		assertTrue(ts2 + " and " + ts3 + " are different", ts2.compareTo(ts3) != 0);
		assertTrue(ts5 + " and " + ts1 + " are the same", ts5.compareTo(ts1) == 0);
		
		IllegalStateException ex = null;
		Integer cmp = null;
		try
		{
			cmp = new Integer(ts1.compareTo(ts4));
		}
		catch(IllegalStateException e)
		{
			ex = e;
		}
		
		assertNull("Comparison should throw error but was: " + cmp, cmp);
		assertNotNull("Exception expected: " + ts1 + " and " + ts4
				+ " are the same but inconsistent due to different capacities", ex);
	}

}
