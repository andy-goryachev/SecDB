// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented.REMOVE;
import goryachev.common.log.Log;
import goryachev.common.util.Hex;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Debug Input Stream.
 */
@Deprecated // TODO remove
public class DebugInputStream
	extends InputStream
{
	private final String name;
	private final int limit;
	private final InputStream in;
	private final ByteArrayOutputStream bytes;
	protected static final Log log = Log.get("DebugInputStream");
	
	
	public DebugInputStream(String name, int limit, InputStream in)
	{
		this.name = name;
		this.limit = limit;
		this.in = in;
		bytes = new ByteArrayOutputStream();
	}
	
	
	public int read() throws IOException
	{
		int rv = in.read();
		if(rv >= 0)
		{
			if(bytes.size() < limit)
			{
				bytes.write(rv);
			}
		}
		return rv;
	}


	public int read(byte[] buf, int off, int len) throws IOException
	{
		int rv = in.read(buf, off, len);
		if(rv > 0)
		{
			int sz = Math.min(rv, limit - bytes.size());
			if(sz > 0)
			{
				bytes.write(buf, off, sz);
			}
		}
		return rv;
	}


	public void close() throws IOException
	{
		in.close();
		log.debug(name + "\n" + Hex.toHexStringASCII(bytes.toByteArray()));
	}
}
