package Eval;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class represents a bid. It contains the elements within the bid, the bidder and also the valuation
 * @author Kevin
 *
 */
public class Bid implements Comparable<Bid>{
	ArrayList<Integer> items = new ArrayList<>();
	double commonVal;
	double value;
	int bidder = -1;
	
	/**
	 * Adds an item to the bundle
	 * @param val
	 */
	public void addItem(int val)
	{
		items.add(new Integer(val));
	}
	
	/**
	 * Clones a bundle
	 */
	@Override
	public Bid clone()
	{
		Bid b = new Bid();
		
		b.commonVal = this.commonVal;
		b.value = this.value;
		
		
		b.items = new ArrayList<>(this.items.size());
		
		for(int i = 0; i < items.size(); i++)
		{
			b.items.add(new Integer(this.items.get(i)));
		}
		
		b.bidder = this.bidder;
		
		return b;
	}
	
	/**
	 * Sorts the items within the bundle
	 */
	public void sort()
	{
		int[] vals = getItems();
		Arrays.sort(vals);
		
		this.items = new ArrayList<>(vals.length);
		
		for(int i = 0; i < vals.length; i++)
		{
			items.add(vals[i]);
		}
	}
	
	/**
	 * Returns an array of the items within the bid
	 * @return
	 */
	public int[] getItems()
	{
		int[] res = new int[items.size()];
		
		for(int i = 0; i < res.length;i++)
		{
			res[i] = items.get(i).intValue();
		}
		
		return res;
	}
	
	/**
	 * Removes an item from the bid
	 * @param item
	 */
	public void remove(int item)
	{
		this.items.remove(new Integer(item));
	}

	/**
	 * Returns the amount of items within the bid
	 * @return
	 */
	public int count()
	{
		return items.size();
	}
	
	/**
	 * Returns true, if the bid contains the item in question
	 * @param val
	 * @return
	 */
	public boolean contains(int val)
	{
		return items.contains(new Integer(val));
	}
	
	/**
	 * Returns the item at index i
	 * @param i
	 * @return
	 */
	public int getAt(int i)
	{
		return items.get(i).intValue();
	}
	
	/**
	 * Returns true, if all the items within bid b are also within this bid
	 * @param b
	 * @return
	 */
	public boolean subsetEqualOf(Bid b)
	{
		if(this.count() > b.count()) return false;
		
		int[] myitems = this.getItems();
		int[] otheritems = b.getItems();
		Arrays.sort(myitems);
		Arrays.sort(otheritems);
		
		int myIt = 0, otherIt = 0;
		while(myIt < this.count())
		{
			if(otherIt >= b.count() || myitems[myIt] < otheritems[otherIt])
				return false;
			else if(myitems[myIt] > otheritems[otherIt])
				otherIt++;
			else
				myIt++;
		}
		return true;
	}
	
	/**
	 * Returns true, if bid a and bid b have exactly the same items
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean identical(Bid a, Bid b)
	{
		int[] as = a.getItems();
		int[] bs = b.getItems();
		if(as.length != bs.length)
			return false;
		Arrays.sort(as);
		Arrays.sort(bs);
		
		for(int i = 0; i < as.length; i++)
		{
			if(as[i] != bs[i])
				return false;
		}
		return true;
	}

	/**
	 * Compares two bids by their value
	 */
	@Override
	public int compareTo(Bid o) {
		if(this.value > o.value)
			return -1;
		if(this.value < o.value)
			return 1;
		return 0;
	}

	/**
	 * Sorts an array of bids, even though it might contain null entries
	 * @param bs
	 * @return
	 */
	public static Bid[] sortWithNulls(Bid[] bs)
	{
		int notnulls = 0;
		for(int i = 0; i < bs.length; i++)
		{
			if(bs[i] != null)
				notnulls++;
		}
		
		Bid[] temp = new Bid[notnulls];
		int counter = 0;
		for(int i = 0; i < bs.length; i++)
		{
			if(bs[i] != null)
				temp[counter++] = bs[i];
		}
		
		Arrays.sort(temp);
		
		return temp;
	}
	
	@Override
	public String toString()
	{
		this.sort();
		StringBuilder sb = new StringBuilder();
		sb.append(new java.text.DecimalFormat("#.##").format(value)); 
		sb.append(' ');
		
		for(int i = 0; i < this.count(); i++)
		{
			sb.append(items.get(i));
			sb.append(' ');
		}
		
		sb.append('#');
		sb.append(this.bidder);
		
		return sb.toString();
	}
}
