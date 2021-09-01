package test;

import static org.junit.Assert.*;
import org.junit.Test;
import maxcu.*;

public class PreferenceTableTest {

	@Test
	public void testAuto()
	{
		PreferenceTable o = new PrefTableAuto(4);
		TimeSlot ts1 = new TimeSlot("ts1", 1);
		TimeSlot ts2 = new TimeSlot("ts2", 1);
		TimeSlot ts3 = new TimeSlot("ts3", 1);
		TimeSlot ts4 = new TimeSlot("ts4", 1);
		TimeSlot ts5 = new TimeSlot("ts5", 1);
		
		Bundle b1 = new Bundle(new TimeSlot[]{ts1, ts5});
		Bundle b2 = new Bundle(new TimeSlot[]{ts1, ts5, ts3});
		Bundle b3 = new Bundle(new TimeSlot[]{ts2, ts2, ts2});
		
		o.addPreference("1first", b1, 1);
		o.addPreference("1first", b1, 2);
		assertTrue("Last value should be overwritten!", o.preferenceTable()[0][0] == 2);
		assertTrue("Client not added to list", o.getClientsArray()[0].equals("1first"));
		assertTrue("Timeslots not added to list", o.getTimeSlotsArray()[0] == ts1 && o.getTimeSlotsArray()[1] == ts5);
		
		o.addPreference("1first", b2, 3);
		assertTrue("Adding new bundle failed!", o.preferenceTable()[1][0] == 3);
		assertTrue("Timeslots not added to list",
				o.getTimeSlotsArray()[0] == ts1
				&& o.getTimeSlotsArray()[1] == ts3
				&& o.getTimeSlotsArray()[2] == ts5);
		
		o.addPreference("2second", b2, 4);
		assertTrue("Adding new client failed!", o.preferenceTable()[1][1] == 4);
		assertTrue("Adding new client failed! A bundle without preference is != 0", o.preferenceTable()[0][1] == 0);
		
		
		o.addPreference("3third", b3, 5);
		assertTrue("Adding new client and new bundle failed!", o.preferenceTable()[2][2] == 5);
		
		o.addPreference("0zero", b3, 6);
		assertTrue("Adding new client to beginning!", o.preferenceTable()[2][0] == 6);
		
		
		IllegalArgumentException ex = null;
		try
		{
			o.addPreference("1first", new Bundle(new TimeSlot[]{ts1, ts2, ts3, ts4, ts5}), 2);
		}
		catch(IllegalArgumentException e)
		{
			ex = e;
		}
		assertNotNull("Adding a bundle which is too big should throw an exception", ex);
		
		
		
		//test client weights functionality
		o.addClientWeight("1first", 0.5);
		o.addClientWeight("3third", 3);
		o.addClientWeight("0zero", 2);
		
		String[] cl = o.getClientsArray();
		double[] w = o.getWeights();
		
		assertTrue("Client and weight does not match!", cl[0].equals("0zero") && w[0] == 2);
		assertTrue("Client and weight does not match!", cl[1].equals("1first") && w[1] == 0.5);
		assertTrue("Client and weight does not match!", cl[2].equals("2second") && w[2] == 1); //default value
		assertTrue("Client and weight does not match!", cl[3].equals("3third") && w[3] == 3);
		
		IllegalArgumentException ex2 = null;
		try
		{
			o.addClientWeight("should throw error", 4);
		}
		catch(IllegalArgumentException e)
		{
			ex2 = e;
		}
		assertNotNull("Adding weight to a client who does not exist should throw an exception", ex2);
	}

	@Test
	public void testManual() {
		TimeSlot ts1 = new TimeSlot("ts1", 1);
		TimeSlot ts2 = new TimeSlot("ts2", 1);
		TimeSlot ts3 = new TimeSlot("ts3", 1);
		TimeSlot ts4 = new TimeSlot("ts4", 1);
		TimeSlot ts5 = new TimeSlot("ts5", 1);
		
		Bundle b1 = new Bundle(new TimeSlot[]{ts1, ts5});
		Bundle b3 = new Bundle(new TimeSlot[]{ts2, ts2, ts2});
		Bundle b2 = new Bundle(new TimeSlot[]{ts1, ts5, ts3});
		PreferenceTable o = new PrefTableManual(4, new String[]{"1first", "2second", "3third"},new Bundle[]{b1, b3, b2});
		
		o.addPreference("1first", b1, 1);
		o.addPreference("1first", b1, 2);
		assertTrue("Last value should be overwritten!", o.preferenceTable()[0][0] == 2);
		assertTrue("Client not added to list", o.getClientsArray()[0].equals("1first"));
		
		o.addPreference("1first", b2, 3);
		assertTrue("Adding new bundle failed!", o.preferenceTable()[2][0] == 3);
		
		o.addPreference("2second", b2, 4);
		assertTrue("Adding new client failed!", o.preferenceTable()[2][1] == 4);
		assertTrue("Adding new client failed! A bundle without preference is != 0", o.preferenceTable()[0][1] == 0);
		
		o.addPreference("3third", b3, 5);
		assertTrue("Adding new client and new bundle failed!", o.preferenceTable()[1][2] == 5);
		
		IllegalArgumentException ex = null;
		try
		{
			o.addPreference("1first", new Bundle(new TimeSlot[]{ts1, ts2, ts3, ts4, ts5}), 2);
		}
		catch(IllegalArgumentException e)
		{
			ex = e;
		}
		assertNotNull("Adding a bundle which is not specified in the list should throw an exception", ex);
		
		//test client weights functionality
		o.addClientWeight("1first", 0.5);
		o.addClientWeight("3third", 3);
		
		String[] cl = o.getClientsArray();
		double[] w = o.getWeights();
		
		assertTrue("Client and weight does not match! (1first client)", cl[0].equals("1first") && w[0] == 0.5);
		assertTrue("Client and weight does not match! (2second client)", cl[1].equals("2second") && w[1] == 1); //default value
		assertTrue("Client and weight does not match! (3third client)", cl[2].equals("3third") && w[2] == 3);
		
		IllegalArgumentException ex2 = null;
		try
		{
			o.addClientWeight("should throw error", 4);
		}
		catch(IllegalArgumentException e)
		{
			ex2 = e;
		}
		assertNotNull("Adding weight to a client who does not exist should throw an exception", ex2);
	}
}
