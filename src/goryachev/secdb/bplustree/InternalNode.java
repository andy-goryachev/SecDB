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
	protected K getFirstLeafKey() throws Exception
	{
		return childAt(0).getFirstLeafKey();
	}
	
	
	public boolean prefixQuery(K prefix, QueryClient<K,V> client) throws Exception
	{
		// similar to queryForward
		int ix = findInsertIndex(prefix);
		int sz = getChildCount();
		
		for(int i=ix; i<sz; i++)
		{
			BPlusTreeNode n = childAt(i);
			if(!n.prefixQuery(prefix, client))
			{
				return false;
			}
		}
		
		return true;
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
	protected void merge(BPlusTreeNode<K,V> sibling) throws Exception
	{
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
	protected BPlusTreeNode<K,V> split() throws Exception
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


	protected BPlusTreeNode<K,V> getChild(K key) throws Exception
	{
		int ix = findInsertIndex(key);
		return childAt(ix);
	}


	protected void deleteChild(K key)
	{
		int ix = indexOf(key);
		if(ix >= 0)
		{
			keys.remove(ix);
			removeChildAt(ix + 1);
			setModified();
		}
	}


	protected void insertChild(K key, BPlusTreeNode<K,V> child)
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


	@Deprecated // TODO remove
	protected BPlusTreeNode<K,V> getChildLeftSibling(K key) throws Exception
	{
		int ix = findInsertIndex(key);
		if(ix > 0)
		{
			return childAt(ix - 1);
		}
		return null;
	}


	@Deprecated // TODO remove
	protected BPlusTreeNode<K,V> getChildRightSibling(K key) throws Exception
	{
		int ix = findInsertIndex(key);
		if(ix < size())
		{
			return childAt(ix + 1);
		}
		return null;
	}
	
	
	public void dump(Appendable out, String indent, int level) throws Exception
	{
		int sz = getChildCount();
		for(int i=0; i<sz; i++)
		{
			BPlusTreeNode<K,V> ch = childAt(i);
			ch.dump(out, indent, level + 1);
			
			if(i < (sz - 1))
			{
				for(int j=0; j<level; j++)
				{
					out.append(indent);
				}
				
				out.append("key=");
				out.append(keyAt(i).toString());
				out.append("\n");
			}
		}
	}
	
	
	public void dumpKeys(Appendable out, String indent, int level) throws Exception
	{
		int sz = getChildCount();
		for(int i=0; i<sz; i++)
		{
			BPlusTreeNode<K,V> ch = childAt(i);
			ch.dumpKeys(out, indent, level + 1);
			
			if(i < (sz - 1))
			{
				K k = keyAt(i);
				
				for(int j=0; j<level; j++)
				{
					out.append(indent);
				}
				
				out.append("I=");
				out.append(k.toString());
				out.append("\n");
			}
		}
	}
	

	@Override
	public BPlusTreeNode<K,V> remove(BPlusTreeNode<K,V> root, K key, int branchingFactor) throws Exception
	{
		int ix = findInsertIndex(key);
		BPlusTreeNode<K,V> child = childAt(ix);
		
		// FIX possibly expensive call
		K firstKey = child.getFirstLeafKey();
		
		BPlusTreeNode<K,V> newRoot = child.remove(root, key, branchingFactor);
		if(newRoot == null)
		{
			// nothing was removed
			return null;
		}
		
		setModified();
		
		if(child.isUnderflow(branchingFactor))
		{
			BPlusTreeNode<K,V> left;
			BPlusTreeNode<K,V> right;
			
			// pick the sibling to merge with
			if(ix == 0)
			{
				// merge with right
				left = child;
				right = childAt(ix + 1);
			}
			else if((ix + 1) >= getChildCount())
			{
				// merge with left
				left = childAt(ix - 1);
				right = child;
			}
			else
			{
				// left or right (pick the largest)
				BPlusTreeNode<K,V> lc = childAt(ix - 1);
				BPlusTreeNode<K,V> rc = childAt(ix + 1);
				
				if(lc.size() > rc.size())
				{
					left = lc;
					right = child;
				}
				else
				{
					left = child;
					right = rc;
				}
			}
			
			if(left == child)
			{
				// right sibling will change
				deleteChild(right.getFirstLeafKey());
			}
			
			// TODO this may not be correct
			// deleted key is no more
			deleteChild(firstKey);
			
			left.merge(right);
			
			if(child == left)
			{
				// re-insert left
				insertChild(left.getFirstLeafKey(), left);
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
}