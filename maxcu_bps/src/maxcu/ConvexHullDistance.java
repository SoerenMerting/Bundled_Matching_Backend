package maxcu;

import gurobi.*;

/**
 * Finds the distance from a point to a the closest point in a convex hull
 * @author Kevin
 */
public class ConvexHullDistance {
	
	private double[] dcY;
	private double[] dcLambda;
	private GRBEnv env;
	private GRBModel model;
	private double distance;
	
	/**
	 * Initializes the class
	 * @param convexHullPoints	The vectors which define the convex hull. vectors[vector in hull][feature]
	 * @param x		The point to which we are looking for the corresponding point within the hull
	 * @throws GRBException
	 */
	public ConvexHullDistance(double[][] convexHullPoints, double[] x) throws GRBException
	{
		int nVec = convexHullPoints.length;
		int nFeat = convexHullPoints[0].length;
		env = new GRBEnv();
		model = new GRBModel(env);
		GRBQuadExpr obj = new GRBQuadExpr();
		
		GRBVar[] dcY = new GRBVar[nFeat];
		this.dcY = new double[nFeat];
		//GRBVar[] vec = new GRBVar[nFeat];
		GRBVar[] dcLambda = new GRBVar[nVec];
		this.dcLambda = new double[nVec];
		
		for(int i = 0; i < nFeat; i++)
		{
			//vec[i] = model.addVar(opt[i], opt[i], 0, GRB.CONTINUOUS, "x*" + i);
			dcY[i] = model.addVar(Double.NEGATIVE_INFINITY, GRB.INFINITY, 0, GRB.CONTINUOUS, "Y" + i);
			
			//use binomial: (x-y)^2 = x^2 - 2xy + y^2
			obj.addConstant(x[i] * x[i]);
			obj.addTerm(-2 * x[i], dcY[i]);
			obj.addTerm(1, dcY[i], dcY[i]);
		}
		
		for(int i = 0; i < nVec; i++)
		{
			dcLambda[i] = model.addVar(0, 1, 0, GRB.CONTINUOUS, "");
		}
		model.update();
		
		model.setObjective(obj);
		model.set(GRB.IntAttr.ModelSense, GRB.MINIMIZE);
		
		model.update();
		
		//lambda constraint
		GRBLinExpr lambdaConstr = new GRBLinExpr();
		for(int i = 0; i < nVec; i++)
		{
			lambdaConstr.addTerm(1, dcLambda[i]);
		}
		model.addConstr(lambdaConstr, GRB.EQUAL, 1, "Sum of lambdas = 1");
		
		
		for(int i = 0; i < nFeat; i++)
		{
			GRBLinExpr distConstr = new GRBLinExpr();
			for(int j = 0; j < nVec; j++)
			{
				distConstr.addTerm(convexHullPoints[j][i], dcLambda[j]);
			}
			distConstr.addTerm(-1, dcY[i]);
			model.addConstr(distConstr, GRB.EQUAL, 0, "Distance Constr for feat. " + i);
		}
		
		model.update();
		model.getEnv().set(GRB.IntParam.OutputFlag, 0);
		model.optimize();
		
		this.distance = Math.sqrt(model.get(GRB.DoubleAttr.ObjVal));
		
		for(int i = 0; i < dcY.length; i++)
		{
			this.dcY[i] = dcY[i].get(GRB.DoubleAttr.X);
		}
		
		for(int i = 0; i < dcLambda.length; i++)
		{
			this.dcLambda[i] = dcLambda[i].get(GRB.DoubleAttr.X);
		}
		
		model.dispose();
		
		env.dispose();
	}

	/**
	 * Returns the vector in the convex hull closest to point opt
	 * @return
	 * @throws GRBException
	 */
	public double[] getY() throws GRBException
	{
		return dcY;
	}
	
	/**
	 * The linear components to each of the original points in the convex hull
	 * @return
	 * @throws GRBException
	 */
	public double[] getLambdas() throws GRBException
	{
		return dcLambda;
	}
	
	/**
	 * The distance between point opt and point y
	 * @return
	 */
	public double getDistance()
	{
		return distance;
	}
}
