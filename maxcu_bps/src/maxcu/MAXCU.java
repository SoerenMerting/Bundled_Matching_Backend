package maxcu;

import java.util.ArrayList;
import java.util.Arrays;

import gurobi.GRBException;
import maxcu.ModelMatrix.constrType;
import maxcu.Vector;
import report.Report;

public class MAXCU {
	/**
	 * The model matrix with the LP
	 */
	private final ModelMatrix mm;
	/**
	 * The amount of dimensions in the linear program
	 */
	private final int dimensions;
	/**
	 * The report. It is specified in the constructor.
	 * If activated, the class always generates a report of the internals 
	 */
	private final Report r;
	
	/**
	 * Delta is a value, which tells by how much the program will approach the solution.
	 */
	private final double delta;
	/**
	 * The epsilon value is a value of quality.
	 * The smaller epsilon, the closer the convex hull of integer solutions must be to the fractional solution
	 */
	private final double epsilon;
	
	/**
	 * The original fractional decision variables, before they were scaled
	 */
	private final double[] fractDV;
	/**
	 * The original fractional optimum, before it was scaled
	 */
	private final double fractOpt;
	
	/**
	 * The scaled decision variables
	 * The optimum value is scaled whenever the slack in the demand constraints do not meet the delta requirement
	 */
	private double[] fractDVScaled;
	/**
	 * The optimum value after it has been scaled.
	 * The optimum value is scaled whenever the slack in the demand constraints do not meet the delta requirement
	 */
	private double fractOptScaled;
	
	/**
	 * The result set, which make up the convex hull
	 */
	private ArrayList<double[]> S = new ArrayList<double[]>();
	/**
	 * These are the scalars of the convex combination. They add up to one.
	 * Therefore, they are also the chance for which an allocation is chosen in the lottery 
	 */
	private double[] lambdas;
	/**
	 * the final distance from x and y (interesting for big epsilons)
	 */
	private double distance;
	/**
	 * The amount of iterations the class needed to calculate the final result
	 */
	private int iterations = 0;
	
	
	
	
	/**
	 * Initializes the class
	 * @param prefTable
	 * @param epsilon
	 * @param delta
	 * @throws GRBException
	 */
	public MAXCU(PreferenceTable prefTable, double epsilon, double delta, Report r) throws GRBException
	{
		this.r = r;
		if(epsilon <= 0)
			throw new IllegalArgumentException("The allowed error epsilon must be strictly greater than 0");
		
		//this.pref = pref;
		this.mm = new ModelMatrix(prefTable, new Report(r.isOn()));
		r.appendReport(mm.getReport());
		OptimumCalculator oc = new OptimumCalculator(mm, false);
		this.fractDV = oc.getDVAssigned();
		this.fractOpt = oc.getOpt();
		
	
		//createSlack(delta, oc.getMaxDelta());
		double a = 2;
		double[] slackparams = createSlack_Soeren(delta, oc.getMaxDelta(), epsilon, a);
		
		
		//if(delta > oc.getMaxDelta())
		//	throw new IllegalArgumentException("Delta must be smaller than " + oc.getMaxDelta());
		this.delta = slackparams[0]; //delta;		
		this.epsilon = slackparams[1] ; //epsilon;
		this.dimensions = mm.getDimensionality();
		System.out.println("Dimensionality is "+this.dimensions);
		
		initS();
	}
	
	/**
	 * Initializes the class
	 * @param prefTable
	 * @param epsilon
	 * @param delta
	 * @throws GRBException
	 */
	public MAXCU(PreferenceTable prefTable, double epsilon, double delta) throws GRBException
	{
		this(prefTable, epsilon, delta, new Report(false));
	}

	/**
	 * Initializes MAXCU with a precalculated fractional solution
	 * @param mm
	 * @param epsilon
	 * @param delta
	 * @param fractDV
	 * @param fractOpt
	 * @throws GRBException 
	 */
	public MAXCU(ModelMatrix mm, double epsilon, double delta, double[] fractDV, double fractOpt, Report r) throws GRBException
	{
		this.r = r;
		this.mm = mm;
		this.dimensions = mm.getDimensionality();
		System.out.println("Dimensionality is "+this.dimensions);


		//this.delta = delta;
		//this.epsilon = epsilon;
		this.fractDV = fractDV;
		this.fractOpt = fractOpt;
		
		double check = checkDelta();
		
		double a = 2;
		double[] slackparams = createSlack_Soeren(delta, check, epsilon, a);
		this.delta = slackparams[0]; //delta;		
		this.epsilon = slackparams[1] ; //epsilon;
		System.out.println("Delta:"+this.delta);

//		Disabled this functionality, as double is not precise enough
//		if(check < 0)
//			throw new IllegalArgumentException("Fractional solution provided is not legal");
//		if(delta > check)
//			throw new IllegalArgumentException("Delta is bigger than smallest slack in demand constraints");
	
		initS();
	}
	
