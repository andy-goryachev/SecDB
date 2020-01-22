// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented.log;


/**
 * Timestamp = java time + sequence.
 */
public class TStamp
{
	public final long sequence;
	public final long time;
	
	
	public TStamp(long sequence, long time)
	{
		this.sequence = sequence;
		this.time = time;
	}
}
