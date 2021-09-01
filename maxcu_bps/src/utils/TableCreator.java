package utils;

/**
 * Class for creating good looking tables for files or console
 * @author Kevin
 *
 */
public class TableCreator {
	
	/**
	 * Creates a string which displays a table while taking different string sizes into account
	 * @param columnNames
	 * @param rowNames
	 * @param table
	 * @return
	 */
	public static String createTable(String[] columnNames, String[] rowNames, String[][] table)
	{
		if(columnNames.length != table[0].length + 1)
			throw new IllegalArgumentException("Size of column names and table do not match. Beware, that the first column name is above the row names");
		
		if(rowNames.length != table.length)
			throw new IllegalArgumentException("Size of row names and table do not match.");
		
		StringBuilder sb = new StringBuilder();
		int columnSize[] = getColumnSize(columnNames, rowNames, table);
		
		//create header!
		int size = 0;
		for(int i = 0; i < columnNames.length; i++)
		{
			size = columnNames[i].length();
			sb.append(columnNames[i].toString());
			sb.append(getSpaces(columnSize[i] - size));
			sb.append('|');
		}
		sb.append('\n');
		
		// draw separator
		for(int i = 0; i < columnSize.length; i++)
		{
			sb.append(repeatCharacter(columnSize[i], '-'));
			sb.append('|');
		}
		sb.append('\n');
		
		// draw table
		for(int i = 0; i < rowNames.length; i++)
		{
			size = rowNames[i].length();
			sb.append(rowNames[i]);
			sb.append(getSpaces(columnSize[0] - size));
			sb.append('|');
			for(int j = 1; j < columnNames.length; j++)
			{
				size = table[i][j-1].length();
				sb.append(table[i][j-1]);
				sb.append(getSpaces(columnSize[j] - size));
				sb.append('|');
			}
			sb.append('\n');
		}
		
		return sb.toString();
	}
	
	/**
	 * Returns the size of each column
	 * @param columnNames
	 * @param rowNames
	 * @param table
	 * @return
	 */
	private static int[] getColumnSize(String[] columnNames, String[] rowNames, String[][] table)
	{
		int[] columnSize = new int[columnNames.length];
		
		int max = 0;
		int temp = 0;
		for(int i = 0; i < rowNames.length; i++)
		{
			temp = rowNames[i].length();
			if(temp > max)
				max = temp;
		}
		temp = columnNames[0].length();
		if(temp > max)
			max = temp;
		columnSize[0] = max;
		
		for(int i = 1; i < columnNames.length; i++)
		{
			max = 0;
			for(int j = 0; j < rowNames.length; j++)
			{
				temp = table[j][i-1].length();
				if(temp > max)
					max = temp;
			}
			temp = columnNames[i].length();
			if(temp > max)
				max = temp;
			columnSize[i] = max;
		}
		
		
		return columnSize;
	}
	
	/**
	 * Transposes a table. Can be used for example when you internally use a different orientation than is required from the createTable(..) method
	 * @param table
	 * @return
	 */
	public static String[][] transpose(String[][] table)
	{
		String[][] temp = new String[table[0].length][table.length];
		
		for(int i = 0; i < table.length; i++)
		{
			for(int j = 0; j < table[0].length; j++)
			{
				temp[j][i] = table[i][j];
			}
		}
		
		return temp;
	}

	/**
	 * Repeats n times the space character
	 * @param n
	 * @return
	 */
	private static String getSpaces(int n)
	{
		return repeatCharacter(n, ' ');
	}
	
	/**
	 * Repeats n times the character c
	 * @param n
	 * @param c
	 * @return
	 */
	private static String repeatCharacter(int n, char c)
	{
		StringBuilder b = new StringBuilder();
		
		for(int i = 0; i < n; i++)
		{
			b.append(c);
		}
		
		return b.toString();
	}
}
