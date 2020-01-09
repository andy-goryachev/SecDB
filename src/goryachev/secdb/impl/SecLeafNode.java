// Copyright © 2019-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.impl;
import goryachev.common.util.SKey;
import goryachev.secdb.IStore;
import goryachev.secdb.bplustree.BPlusTreeNode;


/**
 * SecDB LeafNode.
 */
public class SecLeafNode
	extends BPlusTreeNode.LeafNode<SKey,DataHolder>
{
	private final IStore store;

	
	public SecLeafNode(IStore store)
	{
		this.store = store;
	}
	
	
	public SecLeafNode modified()
	{
		setModified();
		return this;
	}
	
	
	protected LeafNode<SKey,DataHolder> newLeafNode()
	{
		return new SecLeafNode(store).modified();
	}
	
	
	protected InternalNode newInternalNode()
	{
		return new SecInternalNode(store).modified();
	}


	protected void addValue(DataHolder d)
	{
		values.add(d);
	}


	public int getValueCount()
	{
		return values.size();
	}


	public DataHolder valueAt(int ix)
	{
		return values.get(ix);
	}
}