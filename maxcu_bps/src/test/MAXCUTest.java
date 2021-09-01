package test;

import static org.junit.Assert.*;

import org.junit.Test;

import gurobi.GRBException;
import maxcu.*;

public class MAXCUTest {

	@Test
	public void test() {
		PreferenceTable o = new PrefTableAuto(2);
		TimeSlot tsA = new TimeSlot("A", 1);
		TimeSlot tsB = new TimeSlot("B", 1);
		Bundle first = new Bundle(new TimeSlot[]{tsA});
		Bundle second = new Bundle(new TimeSlot[]{tsA, tsB});
		
		o.addPreference("1", first, 1);
		o.addPreference("1", second, 2);
		o.addPreference("2", second, 3);
		
		double[][] vectors = null;
		double[] lambdas = null;
		
		GRBException ex = null;
		try
		{
			MAXCU m = new MAXCU(o, 0.1, 0.25);
			m.calculate();
			
			vectors = m.getS();
			lambdas = m.getLambdas();
		}
		catch(GRBException e)
		{
			ex = e;
		}
		
		assertTrue("GRBException during MAXCU calculation", ex == null);
		
		
		double[][] cvec = new double[2][4];
		cvec[0][0] = 0; cvec[0][1] = 0; cvec[0][2] = 0; cvec[0][3] = 1;
		cvec[1][0] = 0; cvec[1][1] = 1; cvec[1][2] = 0; cvec[1][3] = 0;
		
		double[] clam = new double[]{0.5, 0.5};
		
		int[] result = Methods.compareMatrix(cvec, vectors);
		assertTrue("The vectors where not correctly created: " + result[0] + "; " + result[1], result[0] == -1 && result[1] == -1);
		
		int result2 = Methods.compareVector(clam, lambdas, 0.0000000001);
		assertTrue("The lambdas were not correctly created: " + result2, result2 == -1);
	}

	
	/**
	 * This test gets an input, where the slack in the minimum slack in the demand constraints
	 * is zero after the first fractional calculation. Therefore, the maximum allowed delta is zero.
	 * The program therefore should scale the fractional solution
	 */
	@Test
	public void test2() {
		TimeSlot tsA = new TimeSlot("A", 2);
		TimeSlot tsB = new TimeSlot("B", 1);
		Bundle a = new Bundle(new TimeSlot[]{tsA});
		Bundle ab = new Bundle(new TimeSlot[]{tsA, tsB});
		
		PreferenceTable pt = new PrefTableAuto(2);
		pt.addPreference("1", a, 4);
		pt.addPreference("2", ab, 3);
		pt.addPreference("3", a, 2);
		pt.addPreference("3", ab, 4);
		MAXCU m = null; 
		Exception ex2 = null;
		try
		{
			m = new MAXCU(pt, 0.2, 0.25);
		}
		catch(GRBException e)
		{
			ex2 = e;
		}

		assertTrue("Constructor should not throw a GRBException", ex2 == null);
		
		double[] dv = m.getDVs();
		double[] dvsComp = new double[]{1, 0, 0, 0.5, 0, 0.5};
		
		assertTrue("Deviations in scaled decision variables", 0 > Methods.compareVector(dv, dvsComp, 0.00000001));
		
		double[] dvScaled = m.getDVScaled();
		double[] dvScaledComp = new double[]{0.75, 0, 0, 0.375, 0, 0.375};
		
		assertTrue("Deviations in scaled decision variables", 0 > Methods.compareVector(dvScaled, dvScaledComp, 0.00000001));
	}

	@Test
	public void test3()
	{
		TimeSlot tsA = new TimeSlot("A", 1);
		TimeSlot tsB = new TimeSlot("B", 1);
		TimeSlot tsC = new TimeSlot("C", 1);
		Bundle ab = new Bundle(new TimeSlot[]{tsA, tsB});
		Bundle ac = new Bundle(new TimeSlot[]{tsA, tsC});
		
		PreferenceTable pt = new PrefTableAuto(2);
		pt.addPreference("1", ac, 5);
		pt.addPreference("2", ab, 1);
		pt.addPreference("3", ac, 4);
		
		ModelMatrix mm = new ModelMatrix(pt);
		
		System.out.println(mm.constraintTable());
		
		Exception e = null;
		
		//slack of 0.5
		MAXCU m = null;
		try
		{
			m = new MAXCU(pt, 0.2, 0.12);
			
			m.calculate();
		}
		catch(Exception ex)
		{
			e = ex;
		}
		
		assertTrue("Calculation should not throw an exception", e == null);
		
		assertTrue("Deviations in lambdas", 0 > Methods.compareVector(m.getLambdas(), new double[]{0.5, 0.5}, 0.00000001));
		
		
		int[] deviations = Methods.compareMatrix(m.getS(), new double[][]{{0,1,0,0,0,0},{0,0,0,0,0,1}});
		assertTrue("Deviations in S", deviations[0] < 0 && deviations[1] < 0);
	}
}
