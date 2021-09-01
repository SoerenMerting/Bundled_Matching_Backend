package maxcu;

import java.util.*;


/**
 * This class defines a preference table which can be later converted into an LP.
 * It derives the abstract class PreferenceTable, just like PrefTableManual.
 * Unlike PrefTableManual, the user of the class does not need to know the agents and bundles when calling the constructor.
 * This is especially helpful when creating test instances, where the user of the class does not know yet what bundles and agents will be generated later on.
 * Instead, PrefTableAuto automatically adapts and grows when new instances are added.
 * The downside of this automatic feature is, that the order of input and output may differ (specifically, agents and bundles are ordered).
 * @author Kevin
 */
public class PrefTableAuto extends PreferenceTable{
	private ULList<String> clients;
	private ULList<TimeSlot> timeslots;
	private HashMap<Bundle, HashMap<String, Double>> preferences;
	private HashMap<String, Double> clientWeights;
	
	/**
	 * Initializes the class. The timeslots need to be specified!
	 * @param timeslots
	 */
	public PrefTableAuto(int maxBundleSize)
	{
		super(maxBundleSize);
		//this.maxBundleSize = maxBundleSize;
		preferences = new HashMap<Bundle, HashMap<String, Double>>();
		clients = new ULList<String>();
		timeslots = new ULList<TimeSlot>();
		clientWeights = new HashMap<String, Double>();
	}

	/**
	 * Adds the preferences to the data structure. The key is the client and a bundle.
	 * @param client
	 * @param b
	 * @param value
	 */
	public void addPreference(String client, Bundle b, double value)
	{
		if(b.getElements().length > maxBundleSize)
			throw new IllegalArgumentException("Number of elements in Bundle b exceeds limit");
		
		if(value == 0)
			return;
		
		if(value < 0)
			throw new IllegalArgumentException("Value must be a number > 0; value = " + value);
		
		clients.add(client);
		TimeSlot[] elements = b.getElements();
		for(int i = 0; i < elements.length; i++)
		{
			timeslots.add(elements[i]);
		}
		
		HashMap<String, Double> l = preferences.get(b);
		if(l == null)
		{
			l = new HashMap<String, Double>();
			preferences.put(b, l);
		}
		
		l.put(client, value);
	}
	
	/**
	 * Returns an ordered list without duplicates of all clients added.
	 * @return
	 */
	public ULList<String> getClients()
	{
		return clients;
	}
	
	/**
	 * Returns an ordered array without duplicates of all clients added.
	 * @return
	 */
	public String[] getClientsArray()
	{
		String[] clients = new String[this.clients.count()];
		clients = this.clients.toArray().toArray(clients);
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
		Set<Bundle> bundleSet = preferences.keySet();
		Bundle[] bundles = new Bundle[bundleSet.size()];
		bundles = bundleSet.toArray(bundles);
		java.util.Arrays.sort(bundles);
		return bundles;
	}

	/**
	 * A table of the clients' preferences for bundles and the corresponding preference value.
	 * table[bundles][clients]
	 * @return
	 */
	public double[][] preferenceTable()
	{
		String[] clients = getClientsArray();
		
		Bundle[] bundles = getBundles();
		
		double[][] table = new double[bundles.length][clients.length];
		
		HashMap<String, Double> h = null;
		Double value = (double)0;
		for(int i = 0; i < bundles.length; i++)
		{
			h = preferences.get(bundles[i]);
			for(int j = 0; j < clients.length; j++)
			{
				value = h.get(clients[j]);
				if(value == null)
					table[i][j] = 0;
				else
					table[i][j] = value.intValue();
			}
		}
		
		return table;
	}

	/**
	 * Sets the clients weight.
	 * @param client
	 * @param weight
	 */
	public void addClientWeight(String client, double weight)
	{
		if(clients.getIndex(client) < 0)
			throw new IllegalArgumentException("Client does not exist. Therefore, weight cannot be added");
		
		clientWeights.put(client, new Double(weight));
	}

	/**
	 * Returns the weights corresponding to each client
	 * @return
	 */
	public double[] getWeights()
	{
		ArrayList<String> c = clients.toArray();
		String[] arr = new String[c.size()];
		arr = c.toArray(arr);
		java.util.Arrays.sort(arr);
		
		double[] res = new double[arr.length];
		for(int i = 0; i < arr.length; i++)
		{
			Double val = clientWeights.get(arr[i]);
			res[i] = val == null ? 1 : val.doubleValue();
		}
		
		return res;
	}
}
