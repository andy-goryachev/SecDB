// Copyright © 2019-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.internal;
import goryachev.common.util.SKey;
import goryachev.secdb.IRef;
import goryachev.secdb.IStore;
import goryachev.secdb.bplustree.InternalNode;
import goryachev.secdb.bplustree.LeafNode;


/**
 * DBEngine LeafNode.
 */
public class DBLeafNode<R extends IRef>
	extends LeafNode<SKey,DataHolder<R>>
{
	private final IStore store;

	
	public DBLeafNode(IStore store)
	{
		this.store = store;
	}
	
	
	public static DBLeafNode createModified(IStore store)
	{
		DBLeafNode n = new DBLeafNode(store);
		n.setModified();
		return n;
	}
	
	
	protected LeafNode<SKey,DataHolder<R>> newLeafNode()
	{
		return createModified(store);
	}
	
	
	protected InternalNode newInternalNode()
	{
		return DBInternalNode.createModified(store);
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