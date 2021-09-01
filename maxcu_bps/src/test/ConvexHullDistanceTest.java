package test;

import static org.junit.Assert.*;

import org.junit.Test;

import gurobi.GRBException;
import maxcu.*;

public class ConvexHullDistanceTest {

	@Test
	public void test() {
		GRBException ex = null;
		try
		{
			double[][] t = new double[1][2];
			t[0][0] = 0;
			t[0][1] = 1;
			
			double[] v = new double[2];
			v[0] = 0;
			v[1] = 0;
			
			ConvexHullDistance bla = new ConvexHullDistance(t, v);
			
			double[] y = bla.getY();
			
			assertTrue("y[0] deviates", withinRange(y[0], 0, 0.001));
			assertTrue("y[1] deviates", withinRange(y[1], 1, 0.001));
		}
		catch(GRBException e)
		{
			ex = e;
		}
		
		assertNull("Exception while solving", ex);
	}
	
	@Test
	public void test1(){
		GRBException ex = null;
		try
		{
			double[][] t = new double[2][2];
			t[0][0] = 0;
			t[0][1] = 1;
			t[1][0] = 1;
			t[1][1] = 0;
			
			double[] v = new double[2];
			v[0] = 0;
			v[1] = 0;
			
			ConvexHullDistance bla = new ConvexHullDistance(t, v);
			
			double[] y = bla.getY();
			
			assertTrue("y[0] deviates", withinRange(y[0], 0.5, 0.001));
			assertTrue("y[1] deviates", withinRange(y[1], 0.5, 0.001));
		}
		catch(GRBException e)
		{
			ex = e;
		}
		
		assertNull("Exception while solving", ex);
	}

	@Test
	public void test2(){
		GRBException ex = null;
		try
		{
			double[][] t = new double[3][2];
			t[0][0] = 0;
			t[0][1] = 1;
			t[1][0] = 1;
			t[1][1] = 0;
			t[2][0] = 1;
			t[2][1] = 1;
			
			double[] v = new double[2];
			v[0] = 0;
			v[1] = 0;
			
			ConvexHullDistance bla = new ConvexHullDistance(t, v);
			
			double[] y = bla.getY();
			
			assertTrue("y[0] deviates", withinRange(y[0], 0.5, 0.001));
			assertTrue("y[1] deviates", withinRange(y[1], 0.5, 0.001));
		}
		catch(GRBException e)
		{
			ex = e;
		}
		
		assertNull("Exception while solving", ex);
	}
	
	@Test
	public void test3() {
		GRBException ex = null;
		try
		{
			double[][] t = new double[3][2];
			t[0][0] = 0;
			t[0][1] = 1;
			t[1][0] = 1;
			t[1][1] = 0;
			t[2][0] = 0;
			t[2][1] = -1;
			
			double[] v = new double[2];
			v[0] = 0;
			v[1] = 0;
			
			ConvexHullDistance bla = new ConvexHullDistance(t, v);
			
			double[] y = bla.getY();
			
			assertTrue("y[0] deviates", withinRange(y[0], 0, 0.001));
			assertTrue("y[1] deviates", withinRange(y[1], 0, 0.001));
		}
		catch(GRBException e)
		{
			ex = e;
		}
		
		assertNull("Exception while solving", ex);
	}

	@Test
	public void test4() {
		GRBException ex = null;
		try
		{
			double[][] t = new double[4][3];
			t[0][0] = 2;
			t[0][1] = 0;
			t[0][2] = 0;
			t[1][0] = 2;
			t[1][1] = 6;
			t[1][2] = 0;
			t[2][0] = 2;
			t[2][1] = 3;
			t[2][2] = 3;
			t[3][0] = 5;
			t[3][1] = 2;
			t[3][2] = 2;
			
			double[] v = new double[3];
			v[0] = 0;
			v[1] = 2;
			v[2] = 2;
			
			ConvexHullDistance bla = new ConvexHullDistance(t, v);
			
			double[] y = bla.getY();
			double dist = bla.getDistance();
			
			assertTrue("y[0] deviates", withinRange(y[0], 2, 0.001));
			assertTrue("y[1] deviates", withinRange(y[1], 2, 0.001));
			assertTrue("y[2] deviates", withinRange(y[2], 2, 0.001));
			
			assertTrue("Distance deviates", withinRange(dist, 2, 0.001));
		}
		catch(GRBException e)
		{
			ex = e;
		}
		
		assertNull("Exception while solving", ex);
	}
	
	@Test
	public void testWithinRange()
	{
		//double is a very imprecise data type
		assertTrue("", withinRange(10, 10, 0.0000000005));
		assertTrue("", withinRange(10.0009, 10, 0.001));
		assertTrue("", withinRange(10.0009999, 10, 0.001));
		assertFalse("", withinRange(10.001000001, 10, 0.001));
		assertFalse("", withinRange(9.99899, 10, 0.001));
		assertTrue("", withinRange(9.999001, 10, 0.001));
	}
	
	/**
	 * As double is a very imprecise data type, this method can compare two doubles giving the a possibility to deviate a little
	 * @param value
	 * @param compare
	 * @param range
	 * @return
	 */
	private static boolean withinRange(double value, double compare, double range)
	{
		if(value >= compare + range)
			return false;
		
		if(value <= compare - range)
			return false;
		
		return true;
	}

	public static void main(String[] args) throws GRBException
	{
		double[][] t = new double[3][2];
		t[0][0] = 0;
		t[0][1] = 1;
		t[1][0] = 1;
		t[1][1] = 1;
		t[2][0] = 100000000;
		t[2][1] = 1;
		
		double[] v = new double[2];
		v[0] = 0;
		v[1] = 0;
		
		ConvexHullDistance bla = new ConvexHullDistance(t, v);
		
		double[] y = bla.getY();
		for(int i = 0; i < y.length; i++)
		{
			System.out.println("i = " + i + "; " + y[i]);
		}
	}
}
