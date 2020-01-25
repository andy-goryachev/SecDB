// Copyright © 2019-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.util.CKit;
import goryachev.common.util.FH;
import goryachev.common.util.Hex;
import goryachev.common.util.Parsers;
import goryachev.common.util.SB;
import goryachev.secdb.IRef;


/**
 * SecDB Stored Object Reference.
 * 
 * TODO span multiple segments
 */
public class Ref
	implements IRef
{
	private static final char SEP = '.';
	private final String segment;
	private final long offset;
	private final long length;
	private final byte[] dataKey;


	public Ref(String segment, long offset, long length, byte[] dataKey)
	{
		this.segment = segment;
		this.offset = offset;
		this.length = length;
		this.dataKey = dataKey;
	}
	
	
	/** see parse() */
	public String toPersistentString()
	{
		SB sb = new SB();
		sb.a(segment);
		sb.a(SEP);
		sb.a(offset);
		sb.a(SEP);
		sb.a(length);
		sb.a(SEP);
		if(dataKey != null)
		{
			sb.a(Hex.toHexString(dataKey));
		}
		return sb.toString();
	}
	
	
	/** see toPersistentString() */
	public static Ref parse(String text) throws Exception
	{
		if(text == null)
		{
			return null;
		}
		
		String[] ss = CKit.split(text, SEP);
		if(ss.length != 4)
		{
			throw new Exception("not a Ref: " + text);
		}
		
		String seg = ss[0];
		long off = Long.parseLong(ss[1]);
		long len = Parsers.parseLong(ss[2], -1);
		byte[] key = Parsers.parseByteArray(ss[3]);
		return new Ref(seg, off, len, key);
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
	
	
	public byte[] getDataKey()
	{
		return dataKey;
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
