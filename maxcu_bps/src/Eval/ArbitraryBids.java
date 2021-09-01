package Eval;

import java.util.*;

/**
 * Creates random bids, based on the algorithm of CATS.
 * The functionality of CATS was adapted, so that it supports multiple supply for goods and a maximum bid size.
 * @author Kevin
 *
 */
public class ArbitraryBids{
	private final java.util.Random rnd = new Random();
	private final ArbitraryObj obj;
	private final Params p;
	private int bidder = 0;
	
	//private BidSet bids;
	private ArrayList<Bid> bidlist;
	
	public ArbitraryBids(Params p)
	{
		this.p = p;
		obj = new ArbitraryObj(p);
		bidlist = new ArrayList<>();
	}
	
	/**
	 * Generates the random bids
	 */
	public void generate()
	{
		double[] privateVals = null; // new double[p.n_diff_goods];
		double[] tempVals = null; // = new double[p.n_diff_goods];
		Bid b = new Bid();
		Bid[] bs = new Bid[p.n_diff_goods];
		
		double budget, min_resale_value;
		
		while(bidder < p.n_bidders)
		{
			privateVals = new double[p.n_diff_goods];
			tempVals = new double[p.n_diff_goods];
			b.commonVal = 0; b = new Bid();
			
			createPrivatePrices(privateVals, tempVals);
			
			double drnd = rnd.nextDouble();
			double sum = 0;
			int g = 0;
			for(g = 0; drnd > sum + tempVals[g] && g < p.n_diff_goods; g++)
			{
				sum += tempVals[g];
			}
			if(g >= p.n_diff_goods)
				for(g = p.n_diff_goods-1; tempVals[g] == 0; g--); //CATS: "handle rounding error"
			
			b.addItem(g);
			
			//CATS: "add additional goods as required"
			while(rnd.nextDouble() <= p.additional_good && b.count() < p.n_diff_goods && b.count() < p.max_bid_size)
			{
				addGoodToBundle(b, tempVals);
			}
			
			//CATS: "calculate value of the bid"
			b.commonVal = CommonValue(b);
			b.value = TotalPrivateValue(b, privateVals);
			if(b.value <= 0)
				continue;
			
			budget = p.budget_factor * b.value;
			min_resale_value = p.resale_factor * b.commonVal;
			bs = new Bid[p.n_diff_goods];
			boolean one_valid = makeSubstitutableBids(bs, b, tempVals, privateVals, budget, min_resale_value);
			if(!one_valid)
			{
				//bid.add(b, p.remove_dominated_bids);
				b.bidder = bidder;
				bidlist.add(b);
			}
			else
			{
				/* CATS:
				 * "if there's more than one bid, put them in order so that the most competitive ones are selected.
				 * Make sure that multiple identical bids are not substitutes"
				 */
				createMultipleXORBids(bs, b, budget, min_resale_value, bidder);
			}
			bidder++;
		}
	}
	
	/**
	 * Returns the amount of bidders
	 * @return
	 */
	public int getNumBidder()
	{
		return bidder;
	}
	
	/**
	 * Returns the internal ArbitraryObj instance
	 * @return
	 */
	public ArbitraryObj getObj()
	{
		return obj;
	}
	
	/**
	 * Returns an ArrayList of the created bids
	 * @return
	 */
	public ArrayList<Bid> getBids()
	{
		return bidlist;
	}
	
	/**
	 * Creates more bids for one bidder
	 * @param bs
	 * @param b
	 * @param budget
	 * @param min_resale_value
	 * @param bidder
	 */
	private void createMultipleXORBids(Bid[] bs, Bid b, double budget, double min_resale_value, int bidder)
	{
		//bids.addXor(b);
		b.bidder = bidder;
		bidlist.add(b);
		int numsubs = 0;
		Bid[] newbs = Bid.sortWithNulls(bs);
		boolean seenbefore = false;
		for(int i = 0; i < b.count(); i++)
		{
			if(numsubs >= p.max_substitutable_bids)
				break;
			if(newbs[i].value >= 0 && newbs[i].value <= budget && newbs[i].commonVal >= min_resale_value && !Bid.identical(b, newbs[i]))
			{
				seenbefore = false;
				for(int j = 0; j < i && !seenbefore; j++)
				{
					if(Bid.identical(newbs[j], newbs[i]))
						seenbefore = true;
				}
				if(!seenbefore)
				{
					//bids.addXor(newbs[i]);
					bs[i].bidder = bidder;
					bidlist.add(bs[i]);
					numsubs++;
				}
			}
		}
		//bids.doneAddingXor(p.remove_dominated_bids);
	}
	
	/**
	 * Creats substitutable bids
	 * @param bs
	 * @param b
	 * @param tempVals
	 * @param privateVals
	 * @param budget
	 * @param min_resale_value
	 * @return
	 */
	private boolean makeSubstitutableBids(Bid[] bs, Bid b, double[] tempVals, double[] privateVals, double budget, double min_resale_value)
	{
		boolean one_valid = false;
		for(int i = 0; i < b.count(); i++)
		{
			if(bs[i] == null) bs[i] = new Bid();
			
			bs[i].addItem(b.getAt(i));
			
//			while(bs[i].count() < b.count())
//				addGoodToBundle(bs[i], tempVals);
//			
//			while(rnd.nextDouble() <= p.additional_good)
//			{
//				if(b.count() >= p.n_diff_goods || b.count() >= p.max_bid_size)
//					break;
//				addGoodToBundle(bs[i], tempVals);
//			}
			
			int newSize = newBundleSize(b.count());
			while(bs[i].count() < b.count() && bs[i].count() < newSize && bs[i].count() < p.max_bid_size)
				addGoodToBundle(bs[i], tempVals);
			
			
			bs[i].commonVal = CommonValue(bs[i]);
			bs[i].value = TotalPrivateValue(bs[i], privateVals);
			
			if(bs[i].value >= 0 && bs[i].value <= budget && bs[i].commonVal >= min_resale_value && !Bid.identical(b, bs[i]))
			{
				one_valid = true;
			}
		}
		return one_valid;
	}
	
