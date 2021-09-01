package Eval;

public class Stats {
	
	public static double mean(double[] vals)
	{
		double res = 0;
		
		for(int i = 0; i < vals.length; i++)
		{
			res += vals[i];
		}
		
		return res / vals.length;
	}
	
	public static double min(double[] vals)
	{
		double res = vals[0];
		
		for(int i = 0; i < vals.length; i++)
		{
			if(vals[i] < res)
				res = vals[i];
		}
		
		return res;
	}
	
	public static double max(double[] vals)
	{
		double res = vals[0];
		
		for(int i = 0; i < vals.length; i++)
		{
			if(vals[i] > res)
				res = vals[i];
		}
		
		return res;
	}
	
	public static double var(double[] vals)
	{
		double res = 0;
		double mean = mean(vals);
		
		for(int i = 0; i < vals.length; i++)
		{
			res += (vals[i]-mean) * (vals[i]-mean);
		}
		
		res = res / (vals.length - 1);
		
		return res;
	}
	
	public static double median(double[] vals)
	{
		double[] vals2 = java.util.Arrays.copyOf(vals, vals.length);
		java.util.Arrays.sort(vals2);
		
		if(vals2.length % 2 == 0)
		{
			return (vals2[vals2.length / 2] + vals2[vals2.length / 2 - 1]) / 2;
		}
		else
		{
			return vals2[vals2.length / 2];
		}
	}
}
