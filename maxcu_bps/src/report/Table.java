package report;

/**
 * Transforms a matrix of strings to a good looking table
 * @author Kevin
 *
 */
class Table extends Content{
	private final String[][] table; //table[row][col]
	private final String sep;
	
	Table(String[][] table)
	{
		this(table, "|");
	}
	
	Table(String[][] table, String sep)
	{
		this.table = table;
		this.sep = sep;
	}
	
	/**
	 * Pads a string value with characters by the specified length
	 * @param val
	 * @param length
	 * @param pad
	 * @return
	 */
	private String pad(String val, int length, char pad)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(val);
		
		for(int i = val.length(); i < length; i++)
		{
			sb.append(pad);
		}
		
		return sb.toString();
	}
	
	/**
	 * Pads a string value with characters by the specified length
	 * @param val
	 * @param length
	 * @return
	 */
	private String pad(String val, int length)
	{
		return this.pad(val, length, ' ');
	}

	/**
	 * Returns the maximum length of all the items within a column
	 * @param i
	 * @return
	 */
	private int maxSizeOfCol(int i)
	{
		int res = 0;
		
		int size = 0;
		String val;
		for(int j = 0; j < table.length; j++)
		{
			val = table[j][i];
			if(val.equals("<->"))
				size = 0;
			else
				size = val.length();
			if(size > res)
				res = size;
		}
		
		return res;
	}
	
	/**
	 * Returns the maximum size of entries for each column
	 * @return
	 */
	private int[] getColSizes()
	{
		int[] res = new int[table[0].length];
		
		for(int i = 0; i < res.length; i++)
		{
			res[i] = maxSizeOfCol(i);
		}
		
		return res;
	}
	
	/**
	 * Returns the table with padding, so that each column has the same size
	 * @return
	 */
	String[][] getPaddedTable()
	{
		String[][] res = new String[table.length][table[0].length];
		int[] colSizes = getColSizes();
		
		for(int i = 0; i < res.length; i++)
		{
			for(int j = 0; j < res[0].length; j++)
			{
				if(table[i][j].equals("<->"))
				{
					res[i][j] = pad("-", colSizes[j], '-');
				}
				else
				{
					res[i][j] = pad(table[i][j], colSizes[j]);
				}
			}
		}
		
		return res;
	}
	
	@Override
	public String toString() {
		String[][] tab = getPaddedTable();
		
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < tab.length; i++)
		{
			for(int j = 0; j < tab[0].length;j++)
			{
				sb.append(tab[i][j]);
				sb.append(sep);
			}
			sb.append("\n");
		}
		
		return sb.toString();
	}
}
