package test;

import static org.junit.Assert.*;
import org.junit.Test;
import maxcu.*;

public class ModelMatrixTest {
	/**
	 * Tests the calculations of ModelCreator. It is the same example as in the presentation
	 */
	@Test
	public void test() {
		PreferenceTable o = new PrefTableAuto(10);
		TimeSlot tsA = new TimeSlot("A", 1);
		TimeSlot tsB = new TimeSlot("B", 1);
		Bundle first = new Bundle(new TimeSlot[]{tsA});
		Bundle second = new Bundle(new TimeSlot[]{tsA, tsB});
		
		o.addPreference("1", first, 1);
		o.addPreference("1", second, 2);
		o.addPreference("2", second, 3);
		
		ModelMatrix mc = new ModelMatrix(o);
		double[][] dmatrix = mc.getDmatrix();
		double[] dconstraints = mc.getdConstraint();
		
		double[][] cM = new double[6][4]; //compare matrix
		cM[0][0] = 1;	cM[0][1] = 1;	cM[0][2] = 0;	cM[0][3] = 0;
		cM[1][0] = 0;	cM[1][1] = 0;	cM[1][2] = 0;	cM[1][3] = 1;
		cM[2][0] = 1;	cM[2][1] = 1;	cM[2][2] = 0;	cM[2][3] = 1;
		cM[3][0] = 0;	cM[3][1] = 1;	cM[3][2] = 0;	cM[3][3] = 1;
		cM[4][0] = -1;	cM[4][1] = -2;	cM[4][2] = 0;	cM[4][3] = 2;
		cM[5][0] = 0;	cM[5][1] = 3;	cM[5][2] = 0;	cM[5][3] = -3;
		
		double[] cV = new double[6]; //compare vector
		cV[0] = 1; cV[1] = 1; cV[2] = 1;
		cV[3] = 1; cV[4] = 0; cV[5] = 0;
		
		int[] result = Methods.compareMatrix(dmatrix, cM);
		
		assertTrue("The matrix was not correctly created: " + result[0] + "; " + result[1], result[0] == -1 && result[1] == -1);
		
		int result2 = Methods.compareVector(dconstraints, cV);
		
		assertTrue("The constraint vector was not correctly created: " + result2, result2 == -1);
		
		int dims = mc.getDimensionality();
		assertTrue("The dimensionality was not created correctly: " + dims, dims == 3);
		
		double[] costCoeff = mc.getCostCoeff();
		double[] ccostCoeff = new double[]{1,2,0,3};
		assertTrue("The cost coefficients were not correctly created", Methods.compareVector(costCoeff, ccostCoeff) == -1);
		
		assertTrue("The size of the varDescriptions[] array is not correct", mc.getVarDescr().length == 4);
	}
	
	/**
	 * Tests the calculations of ModelCreator. Same as Test1, but different capacity of a timeslot
	 */
	@Test
	public void test2() {
		// same as test before, just different capacity in time slot B
		
		
		PreferenceTable o = new PrefTableAuto(10);
		TimeSlot tsA = new TimeSlot("A", 1);
		TimeSlot tsB = new TimeSlot("B", 2);
		Bundle first = new Bundle(new TimeSlot[]{tsA});
		Bundle second = new Bundle(new TimeSlot[]{tsA, tsB});
		
		o.addPreference("1", first, 1);
		o.addPreference("1", second, 2);
		o.addPreference("2", second, 3);
		
		ModelMatrix mc = new ModelMatrix(o);
		double[][] dmatrix = mc.getDmatrix();
		double[] dconstraints = mc.getdConstraint();
		
		double[][] cM = new double[6][4]; //compare matrix
		cM[0][0] = 1;	cM[0][1] = 1;	cM[0][2] = 0;	cM[0][3] = 0;
		cM[1][0] = 0;	cM[1][1] = 0;	cM[1][2] = 0;	cM[1][3] = 1;
		cM[2][0] = 1;	cM[2][1] = 1;	cM[2][2] = 0;	cM[2][3] = 1;
		cM[3][0] = 0;	cM[3][1] = 1;	cM[3][2] = 0;	cM[3][3] = 1;
		cM[4][0] = -1;	cM[4][1] = -2;	cM[4][2] = 0;	cM[4][3] = 2;
		cM[5][0] = 0;	cM[5][1] = 3;	cM[5][2] = 0;	cM[5][3] = -3;
		
		double[] cV = new double[6]; //compare vector
		cV[0] = 1; cV[1] = 1; cV[2] = 1;
		cV[3] = 2; cV[4] = 0; cV[5] = 0;
		
		int[] result = Methods.compareMatrix(dmatrix, cM);
		
		assertTrue("The matrix was not correctly created: " + result[0] + "; " + result[1], result[0] == -1 && result[1] == -1);
		
		int result2 = Methods.compareVector(dconstraints, cV);
		
		assertTrue("The constraint vector was not correctly created: " + result2, result2 == -1);
		
		int dims = mc.getDimensionality();
		assertTrue("The dimensionality was not created correctly: " + dims, dims == 3);
	}

