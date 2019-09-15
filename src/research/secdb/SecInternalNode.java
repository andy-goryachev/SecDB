// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;
import goryachev.common.util.CList;
import goryachev.common.util.SKey;
import java.util.List;
import research.bplustree.BPlusTreeNode;


/**
 * SecDB InternalNode.
 */
public class SecInternalNode
	extends BPlusTreeNode.InternalNode<SKey,DataHolder>
{
	private final IStore store;
	protected final List<BPlusTreeNode<SKey,DataHolder>> children;
	
	
	public SecInternalNode(IStore store)
	{
		this.store = store;
		children = new CList();
	}
	
	
	protected void addChild(BPlusTreeNode<SKey,DataHolder> n)
	{
		children.add(n);
	}


	protected int getChildCount()
	{
		return children.size();
	}


	protected BPlusTreeNode<SKey,DataHolder> childAt(int ix)
	{
		return children.get(ix);
	}


	protected void removeChildAt(int ix)
	{
		children.remove(ix);
	}


	protected void setChild(int ix, BPlusTreeNode<SKey,DataHolder> n)
	{
		children.set(ix, n);
	}


	protected void addChild(int ix, BPlusTreeNode<SKey,DataHolder> n)
	{
		children.add(ix, n);
	}
	
	
	protected LeafNode<SKey,DataHolder> newLeafNode()
	{
		return new SecLeafNode(store);
	}
	
	
	protected InternalNode newInternalNode()
	{
		return new SecInternalNode(store);
	}
}