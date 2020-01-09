// Copyright © 2019-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;
import goryachev.common.util.CKit;
import goryachev.common.util.FH;
import goryachev.common.util.Hex;


/**
 * SecDB Stored Object Reference.
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
	
	
	public String toString()
	{
		return "Ref:" + segment + ":" + Hex.toHexString(offset) + ":" + Hex.toHexString(length);
	}
	
	
	public String getSegment()
	{
		return segment;
	}
	
	
	public long getOffset()
	{
		return offset;
	}
	
	
	public long getLength()
	{
		return length;
	}
	
	
	public int hashCode()
	{
		int h = FH.hash(Ref.class);
		h = FH.hash(h, segment);
		h = FH.hash(h, offset);
		return FH.hash(h, length);
	}
	
	
	public boolean equals(Object x)
	{
		if(x == this)
		{
			return true;
		}
		else if(x instanceof Ref)
		{
			Ref r = (Ref)x;
			return
				(offset == r.offset) &&
				(length == r.length) &&
				CKit.equals(segment, r.segment);
		}
		else
		{
			return false;
		}
	}
}
