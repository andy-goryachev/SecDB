// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.impl;
import goryachev.common.util.CList;
import goryachev.common.util.SKey;
import goryachev.secdb.IStore;
import goryachev.secdb.bplustree.BPlusTreeNode;


/**
 * SecDB InternalNode.
 */
public class SecInternalNode
	extends BPlusTreeNode.InternalNode<SKey,DataHolder>
{
	private final IStore store;
	protected final CList<NodeHolder> children = new CList();
	
	
	public SecInternalNode(IStore store)
	{
		this.store = store;
	}
	
	
	protected SecInternalNode modified()
	{
		setModified();
		return this;
	}
	
	
	protected void addChild(BPlusTreeNode<SKey,DataHolder> n)
	{
		children.add(new NodeHolder(n));
	}


	protected int getChildCount()
	{
		return children.size();
	}


	protected BPlusTreeNode<SKey,DataHolder> childAt(int ix) throws Exception
	{
		NodeHolder h = children.get(ix);
		return h.getNode();
	}


	protected void removeChildAt(int ix)
	{
		children.remove(ix);
	}


	protected void setChild(int ix, BPlusTreeNode<SKey,DataHolder> n)
	{
		children.set(ix, new NodeHolder(n));
	}


	protected void addChild(int ix, BPlusTreeNode<SKey,DataHolder> n)
	{
		children.add(ix, new NodeHolder(n));
	}
	
	
	protected LeafNode<SKey,DataHolder> newLeafNode()
	{
		return new SecLeafNode(store).modified();
	}
	
	
	protected InternalNode newInternalNode()
	{
		return new SecInternalNode(store).modified();
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