	/**
	 * Initializes MAXCU with a precalculated fractional solution
	 * @param mm
	 * @param epsilon
	 * @param delta
	 * @param fractDV
	 * @param fractOpt
	 * @throws GRBException 
	 */
	public MAXCU(ModelMatrix mm, double epsilon, double delta, double[] fractDV, double fractOpt) throws GRBException
	{
		this(mm, epsilon, delta, fractDV, fractOpt, new Report(false));
	}
	
	/**
	 * This Method returns the maximum allowed delta value.
	 * If the fractional solution provided is illegal, the method returns -1
	 * @return
	 */
	private double checkDelta()
	{
		double res = Double.POSITIVE_INFINITY;
		double[][] matrix = mm.getDmatrix();
		double[] constr = mm.getdConstraint();
		constrType[] ct = mm.getConstrTypes();
		double temp = 0;
		for(int i = 0; i < constr.length;i++)
		{
			temp = 0;
			for(int j = 0; j < mm.getCostCoeff().length;j++)
			{
				temp += matrix[i][j] * fractDV[j];
			}
			
			temp -= constr[i];
			
//			Calculate validity of fractional solution. Disabled this functionality, as double is not precise enough
//			if(temp < 0)
//				return -1;
			
			if(temp < res && ct[i] == constrType.demand)
				res = temp;
		}
		
//		Sometimes double is not precise and the value is close to 0 but negative.
		if(res < 0)
			res = 0;
		
		return res;
	}

	/**
	 * This method initializes the variables fractDVScaled and fractOptScaled.
	 * The scaling is necessary, if there is not enough slack in the demand constraints.
	 * In that case, the fractional solution is down-scaled, so that the slack is high enough for the program
	 * to have in enough space "around" the fractional solution. 
	 * @param delta
	 * @param maxDemandSlack
	 */
	/*private void createSlack(double delta, double maxDemandSlack)
	{
		if(delta <= maxDemandSlack)
		{
			r.addParagraph("No need to scale the fractional solution as we already have sufficient slack");
			fractDVScaled = Arrays.copyOf(fractDV, fractDV.length);
			fractOptScaled = fractOpt;
			return;
		}
		double factor = 1 - (delta - maxDemandSlack);
		r.addParagraph("Scale the fractional solution by a factor of " + factor);
		
		fractDVScaled = new double[fractDV.length];
		for(int i = 0; i < fractDV.length;i++)
		{
			fractDVScaled[i] = fractDV[i] * factor;
		}
		
		double[] cc = mm.getCostCoeff();
		for(int i = 0; i < cc.length; i++)
		{
			fractOptScaled += cc[i] * fractDVScaled[i];
		}
	}*/
	
	/**
	 * This method initializes the variables fractDVScaled and fractOptScaled.
	 * The scaling is necessary, if there is not enough slack in the demand constraints.
	 * In that case, the fractional solution is down-scaled, so that the slack is high enough for the program
	 * to have in enough space "around" the fractional solution, but the scaled fractional solution is still in
	 * an epsilon environment of the fractional solution. The method returns an array containing an adapted 
	 * delta and the scaled epsilon. "a > 1 " controls how epsilon is divided into scaling and scaled epsilon.
	 * The greater "a" the smaller becomes delta and the bigger becomes the scaled epsilon (allowed error in 
	 * decomposition)
	 * @param delta
	 * @param maxDemandSlack
	 * @param epsilon
	 * @param a
	 */
	private double[] createSlack_Soeren(double delta, double maxDemandSlack, double epsilon, double a)
	{
		double[] result = {delta, epsilon};
		if(delta <= maxDemandSlack)
		{
			r.addParagraph("No need to scale the fractional solution as we already have sufficient slack");
			fractDVScaled = Arrays.copyOf(fractDV, fractDV.length);
			fractOptScaled = fractOpt;			
			return result;
		}
		
		delta = epsilon/(a*Vector.vectorLength(fractDV));
		epsilon = (a-1)*epsilon/a;
		
		result[0] = delta;
		result[1] = epsilon;
		
		double factor = 1 - delta;
		r.addParagraph("Scale the fractional solution by a factor of " + factor);
		
		fractDVScaled = new double[fractDV.length];
		for(int i = 0; i < fractDV.length;i++)
		{
			fractDVScaled[i] = fractDV[i] * factor;
		}
		
		double[] cc = mm.getCostCoeff();
		for(int i = 0; i < cc.length; i++)
		{
			fractOptScaled += cc[i] * fractDVScaled[i];
		}
		return result;
	}
	
