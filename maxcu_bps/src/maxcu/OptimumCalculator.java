package maxcu;

import gurobi.*;
import maxcu.ModelMatrix.constrType;
import report.Report;

/**
 * Class which calculates the LP using gurobi solver
 * @author Kevin
 *
 */
public class OptimumCalculator {
	public static boolean printSolution = false;
	
	private final ModelMatrix mc;
	
	private final Report r;
	
	private GRBEnv env = null;
	private GRBModel model = null;
	private GRBVar[] assigned = null;
	private double maxDelta;
	private double[] assignedVals;
	private boolean[] inUse;
	private double opt;
	
	private double[] slack;
	
	/**
	 * Initializes an OptimumCalculator object
	 * @param mc
	 * @throws GRBException 
	 */
	public OptimumCalculator(PreferenceTable pt, boolean integer, Report r) throws GRBException
	{
		this(new ModelMatrix(pt), integer, r);
	}
	
	/**
	 * Initializes an OptimumCalculator object
	 * @param mc
	 * @throws GRBException 
	 */
	public OptimumCalculator(PreferenceTable pt, boolean integer) throws GRBException
	{
		this(pt, integer, new Report(false));
	}
	/**
	 * Initializes an OptimumCalculator object with the weights per client
	 * @param mc
	 * @throws GRBException 
	 */
	public OptimumCalculator(ModelMatrix mc, boolean integer, Report r) throws GRBException
	{
		this.mc = mc;
		this.r = r;
		
		calculateOptimum(integer);
	}
	
	/**
	 * Initializes an OptimumCalculator object with the weights per client
	 * @param mc
	 * @throws GRBException 
	 */
	public OptimumCalculator(ModelMatrix mc, boolean integer) throws GRBException
	{
		this(mc, integer, new Report(false));
	}
	
	/**
	 * Calculates the optimum of the LP
	 * @param integer true if only integer solutions are allowed. Otherwise fractional
	 * @throws GRBException
	 */
	private void calculateOptimum(boolean integer) throws GRBException
	{
		System.out.println("Try to calculate optimum");
		char varType = integer ? GRB.INTEGER : GRB.CONTINUOUS;
		//int[][] prefTable = mc.prefTable;
		double[][] matrix = mc.getDmatrix();
		double[] vector = mc.getdConstraint();
		String[] names = mc.getConstraintNames();
		double[] costCoeff = mc.getCostCoeff();
		System.out.println("85");

		env = new GRBEnv();
		model = new GRBModel(env);
		model.set(GRB.StringAttr.ModelName, "Optimum");
		System.out.println("90");

		//ArrayList<Tuple<String, Bundle>> dv = mc.getDecisionVariables();
		int numDV = costCoeff.length;
		inUse = inUseVariables();
		int count = 0;
		for(int i = 0; i < inUse.length;i++)
		{
			if(inUse[i])
				count++;
		}
		
		r.addParagraph("OPTIMUMCALCULATOR");
		r.addParagraph("\n\nLP to solve: (" + count + " colums)\n");
		r.openTable();
		
		// Create decision variables
		assigned = new GRBVar[count];
		int iterator = 0;
		for (int j = 0; j < numDV; ++j)
		{
			if(inUse[j])
			{
				assigned[iterator] = model.addVar(0, GRB.INFINITY, costCoeff[j], varType, mc.getVarDescr()[j]);
				//System.out.println("DV added: " + mc.getVarDescr()[j] + " factor: " + costCoeff[j]);
				r.addTableEntry(mc.getVarDescr()[j] + " * " +costCoeff[j]);
				iterator++;
			}
		}
		
		r.addTableSeparator();
		System.out.println("121");

		//maximize utility:
		model.set(GRB.IntAttr.ModelSense, GRB.MAXIMIZE);
		
		model.update();
		
		//constraints
		for(int j = 0; j < matrix.length; j++)
		{
			GRBLinExpr ltot = new GRBLinExpr();
			int counter = 0;
			for(int i = 0; i < matrix[0].length; i++)
			{
				//System.out.print("Rule: ");
				if(inUse[i])
				{
					//System.out.print(matrix[j][i] + "*" + mc.getVarDescr()[i]);
					r.addTableEntry(matrix[j][i]);
					ltot.addTerm(matrix[j][i], assigned[counter]);
			        counter++;
				}
			}
			r.addTableEntry("<=");
			r.addTableEntry(vector[j]);
			r.addTableEntry(names[j]);
			r.addTableNL();
			//System.out.println(" <= " + vector[j] + " - " + names[j]);
	        model.addConstr(ltot, GRB.LESS_EQUAL, vector[j], names[j]);
		}
		
		r.closeTable();
		
		model.update();
		model.getEnv().set(GRB.IntParam.OutputFlag, 0);
		
		// Solve
	    model.optimize();
	    
	    this.maxDelta = calcMaxDelta();
	    this.assignSlack();
	    this.assignedVals = createDVVals();
	    this.opt = model.get(GRB.DoubleAttr.ObjVal);
	    
	    
	    if(printSolution)
	    	printSolution(model, assigned);
	    
	    
	    reportResult();
	}
	
