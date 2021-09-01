package Eval;

import java.util.ArrayList;

import gurobi.GRBException;
import maxcu.*;

public class TestObject {
	private Params p;
	private ArbitraryBids cats;
	
	private PreferenceTable pt;
	private ModelMatrix mm;
	
	private int removedCols;
	
	public TestObject(Params p)
	{
		this.p = p;
		cats = new ArbitraryBids(p);
		cats.generate();
		ArrayList<Bid> b = cats.getBids();
		
		generatePrefTable(b, createObjects());
		ModelMatrix.createReduced = true;
		mm = new ModelMatrix(pt);
		ModelMatrix.createReduced = false;
	}
	
	private TimeSlot[] createObjects()
	{
		TimeSlot[] ts = new TimeSlot[cats.getObj().getAmounts().length];
		
		for(int i = 0; i < ts.length; i++)
		{
			ts[i] = new TimeSlot(Integer.toString(i), cats.getObj().getAmounts()[i]);
		}
		
		return ts;
	}
	
	private Bundle createBundle(Bid b, TimeSlot[] obj)
	{
		TimeSlot[] elements = new TimeSlot[b.count()];
		for(int i = 0; i < b.count(); i++)
		{
			elements[i] = obj[b.getAt(i)];
		}
		return new Bundle(elements);
	}
	
	private void generatePrefTable(ArrayList<Bid> bids, TimeSlot[] obj)
	{
		pt = new PrefTableAuto(p.max_bid_size);
		
		for(int i = 0; i < bids.size(); i++)
		{
			Bid b = bids.get(i);
			
			Bundle bund = createBundle(b, obj);
			
			pt.addPreference(Integer.toString(b.bidder), bund, b.value);
		}
	}
	
	public ArbitraryBids getArbitrary()
	{
		return cats;
	}
	
	public PreferenceTable getPrefTable()
	{
		return pt;
	}
	
	public ModelMatrix getModelMatrix()
	{
		return mm;
	}
	
	public Params getParams()
	{
		return p;
	}
	
	public int getNumBidders()
	{
		return this.getArbitrary().getNumBidder();
	}
	
	public int getNumRemCols()
	{
		return this.removedCols;
	}
	public int getDimensionality()
	{
		return this.mm.getDimensionality();
	}
	public int getNumBids()
	{
		return this.getArbitrary().getBids().size();
	}
	public double getAvgBids()
	{
		return ((double)getNumBids()) / getNumBidders();
	}
	
	public static void main(String[] args) throws GRBException
	{
		PreferenceTable pt = new PrefTableAuto(5);
		TimeSlot ts1 = new TimeSlot("1", 1);
		TimeSlot ts2 = new TimeSlot("2", 1);
		Bundle a = new Bundle(new TimeSlot[]{ts1});
		Bundle b = new Bundle(new TimeSlot[]{ts2});
		
		pt.addPreference("1",a, 2);
		pt.addPreference("2",b, 3);
		
		
		//ModelMatrix.createReduced = true;
		ModelMatrix mm = new ModelMatrix(pt);
		ModelMatrix.createReduced = false;
		//int counter = mm.removeZeroCols2();
		int dim = mm.getDimensionality();
		
		System.out.println(mm.constraintTable());
	}
}
