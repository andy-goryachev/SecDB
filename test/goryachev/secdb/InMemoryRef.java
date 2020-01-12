// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;
import goryachev.common.util.FH;
import goryachev.common.util.Hex;
import goryachev.secdb.segmented.Ref;


/**
 * In-Memory IRef.
 */
public class InMemoryRef
	implements IRef
{
	private final long seq;
	private final long length;
	
	
	public InMemoryRef(long seq, long length)
	{
		this.seq = seq;
		this.length = length;
	}
	
	
	public long getSeq()
	{
		return seq;
	}

	
	public long getLength()
	{
		return length;
	}
	
	
	public int hashCode()
	{
		int h = FH.hash(Ref.class);
		h = FH.hash(h, seq);
		return FH.hash(h, length);
	}
	
	
	public boolean equals(Object x)
	{
		if(x == this)
		{
			return true;
		}
		else if(x instanceof InMemoryRef)
		{
			InMemoryRef r = (InMemoryRef)x;
			return
				(seq == r.seq) &&
				(length == r.length);
		}
		else
		{
			return false;
		}
	}
	
	
	public String toString()
	{
		return "InMemoryRef:" + Hex.toHexString(seq) + ":" + Hex.toHexString(length);
	}
}
