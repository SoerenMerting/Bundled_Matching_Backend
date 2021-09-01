package maxcu;

import report.Report;

/**
 * ModelMatrix represents the mathematical linear program
 * @author Kevin
 *
 */
public class ModelMatrix {
	/**
	 * States, whether the class should create only the necessary rows and columns from the LP
	 * If true all the unnecessary columns (decision variables) and rows (unnecessary envy constraints) are omitted
	 */
	public static boolean createReduced = true;
	
	/**
	 * The enumeration 
	 * @author Kevin
	 *
	 */
	public enum constrType{demand, supply, envy}
	
	final PreferenceTable data;
	
	private final Report r;
	
	// old data
	final TimeSlot[] timeslots;
	final double[][] prefTable; //table[bundles][clients]
	final Bundle[] bundles;
	final String[] clients;
	
	// new model data
	private double[][] Dmatrix;							//Dmatrix[Row][Col] / Dmatrix[decision variable][constraints]
	private double[] dconstraint;						//vector d of upper constraints
	private double[] costCoeff;							//the cost coefficient for each decision variable
	private String[] lineDescriptions;					//a description of each line
	private String[] varDescriptions;					//description of every variable (columns)
	private constrType[] constraintTypes;
	private int dimensionality;
	private int maxLength;
	
	private Bundle[] associatedBundles = null;
	private String[] associatedClients = null;
	
	public ModelMatrix(PreferenceTable data, Report r)
	{
		this.data = data;
		this.timeslots = data.getTimeSlotsArray();
		this.prefTable = data.preferenceTable();
		this.bundles = data.getBundles();
		this.clients = data.getClientsArray();
		
		System.out.println("InitVariables:");
		initializeVariables();

		System.out.println("InitDemand:");
		initDemandConstraint();
		System.out.println("InitSupp:");
		initSupplyConstraint();
		System.out.println("InitEnvy:");
		System.out.println("InitCostCoeff:");
		initCostCoeff();
		System.out.println("DoneModelMatrix:");
		
		
		//this.dimensionality = calcDimensionality();
		
		this.r = r;
		reportPrefTable();
		reportMatrix();
	}
	
	public ModelMatrix(PreferenceTable data)
	{
		this(data, new Report(false));
	}
	
	public ModelMatrix(double[][] DMatrix, double[] dconstraint, double[] costCoeff, constrType[] constraintTypes, Report r)
	{
		this.data = null;
		this.timeslots = null;
		this.prefTable = null;
		this.bundles = null;
		this.clients = null;
		
		if(DMatrix.length != dconstraint.length || dconstraint.length != constraintTypes.length)
			throw new IllegalArgumentException("The size of rows for the matrix, constraints or constraint types do not match");
		if(DMatrix[0].length != costCoeff.length)
			throw new IllegalArgumentException("The size of columns for the matrix and cost coefficents do not match");
		
		this.Dmatrix = DMatrix;
		this.dconstraint = dconstraint;
		this.constraintTypes = constraintTypes;
		this.costCoeff = costCoeff;
		this.lineDescriptions = createEmptyStrings(dconstraint.length);
		this.varDescriptions = createEmptyStrings(costCoeff.length);
		
		this.dimensionality = calcDimensionality();
		
		this.r = r;
		reportMatrix();
	}
	
	public ModelMatrix(double[][] DMatrix, double[] dconstraint, double[] costCoeff, constrType[] constraintTypes,int maxLength)
	{
		this(DMatrix, dconstraint, costCoeff, constraintTypes, new Report(false));
		this.maxLength = maxLength;
	}
	
	private void reportPrefTable()
	{
		if(!r.isOn())
			return;
		
		r.addParagraph("MODELMATRIX - Input (preferences):\n");
		r.openTable();
		
		r.addTableEntry("Bundles ->");
		r.addTableEntries(bundles, 0, 0);
		r.addTableSeparator();
		
		for(int i = 0; i < clients.length;i++)
		{
			r.addTableEntry("Client: " + clients[i]);
			for(int j = 0; j < bundles.length; j++)
			{
				r.addTableEntry(prefTable[j][i]);
			}
			r.addTableNL();
		}
		
		r.closeTable();
		
		r.openTable();
		r.addTableEntry("Goods");
		r.addTableEntry("Supply");
		r.addTableSeparator();
		
		for(int i = 0; i < this.timeslots.length;i++)
		{
			r.addTableEntry(timeslots[i].toString());
			r.addTableEntry(Integer.toString(timeslots[i].getCapacity()));
			r.addTableNL();
		}
		
		r.closeTable("  ");
	}
	
