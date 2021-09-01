package maxcu;

import maxcu.*;

/**
 * This class defines a preference table which can later be converted into an LP
 * @author Kevin
 */
public abstract class PreferenceTable {
	
	protected final int maxBundleSize; 	// L in the slideshow
	
	/**
	 * Initializes the class. The timeslots need to be specified!
	 * @param timeslots
	 */
	protected PreferenceTable(int maxBundleSize)
	{
		this.maxBundleSize = maxBundleSize;
	}
	
	/**
	 * Returns the maximum allowed size of bundles
	 * @return
	 */
	public int getMaxBundleSize()
	{
		return maxBundleSize;
	}

	/**
	 * Adds the preferences to the data structure. The key is the client and a bundle.
	 * @param client
	 * @param b
	 * @param value
	 */
	public abstract void addPreference(String client, Bundle b, double value);
		
	/**
	 * Returns an ordered array without duplicates of all clients added.
	 * @return
	 */
	public abstract String[] getClientsArray();
	
	/**
	 * Returns an ordered list without duplicates of all TimeSlots added.
	 * @return
	 */
	public abstract ULList<TimeSlot> getTimeSlots();
	
	/**
	 * Returns an ordered array without duplicates of all TimeSlots added.
	 * @return
	 */
	public abstract TimeSlot[] getTimeSlotsArray();
	
	/**
	 * Returns an ordered array of all the bundles added.
	 * @return
	 */
	public abstract Bundle[] getBundles();

	/**
	 * A table of the clients' preferences for bundles and the corresponding preference value.
	 * table[bundles][clients]
	 * @return
	 */
	public abstract double[][] preferenceTable();

	/**
	 * Returns a String of a nicely formatted preference table
	 * @return
	 */
	public String toStringTable()
	{
		String[] clients = getClientsArray();
		Bundle[] bundles = getBundles();
		double[][] table = preferenceTable();
		
		String[] bundleStr = new String[bundles.length + 1];
		bundleStr[0] = "";
		for(int i = 0; i < bundles.length; i++)
		{
			bundleStr[i+1] = bundles[i].toString();
		}
		
		String[][] content = new String[table.length][table[0].length];
		for(int i = 0; i < table.length; i++)
		{
			for(int j = 0; j < table[0].length; j++)
			{
				content[i][j] = Double.toString(table[i][j]);
			}
		}
		
		content = TableCreator.transpose(content);
		
		return TableCreator.createTable(bundleStr, clients, content);
	}	

	/**
	 * Sets the clients weight.
	 * @param client
	 * @param weight
	 */
	public abstract void addClientWeight(String client, double weight);

	/**
	 * Returns the weights corresponding to each client
	 * @return
	 */
	public abstract double[] getWeights();

	/**
	 * Gets a clients preference for a bundle
	 * @param client
	 * @param bundle
	 * @return
	 */
	public double getPreference(String client, Bundle bundle)
	{
		String[] clients = this.getClientsArray();
		Bundle[] bundles = this.getBundles();
		double[][] prefTable = this.preferenceTable();
		for(int i = 0; i < clients.length; i++)
		{
			for(int j = 0; j < bundles.length; j++)
			{
				if(bundles[j].compareTo(bundle) == 0 && clients[i].compareTo(client) == 0)
				{
					return prefTable[j][i];
				}
			}
		}
		
		throw new IllegalArgumentException("Client or Bundle does not exist");
	}
}
