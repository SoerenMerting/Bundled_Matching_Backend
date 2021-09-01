package report;

/**
 * Represents a paragraph consisting of a string
 * @author Kevin
 *
 */
class Paragraph extends Content{
	private final String p;
	
	/**
	 * Creates an instance
	 * @param content
	 */
	Paragraph(String content)
	{
		this.p = content;
	}

	@Override
	public String toString() {
		return p;
	}
}