	private void reportResult()
	{
		r.addParagraph("\nResult:");
		r.openTable();
		for(int i = 0; i < inUse.length; i++)
		{
			if(inUse[i])
			{
				r.addTableEntry(mc.getVarDescr()[i]);
				r.addTableEntry(assignedVals[i]);
				r.addTableNL();
			}
		}
		r.closeTable(" ");
		
		r.addParagraph("\nOptimum: " + this.getOpt());
		r.addParagraph("Minimum slack in demand constraint: " + this.getMaxDelta());
		
		r.closeChapter();
	}
	
	/**
	 * Prints the model's solution
	 * @param model
	 * @param assigned
	 * @throws GRBException
	 */
	private static void printSolution(GRBModel model, GRBVar[] assigned) throws GRBException
	{
		if (model.get(GRB.IntAttr.Status) == GRB.Status.OPTIMAL)
		{
			System.out.println("\nTotal utility: " + model.get(GRB.DoubleAttr.ObjVal));
			System.out.println("\nAssigned:");
			for (int j = 0; j < assigned.length; ++j)
			{
				if (assigned[j].get(GRB.DoubleAttr.X) > 0)
				{
					System.out.println(assigned[j].get(GRB.StringAttr.VarName) + " " +
							assigned[j].get(GRB.DoubleAttr.X));
				}
			}
		}
		else
		{
			System.out.println("No solution");
		}
		
		GRBConstr[] c = model.getConstrs();
		for(int i = 0;i < c.length; i++)
		{
			System.out.println("Slack of \"" + c[i].get(GRB.StringAttr.ConstrName) + "\":" + c[i].get(GRB.DoubleAttr.Slack));
		}
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
	 * Returns the optimum
	 * @return
	 */
	public double getOpt()
	{
		return opt;
	}
	
	/**
	 * Finds out, which variables are in use
	 * @param dv
	 * @return
	 */
	private boolean[] inUseVariables()
	{
		boolean[] inUse = new boolean[mc.getCostCoeff().length];
		double[][] dm = mc.getDmatrix();
		
		for(int i = 0; i < inUse.length; i++)
		{
			boolean allZero = true;
			
			for(int j = 0; j < dm.length; j++)
			{
				if(dm[j][i] != 0)
				{
					allZero = false;
					break;
				}
			}
			
			inUse[i] = !allZero;
		}
		
		return inUse;
	}

	private double[] createDVVals() throws GRBException
	{
		double[] res = new double[inUse.length];
		
		int counter = 0;
		
		for(int i = 0; i < res.length; i++)
		{
			if(inUse[i])
			{
				res[i] = assigned[counter++].get(GRB.DoubleAttr.X);
			}
		}
		return res;
	}
	
	private double calcMaxDelta() throws GRBException
	{
		GRBConstr[] c = model.getConstrs();
		double res = Double.POSITIVE_INFINITY;
		for(int i = 0; i < c.length;i++)
		{
			if(mc.getConstrTypes()[i] != constrType.demand)
				continue;
			double current = c[i].get(GRB.DoubleAttr.Slack);
			if(res > current)
				res = current;
		}

		return res;
	}
	
	private void assignSlack() throws GRBException
	{
		GRBConstr[] c = model.getConstrs();
		this.slack = new double[c.length];
		
		for(int i = 0; i < c.length;i++)
		{
			this.slack[i] = c[i].get(GRB.DoubleAttr.Slack);
		}
	}
	
	public double[] getSlack()
	{
		return slack;
	}
	
	/**
	 * Returns the highest allowed value for delta according to the slacks in the demand constraints
	 * @return
	 */
	public double getMaxDelta()
	{
		return maxDelta;
	}

	/**
	 * Returns the main decision variables.
	 * @return
	 */
	public double[] getDVAssigned()
	{
		return assignedVals;
	}
}
