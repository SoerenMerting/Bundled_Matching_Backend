package test;

import static org.junit.Assert.*;

import org.junit.Test;
import maxcu.*;
import gurobi.*;

public class IRATest {

	
	@Test
	public void test()
	{
		PreferenceTable o = new PrefTableAuto(10);
		TimeSlot tsA = new TimeSlot("A", 1);
		TimeSlot tsB = new TimeSlot("B", 1);
		Bundle first = new Bundle(new TimeSlot[]{tsA});
		Bundle second = new Bundle(new TimeSlot[]{tsA, tsB});
		
		o.addPreference("1", first, 1);
		o.addPreference("1", second, 2);
		o.addPreference("2", second, 3);
		
		GRBException ex = new GRBException();
		double[] res = null;
		double[] cres = new double[]{0,0,0,1};
		double opt = -1;
		double copt = 3;
		try
		{
			ModelMatrix mc = new ModelMatrix(o);
			IRA i = new IRA(mc);
			i.calculate();
			res = i.getIntegerResults();
			opt = i.getOpt();
		}
		catch(GRBException e)
		{
			ex = e;
		}
		
		assertNotNull("Exception during IRA calculation", ex);
		
		assertTrue("Decision variables have wrong values", Methods.compareVector(res, cres) == -1);
		assertTrue("Optimum has the wrong value!", opt == copt);
	}
	
	@Test
	public void test2()
	{
		PreferenceTable o = new PrefTableAuto(10);
		TimeSlot tsA = new TimeSlot("A", 1);
		TimeSlot tsB = new TimeSlot("B", 2);
		Bundle first = new Bundle(new TimeSlot[]{tsA});
		Bundle second = new Bundle(new TimeSlot[]{tsA, tsB});
		
		o.addPreference("1", first, 1);
		o.addPreference("1", second, 2);
		o.addPreference("2", second, 3);
		
		GRBException ex = new GRBException();
		double[] res = null;
		double[] cres = new double[]{0,0,0,1};
		double opt = -1;
		double copt = 3;
		try
		{
			ModelMatrix mc = new ModelMatrix(o);
			IRA i = new IRA(mc);
			i.calculate();
			res = i.getIntegerResults();
			opt = i.getOpt();
		}
		catch(GRBException e)
		{
			ex = e;
		}
		
		assertNotNull("Exception during IRA calculation", ex);
		
		assertTrue("Decision variables have wrong values", Methods.compareVector(res, cres) == -1);
		assertTrue("Optimum has the wrong value!", opt == copt);
	}
	
	@Test
	public void test3()
	{
		PreferenceTable o = new PrefTableAuto(10);
		TimeSlot tsA = new TimeSlot("A",2);
		TimeSlot tsB = new TimeSlot("B",3);
		TimeSlot tsC = new TimeSlot("C",1);
		Bundle first = new Bundle(new TimeSlot[]{tsA});
		Bundle second = new Bundle(new TimeSlot[]{tsA, tsB});
		Bundle third = new Bundle(new TimeSlot[]{tsB, tsC});
		
		o.addPreference("1", first, 2);
		o.addPreference("1", third, 3);
		o.addPreference("2", first, 1);
		o.addPreference("2", second, 4);
		
		GRBException ex = new GRBException();
		double[] res = null;
		double[] cres = new double[]{0,0,1,0,1,0};
		double opt = -1;
		double copt = 7;
		try
		{
			ModelMatrix mc = new ModelMatrix(o);
			IRA i = new IRA(mc);
			i.calculate();
			res = i.getIntegerResults();
			opt = i.getOpt();
		}
		catch(GRBException e)
		{
			ex = e;
		}
		
		assertNotNull("Exception during IRA calculation", ex);
		
		assertTrue("Decision variables have wrong values", Methods.compareVector(res, cres) == -1);
		assertTrue("Optimum has the wrong value!", opt == copt);
	}
	
	@Test
	public void test4() {
		PreferenceTable o = new PrefTableAuto(2);
		TimeSlot tsA = new TimeSlot("A", 2);
		TimeSlot tsB = new TimeSlot("B", 3);
		Bundle first = new Bundle(new TimeSlot[]{tsA});
		Bundle second = new Bundle(new TimeSlot[]{tsA, tsB});
		
		o.addPreference("1", first, 4);
		o.addPreference("2", second, 1);
		o.addPreference("3", first, 2);
		o.addPreference("3", second, 3);
		
		GRBException ex = new GRBException();
		double[] res = null;
		double[] cres = new double[]{1,0,0,0,1,0};
		double opt = -1;
		double copt = 6;
		try
		{
			ModelMatrix mc = new ModelMatrix(o);
			IRA i = new IRA(mc);
			i.calculate();
			res = i.getIntegerResults();
			opt = i.getOpt();
		}
		catch(GRBException e)
		{
			ex = e;
		}
		
		assertNotNull("Exception during IRA calculation", ex);
		
		assertTrue("Decision variables have wrong values", Methods.compareVector(res, cres) == -1);
		assertTrue("Optimum has the wrong value!", opt == copt);
	}
	
