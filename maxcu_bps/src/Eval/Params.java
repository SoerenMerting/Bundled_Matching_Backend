package Eval;

public class Params {
	int max_good_value = 100; 				//maximum public price for a good in the graph
	double additional_good = 0.9;			//the chance of adding a good to a bundle
	int max_substitutable_bids = 5;			//How many bids each bidder can have
	double additivity = 0.2;				//each bundle has a higher value than the sum of it's components
	//S(n) = n ^(1+additivity)				//this function determines, by how much the price of a bundle is higher than its components
	double deviation = 0.5;					//The factor by how much the private values can deviate from the common value (uniform distr)
	double budget_factor = 1.5;				//the budget of a customer is 0.5 of the value of the first bid
	double resale_factor = 0.5;				//what is the price if you sold the bundle (based on common value of course)

	double normal_mean = 0.0;				//used in case of normal distribution
	double normal_stdev = 30.0;				//used in case of normal distribution
	double jump_prob = 0.05;				//probability, that an item is picked, which is not connected to current bundle
	
	/*
	the funny thing is, that the in their original code, they chose this value randomly.
	I mimic this behavior here in the constructor.
	*/
	boolean normal_prices;					//determines, whether the personal price offsets are drawn from normal or unified distributions
	
	//int n_bids;								//Desired amount of bids
	int n_goods;							//amount of goods in total
	
	//my parameters
	int min_supp_good;						//minimum supply of each good
	int n_diff_goods;						//amount of different goods
	int max_bid_size;						//Maximum size of each bid
	double sizedeviation = 0.2;				//This factor determines, by how much additional bids from a bidder differs in terms of size from the first bid
	int n_bidders;							//desired amount of bidders
	
	public Params(int n_bidders, int n_goods, int min_supp_good, int n_diff_goods, int max_bid_size)
	{
		if(min_supp_good * n_diff_goods > n_goods)
			throw new IllegalArgumentException("The total amount of items is smaller than nDiffGoods * minEach");
		
		this.n_bidders = n_bidders;
		this.n_goods = n_goods;
		this.min_supp_good = min_supp_good;
		this.n_diff_goods = n_diff_goods;
		this.max_bid_size = max_bid_size;
		
		//they seriously do the same in the original code. See constructor in file regions.cpp
		normal_prices = (new java.util.Random()).nextBoolean();
	}
	
	public double getNormalStDev()
	{
		return this.normal_stdev;
	}
	
	public void setNormalStDev(double val)
	{
		this.normal_stdev = val;
	}
	
	public double getNormalMean()
	{
		return normal_mean;
	}
	
	public void setNormalMean(double val)
	{
		this.normal_mean = val;
	}
	
	public double getResaleFactor()
	{
		return this.resale_factor;
	}
	
	public void setResaleFactor(double val)
	{
		this.resale_factor = val;
	}
	
	public void setJumpProb(double val)
	{
		this.jump_prob = val;
	}
	
	public double getJumpProb()
	{
		return jump_prob;
	}
	
	public void setNormalPrices(boolean val)
	{
		this.normal_prices = val;
	}
	
	public boolean getNormalPrices()
	{
		return normal_prices;
	}
	
	public void setNDiffGoods(int val)
	{
		this.n_diff_goods = val;
	}
	
	public int getNDiffGoods()
	{
		return n_diff_goods;
	}
	
	public void setMinSuppGood(int val)
	{
		this.min_supp_good = val;
	}
	
	public int getMinSuppGood()
	{
		return min_supp_good;
	}
	
	public void setNGoods(int val)
	{
		this.n_goods = val;
	}
	
	public int getNGoods()
	{
		return n_goods;
	}
	
	public void setNBidders(int val)
	{
		this.n_bidders = val;
	}
	
	public int getNBidders()
	{
		return n_bidders;
	}
	
	public void setBudgetFactor(double val)
	{
		this.budget_factor = val;
	}
	
	public double getBudgetFactor()
	{
		return budget_factor;
	}
	
	public void setDeviation(double val)
	{
		this.deviation = val;
	}
	
	public double getDeviation()
	{
		return deviation;
	}
	
	public void setAdditivity(double val)
	{
		this.additivity = val;
	}
	
	public double getAdditivity()
	{
		return additivity;
	}
	
	public void setMaxSubstitutableBids(int val)
	{
		this.max_substitutable_bids = val;
	}
	
	public int getMaxSubstitutableBids()
	{
		return max_substitutable_bids;
	}
	
	public void setAdditionalGood(double val)
	{
		this.additional_good = val;
	}
	
	public double getAdditionalGood()
	{
		return additional_good;
	}
	
	public int getMaxGoodValue()
	{
		return max_good_value;
	}
	
	public void setMaxGoodValue(int val)
	{
		this.max_good_value = val;
	}
}
