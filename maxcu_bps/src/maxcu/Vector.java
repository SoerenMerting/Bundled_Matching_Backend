package maxcu;

public class Vector {

	public static double[] vectorScalar(double[] vector, double scalar)
	{
		double[] res = new double[vector.length];
		
		for(int i = 0; i < res.length; i++)
		{
			res[i] = vector[i] * scalar;
		}
		
		return res;
	}

	public static double[] vectorAddition(double[] first, double[] second)
	{
		if(first.length != second.length)
			throw new IllegalArgumentException("Lengths of vectors do not match");
		
		double[] res = new double[first.length];
		for(int i = 0; i < first.length; i++)
		{
			res[i] = first[i] + second[i];
		}
		
		return res;
	}

	public static double[] vectorSubtraction(double[] first, double[] second)
	{
		return vectorAddition(first, vectorScalar(second, -1));
	}

	public static double vectorLength(double[] vector)
	{
		double res = 0;
		
		for(int i = 0; i < vector.length; i++)
		{
			res += vector[i] * vector[i];
		}
		
		return Math.sqrt(res);
	}
	
	public static double vectorVector(double[] first, double[] second)
	{
		if(first.length != second.length)
			throw new IllegalArgumentException("Lengths of vectors do not match");
		
		double res = 0;
		
		for(int i = 0; i < first.length; i++)
		{
			res += first[i] * second[i];
		}
		
		return res;
	}
}
