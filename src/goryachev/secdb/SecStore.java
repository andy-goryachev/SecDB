// Copyright © 2019-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;
import goryachev.common.util.CMap;
import goryachev.secdb.impl.SegmentFile;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;


/**
 * Encrypted Local Segmented Store.
 * 
 * - stores encrypted blocks in append-only files named segments
 * - (long) data values are encrypted with a random key to enable sharing
 * 
 */
public class SecStore
	implements Closeable,IStore<Ref>
{
	private final File dir;
	private final CMap<String,SegmentFile> segments = new CMap();
	private Ref root;
	
	
	public SecStore(File dir)
	{
		this.dir = dir;
	}
	
	
	// TODO OpaqueString
	public void open(char[] passphrase) throws Exception
	{
		// TODO unlock the key file
		// load descriptor
		// check segments
		// read root ref
		// load root node and do some checks
	}


	public void close() throws IOException
	{
		// TODO
		// zero out the main key
		// clear all buffers
	}


	public Ref getRootRef()
	{
		return root;
	}


	public void setRootRef(Ref ref) throws Exception
	{
		// TODO synchronize
		root = ref;
	}


	public Ref store(IStream in, boolean isTree) throws Exception
	{
		long len = in.getLength();
		
		// TODO pick a segment
		// get offset
		// nonce = segment + offset
		// if isTree, use the main key
		// if !isTree, generate a random data key
		// TODO write, update heads
		// on failure: reset heads?
		// store data key in the ref
		
		return null;
	}


	public IStream load(Ref ref) throws Exception
	{
		String seg = ref.getSegment();
		long off = ref.getOffset();
		long len = ref.getLength();
		// TODO data key
		// TODO need to explicitly clear the ref (because of the data key)
		
		return null;
	}
}
