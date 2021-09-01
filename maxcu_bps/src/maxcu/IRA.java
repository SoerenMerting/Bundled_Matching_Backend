package maxcu;

import java.util.Arrays;

import gurobi.*;
import maxcu.ModelMatrix.constrType;
import report.Report;

/**
 * The iterative rounding class
 * @author Kevin
 *
 */
public class IRA {
	private final double[][] matrix; //matrix[row/constr][col/decision variables]
	private final double[] constr;
	private final double[] costCoeff;
	private String[] constrNames;
	private String[] varNames;
	private final ModelMatrix.constrType constraintTypes[];
	private boolean[] constrUsed;
	private boolean[] varUsed;
	private double[] result;
	private double[] fractional;
	private final int bundleLimit;
	private int numIterations = 0;

	private GRBEnv env;
	private GRBModel mod;
	private GRBVar[] dv;

	private double[] additLine;
	private double additConstr;
	private String additLineDescr;

	private final Report r;

	/**
	 * Initializes IRA manually with all the parameters and custom cost coefficients
	 * @param matrix
	 * @param constr
	 * @param costCoeff
	 * @param constraintTypes
	 * @param bundleLimit
	 */
	public IRA(double[][] matrix, double[] constr, double[] costCoeff, ModelMatrix.constrType[] constraintTypes, int bundleLimit, Report r)
	{
		this.r = r;
		if(matrix.length != constr.length)
			throw new IllegalArgumentException("Size of matrix and amount of constraints do not match");
		if(matrix[0].length != costCoeff.length)
			throw new IllegalArgumentException("Size of matrix and amount of decision variables do not match");
		if(constraintTypes.length != constr.length)
			throw new IllegalArgumentException("Amount of constraint types and amount of constraints do not match");

		this.matrix = matrix; // copyMatrix(matrix);
		this.constr = Arrays.copyOf(constr, constr.length); //copying is necessary, as changes might be possible!
		this.costCoeff = costCoeff; //Arrays.copyOf(costCoeff, costCoeff.length);
		this.constrNames = createEmptyStrings(constr.length);
		this.varNames = createEmptyStrings(costCoeff.length);
		this.bundleLimit = bundleLimit;
		this.constraintTypes = constraintTypes;

		constrUsed = new boolean[matrix.length];
		varUsed = new boolean[matrix[0].length];
		result = new double[matrix[0].length];

		for(int i = 0; i < constrUsed.length; i++)
		{
			constrUsed[i] = true;
		}

		disableZeroCols();
	}

	/**
	 * Initializes IRA manually with all the parameters and custom cost coefficients
	 * @param matrix
	 * @param constr
	 * @param costCoeff
	 * @param constraintTypes
	 * @param bundleLimit
	 */
	public IRA(double[][] matrix, double[] constr, double[] costCoeff, ModelMatrix.constrType[] constraintTypes, int bundleLimit)
	{
		this(matrix, constr, costCoeff, constraintTypes, bundleLimit, new Report(false));
	}

	/**
	 * Initializes IRA. The fractional solution will be calculated beforehand
	 * @param mm
	 */
	public IRA(ModelMatrix mm, Report r)
	{
		this(mm.getDmatrix(), mm.getdConstraint(), mm.getCostCoeff(), mm.getConstrTypes(), mm.getBundleLimit(), r);
		System.out.println("bundle limit"+mm.getBundleLimit());
		this.constrNames = mm.getConstraintNames();
		this.varNames = mm.getVarDescr();
	}

	/**
	 * Initializes IRA. The fractional solution will be calculated beforehand
	 * @param mm
	 */
	public IRA(ModelMatrix mm)
	{
		this(mm, new Report(false));
	}

