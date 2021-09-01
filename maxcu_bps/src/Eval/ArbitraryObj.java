package Eval;

import java.util.Random;

/**
 * This class creates the fully connected graph of objects.
 * The weights of the adjacency matrix states the similarity of the object.
 * Each objects also gets a public value and the amount of available objects. 
 * @author Kevin
 *
 */
public class ArbitraryObj {
	private int[] amounts;
	private double[] prices;
	private double[][] adjmatrix;
	
	public ArbitraryObj(Params p)
	{
		Random rnd = new Random();
		double val = 0;
		adjmatrix = new double[p.n_diff_goods][p.n_diff_goods];
		
		for(int i = 0; i < p.n_diff_goods; i++)
		{
			for(int j = i; j < p.n_diff_goods; j++)
			{
				if(i == j) adjmatrix[i][j] = 0;
				else
				{
					do
					{
						val = rnd.nextDouble();
						adjmatrix[i][j] = val;
						adjmatrix[j][i] = val;
					}
					while(val == 0); //CATS: "make sure nothing has zero probability"
				}
			}
		}
		
		amounts = new int[p.n_diff_goods];
		int goodsLeft = p.n_goods - p.min_supp_good * p.n_diff_goods;
		for(int i = 0; i < p.n_diff_goods;i++)
		{
			amounts[i] = p.min_supp_good;
		}
		
		for(int i = 0; i < goodsLeft; i++)
		{
			amounts[rnd.nextInt(p.n_diff_goods)] += 1;
		}
		
		prices = new double[p.n_diff_goods];
		for(int i = 0; i < p.n_diff_goods;i++)
		{
			//regions.cpp: Regions::generate(..)
			prices[i] = rnd.nextDouble() * (p.max_good_value - 1) + 1; //makes sure we have at least price of 1
		}
	}

	/**
	 * Returns an array stating how many items of each type are available
	 * @return
	 */
	public int[] getAmounts()
	{
		return amounts;
	}
	
	/**
	 * Returns an array of the goods' prices
	 * @return
	 */
	public double[] getPrices()
	{
		return prices;
	}
	
	/**
	 * Returns the similarity (adjacency) matrix
	 * @return
	 */
	public double[][] getAdjMatrix()
	{
		return adjmatrix;
	}
}