	/**
	 * Randomly calculates the size of a new bundle based on the size of the former bundle
	 * @param formerSize
	 * @return
	 */
	private int newBundleSize(int formerSize)
	{
		double factor = rnd.nextDouble() * 2 * p.sizedeviation - p.sizedeviation;
		
		return (int)(formerSize * (1+factor));
	}
	
	/**
	 * Returns the common valuation for a bid
	 * @param b
	 * @return
	 */
	private double CommonValue(Bid b)
	{
		double total = 0;
		
		for(int i = 0; i < b.count(); i++)
		{
			total += obj.getPrices()[b.getAt(i)];
		}
		return total;
	}
	
	/**
	 * Returns the total valuation of a client for a bundle (it takes account of the private values) 
	 * @param b
	 * @param privateVals
	 * @return
	 */
	private double TotalPrivateValue(Bid b, double[] privateVals)
	{
		double total = 0;
		for(int i = 0; i < b.count(); i++)
		{
			total+=obj.getPrices()[b.getAt(i)] + privateVals[b.getAt(i)];
		}
		total += Math.pow(b.count(), 1+this.p.additivity);
		
		if(total < 0)
			return 0;
		
		return total;
	}
	
	/**
	 * Adds a good to the bundle based on the bundle size, the similarity of the items and the supply of the items
	 * @param b
	 * @param tempVals
	 */
	private void addGoodToBundle(Bid b, double[] tempVals)
	{
		if (b.count() >= p.n_diff_goods || b.count() > p.max_bid_size) return;
		
		int new_item;
		double prob;
		
		if(rnd.nextDouble() < p.jump_prob)
		{
			do
			{
				new_item = (int)(Math.abs(rnd.nextLong()) % p.n_diff_goods);
			}
			while(b.contains(new_item));
			b.addItem(new_item);
		}
		else //CATS: add a good based on the weights
		{
			double[] p = new double[this.p.n_diff_goods];
			double sum = 0;
			for(int i = 0; i < this.p.n_diff_goods; i++)
			{
				if(b.contains(i))
					p[i] = 0;
				else
				{
					prob = 0;
					
					for(int j = 0; j < b.count(); j++)
					{
						//Compare every object with those, that are already in the bundle
						prob += tempVals[i] * obj.getAdjMatrix()[i][b.getAt(j)] * (obj.getAmounts()[i] / this.p.n_goods);
					}
					p[i] = prob;
					sum += prob;
				}
			}
			
			if(sum <= 0)
				return;
			
			//CATS: "normalize"
			for(int i = 0; i < this.p.n_diff_goods; i++)
				p[i] /= sum;
			
			double rand_num = rnd.nextDouble();
			sum = 0;
			for(new_item = 0; rand_num > p[new_item] + sum && new_item < this.p.n_diff_goods; new_item++)
				sum += p[new_item];
			if(new_item >= this.p.n_diff_goods)
				for(new_item = this.p.n_diff_goods -1; tempVals[new_item] == 0; new_item--);
			
			b.addItem(new_item);
		}
	}
	
	/**
	 * Creates private prices for each good
	 * @param privateVals
	 * @param tempVals
	 */
	private void createPrivatePrices(double[] privateVals, double[] tempVals)
	{
		double total = 0;
		double minP=p.normal_mean + 1000 * p.normal_stdev;
		
		// CATS: "make the private values for the bidder: either uniform or normal"
		for(int i = 0; i < p.n_diff_goods; i++)
		{
			if(p.normal_prices)
			{
				privateVals[i] = rnd.nextGaussian()*p.normal_stdev + p.normal_mean;
				total += privateVals[i];
				
				if( privateVals[i] < minP )
				      minP = privateVals[i];
			}
			else
			{
				privateVals[i] = -p.deviation * p.max_good_value +
						(rnd.nextDouble() * 2 * p.deviation * p.max_good_value);
				
				tempVals[i] = privateVals[i] + p.deviation * p.max_good_value / 2 * p.deviation * p.max_good_value;
				total += tempVals[i];
			}
		}
		
		//normalize
	    if(p.normal_prices)
	    {
	    	total -= p.n_diff_goods * minP;
	    	for(int i = 0; i < p.n_diff_goods; i++)
	    	{
	    		tempVals[i] = privateVals[i] - minP;
	    		tempVals[i] /= total;
		    }
	    }
	    else
	    {
	    	for (int i = 0; i < p.n_diff_goods; i++)
	    		tempVals[i] /= total;
	    }
	}
	
	/**
	 * Outputs the bids
	 */
	public String toString()
	{
		ArrayList<Bid> b = this.getBids();
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < b.size(); i++)
		{
			sb.append(b.get(i));
			sb.append('\n');
		}
		
		return sb.toString();
	}
}
