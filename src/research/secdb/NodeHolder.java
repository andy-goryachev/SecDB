// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;


/**
 * Node Holder.
 */
public class NodeHolder
	implements IStored
{
	private final boolean hasValue;
	private final long length;
	private SecNode node;
	
	
	public NodeHolder(boolean hasValue, long length)
	{
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
		// TODO problem: need IStore
		return null;
	}
	
	
	public SecNode getNode() throws Exception
	{
		if(node == null)
		{
			byte[] b = getIStream().readBytes(Integer.MAX_VALUE);
			node = SecNode.read(b);
		}
		return node;
	}
}
