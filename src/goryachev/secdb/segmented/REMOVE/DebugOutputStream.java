// Copyright © 2020-2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented.REMOVE;
import goryachev.common.log.Log;
import goryachev.common.util.Hex;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Debug OutputStream.
 */
@Deprecated // TODO remove
public class DebugOutputStream
	extends OutputStream
{
	private final String name;
	private final int limit;
	private final OutputStream out;
	private final ByteArrayOutputStream bytes;
	protected static final Log log = Log.get("DebugOutputStream");
	
	
	public DebugOutputStream(String name, int limit, OutputStream out)
	{
		this.name = name;
		this.limit = limit;
		this.out = out;
		bytes = new ByteArrayOutputStream();
	}
	
	
	public void write(int b) throws IOException
	{
		out.write(b);
		if(bytes.size() < limit)
		{
			bytes.write(b);
		}
	}


	public void write(byte[] buf, int off, int len) throws IOException
	{
		out.write(buf, off, len);
		int sz = Math.min(len, limit - bytes.size());
		if(sz > 0)
		{
			bytes.write(buf, off, sz);
		}
	}


	public void close() throws IOException
	{
		out.close();
		log.debug(name + "\n" + Hex.toHexStringASCII(bytes.toByteArray()));
	}
}
