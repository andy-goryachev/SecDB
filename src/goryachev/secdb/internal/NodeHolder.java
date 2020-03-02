// Copyright © 2019-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.internal;
import goryachev.common.util.D;
import goryachev.common.util.SKey;
import goryachev.secdb.IRef;
import goryachev.secdb.bplustree.BPlusTreeNode;
import goryachev.secdb.util.Utils;


/**
 * DBEngine Node Holder.
 */
public class NodeHolder<R extends IRef>
{
	private DataHolder<R> dataHolder;
	private BPlusTreeNode<SKey,DataHolder<R>> node;

	
	public NodeHolder(DataHolder<R> h)
	{
		this.dataHolder = h;
	}
	

	public NodeHolder(BPlusTreeNode<SKey,DataHolder<R>> node)
	{
		this.node = node;
	}

	
	public BPlusTreeNode<SKey,DataHolder<R>> getNode() throws Exception
	{
		if(node == null)
		{
			byte[] b = Utils.readBytes(dataHolder.getStoredValue().getIStream(), Integer.MAX_VALUE);
			node = DBEngineIO.read(dataHolder.getIStore(), b);
		}
		return node;
	}


	public R getRef()
	{
		if(dataHolder == null)
		{
			D.p(); // FIX remove
		}
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
