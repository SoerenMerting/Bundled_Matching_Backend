package Eval;

import gurobi.GRBException;
import maxcu.MAXCU;
import maxcu.ModelMatrix;
import maxcu.ModelMatrix.constrType;
import maxcu.OptimumCalculator;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestRun
{
	private static String[] createCSVLines(String[][] elements)
	{
		String[] result = new String[elements.length];
		for(int i = 0; i < result.length; i++)
		{
			StringBuilder sb = new StringBuilder();
			for(int j = 0; j < elements[0].length; j++)
			{
				sb.append(elements[i][j]);
				sb.append(';');
			}
			result[i] = sb.toString();
		}
			
		return result;
	}
	
	public static void saveCSV(String filepath, String[][] elements) throws IOException
	{
		String[] lines = createCSVLines(elements);
		
		FileWriter fw = null;
		
		BufferedWriter bw = null;
		try
		{
			fw = new FileWriter(filepath);
		    bw = new BufferedWriter(fw);
		    

		    for(int i = 0; i < lines.length; i++)
		    {
		    	bw.write(0 + ";" + lines[i] + "\n");
		    }
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			try
			{
				if(bw != null)
					bw.close();
			    if(fw != null)
			    	fw.close();
			}
			catch(IOException e)
			{
				throw e;
			}
		}
	}
	
	public static void append(String filepath, String[][] elements, boolean increase) throws IOException
	{
		writeNewCSV(filepath, filepath + "temp", elements, increase);
		
		java.io.File f = new File(filepath + "temp");
		if(f.exists())
		{
			java.io.File old = new File(filepath);
			old.delete();
			
			f.renameTo(new File(filepath));
		}
	}
	
	public static void writeNewCSV(String filepath, String newFilePath, String[][] elements, boolean increase) throws IOException
	{
		String[] lines = createCSVLines(elements);
		
		FileWriter fw = null;
		FileReader fr = null;
		
		BufferedWriter bw = null;
		BufferedReader br = null;
		
		int index = 0;
		try
		{
			fw = new FileWriter(newFilePath);
		    bw = new BufferedWriter(fw);
		    fr = new FileReader(filepath);
		    br = new BufferedReader(fr);
		    
		    String line = null;
		    while((line = br.readLine()) != null)
		    {
		    	index = Integer.parseInt(line.substring(0, line.indexOf(";")));
		    	bw.write(line + "\n");
		    }
		    if(increase)
		    	index++;
		    for(int i = 0; i < lines.length; i++)
		    {
		    	bw.write(index + ";" + lines[i] + "\n");
		    }
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			try
			{
				if(bw != null)
					bw.close();
			    if(fw != null)
			    	fw.close();
			    if(br != null)
					br.close();
			    if(fr != null)
			    	fr.close();
			}
			catch(IOException e)
			{
				throw e;
			}
		}
	}
	
	public static String[][] bulkRun(Params p, double[] epsilon, boolean headYN, String path) throws GRBException, IOException
	{
		TestObject to = new TestObject(p);
		println("TestObject created");
		IntegerResult ir = calcInteger(to);
		println("Integer solution calculated");
		IntegerResult irrel = calcIntegerRelaxed(to);
		println("Relaxed integer solution calculated");
		MAXCUResult[] mr = new MAXCUResult[epsilon.length];
		
		String[] header = new String[]{"Bidders", "Bids", "ngoods", "ndiffgoods",  "minsupp", "maxbidsize", "Dimensionality", "AvgBids",
				"IntOpt", "IntegerTimeMS", "IntOptNumWastedGoods", "IntOptAgentsWithGoods",
				"IntRelOpt", "IntRelTimeMS", "IntRelNumRel", "IntRelNumWastedGoods", "IntRelAgentsWithGoods",
				"MAXCUIterations", "MAXCUTimeMS", "MAXCUScaledOpt", "MAXCUDistance",
				"epsilon", "MAXCUOpt", "MAXCUNumIntRes",
				"MAXCURelaxedMin", "MAXCURelaxedMax", "MAXCURelaxedMean", "MAXCURelaxedMedian", "MAXCURelaxedVar",
				"MAXCUIntOptMin", "MAXCUIntOptMax", "MAXCUIntOptMean", "MAXCUIntRelMedian", "MAXCUIntRelVar",
				"MAXCUAgentsWithGoods", "MAXCUNumWastedGoods", "MAXCUNumSolRelaxed"};
		
		for(int i = 0; i < mr.length; i++)
		{
			mr[i] = calcMAXCU(to, epsilon[i], epsilon[i]);
			println("One MAXCU solution calculated, epsilon: " + epsilon[i]);
			writeBulk(path, to, p, ir, irrel, headYN && i == 0 ? true : false, header, mr[i], epsilon[i]);
		}
		
		
		String[][] result = new String[headYN ? mr.length+1 : mr.length][header.length];
		if(headYN)
			result[0] = header;
		
		return result;
	}
	
	private static void writeBulk(String path, TestObject to, Params p, IntegerResult ir, IntegerResult irrel,
			boolean headerYN, String[] header, MAXCUResult mr, double epsilon) throws IOException
	{
		String[][] result = new String[headerYN ? 2 : 1][header.length];
		
		if(headerYN)
			result[0] = header;
		
		int j = 0;

		if(headerYN)
			j = 1;
		else
			j = 0;
		result[j][0] = Integer.toString(to.getNumBidders());
		result[j][1] = Integer.toString(to.getNumBids());
		
		result[j][2] = Integer.toString(p.n_goods);
		result[j][3] = Integer.toString(p.n_diff_goods);
		result[j][4] = Integer.toString(p.min_supp_good);
		result[j][5] = Integer.toString(p.max_bid_size);
		
		result[j][6] = Integer.toString(to.getDimensionality());
		result[j][7] = toString(to.getAvgBids());
		
		
		result[j][8] = toString(ir.opt);
		result[j][9] = Long.toString(ir.timeNS / (1000*1000));
		result[j][10] = Double.toString(ir.numWastedGoods);
		result[j][11] = Double.toString(ir.agentsWithGoods);
		
		result[j][12] = toString(irrel.opt); //IntegerOptRel
		result[j][13] = Long.toString(irrel.timeNS / (1000*1000));
		result[j][14] = Integer.toString(irrel.numRelaxations);
		result[j][15] = Double.toString(irrel.numWastedGoods);
		result[j][16] = Double.toString(irrel.agentsWithGoods);
		
		result[j][17] = Integer.toString(mr.iterations);
		result[j][18] = Long.toString(mr.timeNS / (1000*1000));
		result[j][19] = toString(mr.scaledOpt);
		result[j][20] = toString(mr.distance);
		result[j][21] = toString(epsilon);
		result[j][22] = toString(mr.origOpt);
		result[j][23] = toString(mr.statsRelaxations[5]);
		
		result[j][24] = toString(mr.statsRelaxations[0]);
		result[j][25] = toString(mr.statsRelaxations[1]);
		result[j][26] = toString(mr.statsRelaxations[2]);
		result[j][27] = toString(mr.statsRelaxations[3]);
		result[j][28] = toString(mr.statsRelaxations[4]);
		
		result[j][29] = toString(mr.statsSolution[0]);
		result[j][30] = toString(mr.statsSolution[1]);
		result[j][31] = toString(mr.statsSolution[2]);
		result[j][32] = toString(mr.statsSolution[3]);
		result[j][33] = toString(mr.statsSolution[4]);
		
		result[j][34] = toString(mr.agentsWithGoods);
		result[j][35] = toString(mr.numWastedGoods);
		
		result[j][36] = toString(mr.statsRelaxations[6]);
		
		append(path, result, headerYN);
	}
	
	public static String toString(double val)
	{
		return Double.toString(val).replace('.', ',');
	}
	
	public static class MAXCUResult
	{
		public long timeNS;
		public int iterations;
		public double scaledOpt;
		public double origOpt;
		public double distance;
		
		public double agentsWithGoods;
		public double numWastedGoods;
		
		public double[] statsRelaxations;
		
		public double[] statsSolution;
	}
	public static MAXCUResult calcMAXCU(TestObject to, double epsilon, double delta) throws GRBException
	{		
		ModelMatrix mm = to.getModelMatrix();
		
		long start = System.nanoTime();
		
		OptimumCalculator oc = new OptimumCalculator(mm, false);
		MAXCU m = new MAXCU(mm, epsilon, delta, oc.getDVAssigned(), oc.getOpt());
		m.calculate();
		
		long end = System.nanoTime();
		
		MAXCUResult mr = new MAXCUResult();
		mr.iterations = m.getIterations();
		mr.timeNS = end - start;
		mr.scaledOpt = m.getFractOptScaled();
		mr.distance = m.getDistance();
		mr.origOpt = oc.getOpt();
		
		mr.agentsWithGoods = numAgentsWithGoods(oc.getDVAssigned());
		mr.numWastedGoods = getNumWastedGoods(oc.getSlack(), mm);
		
		mr.statsRelaxations = statsRelaxation(mm, mm.getdConstraint(), m.getS());
		mr.statsSolution = statsSolution(mm, m.getS());
		return mr;
	}
	
	/**
	 * The method returns the statistics, about how the number of relaxations in the integer solutions
	 * @param mm
	 * @param dconstr
	 * @param S
	 * @return
	 */
	public static double[] statsRelaxation(ModelMatrix mm, double[] dconstr, double[][] S)
	{
		double[] stats = new double[7];
		
		double[] counts = new double[S.length];
		
		int countSolRel = 0;
		
		for(int i = 0; i < S.length; i++)
		{
			counts[i] = countRelaxation(mm, dconstr, S[i]);
			if(counts[i] > 0) countSolRel++;
		}
		
		stats[0] = Stats.min(counts);
		stats[1] = Stats.max(counts);
		stats[2] = Stats.mean(counts);
		stats[3] = Stats.median(counts);
		stats[4] = Stats.var(counts);
		stats[5] = S.length;
		stats[6] = countSolRel;
		
		return stats;
	}
	
	/**
	 * The method counts, how often an integer solution relaxed the supply constraints
	 * @param mm
	 * @param dconstr
	 * @param S1
	 * @return
	 */
	public static int countRelaxation(ModelMatrix mm, double[] dconstr, double[] S1)
	{
		int res = 0;
		double[][] matrix = mm.getDmatrix();
		ModelMatrix.constrType[] ctype = mm.getConstrTypes();
		
		double lineRes = 0;
		
		for(int i = 0; i < ctype.length; i++)
		{
			if(ctype[i] == constrType.supply)
			{
				lineRes = 0;
				for(int j = 0; j < matrix[0].length; j++)
				{
					lineRes += S1[j] * matrix[i][j];
				}
				if(lineRes > dconstr[i])
					res++;
			}
		}
		
		return res;
	}
	
	/**
	 * Returns the statistics about the objective values of the solutions
	 * @param mm
	 * @param S
	 * @return
	 */
	public static double[] statsSolution(ModelMatrix mm, double[][] S)
	{
		double[] stats = new double[6];
		
		double[] objVal = new double[S.length];
		
		for(int i = 0; i < objVal.length; i++)
		{
			objVal[i] = calcObjectiveValue(mm, S[i]);
		}
		
		stats[0] = Stats.min(objVal);
		stats[1] = Stats.max(objVal);
		stats[2] = Stats.mean(objVal);
		stats[3] = Stats.median(objVal);
		stats[4] = Stats.var(objVal);
		stats[5] = S.length;
		
		return stats;
	}
	
	/**
	 * Calculates the objective value
	 * @param mm
	 * @param S1
	 * @return
	 */
	public static double calcObjectiveValue(ModelMatrix mm, double[] S1)
	{
		double res = 0;
		double[] costCoef = mm.getCostCoeff();
		
		for(int i = 0; i < costCoef.length; i++)
		{
			res += costCoef[i] * S1[i];
		}
		
		return res;
	}
	
	public static class IntegerResult
	{
		public long timeNS;
		public double opt;
		public double agentsWithGoods;
		public double numWastedGoods;
		public int numRelaxations;
	}
	
	public static IntegerResult calcInteger(TestObject to) throws GRBException
	{
		ModelMatrix mm = to.getModelMatrix();
		
		long start = System.nanoTime();
		
		OptimumCalculator oc = new OptimumCalculator(mm, true);
		
		long end = System.nanoTime();
		
		IntegerResult ir = new IntegerResult();
		ir.timeNS = end-start;
		ir.opt = oc.getOpt();
		ir.agentsWithGoods = numAgentsWithGoods(oc.getDVAssigned());
		ir.numWastedGoods = getNumWastedGoods(oc.getSlack(), mm);
		ir.numRelaxations = 0;
		return ir;
	}
	
	private static double numAgentsWithGoods(double[] dv)
	{
		double result = 0;
		
		for(int i = 0; i < dv.length; i++)
		{
			result += dv[i];
		}
		return result;
	}
	
	private static double getNumWastedGoods(double[] slack, ModelMatrix mm)
	{
		double result = 0;
		ModelMatrix.constrType[] constrType = mm.getConstrTypes();
		for(int i = 0; i < slack.length; i++)
		{
			if(constrType[i] == ModelMatrix.constrType.supply)
			{
				result += slack[i];
			}
		}
		
		return result;
	}
	
	public static IntegerResult calcIntegerRelaxed(TestObject to) throws GRBException
	{
		ModelMatrix mm = to.getModelMatrix();
		double[] dconstraint = java.util.Arrays.copyOf(mm.getdConstraint(), mm.getdConstraint().length);
		ModelMatrix.constrType[] constrTypes = mm.getConstrTypes();
		int relax = to.getParams().max_bid_size;
		
		for(int i = 0; i < dconstraint.length; i++)
		{
			if(constrTypes[i] == constrType.supply)
			{
				dconstraint[i] += relax;
			}
		}
		
		ModelMatrix newmm = new ModelMatrix(mm.getDmatrix(), dconstraint, mm.getCostCoeff(), constrTypes);
		
		long start = System.nanoTime();
		
		OptimumCalculator oc = new OptimumCalculator(newmm, true);
		
		long end = System.nanoTime();
		
		IntegerResult ir = new IntegerResult();
		ir.timeNS = end-start;
		ir.opt = oc.getOpt();
		ir.agentsWithGoods = numAgentsWithGoods(oc.getDVAssigned());
		ir.numWastedGoods = getNumWastedGoods(oc.getSlack(), mm);
		ir.numRelaxations = countRelaxation(mm, mm.getdConstraint(), oc.getDVAssigned());
		return ir;
	}

	public static void main(String[] args) throws GRBException, IOException, InterruptedException
	{
		String path = "C:\\Users\\falkenstein\\Desktop\\result.csv";
		Params p = new Params(200, 800, 20, 25, 5);
		double[] epsilon = new double[]{0.15};
		
		//println("Calculate " + p.n_bidders + " - " + p.n_goods);
		String[][] elements = null;
		//println("Write");
		//saveCSV(, elements);
		
		// bidders, total items, min supp, diff goods, max bundle
		p = new Params(50, 150, 7, 10, 5);
		println("Calculate " + p.n_bidders + " - " + p.n_goods);
		elements = bulkRun(p, epsilon, true, path);
		
//		p = new Params(400, 1500, 25, 30, 6);
//		println("Calculate " + p.n_bidders + " - " + p.n_goods);
//		elements = bulkRun(p, epsilon, false, path);
//		
//		p = new Params(400, 1400, 25, 30, 6);
//		println("Calculate " + p.n_bidders + " - " + p.n_goods);
//		elements = bulkRun(p, epsilon, false, path);
//		
//		p = new Params(400, 1300, 25, 30, 6);
//		println("Calculate " + p.n_bidders + " - " + p.n_goods);
//		elements = bulkRun(p, epsilon, false, path);
//		
//		p = new Params(400, 1200, 25, 30, 6);
//		println("Calculate " + p.n_bidders + " - " + p.n_goods);
//		elements = bulkRun(p, epsilon, false, path);
//		
//		p = new Params(400, 1100, 25, 30, 6);
//		println("Calculate " + p.n_bidders + " - " + p.n_goods);
//		elements = bulkRun(p, epsilon, false, path);
//	
//		p = new Params(400, 1000, 25, 30, 6);
//		println("Calculate " + p.n_bidders + " - " + p.n_goods);
//		elements = bulkRun(p, epsilon, false, path);
//		
//		p = new Params(400, 900, 25, 30, 6);
//		println("Calculate " + p.n_bidders + " - " + p.n_goods);
//		elements = bulkRun(p, epsilon, false, path);
//	
//		p = new Params(400, 800, 25, 30, 6);
//		println("Calculate " + p.n_bidders + " - " + p.n_goods);
//		elements = bulkRun(p, epsilon, false, path);
		
		println("Done");
	}
	
	public static void wait(int ms)
	{
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testSmall() throws IOException, GRBException
	{
		String path = "C:\\Users\\falkenstein\\Desktop\\result.csv";
		Params p = new Params(60, 250, 10, 20, 5);
		double[] epsilon = new double[]{0.3, 0.275, 0.25, 0.215, 0.2, 0.175, 0.15, 0.125, 0.1, 0.075, 0.05, 0.025};
		
		println("Calculate " + p.n_bidders + " - " + p.n_goods);
		String[][] elements = bulkRun(p, epsilon, true, path);
		println("Write");
		//saveCSV("C:\\Users\\Kevin\\Desktop\\result.csv", elements);
		
		println("Calculate " + p.n_bidders + " - " + p.n_goods);
		elements = bulkRun(p, epsilon, false, path);
		println("Write");
		//ppend("C:\\Users\\Kevin\\Desktop\\result.csv", elements, true);
		
		p = new Params(60, 250, 10, 20, 5);
		println("Calculate " + p.n_bidders + " - " + p.n_goods);
		elements = bulkRun(p, epsilon, false, path);
		println("Write");
		//append("C:\\Users\\Kevin\\Desktop\\result.csv", elements, true);
//		println("Wait");
//		Thread.sleep(1000 * 60 * 5);
		
		p = new Params(100, 400, 10, 40, 5);
		println("Calculate " + p.n_bidders + " - " + p.n_goods);
		elements = bulkRun(p, epsilon, false, path);
		println("Write");
		//append("C:\\Users\\Kevin\\Desktop\\result.csv", elements, true);
		
		elements = bulkRun(p, epsilon, false, path);
		println("Write");
		//append("C:\\Users\\Kevin\\Desktop\\result.csv", elements, true);
//		println("Wait");
//		Thread.sleep(1000 * 60 * 5);
		
		p = new Params(150, 650, 20, 25, 5);
		println("Calculate " + p.n_bidders + " - " + p.n_goods);
		elements = bulkRun(p, epsilon, false, path);
		println("Write");
		//append("C:\\Users\\Kevin\\Desktop\\result.csv", elements, true);
//		println("Wait");
//		Thread.sleep(1000 * 60 * 5);
		
		println("Calculate " + p.n_bidders + " - " + p.n_goods);
		elements = bulkRun(p, epsilon, false, path);
		println("Write");
		//append("C:\\Users\\Kevin\\Desktop\\result.csv", elements,true);
		
		println("Done");
		
		
		Runtime runtime = Runtime.getRuntime();
		runtime.exec("shutdown -s -t 0");
		System.exit(0);
	}
	
	public static void println(String text)
	{
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    Date now = new Date();
	    String strDate = sdfDate.format(now);
	    System.out.println(strDate + " - " + text);
	}
}
