// Copyright © 2020-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.bplustree;
import java.util.ArrayList;
import java.util.List;


/**
 * Local Internal Node, 
 * an internal (index) node with locally stored children, for testing purposes only.
 */
public class LocalInternalNode<K extends Comparable<? super K>,V> 
	extends InternalNode<K,V>
{
	protected final List<BPlusTreeNode<K,V>> children;


	public LocalInternalNode()
	{
		this.children = new ArrayList<>();
	}
	
	
	@Override
	protected void addChild(BPlusTreeNode<K,V> n)
	{
		children.add(n);
	}


	@Override
	protected int getChildCount()
	{
		return children.size();
	}


	@Override
	protected BPlusTreeNode<K,V> childAt(int ix)
	{
		return children.get(ix);
	}


	@Override
	protected void removeChildAt(int ix)
	{
		children.remove(ix);
	}


	@Override
	protected void setChild(int ix, BPlusTreeNode<K,V> n)
	{
		children.set(ix, n);
	}


	@Override
	protected void addChild(int ix, BPlusTreeNode<K,V> n)
	{
		children.add(ix, n);
	}
}