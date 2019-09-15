// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;


/**
 * Reference.
 */
public class Ref
{
	private final String segment;
	private final long offset;
	private final long length;


	public Ref(String segment, long offset, long length)
	{
		this.segment = segment;
		this.offset = offset;
		this.length = length;
	}
}
