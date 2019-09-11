// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;


/**
 * Node Holder.
 */
public class NodeHolder
	implements IStored
{
	private final IStore store;
	private final boolean hasValue;
	private final long length;
	private SecNode node;
	
	
	public NodeHolder(IStore store, boolean hasValue, long length)
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
	
	
	public SecNode getNode() throws Exception
	{
		if(node == null)
		{
			byte[] b = getIStream().readBytes(Integer.MAX_VALUE);
			node = SecNode.read(store, b);
		}
		return node;
	}
}