	/**
	 * Initializes IRA with the ModelMatrix and a fractional solution to it.
	 * The fractional solution therefore will not be calculated
	 * @param mm
	 * @param fractionalRes
	 */
	public IRA(ModelMatrix mm, double[] fractionalRes, Report r)
	{
		this(mm, r);
		System.out.println(fractionalRes.length+"fractRes/CostCoeff"+mm.getCostCoeff().length);
		if(fractionalRes.length != mm.getVarDescr().length)//mm.getDecisionVariables().size())
			throw new IllegalArgumentException("The amount of decision variables does not match the length of the fractionalRes array");

		this.fractional = Arrays.copyOf(fractionalRes, fractionalRes.length);
	}

	/**
	 * Initializes IRA with the ModelMatrix and a fractional solution to it.
	 * The fractional solution therefore will not be calculated
	 * @param mm
	 * @param fractionalRes
	 */
	public IRA(ModelMatrix mm, double[] fractionalRes)
	{
		this(mm, fractionalRes, new Report(false));
	}

	public Report getReport()
	{
		return r;
	}

	/**
	 * Finds, which columns in the constraint matrix consist solely of zeros and disables them in the varUsed array
	 */
	private void disableZeroCols()
	{
		for(int i = 0; i < varUsed.length; i++)
		{
			varUsed[i] = false;
			for(int j = 0; j < constrUsed.length; j++)
			{
				if(matrix[j][i] != 0)
					varUsed[i] = true;
			}
		}
	}

	/**
	 * Creates an array of empty strings with specified size
	 * Used to initialize the names of columns or rows
	 * @param size
	 * @return
	 */
	private static String[] createEmptyStrings(int size)
	{
		String empty = "";
		String[] res = new String[size];
		for(int i = 0; i < res.length; i++)
		{
			res[i] = empty;
		}
		return res;
	}

	/**
	 * Set constraint names here
	 * @param names
	 */
	public void setConstraintNames(String[] names)
	{
		if(names.length != matrix.length)
			throw new IllegalArgumentException("Size of names vector and amount of constraints do not match");

		this.constrNames = names;
	}

	/**
	 * Get the constraints' names
	 * @return
	 */
	public String[] getConstraintNames()
	{
		return constrNames;
	}

	/**
	 * Sets the variable names
	 * @param names
	 */
	public void setVariableNames(String[] names)
	{
		if(names.length != matrix[0].length)
			throw new IllegalArgumentException("Size of names vector and amount of constraints do not match");

		this.varNames = names;
	}

	/**
	 * Gets the variable names
	 * @return
	 */
	public String[] getVariableNames()
	{
		return varNames;
	}

