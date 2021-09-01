 package maxcu;

import maxcu.ULList;

/**
 * This class defines a preference table which can be later converted into an LP.
 * It derives the abstract class PreferenceTable, just like PrefTableAuto.
 * Unlike PrefTableAuto, the user of this class has to pass a list of future bundles and agents to the constructor.
 * These lists define the structure of the the preference table.
 * @author Kevin
 */
public class PrefTableManual extends PreferenceTable {
	private final ULList<TimeSlot> timeslots;
	private final String[] clients;
	private final Bundle[] bundles;
	private final double[][] preferences;		//prefernces[bundles/row][clients/col]
	private final double[] clientWeights;
	
	/**
	 * Initializes the class.
	 * @param timeslots
	 */
	public PrefTableManual(int maxBundleSize, String[] clients, Bundle[] bundles)
	{
		super(maxBundleSize);
		//this.maxBundleSize = maxBundleSize;
		this.clients = clients;
		timeslots = new ULList<TimeSlot>();
		clientWeights = new double[clients.length];
		for(int i = 0; i < clientWeights.length; i++)
		{
			clientWeights[i] = 1;
		}
		
		this.bundles = bundles;
		
		this.preferences = new double[bundles.length][clients.length];
		
		for(int i = 0; i < bundles.length; i++)
		{
			TimeSlot[] ts = bundles[i].getElements();
			if(ts.length > maxBundleSize)
				throw new IllegalArgumentException("A bundle is bigger than the allowed size");
			
			for(int j = 0; j < ts.length; j++)
			{
				timeslots.add(ts[j]);
			}
		}
	}

	/**
	 * Adds the preferences to the data structure. The key is the client and a bundle.
	 * @param client
	 * @param b
	 * @param value
	 */
	public void addPreference(String client, Bundle b, double value)
	{
		int clnt = -1;
		int bndl = -1;
		for(int i = 0; i < clients.length; i++)
		{
			if(client.equals(clients[i]))
			{
				clnt = i;
				break;
			}
		}
		if(clnt == -1)
			throw new IllegalArgumentException("Client not found");
		
		for(int i = 0; i < bundles.length; i++)
		{
			if(b.equals(bundles[i]))
			{
				bndl = i;
				break;
			}
		}
		if(bndl == -1)
			throw new IllegalArgumentException("Bundle not found");
		
		preferences[bndl][clnt] = value;
	}
	
	/**
	 * Returns an ordered array without duplicates of all clients added.
	 * @return
	 */
	public String[] getClientsArray()
	{
		return clients;
	}
	
	/**
	 * Returns an ordered list without duplicates of all TimeSlots added.
	 * @return
	 */
	public ULList<TimeSlot> getTimeSlots()
	{
		return timeslots;
	}
	
	/**
	 * Returns an ordered array without duplicates of all TimeSlots added.
	 * @return
	 */
	public TimeSlot[] getTimeSlotsArray()
	{
		TimeSlot[] ts = new TimeSlot[this.timeslots.count()];
		ts = this.timeslots.toArray().toArray(ts);
		return ts;
	}
	
	/**
	 * Returns an ordered array of all the bundles added.
	 * @return
	 */
	public Bundle[] getBundles()
	{
		return bundles;
	}

	/**
	 * A table of the clients' preferences for bundles and the corresponding preference value.
	 * table[bundles][clients]
	 * @return
	 */
	public double[][] preferenceTable()
	{
		return preferences;
	}

	/**
	 * Sets the clients weight.
	 * @param client
	 * @param weight
	 */
	public void addClientWeight(String client, double weight)
	{
		int clnt = -1;
		
		for(int i = 0; i < clients.length; i++)
		{
			if(client.equals(clients[i]))
			{
				clnt = i;
				break;
			}
		}
		
		if(clnt < 0)
			throw new IllegalArgumentException("Client does not exist. Therefore, weight cannot be added");
		
		clientWeights[clnt] = weight;
	}

	/**
	 * Returns the weights corresponding to each client
	 * @return
	 */
	public double[] getWeights()
	{
		return clientWeights;
	}
}
