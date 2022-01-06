// Copyright © 2020-2022 Andy Goryachev <andy@goryachev.com>
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
	
	
	protected void addChild(BPlusTreeNode<K,V> n)
	{
		children.add(n);
	}


	protected int getChildCount()
	{
		return children.size();
	}


	protected BPlusTreeNode<K,V> childAt(int ix)
	{
		return children.get(ix);
	}


	protected void removeChildAt(int ix)
	{
		children.remove(ix);
	}


	protected void setChild(int ix, BPlusTreeNode<K,V> n)
	{
		children.set(ix, n);
	}


	protected void addChild(int ix, BPlusTreeNode<K,V> n)
	{
		children.add(ix, n);
	}
}