	/**
	 * Builds the model according to the varUsed and constrUsed arrays
	 * @param envy Disables the envy constraints and also the additional constraint
	 * @throws GRBException
	 */
	private void buildModel(boolean envy) throws GRBException
	{
		char varType = GRB.CONTINUOUS;
		env = new GRBEnv();
		mod = new GRBModel(env);
		mod.set(GRB.StringAttr.ModelName, "Optimum");

		dv = new GRBVar[sumBool(varUsed)];
		int counter = 0;
		//System.out.print("Objective function: ");
		r.addParagraph("\nStep 2: LP to solve:\n");
		r.openTable();
		r.addTableEntry("Max:");
		for(int i = 0; i < varUsed.length; i++)
		{
			if(varUsed[i])
			{
				//r.addTableEntry("");
				//System.out.print(costCoeff[i] + "*" +  varNames[i] + "+");
				r.addTableEntry(varNames[i] + " * " +costCoeff[i]);
				dv[counter] = mod.addVar(0, GRB.INFINITY, costCoeff[i], varType, varNames[i]);
				counter++;
			}
		}
		//System.out.println(" - Maximize");

		r.addTableSeparator();
		mod.set(GRB.IntAttr.ModelSense, GRB.MAXIMIZE);

		mod.update();

		//constraints
		for(int j = 0; j < matrix.length; j++)
		{
			//we can switch off envy constraints in IRA iterations
			if(constrUsed[j] && (envy || constraintTypes[j] != constrType.envy))
			{
				GRBLinExpr ltot = new GRBLinExpr();
				int count = 0;
				//System.out.print("Rule: ");
				r.addTableEntry("");
				for(int i = 0; i < matrix[0].length; i++)
				{
					if(varUsed[i])
					{
						//System.out.print(matrix[j][i] + "*" + varNames[i] + "+");
						r.addTableEntry(matrix[j][i]);
						ltot.addTerm(matrix[j][i], dv[count]);
				        count++;
					}
				}
				//System.out.println(" <= " + constr[j] + " - " + constrNames[j]);
				r.addTableEntry("<=");
				r.addTableEntry(constr[j]);
				r.addTableEntry(constrNames[j]);
				r.addTableNL();

		        mod.addConstr(ltot, GRB.LESS_EQUAL, constr[j], constrNames[j]);
			}
		}

		if(additLine != null && !envy)
		{
			GRBLinExpr ltot = new GRBLinExpr();
			int count = 0;
			//System.out.print("Rule: ");
			r.addTableEntry("");
			for(int i = 0; i < matrix[0].length; i++)
			{
				if(varUsed[i])
				{
					//System.out.print(additLine[i] + "*" + varNames[i] + "+");
					r.addTableEntry(additLine[i]);
					ltot.addTerm(additLine[i], dv[count]);
			        count++;
				}
			}
			//System.out.println(" <= " + additConstr + " - " + additLineDescr);
			r.addTableEntry("<=");
			r.addTableEntry(additConstr);
			r.addTableEntry(additLineDescr);
			r.addTableNL();


	        mod.addConstr(ltot, GRB.LESS_EQUAL, additConstr, additLineDescr);
		}

		r.closeTable();

		mod.update();
	}

	/**
	 * Counts the amount of "true" values in a boolean array
	 * @param arr
	 * @return
	 */
	private static int sumBool(boolean[] arr)
	{
		int count = 0;
		for(boolean b : arr)
		{
			if(b)
				count++;
		}
		return count;
	}

	/**
	 * Calculates the result. Initial fractional solution will also be calculated if not passed in the constructor
	 * @throws GRBException
	 */
	public void calculate() throws GRBException
	{
		r.addParagraph("IRA\n");

		if(fractional == null)
		{
			buildModel(true);
			mod.optimize();
			fractional = createDVVals(mod.getVars()); //the "result" variable will be updated using IRA
			dispose();
		}

		this.result = Arrays.copyOf(fractional, fractional.length);

		r.addParagraph("Initialize with fractional solution: ");
		r.openTable();
		r.addTableEntries(varNames, 0, 0);
		r.addTableNL();
		r.addTableEntries(fractional, 0, 0);
		r.closeTable();

		ira();
	}

