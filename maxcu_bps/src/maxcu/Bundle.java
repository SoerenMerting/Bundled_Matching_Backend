package maxcu;

public class Bundle implements Comparable<Bundle>{
	public static boolean DUPLICATESALLOWED = true;
	
	private TimeSlot[] elements;
	
	public Bundle(TimeSlot[] elements)
	{
		if(elements.length == 0)
			throw new IllegalArgumentException("The bundle must contain at least 1 element");
		
		if(!DUPLICATESALLOWED && checkDuplicates(elements))
			throw new IllegalArgumentException("The array of elements should not contain duplicates");
		
		java.util.Arrays.sort(elements);
		this.elements = elements;
	}
	
	public TimeSlot[] getElements()
	{
		return elements;
	}

	public boolean equals(Bundle arg0)
	{
		if(arg0 == null)
			return false;
		
		return this.compareTo(arg0) == 0;
	}
	
	public boolean equals(Object arg0)
	{
		if(arg0 == null)
			return false;
		
		if(arg0 instanceof Bundle)
			return this.equals((Bundle)arg0);
		return false;
	}
	
	@Override
	public int compareTo(Bundle arg0) {
		if(elements.length < arg0.elements.length)
			return -1;
		if(elements.length > arg0.elements.length)
			return 1;
		
		int cmp = 0;
		
		//elements are sorted already in the constructor!
		for(int i = 0; i < elements.length; i++)
		{
			cmp = elements[i].compareTo(arg0.elements[i]);
			if(cmp != 0)
				return cmp;
		}
		
		return 0;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append('(');
		
		for(int i = 0; i < elements.length; i++)
		{
			sb.append(elements[i].toString());
			
			if(i < elements.length - 1)
				sb.append(';');
		}
		
		sb.append(')');
		
		return sb.toString();
	}

	public int count(TimeSlot ts)
	{
		int res = 0;
		for(int i = 0; i < elements.length; i++)
		{
			if(elements[i].compareTo(ts) == 0)
				res++;
		}
		return res;
	}
	
	@Override
	public int hashCode()
	{
		return this.toString().hashCode();
	}

	/**
	 * True if array contains duplicates (based on TimeSlot.compareTo() method)
	 * @param arr
	 * @return
	 */
	private boolean checkDuplicates(TimeSlot[] arr)
	{
		for(int i = 0; i < arr.length; i++)
			for(int j = i + 1; j < arr.length; j++)
				if(arr[i].compareTo(arr[j]) == 0)
					return true;
		
		return false;
	}
}