	private void reportMatrix()
	{
		if(!r.isOn())
			return;
		
		r.addParagraph("\nMODELMATRIX - Output (linear program):\n");
		r.openTable();
		
		r.addTableEntries(varDescriptions, 1, 0);
		r.addTableNL();
		r.addTableEntry("MAX:");
		r.addTableEntries(costCoeff, 0, 0);
		r.addTableSeparator();
		for(int i = 0; i < dconstraint.length; i++)
		{
			r.addTableEntry("");
			for(int j = 0; j < varDescriptions.length;j++)
			{
				r.addTableEntry(Dmatrix[i][j]);
			}
			r.addTableEntry("<=");
			r.addTableEntry(dconstraint[i]);
			r.addTableEntry(lineDescriptions[i]);
			r.addTableNL();
		}
		
		r.closeTable();
		
		r.addParagraph("Dimensions: " + dimensionality);
		
		r.closeChapter();
	}
		
	/**
	 * Returns the Report
	 * @return
	 */
	public Report getReport()
	{
		return r;
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
	 * Creates a nice table of the preferences
	 * @return
	 */
	public String preferenceTable()
	{
		return data.toStringTable();
	}
	
	/**
	 * Creates a nice table for the mathematical matrix of the constraints
	 * @return
	 */
	public String constraintTable()
	{
		String[] dv = new String[varDescriptions.length + 1];
		dv[0] = "";
		for(int i = 0; i < dv.length -1; i++)
		{
			dv[i +1] = varDescriptions[i];
		}
		
		String[][] Dmatrixstring = new String[Dmatrix.length][Dmatrix[0].length];
		for(int i = 0; i < Dmatrix.length; i++)
		{
			for(int j = 0; j < Dmatrix[0].length; j++)
			{
				Dmatrixstring[i][j] = Double.toString(Dmatrix[i][j]);
			}
		}
		
		return TableCreator.createTable(dv, lineDescriptions, Dmatrixstring);
	}
	
	/**
	 * Returns the mathematical constraint matrix. (typically referred to as D)
	 * Dmatrix[Row][Col] / Dmatrix[decision variable][constraints]
	 * @return
	 */
	public double[][] getDmatrix()
	{
		return Dmatrix;
	}
	
	/**
	 * Returns the constraint vector. (typically referred to as d)
	 * @return
	 */
	public double[] getdConstraint()
	{
		return dconstraint;
	}
	
	/**
	 * Sets the names of the constraints.
	 * This can be used later, so that the user can better read the output of the program.
	 * @param names
	 */
	public void setConstraintNames(String[] names)
	{
		if(names.length != dconstraint.length)
			throw new IllegalArgumentException("The size of the name array is not correct");
		
		this.lineDescriptions = names;
	}
	
	/**
	 * Sets the names of the variables.
	 * This can be used later, so that the user can better read the output of the program.
	 * @param names
	 */
	public void setVarNames(String[] names)
	{
		if(names.length != costCoeff.length)
			throw new IllegalArgumentException("The size of the name array is not correct");
		
		this.varDescriptions = names;
	}
	
	/**
	 * Returns the name of the constraints (the lines in Dmatrix)
	 * @return
	 */
	public String[] getConstraintNames()
	{
		return lineDescriptions;
	}
	
	/**
	 * Returns the type of each line of constraints
	 * @return
	 */
	public constrType[] getConstrTypes()
	{
		return constraintTypes;
	}
	
	/**
	 * Returns the coefficients of the cost function
	 * @return
	 */
	public double[] getCostCoeff()
	{
		return costCoeff;
	}
	
	/**
	 * Returns the maxium bundle size
	 * @return
	 */
	public int getBundleLimit()
	{//bundle size falsch
//		return data.getMaxBundleSize();
		if(this.data == null){
		return this.maxLength;
		}else{
			return data.getMaxBundleSize();
		}
	}
	
	/**
	 * Returns each variable's name
	 * @return
	 */
	public String[] getVarDescr()
	{
		return varDescriptions;
	}
	
	/**
	 * Counts, how many decision variables are actually used (some columns in the LP might be completely empty)
	 * In contrast to calcDimensionality, this method only works if the class is initialized using a PreferenceTable
	 * @return
	 */
	private int countDV()
	{
		int res = 0;
		
		for(int i = 0; i < prefTable.length; i++)
		{
			for(int j = 0; j < prefTable[0].length; j++)
			{
				if(prefTable[i][j] != 0)
					res++;
			}
		}
		
		return res;
	}
	
	/**
	 * Counts, how many decision variables are actually used (some columns in the LP might be completely empty)
	 * @return
	 */
	private int calcDimensionality()
	{
		int counter = 0;
		for(int i = 0; i < costCoeff.length; i++)
		{
			for(int j = 0; j < dconstraint.length; j++)
			{
				if(Dmatrix[j][i] != 0)
				{
					counter++;
					break;
				}
			}
		}
		return counter;
	}
	
	/**
	 * Counts, how many decision variables are actually used (some columns in the LP might be completely empty)
	 * In contrast to countDV, this method only works after creating the LP
	 * @return
	 */
	public int getDimensionality()
	{
		return dimensionality;
	}
	
	/**
	 * Returns the associated bundles. (only if initializes with a PreferenceTalbe)
	 * Explanation: every decision variable is based on a client bidding for a bundle.
	 * By retrieving this data, one can find out, what bundle this decision variable refers to
	 * @return
	 */
	public Bundle[] getAssociatedBundles()
	{
		return associatedBundles;
	}
	
	/**
	 * Returns the associated clients. (only if initializes with a PreferenceTalbe)
	 * Explanation: every decision variable is based on a client bidding for a bundle.
	 * By retrieving this data, one can find out, what client this decision variable refers to
	 * @return
	 */
	public String[] getAssociatedClients()
	{
		return associatedClients;
	}
	
	/**
	 * Initializes the private variables according to the required size
	 */
	private void initializeVariables()
	{
		boolean old = createReduced;
		int dims = countDV();
		createReduced = false;
		int origRowSize = clients.length + timeslots.length + countEnvyConstraints();
		createReduced = true;
		int redRowSize = clients.length + timeslots.length + countEnvyConstraints();
		int origColSize = clients.length * bundles.length;
		int redColSize = dims;
		System.out.println("Orig (R,C): (" + origRowSize + "," + origColSize + ")"
				+ ", reduced (R,C): (" + redRowSize + "," + redColSize + ")");
		createReduced = old;
		
		this.dimensionality = countDV();
		int nDec = createReduced ? dimensionality : clients.length * bundles.length; // number of decision variables and also number of columns
		
		int nRow = //number of constraint functions = number of rows
				clients.length											//constraints (1) - limit per client
				+ timeslots.length;									//constraints (2) - capacity
				//+ countEnvyConstraints();								//constraints (3) - Envy
				//+ (clients.length * clients.length) - clients.length;	//constraints (3) - Envy
		
		System.out.println("nRow; " + nRow + "; nDec: " + nDec);
		Dmatrix = new double[nRow][nDec];
		lineDescriptions = new String[nRow];
		dconstraint = new double[nRow];
		constraintTypes = new constrType[nRow];
		varDescriptions = new String[nDec];
		
		associatedBundles = new Bundle[nDec];
		associatedClients = new String[nDec];
	}
	private void initDemandConstraint()
	{
		for(int i = 0; i < clients.length; i++)
		{
			lineDescriptions[i] = "Demand constr " + clients[i];
			dconstraint[i] = 1;
			constraintTypes[i] = constrType.demand;
			for(int j = 0; j < bundles.length; j++)
			{
				if(prefTable[j][i] > 0)
				{
					Dmatrix[i][convertColIndex(i * bundles.length + j)] = 1;
				}
				if(!createReduced || (createReduced && prefTable[j][i] > 0))
				{
					varDescriptions[convertColIndex(i * bundles.length + j)] = clients[i] + bundles[j].toString();
					associatedBundles[convertColIndex(i * bundles.length + j)] = bundles[j];
					associatedClients[convertColIndex(i * bundles.length + j)] = clients[i];
				}
			}
		}
	}
	private void initSupplyConstraint()
	{
		int count = 0;
		for(int i = 0; i < timeslots.length; i++)
		{
			lineDescriptions[i + clients.length] = "Supply constr. " + timeslots[i];
			dconstraint[i + clients.length] = timeslots[i].getCapacity();
			constraintTypes[i + clients.length] = constrType.supply;
			for(int j = 0; j < bundles.length; j++)
			{
				//count is bei uns 1 
				count = bundles[j].count(timeslots[i]);
				if(count == 0)
					continue;
				for(int k = 0; k < clients.length; k++)
				{
					if(prefTable[j][k] > 0)
					{
						// clients.length + i: because clients.length rows are already occupied by the clients constraints
						// k * bundles.length: each client has bundles.length times decision variables. J is the current bundle
						Dmatrix[clients.length + i][convertColIndex(k * bundles.length + j)] = count;
					}
				}
			}
		}
	}
	
	private void initEnvyConstraint()
	{
		int lineOffset = clients.length + timeslots.length;
		int envyIndex = 0;
		double util = 0;
		for(int i = 0; i < clients.length; i++)
		{
			for(int j = 0; j < clients.length; j++)
			{
				if(envyExists(i, j))
				{
					lineDescriptions[lineOffset + envyIndex] = "Envy constr " + clients[i] + " and " + clients[j];
					dconstraint[lineOffset + envyIndex] = 0;
					constraintTypes[lineOffset + envyIndex] = constrType.envy;
					
					for(int k = 0; k < bundles.length; k++)
					{
						util = prefTable[k][i];

						//System.out.println("k = " + k + "; i = " + i + "; j = " + j + "; util = " + util);

						if(prefTable[k][i] > 0) //check if variable exists
							Dmatrix[lineOffset + envyIndex][convertColIndex(k + i * bundles.length)] -= util;
						if(prefTable[k][j] > 0) //check if variable exists
							Dmatrix[lineOffset + envyIndex][convertColIndex(k + j * bundles.length)] += util;
					}
					envyIndex++;
				}
			}
		}
	}
	private void initCostCoeff()
	{
		costCoeff = new double[Dmatrix[0].length];
		double[] weights = data.getWeights();
		for(int i = 0; i < clients.length; i++)
		{
			for(int j = 0; j < bundles.length; j++)
			{
				double util = prefTable[j][i];
				if(util != 0)
				{
					costCoeff[convertColIndex(i * bundles.length + j)] = util * weights[i];
				}
			}
		}
	}

	/**
	 * This method converts the column index to the correct value.
	 * If createReduced is deactivated, than the output is the same as the input
	 * If createReduced is activated, than this method converts the input index to the index of the reduced table!
	 * @param col
	 * @return
	 */
	private int convertColIndex(int col)
	{
		if(createReduced)
		{
			int res = 0;
			for(int i = 0; i < col; i++)
			{
				int clientIndex = i / prefTable.length;
				int bundleIndex = i % prefTable.length;
				if(prefTable[bundleIndex][clientIndex] != 0)
				{
					res++;
				}
			}
			return res;
		}
		else
		{
			return col;
		}
	}

	/**
	 * Counts how many envy constraints are required, according to the flag createReduced
	 * Many of the envy constraints can be removed.
	 * If set of bids of two different clients is completely disjunct, then those two clients do not need an envy constraint
	 * This method counts, how many envy constraints are required!
	 * @return
	 */
	private int countEnvyConstraints()
	{
		if(!createReduced)
			return (clients.length * clients.length) - clients.length;
		
		int counter = 0;
		for(int i = 0; i < clients.length; i++)
		{
			for(int j = 0; j < clients.length; j++)
			{
				if(i != j)
				{
					for(int b = 0; b < bundles.length; b++)
					{
						//test for every possible pair of agents whether they have a common bundle they both bid for
						if(prefTable[b][i] != 0 && prefTable[b][j] != 0)
						{
							counter++;
							break;
						}
							
					}
				}
			}
		}
		return counter;
	}
	
	/**
	 * Returns true, if an envy constraint for the two clients is necessary.
	 * The method takes the flag "createReduced" into account.
	 * If set of bids of two different clients is completely disjunct, then those two clients do not need an envy constraint
	 * This method counts, how many envy constraints are required!
	 * @param ifirst
	 * @param isecond
	 * @return
	 */
	private boolean envyExists(int ifirst, int isecond)
	{
		if(ifirst == isecond)
			return false;
		
		if(!createReduced)
			return true;
		
		for(int i = 0; i < bundles.length; i++)
		{
			if(prefTable[i][ifirst] != 0 && prefTable[i][isecond] != 0)
				return true;
		}
		
		return false;
	}
}