	/**
	 * Test the calculation on a matrix with 3 bundles and 2 clients.
	 * Also tests the associatedBundles and associatedClients
	 */
	@Test
	public void test3()
	{
		PreferenceTable o = new PrefTableAuto(10);
		TimeSlot tsA = new TimeSlot("A", 2);
		TimeSlot tsB = new TimeSlot("B", 3);
		TimeSlot tsC = new TimeSlot("C", 1);
		Bundle first = new Bundle(new TimeSlot[]{tsA});
		Bundle second = new Bundle(new TimeSlot[]{tsA, tsB});
		Bundle third = new Bundle(new TimeSlot[]{tsB, tsC});
		
		o.addPreference("1", first, 2);
		o.addPreference("1", third, 3);
		o.addPreference("2", first, 1);
		o.addPreference("2", second, 4);
		
		ModelMatrix mc = new ModelMatrix(o);
		double[][] dmatrix = mc.getDmatrix();
		double[] dconstraints = mc.getdConstraint();
		
		double[][] cM = new double[7][6]; //compare matrix
		cM[0][0] = 1;	cM[0][1] = 0;	cM[0][2] = 1;	cM[0][3] = 0;	cM[0][4] = 0;	cM[0][5] = 0;
		cM[1][0] = 0;	cM[1][1] = 0;	cM[1][2] = 0;	cM[1][3] = 1;	cM[1][4] = 1;	cM[1][5] = 0;
		cM[2][0] = 1;	cM[2][1] = 0;	cM[2][2] = 0;	cM[2][3] = 1;	cM[2][4] = 1;	cM[2][5] = 0;
		cM[3][0] = 0;	cM[3][1] = 0;	cM[3][2] = 1;	cM[3][3] = 0;	cM[3][4] = 1;	cM[3][5] = 0;
		cM[4][0] = 0;	cM[4][1] = 0;	cM[4][2] = 1;	cM[4][3] = 0;	cM[4][4] = 0;	cM[4][5] = 0;
		cM[5][0] = -2;	cM[5][1] = 0;	cM[5][2] = -3;	cM[5][3] = 2;	cM[5][4] = 0;	cM[5][5] = 0;
		cM[6][0] = 1;	cM[6][1] = 0;	cM[6][2] = 0;	cM[6][3] = -1;	cM[6][4] = -4;	cM[6][5] = 0;
		
		double[] cV = new double[7]; //compare vector
		cV[0] = 1; cV[1] = 1; cV[2] = 2;
		cV[3] = 3; cV[4] = 1; cV[5] = 0; cV[6] = 0;
		
		int[] result = Methods.compareMatrix(dmatrix, cM);
		
		assertTrue("The matrix was not correctly created: " + result[0] + "; " + result[1], result[0] == -1 && result[1] == -1);
		
		int result2 = Methods.compareVector(dconstraints, cV);
		
		assertTrue("The constraint vector was not correctly created: " + result2, result2 == -1);
		
		int dims = mc.getDimensionality();
		assertTrue("The dimensionality was not created correctly: " + dims, dims == 4);
		
		Bundle[] associatedBundles = mc.getAssociatedBundles();
		String[] associatedClients = mc.getAssociatedClients();
		
		assertTrue("The associatedBundles where not created correctly", 
				associatedBundles[0] == first &&
				associatedBundles[1] == second &&
				associatedBundles[2] == third &&
				associatedBundles[3] == first &&
				associatedBundles[4] == second &&
				associatedBundles[5] == third &&
				associatedBundles.length == 6);
		
		assertTrue("The associatedClients where not created correctly", 
				associatedClients[0].equals("1") &&
				associatedClients[1].equals("1") &&
				associatedClients[2].equals("1") &&
				associatedClients[3].equals("2") &&
				associatedClients[4].equals("2") &&
				associatedClients[5].equals("2") &&
				associatedClients.length == 6);
	}

