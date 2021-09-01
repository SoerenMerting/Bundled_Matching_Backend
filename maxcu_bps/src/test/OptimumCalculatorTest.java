package test;

import static org.junit.Assert.*;

import org.junit.Test;

import gurobi.GRBException;
import maxcu.*;

public class OptimumCalculatorTest {

	@Test
	public void test() {
		GRBException ex = null;
		try
		{
			PreferenceTable o = new PrefTableAuto(10);
			TimeSlot tsA = new TimeSlot("A", 1);
			TimeSlot tsB = new TimeSlot("B", 1);
			Bundle first = new Bundle(new TimeSlot[]{tsA});
			Bundle second = new Bundle(new TimeSlot[]{tsA, tsB});
			
			o.addPreference("1", first, 1);
			o.addPreference("1", second, 2);
			o.addPreference("2", second, 3);
			
			ModelMatrix mc = new ModelMatrix(o);
			
			OptimumCalculator oc = new OptimumCalculator(mc, false);
			
			double[] res = oc.getDVAssigned();
			assertTrue("Decision variable not correct", res[0] == 0);
			assertTrue("Decision variable not correct", res[1] == 0.5);
			assertTrue("Decision variable not correct", res[2] == 0);
			assertTrue("Decision variable not correct", res[3] == 0.5);
			
			assertTrue("Delta not correct", oc.getMaxDelta() == 0.5);
			assertTrue("Optimum not correct", oc.getOpt() == 2.5);
		}
		catch(GRBException e)
		{
			ex = e;
		}
		
		assertNull("Caculation threw an exception", ex);
	}

}
