// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import java.io.IOException;
import java.io.InputStream;


/**
 * InputStream reads data from the database SegmentFiles.
 */
public class SecStream
	extends InputStream
{
	protected final SecStore store;
	protected final Ref ref;
	
	
	public SecStream(SecStore store, Ref ref)
	{
		this.store = store;
		this.ref = ref;
	}


	public int read() throws IOException
	{
		// TODO 
		throw new Error();
	}


	public int read(byte b[], int off, int len) throws IOException
	{
		// TODO 
		throw new Error();
	}


	public void close() throws IOException
	{
		// TODO
	}
}
