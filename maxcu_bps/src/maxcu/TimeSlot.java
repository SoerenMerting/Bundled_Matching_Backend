package maxcu;

/**
 * TimeSlot class. Defined by name (warehouse), time and capacity
 * @author Kevin
 *
 */
public class TimeSlot implements Comparable<TimeSlot>{
	private String name;
	private int time;
	private int capacity;
	
	/**
	 * Initializes a timeslot
	 * @param name
	 * @param time
	 * @param capacity
	 */
	public TimeSlot(String name, int time, int capacity)
	{
		this.name = name;
		this.time = time;
		this.capacity = capacity;
	}
	
	public TimeSlot(String name, int capacity)
	{
		this.name = name;
		this.capacity = capacity;
		this.time = 0;
	}
	
	/**
	 * Returns the timeslot's name
	 * @return
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Returns the timeslot's time
	 * @return
	 */
	public int getTime()
	{
		return time;
	}
	
	/**
	 * Returns the timeslot's capacity
	 * @return
	 */
	public int getCapacity()
	{
		return capacity;
	}
	
	/**
	 * True if the TimeSlot objects are equal
	 * @param ts
	 * @return
	 */
	public boolean equals(TimeSlot ts)
	{
		if(ts == null)
			return false;
		
		return this.compareTo(ts) == 0;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj == null)
			return false;
		
		if(obj instanceof TimeSlot)
			return equals((TimeSlot)obj);
		
		return false;
	}

	/**
	 * Compares a timeslot first by its name and then by its time.
	 */
	@Override
	public int compareTo(TimeSlot arg0) {
		if(this == arg0)
			return 0;
		
		int strcmp = this.name.compareTo(arg0.name);
		if(strcmp != 0)
			return strcmp;
		
		int intcmp = (new Integer(this.time)).compareTo(new Integer(arg0.time));
		if(intcmp != 0)
			return intcmp;
		
		if(this.capacity == arg0.capacity)
			return 0;
		
		throw new IllegalStateException("Compared two TimeSlot objects " + this.toString() 
				+ "\nHowever, they have inconsistent capacities: " + this.capacity + " vs " + arg0.capacity);
	}

	@Override
	public int hashCode()
	{
		return this.toString().hashCode();
	}
	
	@Override
	public String toString()
	{
		if(time != 0)
			return name + ":" + time;
		else
			return name;
	}
}
