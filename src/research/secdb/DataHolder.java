// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;
import goryachev.common.util.SKey;
import research.bplustree.BPlusTreeNode;


/**
 * Data Holder: stores a reference to and, if possible, the cached value of
 * - short inline value
 * - BPlusTreeNode
 * - large object (reference only)
 */
public class DataHolder
	implements IStored
{
	private final IStore store;
	private final boolean hasValue;
	private final long length;
	private BPlusTreeNode<SKey,DataHolder> node;
	
	
	public DataHolder(IStore store, boolean hasValue, long length)
	{
		this.store = store;
		this.hasValue = hasValue;
		this.length = length;
	}
	
	
	public boolean hasValue()
	{
		return hasValue;
	}


	public long getLength()
	{
		return length;
	}


	public IStream getIStream()
	{
		return null;
	}
	
	
	public BPlusTreeNode<SKey,DataHolder> getNode() throws Exception
	{
		if(node == null)
		{
			byte[] b = getIStream().readBytes(Integer.MAX_VALUE);
			node = SecIO.read(store, b);
		}
		return node;
	}
}