	/**
	 * Test the calculation on a matrix with 2 bundles and 3 clients
	 */
	@Test
	public void test4()
	{
		PreferenceTable o = new PrefTableAuto(10);
		TimeSlot tsA = new TimeSlot("A", 2);
		TimeSlot tsB = new TimeSlot("B", 3);
		Bundle first = new Bundle(new TimeSlot[]{tsA});
		Bundle second = new Bundle(new TimeSlot[]{tsA, tsB});
		
		o.addPreference("1", first, 4);
		o.addPreference("2", second, 1);
		o.addPreference("3", first, 3);
		o.addPreference("3", second, 2);
		
		ModelMatrix mc = new ModelMatrix(o);
		double[][] dmatrix = mc.getDmatrix();
		double[] dconstraints = mc.getdConstraint();
		
		double[][] cM = new double[11][6]; //compare matrix
		cM[0][0] = 1;	cM[0][1] = 0;	cM[0][2] = 0;	cM[0][3] = 0;	cM[0][4] = 0;	cM[0][5] = 0;	//demand 1
		cM[1][0] = 0;	cM[1][1] = 0;	cM[1][2] = 0;	cM[1][3] = 1;	cM[1][4] = 0;	cM[1][5] = 0;	//demand 2
		cM[2][0] = 0;	cM[2][1] = 0;	cM[2][2] = 0;	cM[2][3] = 0;	cM[2][4] = 1;	cM[2][5] = 1;	//demand 3
		cM[3][0] = 1;	cM[3][1] = 0;	cM[3][2] = 0;	cM[3][3] = 1;	cM[3][4] = 1;	cM[3][5] = 1;	//supply A
		cM[4][0] = 0;	cM[4][1] = 0;	cM[4][2] = 0;	cM[4][3] = 1;	cM[4][4] = 0;	cM[4][5] = 1;	//supply B
		cM[5][0] = -4;	cM[5][1] = 0;	cM[5][2] = 0;	cM[5][3] = 0;	cM[5][4] = 0;	cM[5][5] = 0;	//envy 1-2
		cM[6][0] = -4;	cM[6][1] = 0;	cM[6][2] = 0;	cM[6][3] = 0;	cM[6][4] = 4;	cM[6][5] = 0;	//envy 1-3
		cM[7][0] = 0;	cM[7][1] = 0;	cM[7][2] = 0;	cM[7][3] = -1;	cM[7][4] = 0;	cM[7][5] = 0;	//envy 2-1
		cM[8][0] = 0;	cM[8][1] = 0;	cM[8][2] = 0;	cM[8][3] = -1;	cM[8][4] = 0;	cM[8][5] = 1;	//envy 2-3
		cM[9][0] = 3;	cM[9][1] = 0;	cM[9][2] = 0;	cM[9][3] = 0;	cM[9][4] = -3;	cM[9][5] = -2;	//envy 3-1
		cM[10][0] = 0;	cM[10][1] = 0;	cM[10][2] = 0;	cM[10][3] = 2;	cM[10][4] = -3;	cM[10][5] = -2;	//envy 3-2
		
		double[] cV = new double[11]; //compare vector
		cV[0] = 1; cV[1] = 1; cV[2] = 1;
		cV[3] = 2; cV[4] = 3; cV[5] = 0;
		cV[6] = 0; cV[7] = 0; cV[8] = 0;
		cV[9] = 0; cV[10] = 0;
		
		int[] result = Methods.compareMatrix(dmatrix, cM);
		
		assertTrue("The matrix was not correctly created: " + result[0] + "; " + result[1], result[0] == -1 && result[1] == -1);
		
		int result2 = Methods.compareVector(dconstraints, cV);
		
		assertTrue("The constraint vector was not correctly created: " + result2, result2 == -1);
		
		int dims = mc.getDimensionality();
		assertTrue("The dimensionality was not created correctly: " + dims, dims == 4);
	}
	
