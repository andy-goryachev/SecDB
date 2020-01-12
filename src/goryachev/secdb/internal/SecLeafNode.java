// Copyright © 2019-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.internal;
import goryachev.common.util.SKey;
import goryachev.secdb.IRef;
import goryachev.secdb.IStore;
import goryachev.secdb.bplustree.BPlusTreeNode;


/**
 * SecDB LeafNode.
 */
public class SecLeafNode<R extends IRef>
	extends BPlusTreeNode.LeafNode<SKey,DataHolder<R>>
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
	
	
	protected LeafNode<SKey,DataHolder<R>> newLeafNode()
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