	/**
	 * The IRA iterations
	 * @throws GRBException
	 */
	private void ira() throws GRBException
	{
		int iraIterations = 0;
		while(true)
		{
			numIterations++;
			System.out.println("number of iterations:"+numIterations);
			r.addParagraph("Start iteration #" + numIterations + "\n");

			boolean lpchanged = false;
			boolean integerVals = false; //have we found integer variables?
			int numVarsFixed = 0;
			int numConstrRelaxed = 0;
			for(int i = 0; i < varUsed.length; i++)
			{
				if(varUsed[i] && (result[i] == 0 || result[i] == 1))
				{
					numVarsFixed++;
					varUsed[i] = false;
					integerVals = true;
					updateConstraints(i);
					lpchanged = true;
				}
			}

			//if integer variables were found, no need to relax constraints in this iteration
			if(!integerVals)
			{
				numConstrRelaxed = relaxSupplyConstr();
				r.addParagraph("Step 1b: " + numConstrRelaxed + " supply constraints eliminated");
				lpchanged = numConstrRelaxed > 0;
			}
			else{
				r.addParagraph("Step 1a: Fixed " + numVarsFixed + " integer variables");
			}
			//if every variable is an integer value, stop algorithm
			if(sumBool(varUsed) == 0)
			{
				break;
			}

			//this is a backup in case something within the implementation went wrong...
			if(!lpchanged&&iraIterations>0)
			{

				//this handles the new special case!
				if(sumBool(varUsed) == 2 && additLine != null)
				{
					r.addParagraph("special case with 2 variables. Fix remaining two vars");
					roundRemainingTwoVars();
				}
				else if(sumBool(varUsed) == 1 && additLine != null)
				{
					r.addParagraph("special case with 1 variable. Fix remaining var");
					for(int i = 0; i < varUsed.length; i++)
					{
						if(varUsed[i])
						{
							result[i] = 0;
							varUsed[i] = false;
							lpchanged = true;
							break;
						}
					}
				}
				else //just in case there is another problem we did not take into account yet
				{
					r.addParagraph("Loop detected");
					System.out.println("Loop detected, program stops");
				}
				break;
			}
			iraIterations++;

			buildModel(false);
			mod.getEnv().set(GRB.IntParam.OutputFlag, 0);
			mod.optimize();
			result = createDVVals(mod.getVars());
			dispose();

			reportResult();
		}
		dispose();
		r.addParagraph("\nAll vars integer! Exit after " + numIterations + " iterations");
		r.closeChapter();
	}

	private void reportResult()
	{
		r.addParagraph("Result:");
		r.openTable();
		for(int i = 0; i < varUsed.length; i++)
		{
			if(varUsed[i])
			{
				r.addTableEntry(varNames[i]);
				r.addTableEntry(result[i]);
				r.addTableNL();
			}
		}
		r.closeTable(" ");

		r.addParagraph("Optimum: " + Report.round(this.getOpt()) + "\n\n");
	}

	/**
	 * This method is written for the special case, when there are two fractional variables left
	 * and the additional constraint prevents them from being integer
	 */
	private void roundRemainingTwoVars()
	{
		//indices of the two remaining variables
		int iVarA = -1;
		int iVarB = -1;


		double[][] m = new double[2][2];
		double[] d = new double[2];

		for(int i = 0; i < varUsed.length; i++)
		{
			if(varUsed[i] && iVarA == -1)
				iVarA = i;
			else if(varUsed[i])
			{
				iVarB = i;
				break;
			}

		}
		//System.out.println("Fix variables no. " + iVarA + " and " + iVarB);
		for(int i = 0; i < constr.length; i++)
		{
			//alternative: matrix[i][iVarA] + matrix[i][iVarB] = constr[i]
			//but pay attention, that doubles are not always precise
			if(constraintTypes[i] == constrType.demand && matrix[i][iVarA] != 0 && matrix[i][iVarB] != 0)
			{
				m[0][0] = matrix[i][iVarA];
				m[0][1] = matrix[i][iVarB];
				d[0] = constr[i];
				break;
			}
		}


		m[1][0] = additLine[iVarA];
		m[1][1] = additLine[iVarB];
		d[1] = additConstr;

		//System.out.println("Table:");
		//System.out.println(m[0][0] + "\t" + m[0][1] + "\t<= " + d[0] + "\n");
		//System.out.println(m[1][0] + "\t" + m[1][1] + "\t<= " + d[1] + "\n");

		//just try possible solutions, this is probably easier than calculating them!
		boolean fixA = false;
		boolean fixB = false;

		if(m[0][0] <= d[0] && m[1][0] <= d[1])
		{
			fixA = true;
		}
		if(m[0][1] <= d[0] && m[1][1] <= d[1])
		{
			fixB = true;
		}

		if(fixA && fixB) //we can choose the one which attributes more to the result
		{
			result[iVarA] = costCoeff[iVarA] > costCoeff[iVarB] ? 1 : 0;
			result[iVarB] = result[iVarA] == 0 ? 1 : 0;
		}
		else if(fixA || fixB) //we have no choice, only one can be 1
		{
			result[iVarA] = fixA ? 1 : 0;
			result[iVarB] = fixB ? 1 : 0;
		}
		else //both must be 0
		{
			System.out.println("None can be fixed");
			result[iVarA] = 0;
			result[iVarB] = 0;
		}

		varUsed[iVarA] = false;
		varUsed[iVarB] = false;
	}