	/**
	 * Initializes the S set with IRA
	 * @throws GRBException
	 */
	private void initS() throws GRBException
	{
		r.addParagraph("Fractional solution (OptimumCalculator)\n");
		r.openTable();
		r.addTableEntries(mm.getVarDescr(), 0,0);
		r.addTableSeparator();
		r.addTableEntries(fractDVScaled, 0, 0);
		r.closeTable();
		r.addParagraph("Optimum: " + Report.round(fractOpt) + "; scaled: " + Report.round(fractOptScaled));
		
		r.addParagraph("\n\nMAXCU\n\n");
		
		r.addParagraph("0: Initialize S using IRA (see next chapter)");
		//step 0 in the presentation
		System.out.println("Start Calculation for initS");

		//Aykut
		OptimumCalculator lp = new OptimumCalculator(this.mm, false);
		System.out.println("lp");
		double[] solution = lp.getDVAssigned();

		IRA ira = new IRA(mm, solution, new Report(r.isOn()));
		System.out.println("ira");

		//Aykut ende
		ira.calculate();
		System.out.println("Ira calculated in initS");
		
		S.add(ira.getIntegerResults());
		lambdas = new double[]{1};
		reportS();
		r.appendReport(ira.getReport());
	}
	
	/**
	 * Creates the report for the result set S
	 */
	private void reportS()
	{
		String[] varnames = mm.getVarDescr();
		double[] current = null;
		r.addParagraph("\nSet S of integer solutions:\n");
		r.openTable();
		r.addTableEntries(varnames, 1, 0);
		r.addTableEntry("Probabilities");
		r.addTableSeparator();
		for(int i = 0; i < S.size(); i++)
		{
			r.addTableEntry("S["+i+"]");
			current = S.get(i);
			for(int j = 0; j < varnames.length; j++)
			{
				r.addTableEntry(current[j]);
			}
			r.addTableEntry(lambdas[i]);
			r.addTableNL();
		}
		r.closeTable();
	}
	
	/**
	 * Returns the report
	 * @return
	 */
	public Report getReport()
	{
		return r;
	}
	
	/**
	 * Calculates the decomposition
	 * @throws GRBException
	 */
	public void calculate() throws GRBException
	{	System.out.println("In MAXCU.Calculate");
		while(true)
		{
			r.addParagraph("MAXCU iteration: " + iterations);
			//STEP 1
			r.addParagraph("\nMAXCU step 1:");
			double[][] vectors = getVectors(S);

			
			ConvexHullDistance chd = new ConvexHullDistance(vectors, fractDVScaled);
			double[] y = chd.getY();
			double distance = chd.getDistance();
			this.lambdas = chd.getLambdas();
			
			reportS();
			
			r.openTable();
			r.addTableEntry("y (point in convex hull): "); r.addTableEntries(y, 0, 0);
			r.closeTable(" ");
			r.addParagraph("Distance between y and x: " + Report.round(distance) + "; epsilon: " + epsilon);
			System.out.println("Distance:"+(Math.round(distance*1000.0)/1000.0)+"vs Epsilon:" + epsilon);
			if(distance < epsilon)
			{
				r.addParagraph("Distance small enough. Exit after " + iterations + " iterations");
				this.distance = distance;
				break;
			}
		
			//STEP 2
			r.addParagraph("\nMAXCU step 2:");
			
			//A: reduce S to S' (Snew)
			r.addParagraph("\nReduce the set S of points to the hyperplane containing y");
			//double[] lambda = chd.getLambdas();
			
			int[] indices = getIndicesOfNMaxValues(lambdas, min(lambdas.length, dimensions));
			
			ArrayList<double[]> Snew = new ArrayList<double[]>();
			for(int i = 0; i < indices.length;i++)
			{
				Snew.add(S.get(indices[i]));
			}
			
			//B: calculate line
			r.addParagraph("\nCalculate the line constraint");
			double[] XminY = Vector.vectorSubtraction(fractDVScaled, y);
			double[] richtungsVector = Vector.vectorScalar(XminY, 1.0/distance);
			double[] z = Vector.vectorAddition(fractDVScaled, Vector.vectorScalar(richtungsVector, delta));
			
			double constraint = Vector.vectorVector(XminY, z);
			r.addParagraph(constraint);
			
			//C: calculate next iteration with the additional line constraint
			r.addParagraph("\nCalculate next integer solution using IRA (see next chapter)");
			IRA ira = new IRA(mm, z, new Report(r.isOn()));
			
			//in this line, we multiply with -1 in order to flip the greater than sign in the constraint

			ira.addConstraint(Vector.vectorScalar(XminY, -1), (-1) * constraint, "Line constraint");
			
			ira.calculate();
			
			//STEP 3
			r.addParagraph("\nMAXCU Step 3:\nAppend new IRA solution to S");
			Snew.add(ira.getIntegerResults());
			S = Snew;
			
			//reportS();
			
			r.appendReport(ira.getReport());
			
			iterations++;
		}
	}
	
//	public static void printArray(double[] arr)
//	{
//		
//		//System.out.println("AdditConstr: " + constraint);
//		//System.out.print("xminY: "); printArray(XminY);
//		//System.out.print("z: "); printArray(z);
//		for(int i = 0; i < arr.length; i++)
//		{
//			System.out.print(arr[i]);
//			System.out.print("; ");
//		}
//		System.out.println("");
//	}
	