	@Test
	public void testCostCoeff()
	{
		PreferenceTable o = new PrefTableAuto(10);
		TimeSlot tsA = new TimeSlot("A", 1);
		TimeSlot tsB = new TimeSlot("B", 1);
		Bundle first = new Bundle(new TimeSlot[]{tsA});
		Bundle second = new Bundle(new TimeSlot[]{tsA, tsB});
		
		o.addPreference("1", first, 1);
		o.addPreference("1", second, 2);
		o.addPreference("2", second, 3);
		
		o.addClientWeight("1", 4);
		
		ModelMatrix mc = new ModelMatrix(o);
		double[] costCoeff = mc.getCostCoeff();
		
		assertTrue("Cost coefficient not correct: " + costCoeff[0], costCoeff[0] == 4);
		assertTrue("Cost coefficient not correct: " + costCoeff[1], costCoeff[1] == 8);
		assertTrue("Cost coefficient not correct: " + costCoeff[2], costCoeff[2] == 0);
		assertTrue("Cost coefficient not correct: " + costCoeff[3], costCoeff[3] == 3);
		
		int dims = mc.getDimensionality();
		assertTrue("The dimensionality was not created correctly: " + dims, dims == 3);
	}
	
	/**
	 * This method tests the removeZeroCols() method in ModelMatrix
	 */
	@Test
	public void testColumnRemoval()
	{
		PreferenceTable o = new PrefTableAuto(10);
		TimeSlot tsA = new TimeSlot("A", 1);
		TimeSlot tsB = new TimeSlot("B", 1);
		Bundle first = new Bundle(new TimeSlot[]{tsA});
		Bundle second = new Bundle(new TimeSlot[]{tsA, tsB});
		
		o.addPreference("1", first, 1);
		o.addPreference("1", second, 2);
		o.addPreference("2", second, 3);
		
		ModelMatrix.createReduced = true;
		ModelMatrix mc = new ModelMatrix(o);
		ModelMatrix.createReduced = false;
		//mc.removeZeroCols2();
		
		double[][] dmatrix = mc.getDmatrix();
		double[] dconstraints = mc.getdConstraint();
		
		double[][] cM = new double[6][3]; //compare matrix
		cM[0][0] = 1;	cM[0][1] = 1;	cM[0][2] = 0;
		cM[1][0] = 0;	cM[1][1] = 0;	cM[1][2] = 1;
		cM[2][0] = 1;	cM[2][1] = 1;	cM[2][2] = 1;
		cM[3][0] = 0;	cM[3][1] = 1;	cM[3][2] = 1;
		cM[4][0] = -1;	cM[4][1] = -2;	cM[4][2] = 2;
		cM[5][0] = 0;	cM[5][1] = 3;	cM[5][2] = -3;
		
		double[] cV = new double[6]; //compare vector
		cV[0] = 1; cV[1] = 1; cV[2] = 1;
		cV[3] = 1; cV[4] = 0; cV[5] = 0;
		
		int[] result = Methods.compareMatrix(dmatrix, cM);
		
		assertTrue("The matrix was not correctly created: " + result[0] + "; " + result[1], result[0] == -1 && result[1] == -1);
		
		int result2 = Methods.compareVector(dconstraints, cV);
		
		assertTrue("The constraint vector was not correctly created: " + result2, result2 == -1);
		
		int dims = mc.getDimensionality();
		assertTrue("The dimensionality was not created correctly: " + dims, dims == 3);
		
		double[] costCoeff = mc.getCostCoeff();
		double[] ccostCoeff = new double[]{1,2,3};
		assertTrue("The cost coefficients were not correctly created", Methods.compareVector(costCoeff, ccostCoeff) == -1);
		
		assertTrue("The size of the varDescriptions[] array is not correct", mc.getVarDescr().length == 3);
	}

