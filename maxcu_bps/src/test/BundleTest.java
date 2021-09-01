package test;

import static org.junit.Assert.*;

import org.junit.Test;

import maxcu.*;

public class BundleTest {

	/**
	 * Tests the compare functionality of Bundles
	 */
	@Test
	public void testCompare()
	{
		TimeSlot ts1 = new TimeSlot("ts1", 1, 1);
		TimeSlot ts2 = new TimeSlot("ts2", 1, 1);
		TimeSlot ts3 = new TimeSlot("ts3", 1, 1);
		TimeSlot ts4 = new TimeSlot("ts4", 1, 1);
		TimeSlot ts5 = new TimeSlot("ts5", 1, 1);
		TimeSlot ts3l = new TimeSlot("ts3", 2, 1);
		TimeSlot ts1f = new TimeSlot("ts1", 1, 2);
		
		
		assertTrue("Ordering within Bundles should not matter!",
						  (new Bundle(new TimeSlot[]{ts1, ts2, ts3}))
				.compareTo(new Bundle(new TimeSlot[]{ts1, ts3, ts2})) == 0);
		
		assertTrue("Bigger elements!",
				  			(new Bundle(new TimeSlot[]{ts2, ts4, ts5}))
				  .compareTo(new Bundle(new TimeSlot[]{ts1, ts2, ts3})) > 0);
		
		
		assertTrue("Time in timeslot not taken into account!",
				  			(new Bundle(new TimeSlot[]{ts1, ts2, ts3}))
				  .compareTo(new Bundle(new TimeSlot[]{ts1, ts2, ts3l})) < 0);
		
		
		IllegalStateException ex = null;
		try
		{
			
			(new Bundle(new TimeSlot[]{ts1, ts2, ts3}))
					  .compareTo(new Bundle(new TimeSlot[]{ts1f, ts2, ts3}));
		}
		catch(IllegalStateException e)
		{
			ex = e;
		}
		assertNotNull("Two same TimeSlots but with different capacities must throw and exception", ex);
		
		
		assertTrue("Bundles with less elements are always smaller, no matter the content!",
						    (new Bundle(new TimeSlot[]{ts4, ts5}))
				  .compareTo(new Bundle(new TimeSlot[]{ts1, ts2, ts3})) < 0);
	}
}
