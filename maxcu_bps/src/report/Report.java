package report;

import java.util.*;

/**
 * Dynamically creates a report
 * @author Kevin
 *
 */
public class Report {
	private final ArrayList<Content> protocol = new ArrayList<Content>();
	private final boolean on;
	private int chapters = 1;
	public static int decimalPLaces = 4;
	
	private TableBuilder tb = null;
	
	public Report(boolean on)
	{
		this.on = on;
	}
	
	/**
	 * States, whether the report is switched on
	 * @return
	 */
	public boolean isOn()
	{
		return on;
	}
	
	/**
	 * Opens a table. Use this method before using the table functions
	 */
	public void openTable()
	{
		if(!on)
			return;
		
		if(tb == null)
			tb = new TableBuilder();
		else
			throw new IllegalStateException("Close the former table first");
	}
	
	/**
	 * Dynamically appends an entry to the current line of the table.
	 * Use openTable() before using this method
	 * @param e
	 */
	public void addTableEntry(String e)
	{
		if(!on)
			return;
		
		tb.addEntry(e);
	}
	
	public void addTableEntry(double e)
	{
		addTableEntry(Double.toString(round(e, decimalPLaces)));
		//addTableEntry(Double.toString(e));
	}
	
	public static double round(double value)
	{
		return round(value, decimalPLaces);
	}
	public static double round(double value, int places) {
		if(places < 0)
			throw new IllegalArgumentException("Round: use a positive amount of decimal places");
		
		value = Math.round(value * Math.pow(10, places));
	    
	    return value / Math.pow(10, places);
	}
	
	/**
	 * Adds a new line to the current table
	 * Use openTable() before
	 */
	public void addTableNL()
	{
		if(!on)
			return;
		
		tb.addNL();
	}
		
	/**
	 * Adds a line of dashes to the current table
	 * Use openTable() before
	 */
	public void addTableSeparator()
	{
		if(!on)
			return;
		
		tb.addSepLine();
	}
	
	/**
	 * Adds several values to the current line in the current table
	 * Use openTable() before
	 */
	public void addTableEntries(String[] es, int padLeft, int padRight)
	{
		if(!on)
			return;
		
		tb.addLine(es, padLeft, padRight);
	}
	
	/**
	 * Adds several values to the current line in the current table
	 * Use openTable() before
	 */
	public void addTableEntries(double[] es, int padLeft, int padRight)
	{
		if(!on)
			return;
		
		String[] vals = new String[es.length];
		for(int i = 0; i < vals.length; i++)
		{
			vals[i] = Double.toString(round(es[i],decimalPLaces));
		}
		
		tb.addLine(vals, padLeft, padRight);
	}
	
	/**
	 * Adds several values to the current line in the current table
	 * Use openTable() before
	 */
	public void addTableEntries(Object[] es, int padLeft, int padRight)
	{
		if(!on)
			return;
		
		String[] vals = new String[es.length];
		for(int i = 0; i < vals.length; i++)
		{
			vals[i] = es[i].toString();
		}
		
		tb.addLine(vals, padLeft, padRight);
	}
	
	/**
	 * Closes the current table
	 */
	public void closeTable()
	{
		closeTable("|");
	}
	
	
	public void closeTable(String sep)
	{
		if(!on)
			return;
		
		protocol.add(new Table(tb.create(), sep));
		tb = null;
	}
	
	/**
	 * Adds a paragraph to the report
	 * @param p
	 */
	public void addParagraph(String p)
	{
		if(!on)
			return;
		
		protocol.add(new Paragraph(p));
	}
	
	public void addParagraph(double p)
	{
		addParagraph(Double.toString(round(p, decimalPLaces)));
	}
	
	/**
	 * Closes the current chapter
	 */
	public void closeChapter()
	{
		if(!on)
			return;
		
		if(protocol.size() == 0 || protocol.get(protocol.size() -1) instanceof ChapterEnd)
			return;
		
		protocol.add(new ChapterEnd());
		chapters++;
	}
	
	/**
	 * Returns the amount of chapters
	 * @return
	 */
	public int getNumChapters()
	{
		return chapters;
	}
	
	/**
	 * Gets the entire chapter with index i
	 * @param i
	 * @return
	 */
	public String getChapter(int i)
	{
		ArrayList<Content> chapter = getChapterContent(i);
		StringBuilder res = new StringBuilder();
		
		for(int k = 0; k < chapter.size(); k++)
		{
			res.append(chapter.get(k).toString());
			res.append("\n");
		}
		
		return res.toString();
	}
	
	/**
	 * Returns all the elements of chapter with index i
	 * @param i
	 * @return
	 */
	private ArrayList<Content> getChapterContent(int i)
	{
		if(i >= chapters)
			throw new IndexOutOfBoundsException();
		
		int protIndex = 0;
		int counter = 0;
		for(int k = 0; k < protocol.size(); k++)
		{
			if(counter >= i)
			{
				protIndex = k;
				break;
			}
			if(protocol.get(k) instanceof ChapterEnd)
			{
				counter++;
				protIndex = k;
			}
		}
		
		ArrayList<Content> res = new ArrayList<Content>();
		
		for(int k = protIndex; k < protocol.size(); k++)
		{
			if(protocol.get(k) instanceof ChapterEnd)
			{
				if(res.size() == 0)
					res.add(protocol.get(k));
				break;
			}
			res.add(protocol.get(k));
		}
		
		return res;
	}
	
	/**
	 * Appends another report to this one
	 * @param r
	 */
	public void appendReport(Report r)
	{
		if(!on)
			return;
		
		this.closeChapter();
		
		for(int i = 0; i < r.protocol.size(); i++)
		{
			this.protocol.add(r.protocol.get(i));
		}
		this.chapters += r.chapters;
	}
	
	public static void main(String[] args)
	{
		Report r = new Report(true);
		
		r.openTable();
		r.addTableEntry("bla");
		r.addTableEntry("keks");
		r.addTableEntry("sau");
		
		r.addTableNL();
		
		r.addTableEntry("dies");
		r.addTableEntry("ist");
		r.addTableEntry("ein");
		
		r.addTableSeparator();
		
		r.addTableEntry("test");
		r.addTableEntry("viele schöne grüße");
		r.addTableEntry("sau");
		
		r.closeTable();
		
		r.addParagraph("Dies ist ein paragraph");
		
		
		
		r.closeChapter();
		r.closeChapter();
		
		r.addParagraph("blablalblalbal");
		
		r.addParagraph("Dies ist ein paragraph");
		
		
		r.closeChapter();
		
		r.addParagraph("bla2222");
		
		r.addParagraph("Dies ist ein paragraph");
		
		
		r.closeChapter();
		
		r.addParagraph("bla3333");
		
		r.addParagraph("Dies ist ein paragraph");
		
		
		//r.closeChapter();
		
		System.out.println(r.getChapter(3));
	}
}
