// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;


/**
 * Node Holder.
 */
public class NodeHolder
	implements IStored
{
	private SecNode node;
	
	
	public NodeHolder()
	{
	}
	
	
	public boolean hasValue()
	{
		// TODO
		return false;
	}


	public long getLength()
	{
		// TODO
		return 0;
	}


	public IStream getIStream()
	{
		// TODO
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
