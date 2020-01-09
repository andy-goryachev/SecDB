// Copyright © 2019-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.impl;
import goryachev.common.util.SKey;
import goryachev.secdb.Ref;
import goryachev.secdb.bplustree.BPlusTreeNode;


/**
 * Node Holder.
 */
public class NodeHolder
{
	private DataHolder dataHolder;
	private BPlusTreeNode<SKey,DataHolder> node;

	
	public NodeHolder(DataHolder h)
	{
		this.dataHolder = h;
	}
	

	public NodeHolder(BPlusTreeNode<SKey,DataHolder> node)
	{
		this.node = node;
	}

	
	public BPlusTreeNode<SKey,DataHolder> getNode() throws Exception
	{
		if(node == null)
		{
			byte[] b = dataHolder.getIStream().readBytes(Integer.MAX_VALUE);
			node = SecIO.read(dataHolder.getIStore(), b);
		}
		return node;
	}


	public Ref getRef()
	{
		return dataHolder.getRef();
	}


	public boolean isModified()
	{
		if(node == null)
		{
			return false;
		}
		else
		{
			return node.isModified();
		}
	}
}