	/**
	 * Updates the constraint vector. Used, when IRA fixes an integer variable
	 * @param indexVar
	 */
	private void updateConstraints(int indexVar)
	{
		for(int i = 0; i < matrix.length; i++)
		{
			constr[i] -= matrix[i][indexVar] * result[indexVar];
		}
		if(additLine != null)
		{
			additConstr -= additLine[indexVar] * result[indexVar];
		}
	}

	/**
	 * Relaxes the supply constraints
	 * @return True if a constraint was relaxed
	 */
	private int relaxSupplyConstr()
	{
		int relaxed = 0;
		for(int i = 0; i < constraintTypes.length; i++)
		{
			if(constraintTypes[i] == constrType.supply && constrUsed[i])
			{
				double val = 0;
				for(int j = 0; j < varUsed.length; j++)
				{
					if(varUsed[j])
						val += matrix[i][j];
				}
				if(val <= constr[i] + bundleLimit - 1)
				{
					constrUsed[i] = false;
					relaxed++;
				}
			}
		}
		return relaxed;
	}

	/**
	 * Calculates the value of all decision variables, no matter if they are disabled or not
	 * @param vars
	 * @return
	 * @throws GRBException
	 */
	private double[] createDVVals(GRBVar[] vars)
	{
		double[] res = result;

		int counter = 0;

		for(int i = 0; i < res.length; i++)
		{
			if(varUsed[i]){
				try {
					res[i] = vars[counter++].get(GRB.DoubleAttr.X);
				} catch (GRBException e) {
					// TODO Auto-generated catch block
					System.out.println("ErrorCode:"+e.getErrorCode()+"ErrorMessage:"+e.getMessage()+"ErrorCause:"+e.getCause());
					e.printStackTrace();
				}
			}
		}
		return res;
	}

	/**
	 * Disposes the internal Gurobi variables
	 */
	public void dispose()
	{
		try {
			if(mod != null)
				mod.dispose();
			if(env != null)
				env.dispose();
		} catch (GRBException e) {
			// Unlucky...
			e.printStackTrace();
		}
	}

	/**
	 * Returns the amount of IRA iterations
	 * @return
	 */
	public int getNumIterations()
	{
		return numIterations;
	}

	/**
	 * Returns the total value of the cost function
	 * @return
	 */
	public double getOpt()
	{
		//The solution of the last gurobi optium is not correct
		//Not all decision variables are still in the last gurobi LP. Therefore, the true value here is sometimes bigger

		double val = 0;
		for(int i = 0; i < result.length; i++)
		{
			val += result[i] * costCoeff[i];
		}

		return val;
	}

	/**
	 * Adds an additional constraint to the model. Typically used for the line constraint in step 2 of MAXCU
	 * @param matr
	 * @param constr
	 * @param descr
	 */
	public void addConstraint(double[] matr, double constr, String descr)
	{
		if(matr.length != matrix[0].length)
			throw new IllegalArgumentException("The length of the matr array must be the same as the number of columns in the IRA class");

		this.additLine = matr;
		this.additLineDescr = descr;
		this.additConstr = constr;
	}

	/**
	 * Returns the values of the decision variables
	 * @return
	 */
	public double[] getIntegerResults()
	{
		return result;
	}

	/**
	 * Returns the values of the decision variables of the fractional solution
	 * @return
	 */
	public double[] getFractionalResult()
	{
		return fractional;
	}
}
