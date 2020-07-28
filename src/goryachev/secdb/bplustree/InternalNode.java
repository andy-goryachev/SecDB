// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.bplustree;
import goryachev.secdb.QueryClient;

/**
 * Internal B+ Tree Node (an Index Node).
 */
public abstract class InternalNode<K extends Comparable<? super K>,V>
	extends BPlusTreeNode<K,V>
{
	protected abstract int getChildCount();
	
	protected abstract BPlusTreeNode<K,V> childAt(int ix) throws Exception;
	
	protected abstract void removeChildAt(int ix);
	
	protected abstract void setChild(int ix, BPlusTreeNode<K,V> n);
	
	protected abstract void addChild(int ix, BPlusTreeNode<K,V> n);
	
	//
	
	public InternalNode()
	{
	}
	
	
	public boolean isLeafNode()
	{
		return false;
	}
	
	
	@Override
	public boolean containsKey(K key) throws Exception
	{
		return getChild(key).containsKey(key);
	}


	@Override
	public V getValue(K key) throws Exception
	{
		return getChild(key).getValue(key);
	}
	

	@Override
	public BPlusTreeNode<K,V> remove(BPlusTreeNode<K,V> root, K key, int branchingFactor) throws Exception
	{
		BPlusTreeNode<K,V> child = getChild(key);
		BPlusTreeNode<K,V> newRoot = child.remove(root, key, branchingFactor);
		if(newRoot == null)
		{
			// nothing was removed
			return null;
		}
		
		setModified();
		
		if(child.isUnderflow(branchingFactor))
		{
			BPlusTreeNode<K,V> childLeftSibling = getChildLeftSibling(key);
			BPlusTreeNode<K,V> childRightSibling = getChildRightSibling(key);
			BPlusTreeNode<K,V> left = childLeftSibling != null ? childLeftSibling : child;
			BPlusTreeNode<K,V> right = childLeftSibling != null ? child : childRightSibling;
			
			left.merge(right);
			
			// FIX here lies the bug
			{
				if(keys.contains(key))
				{
					deleteChild(key);
				}
				else
				{
					deleteChild(right.getFirstLeafKey()); // FIX this causes an issue if the key being deleted is also in this tree node.
				}
			}
			
			if(left.isOverflow(branchingFactor))
			{
				BPlusTreeNode<K,V> sibling = left.split();
				insertChild(sibling.getFirstLeafKey(), sibling);
			}
			
			if(newRoot.size() == 0)
			{
				return left;
			}
		}
		return newRoot;
	}


	@Override
	public BPlusTreeNode<K,V> insertValue(BPlusTreeNode<K,V> root, K key, V value, int branchingFactor) throws Exception
	{
		BPlusTreeNode<K,V> child = getChild(key);
		child.insertValue(root, key, value, branchingFactor);
		setModified();
		
		if(child.isOverflow(branchingFactor))
		{
			BPlusTreeNode<K,V> sibling = child.split();
			insertChild(sibling.getFirstLeafKey(), sibling);
		}
		
		if(root.isOverflow(branchingFactor))
		{
			BPlusTreeNode<K,V> sibling = split();
			InternalNode<K,V> newRoot = newInternalNode();
			newRoot.keys.add(sibling.getFirstLeafKey());
			newRoot.addChild(this);
			newRoot.addChild(sibling);
			newRoot.setModified();
			return newRoot;
		}
		return root;
	}


	@Override
	public K getFirstLeafKey() throws Exception
	{
		return childAt(0).getFirstLeafKey();
	}

	
	public boolean queryForward(K start, boolean includeStart, K end, boolean includeEnd, QueryClient<K,V> client) throws Exception
	{
		int ix = findInsertIndex(start);
		int sz = getChildCount();
		
		for(int i=ix; i<sz; i++)
		{
			BPlusTreeNode n = childAt(i);
			if(!n.queryForward(start, includeStart, end, includeEnd, client))
			{
				return false;
			}
		}
		
		return true;
	}


	public boolean queryBackward(K start, boolean includeStart, K end, boolean includeEnd, QueryClient<K,V> client) throws Exception
	{
		int ix = findInsertIndex(start);
		
		for(int i=getChildCount()-1; i>=0; i--)
		{
			BPlusTreeNode n = childAt(i);
			if(!n.queryBackward(start, includeStart, end, includeEnd, client))
			{
				return false;
			}
		}
		
		return true;
	}


	@Override
	public void merge(BPlusTreeNode<K,V> sibling) throws Exception
	{
		@SuppressWarnings("unchecked")
		InternalNode<K,V> node = (InternalNode)sibling;
		keys.add(node.getFirstLeafKey());
		keys.addAll(node.keys);
		
		int sz = node.getChildCount();
		for(int i=0; i<sz; i++)
		{
			addChild(node.childAt(i));
		}
		
		setModified();
	}


	@Override
	public BPlusTreeNode<K,V> split() throws Exception
	{
		int to = size();
		int from = to / 2 + 1;
		InternalNode sibling = newInternalNode();
		
		sibling.keys.addAll(keys.subList(from, to));
		keys.subList(from - 1, to).clear();
		sibling.setModified();

		for(int i=from; i<=to; i++)
		{
			sibling.addChild(childAt(from));
			removeChildAt(from);
		}
		
		setModified();

		return sibling;
	}


	@Override
	public boolean isOverflow(int branchingFactor)
	{
		return getChildCount() > branchingFactor;
	}


	@Override
	public boolean isUnderflow(int branchingFactor)
	{
		return getChildCount() < (branchingFactor + 1) / 2;
	}


	public BPlusTreeNode<K,V> getChild(K key) throws Exception
	{
		int ix = findInsertIndex(key);
		return childAt(ix);
	}


	public void deleteChild(K key)
	{
		int ix = indexOf(key);
		if(ix >= 0)
		{
			keys.remove(ix);
			removeChildAt(ix + 1);
			setModified();
		}
	}


	public void insertChild(K key, BPlusTreeNode<K,V> child)
	{
		int ix = indexOf(key);
		if(ix >= 0)
		{
			setChild(ix, child);
		}
		else
		{
			ix = -ix;
			keys.add(ix - 1, key);
			addChild(ix, child);
		}
		
		setModified();
	}


	public BPlusTreeNode<K,V> getChildLeftSibling(K key) throws Exception
	{
		int ix = findInsertIndex(key);
		if(ix > 0)
		{
			return childAt(ix - 1);
		}
		return null;
	}


	public BPlusTreeNode<K,V> getChildRightSibling(K key) throws Exception
	{
		int ix = findInsertIndex(key);
		if(ix < size())
		{
			return childAt(ix + 1);
		}
		return null;
	}
	
	
	public void dump(Appendable out, int indent) throws Exception
	{
		int sz = getChildCount();
		for(int i=0; i<sz; i++)
		{
			BPlusTreeNode<K,V> ch = childAt(i);
			ch.dump(out, indent + 1);
			
			if(i < (sz - 1))
			{
				for(int j=0; j<indent; j++)
				{
					out.append("  ");
				}
				out.append("key=");
				out.append(keyAt(i).toString());
				out.append("\n");
			}
		}
	}
	
	
	public boolean prefixQuery(K prefix, QueryClient<K,V> client) throws Exception
	{
		throw new UnsupportedOperationException();
	}
}