	@Test
	public void test5() {
		PreferenceTable o = new PrefTableAuto(2);
		TimeSlot tsA = new TimeSlot("A", 2);
		TimeSlot tsB = new TimeSlot("B", 1);
		TimeSlot tsC = new TimeSlot("C", 1);
		Bundle first = new Bundle(new TimeSlot[]{tsA});
		Bundle second = new Bundle(new TimeSlot[]{tsA, tsB});
		Bundle third = new Bundle(new TimeSlot[]{tsB, tsC});
		
		o.addPreference("1", first, 4);
		o.addPreference("1", third, 4);
		o.addPreference("2", second, 5);
		o.addPreference("3", first, 2);
		o.addPreference("3", second, 3);
		
		GRBException ex = new GRBException();
		double[] res = null;
		double[] cres = new double[]{1,0,0,0,1,0,0,0,0};
		double opt = -1;
		double copt = 9;
		try
		{
			ModelMatrix mc = new ModelMatrix(o);
			IRA i = new IRA(mc);
			i.calculate();
			res = i.getIntegerResults();
			opt = i.getOpt();
		}
		catch(GRBException e)
		{
			ex = e;
		}
		
		assertNotNull("Exception during IRA calculation", ex);
		
		assertTrue("Decision variables have wrong values", Methods.compareVector(res, cres) == -1);
		assertTrue("Optimum has the wrong value!", opt == copt);
	}
	
	@Test
	public void test6() {
		PreferenceTable o = new PrefTableAuto(2);
		TimeSlot tsA = new TimeSlot("A", 2);
		TimeSlot tsB = new TimeSlot("B", 1);
		TimeSlot tsC = new TimeSlot("C", 2);
		Bundle first = new Bundle(new TimeSlot[]{tsA});
		Bundle second = new Bundle(new TimeSlot[]{tsA, tsB});
		Bundle third = new Bundle(new TimeSlot[]{tsB, tsC});
		
		o.addPreference("1", first, 4);
		o.addPreference("1", third, 6);
		o.addPreference("2", second, 5);
		o.addPreference("2", third, 2);
		o.addPreference("3", first, 2);
		o.addPreference("3", second, 3);
		
		GRBException ex = new GRBException();
		double[] res = null;
		double[] cres = new double[]{0,0,1,0,1,0,1,0,0};
		double opt = -1;
		double copt = 13;
		try
		{
			ModelMatrix mc = new ModelMatrix(o);
			IRA i = new IRA(mc);
			i.calculate();
			res = i.getIntegerResults();
			opt = i.getOpt();
		}
		catch(GRBException e)
		{
			ex = e;
		}
		
		assertNotNull("Exception during IRA calculation", ex);
		
		assertTrue("Decision variables have wrong values", Methods.compareVector(res, cres) == -1);
		assertTrue("Optimum has the wrong value!", opt == copt);
	}

	/**
	 * This test is basically the same as in method test().
	 * However, we test the functionality of adding an additional constraint.
	 * In this case, we make sure, that the additional constraint will not allow any value other than 0 for the variables.
	 * This is an easy way of testing this functionality
	 */
	@Test
	public void testAdditionalLine()
	{
		PreferenceTable o = new PrefTableAuto(10);
		TimeSlot tsA = new TimeSlot("A", 1);
		TimeSlot tsB = new TimeSlot("B", 1);
		Bundle first = new Bundle(new TimeSlot[]{tsA});
		Bundle second = new Bundle(new TimeSlot[]{tsA, tsB});
		
		o.addPreference("1", first, 1);
		o.addPreference("1", second, 2);
		o.addPreference("2", second, 3);
		
		GRBException ex = new GRBException();
		double[] res = null;
		double[] cres = new double[]{0,0,0,0};
		double opt = -1;
		double copt = 0;
		try
		{
			ModelMatrix mc = new ModelMatrix(o);
			IRA i = new IRA(mc); 
			i.addConstraint(new double[]{1, 1, 1, 1}, 0, "additional Constraint");
			i.calculate();
			res = i.getIntegerResults();
			opt = i.getOpt();
		}
		catch(GRBException e)
		{
			ex = e;
		}
		
		assertNotNull("Exception during IRA calculation", ex);
		
		assertTrue("Decision variables have wrong values", Methods.compareVector(res, cres) == -1);
		assertTrue("Optimum has the wrong value!", opt == copt);
	}
	
	/**
	 * In case there is the additional Z-constraint there might be a problem.
	 * One more constraint might prevent IRA from finding an integer solution.
	 * Therefore, it was necessary to take this special case into account.
	 */
	@Test
	public void testSpecialZConstrCase()
	{
		PreferenceTable pt = new PrefTableAuto(2);
		TimeSlot a = new TimeSlot("a", 1);
		TimeSlot b = new TimeSlot("b", 1);
		
		Bundle first = new Bundle(new TimeSlot[]{a,a});
		Bundle second = new Bundle(new TimeSlot[]{b,b});
		
		pt.addPreference("1", first, 5);
		pt.addPreference("1", second, 1);
		
		ModelMatrix mm = new ModelMatrix(pt);
		
		GRBException ex = new GRBException();
		double[] res = null;
		double[] cres = new double[]{0,1};
		double opt = -1;
		double copt = 1;
		try
		{
			IRA i = new IRA(mm); 
			i.addConstraint(new double[]{0.75, -0.5}, 0.5, "Z constr");
			i.calculate();
			res = i.getIntegerResults();
			opt = i.getOpt();
		}
		catch(GRBException e)
		{
			ex = e;
		}
		
		assertNotNull("Exception during IRA calculation", ex);
		
		assertTrue("Decision variables have wrong values", Methods.compareVector(res, cres) == -1);
		assertTrue("Optimum has the wrong value!", opt == copt);
	}
}