	/**
	 * Test the calculation on a matrix with 2 bundles and 3 clients
	 */
	@Test
	public void testColumnRemoval2()
	{
		PreferenceTable o = new PrefTableAuto(10);
		TimeSlot tsA = new TimeSlot("A", 2);
		TimeSlot tsB = new TimeSlot("B", 3);
		Bundle first = new Bundle(new TimeSlot[]{tsA});
		Bundle second = new Bundle(new TimeSlot[]{tsA, tsB});
		
		o.addPreference("1", first, 4);
		o.addPreference("2", second, 1);
		o.addPreference("3", first, 3);
		o.addPreference("3", second, 2);
		ModelMatrix.createReduced = true;
		ModelMatrix mc = new ModelMatrix(o);
		ModelMatrix.createReduced = false;
		double[][] dmatrix = mc.getDmatrix();
		double[] dconstraints = mc.getdConstraint();
		
		double[][] cM = new double[9][4]; //compare matrix
		cM[0][0] = 1;	cM[0][1] = 0;	cM[0][2] = 0;	cM[0][3] = 0;	//demand 1
		cM[1][0] = 0;	cM[1][1] = 1;	cM[1][2] = 0;	cM[1][3] = 0;	//demand 2
		cM[2][0] = 0;	cM[2][1] = 0;	cM[2][2] = 1;	cM[2][3] = 1;	//demand 3
		cM[3][0] = 1;	cM[3][1] = 1;	cM[3][2] = 1;	cM[3][3] = 1;	//supply A
		cM[4][0] = 0;	cM[4][1] = 1;	cM[4][2] = 0;	cM[4][3] = 1;	//supply B
		//cM[5][0] = -4;	cM[5][1] = 0;	cM[5][2] = 0;	cM[5][3] = 0;	//envy 1-2
		cM[5][0] = -4;	cM[5][1] = 0;	cM[5][2] = 4;	cM[5][3] = 0;	//envy 1-3
		//cM[7][0] = 0;	cM[7][1] = -1;	cM[7][2] = 0;	cM[7][3] = 0;	//envy 2-1
		cM[6][0] = 0;	cM[6][1] = -1;	cM[6][2] = 0;	cM[6][3] = 1;	//envy 2-3
		cM[7][0] = 3;	cM[7][1] = 0;	cM[7][2] = -3;	cM[7][3] = -2;	//envy 3-1
		cM[8][0] = 0;	cM[8][1] = 2;	cM[8][2] = -3;	cM[8][3] = -2;	//envy 3-2
		
		double[] cV = new double[9]; //compare vector
		cV[0] = 1; cV[1] = 1; cV[2] = 1;
		cV[3] = 2; cV[4] = 3; //cV[5] = 0;
		cV[5] = 0; //cV[7] = 0;
		cV[6] = 0;
		cV[7] = 0; cV[8] = 0;
		
		int[] result = Methods.compareMatrix(dmatrix, cM);
		
		assertTrue("The matrix was not correctly created: " + result[0] + "; " + result[1], result[0] == -1 && result[1] == -1);
		
		int result2 = Methods.compareVector(dconstraints, cV);
		
		assertTrue("The constraint vector was not correctly created: " + result2, result2 == -1);
		
		int dims = mc.getDimensionality();
		assertTrue("The dimensionality was not created correctly: " + dims, dims == 4);
	}
		
