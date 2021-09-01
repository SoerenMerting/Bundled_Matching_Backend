package utils;

public class Tuple <T1 extends Comparable<T1>, T2 extends Comparable<T2>> implements Comparable<Tuple<T1,T2>>{

	private T1 e1;
	private T2 e2;
	
	public Tuple(T1 e1, T2 e2)
	{
		this.e1 = e1;
		this.e2 = e2;
	}
	
	public T1 getE1()
	{
		return e1;
	}
	public T2 getE2()
	{
		return e2;
	}
	
	@Override
	public int compareTo(Tuple<T1, T2> arg0) {
		int cmp1 = e1.compareTo(arg0.e1);
		if(cmp1 != 0)
			return cmp1;
		
		return e2.compareTo(arg0.e2);
	}
	
	@Override
	public String toString()
	{
		return e1.toString() + "-" + e2.toString();
	}
	 
}
