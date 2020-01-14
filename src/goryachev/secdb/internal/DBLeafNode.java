// Copyright © 2019-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.internal;
import goryachev.common.util.SKey;
import goryachev.secdb.IRef;
import goryachev.secdb.IStore;
import goryachev.secdb.bplustree.BPlusTreeNode;


/**
 * DBEngine LeafNode.
 */
public class DBLeafNode<R extends IRef>
	extends BPlusTreeNode.LeafNode<SKey,DataHolder<R>>
{
	private final IStore store;

	
	public DBLeafNode(IStore store)
	{
		this.store = store;
	}
	
	
	public DBLeafNode modified()
	{
		setModified();
		return this;
	}
	
	
	protected LeafNode<SKey,DataHolder<R>> newLeafNode()
	{
		return new DBLeafNode(store).modified();
	}
	
	
	protected InternalNode newInternalNode()
	{
		return new DBInternalNode(store).modified();
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