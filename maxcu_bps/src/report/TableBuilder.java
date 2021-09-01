package report;

import java.util.ArrayList;

/**
 * This class dynamically creates a table
 * @author Kevin
 *
 */
class TableBuilder {
	private ArrayList<ArrayList<String>> table = new ArrayList<ArrayList<String>>();
	
	private int rowIndex;
	
	TableBuilder()
	{
		table.add(new ArrayList<String>());
		rowIndex = 0;
	}
	
	/**
	 * Adds a new line to the table
	 */
	void addNL()
	{
		table.add(new ArrayList<String>());
		rowIndex++;
	}
	
	/**
	 * Adds a line of separators to the table
	 */
	void addSepLine()
	{
		table.add(new ArrayList<String>());
		rowIndex++;
		table.get(rowIndex).add("<->");
		addNL();
	}
	
	/**
	 * Adds an array of values to the current line
	 * @param vals
	 * @param padLeft
	 * @param padRight
	 */
	void addLine(String[] vals, int padLeft, int padRight)
	{
		//table.add(new ArrayList<String>());
		//rowIndex++;
		
		for(int i = 0; i < padLeft; i++)
		{
			table.get(rowIndex).add("");
		}
		
		for(int i = 0; i < vals.length; i++)
		{
			table.get(rowIndex).add(vals[i]);
		}
		
		for(int i = 0; i < padRight; i++)
		{
			table.get(rowIndex).add("");
		}
		
		//addNL();
	}
	
	/**
	 * Appends an entry to the current line
	 * @param val
	 */
	void addEntry(String val)
	{
		table.get(rowIndex).add(val);
	}
	
	/**
	 * Converts the table to a String matrix
	 * @return
	 */
	String[][] create()
	{
		int rowCount = rowIndex+1;
		int colCount = 0;
		
		int temp;
		for(int i = 0; i < rowCount; i++)
		{
			temp = table.get(i).size();
			if(temp > colCount)
				colCount = temp;
		}
		
		String[][] res = new String[rowCount][colCount];
		
		int size;
		boolean isSeparator;
		for(int i = 0; i < res.length; i++)
		{
			size = table.get(i).size();
			isSeparator = false;
			if(size > 0)
			{
				isSeparator = table.get(i).get(0).equals("<->");
			}
			for(int j = 0; j < size; j++)
			{
				res[i][j] = table.get(i).get(j);
			}
			for(int j = size; j < res[0].length; j++)
			{
				if(isSeparator)
				{
					res[i][j] = "<->";
				}
				else
				{
					res[i][j] = "";
				}
			}
		}
		
		return res;
	}
}