	/**
	 * Test the calculation on a matrix with 3 bundles and 2 clients.
	 * Key of this test, is that some of the columns (decision variables) and the envy constraints are not necessary.
	 * Those should not be created right from the beginning 
	 * Also tests the associatedBundles and associatedClients variables
	 */
	@Test
	public void testColumnAndEnvyRemoval()
	{
		PreferenceTable o = new PrefTableAuto(10);
		TimeSlot tsA = new TimeSlot("A", 2);
		TimeSlot tsB = new TimeSlot("B", 3);
		TimeSlot tsC = new TimeSlot("C", 1);
		Bundle first = new Bundle(new TimeSlot[]{tsA});
		Bundle second = new Bundle(new TimeSlot[]{tsA, tsB});
		Bundle third = new Bundle(new TimeSlot[]{tsB, tsC});
		
		o.addPreference("1", first, 2);
		o.addPreference("1", third, 3);
		o.addPreference("2", first, 1);
		o.addPreference("2", second, 4);
		
		o.addClientWeight("1", 3);
		o.addClientWeight("2", 2);
		
		ModelMatrix.createReduced = true;
		ModelMatrix mc = new ModelMatrix(o);
		ModelMatrix.createReduced = false;
		
		double[][] dmatrix = mc.getDmatrix();
		double[] dconstraints = mc.getdConstraint();
		
		double[][] cM = new double[7][4]; //compare matrix
		cM[0][0] = 1;	cM[0][1] = 1;	cM[0][2] = 0;	cM[0][3] = 0;
		cM[1][0] = 0;	cM[1][1] = 0;	cM[1][2] = 1;	cM[1][3] = 1;
		cM[2][0] = 1;	cM[2][1] = 0;	cM[2][2] = 1;	cM[2][3] = 1;
		cM[3][0] = 0;	cM[3][1] = 1;	cM[3][2] = 0;	cM[3][3] = 1;
		cM[4][0] = 0;	cM[4][1] = 1;	cM[4][2] = 0;	cM[4][3] = 0;
		cM[5][0] = -2;	cM[5][1] = -3;	cM[5][2] = 2;	cM[5][3] = 0;
		cM[6][0] = 1;	cM[6][1] = 0;	cM[6][2] = -1;	cM[6][3] = -4;
		
		double[] cV = new double[7]; //compare vector
		cV[0] = 1; cV[1] = 1; cV[2] = 2;
		cV[3] = 3; cV[4] = 1; cV[5] = 0; cV[6] = 0;
		
		int[] result = Methods.compareMatrix(dmatrix, cM);
		
		assertTrue("The matrix was not correctly created: " + result[0] + "; " + result[1], result[0] == -1 && result[1] == -1);
		
		int result2 = Methods.compareVector(dconstraints, cV);
		
		assertTrue("The constraint vector was not correctly created: " + result2, result2 == -1);
		
		int dims = mc.getDimensionality();
		assertTrue("The dimensionality was not created correctly: " + dims, dims == 4);
		
		Bundle[] associatedBundles = mc.getAssociatedBundles();
		String[] associatedClients = mc.getAssociatedClients();
		
		assertTrue("The associatedBundles where not created correctly", 
				associatedBundles[0] == first &&
				associatedBundles[1] == third &&
				associatedBundles[2] == first &&
				associatedBundles[3] == second &&
				associatedBundles.length == 4);
		
		assertTrue("The associatedClients where not created correctly", 
				associatedClients[0].equals("1") &&
				associatedClients[1].equals("1") &&
				associatedClients[2].equals("2") &&
				associatedClients[3].equals("2") &&
				associatedClients.length == 4);
		
		double[] costCoeff = mc.getCostCoeff();
		
		assertTrue("The cost coefficients where not created correctly", 
				costCoeff[0] == 6 &&
				costCoeff[1] == 9 &&
				costCoeff[2] == 2 &&
				costCoeff[3] == 8 &&
				costCoeff.length == 4);
	}
}
