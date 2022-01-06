// Copyright © 2019-2022 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.internal;
import goryachev.common.util.CList;
import goryachev.common.util.SKey;
import goryachev.secdb.IRef;
import goryachev.secdb.IStore;
import goryachev.secdb.bplustree.BPlusTreeNode;
import goryachev.secdb.bplustree.InternalNode;
import goryachev.secdb.bplustree.LeafNode;


/**
 * DBEngine InternalNode.
 */
public class DBInternalNode<R extends IRef>
	extends InternalNode<SKey,DataHolder<R>>
{
	private final IStore<R> store;
	protected final CList<NodeHolder> children = new CList();
	
	
	public DBInternalNode(IStore store)
	{
		this.store = store;
	}
	
	
	public static DBInternalNode createModified(IStore store)
	{
		DBInternalNode n = new DBInternalNode(store);
		n.setModified();
		return n;
	}
	
	
	protected void addChild(BPlusTreeNode<SKey,DataHolder<R>> n)
	{
		children.add(new NodeHolder(n));
	}


	protected int getChildCount()
	{
		return children.size();
	}


	protected BPlusTreeNode<SKey,DataHolder<R>> childAt(int ix) throws Exception
	{
		NodeHolder h = children.get(ix);
		return h.getNode();
	}


	protected void removeChildAt(int ix)
	{
		children.remove(ix);
	}


	protected void setChild(int ix, BPlusTreeNode<SKey,DataHolder<R>> n)
	{
		children.set(ix, new NodeHolder(n));
	}


	protected void addChild(int ix, BPlusTreeNode<SKey,DataHolder<R>> n)
	{
		children.add(ix, new NodeHolder(n));
	}
	
	
	protected LeafNode<SKey,DataHolder<R>> newLeafNode()
	{
		return DBLeafNode.createModified(store);
	}
	
	
	protected InternalNode newInternalNode()
	{
		return DBInternalNode.createModified(store);
	}


	protected void addChild(DataHolder d)
	{
		NodeHolder h = new NodeHolder(d);
		children.add(h);
	}


	public NodeHolder nodeHolderAt(int ix)
	{
		return children.get(ix);
	}
}