	/**
	 * Returns the amount of maxcu iterations
	 * @return
	 */
	public int getIterations()
	{
		return iterations;
	}
	
	/**
	 * Returns the minimum value of the two
	 * @param a
	 * @param b
	 * @return
	 */
	private static int min(int a, int b)
	{
		if(a < b)
			return a;
		return b;
	}

	/**
	 * Converts an ArrayList<double[]> to double[][]
	 * @param S
	 * @return
	 */
	private static double[][] getVectors(ArrayList<double[]> S)
	{
		double[][] result = new double[S.size()][S.get(0).length];
		
		for(int i = 0; i < S.size(); i++)
		{
			result[i] = (double[])S.get(i);
		}
		
		return result;
	}
	
	/**
	 * Returns the lambdas of the convex combination
	 * These values are the probabilities, for which one of the allocations is chosen in the lottery.
	 * @return
	 */
	public double[] getLambdas()
	{
		return lambdas;
	}
	
	/**
	 * Returns the vectors of the decomposition.
	 * Each of the vector represents a valid solution to the matching problem.
	 * Combined with the lambdas (probabilites), the values make up the lottery.
	 * double[vector][feature]. To find out, what feature belongs to what client and bundle
	 * use the values associatedBundles and associatedClients of the class MAXCU.
	 * 1 means: client gets the bundle
	 * 0 means: client does not get the bundle
	 * @return
	 */
	public double[][] getS()
	{
		return getVectors(S);
	}
	
	/**
	 * Returns the value of the fractional optimum before it was scaled.
	 * Therefore, it is the original input of the constructor
	 * @return
	 */
	public double getFractOpt()
	{
		return fractOpt;
	}
	
	/**
	 * Returns the value of the scaled fractional optimum.
	 * This value differs in the case that the original fractional solution.
	 * See method "createSlack" doc for more info
	 * @return
	 */
	public double getFractOptScaled()
	{
		return fractOptScaled;
	}
	
	/**
	 * Returns the fractional decision variables, before they where scaled.
	 * Therefore, it returns just the original input from the constructor
	 * @return
	 */
	public double[] getDVs()
	{
		return fractDV;
	}
	
	/**
	 * Returns the scaled fractional solution. See doc of method "createSlack" for more details.
	 * @return
	 */
	public double[] getDVScaled()
	{
		return fractDVScaled;
	}
	
	/**
	 * Returns the final distance of x and y.
	 * x being the initial fractional solution and y being the closest point to x in the convex hull.
	 * @return
	 */
	public double getDistance()
	{
		return distance;
	}
	
	/**
	 * Returns the indices of the n highest values
	 * @param lambdas
	 * @param n
	 * @return
	 */
	private static int[] getIndicesOfNMaxValues(double[] lambdas, int n)
	{
		/*
		 * Why two loops are necessary for this algorithm:
		 * Imagine the input to be like this array: 4 7 3 6 4 9
		 * and we want the the 4 highest values
		 * internalLambdas (sorted) would be: 3 4 4 6 7 9
		 * the threshold would therefore be: 4 (index 2)
		 * If we had only one loop, where we would take every
		 * index which represents a value greater or equal the threshold
		 * we would get the values 4 - 7 - 6 - 4.
		 * This is wrong, as we would like a 9 instead of one of the 4s.
		 * 
		 * That's why the first loop takes every value strictly greater than the threshold
		 * The second loops fills up the remaining indices with the values equal the threshold
		 */
		
		
		double[] internalLambdas = Arrays.copyOf(lambdas,lambdas.length);
		Arrays.sort(internalLambdas);
	
		double threshold = internalLambdas[lambdas.length - n];
		
		int[] res = new int[n];
		int counter = 0;
		for(int i = 0; i < lambdas.length; i++)
		{
			if(lambdas[i] > threshold)
			{
				res[counter] = i;
				counter++;
			}
		}

		//take account of the possibility, that there are several lambdas with the threshold value
		for(int i = 0; i < lambdas.length;i++)
		{
			if(counter >= n)
				break;
			
			if(lambdas[i] == threshold)
			{
				res[counter] = i;
				counter++;
			}
		}
		
		return res;
	}
}
