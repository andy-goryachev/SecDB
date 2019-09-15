//	The MIT License (MIT)
//	
//	Copyright (c) 2014 Fang Jiaguo
//	Copyright © 2018 Andy Goryachev <andy@goryachev.com>
//	
//	Permission is hereby granted, free of charge, to any person obtaining a copy
//	of this software and associated documentation files (the "Software"), to deal
//	in the Software without restriction, including without limitation the rights
//	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//	copies of the Software, and to permit persons to whom the Software is
//	furnished to do so, subject to the following conditions:
//	
//	The above copyright notice and this permission notice shall be included in all
//	copies or substantial portions of the Software.
//	
//	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//	SOFTWARE.
//
package research.bplustree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A Java implementation of B+ tree node for key-value store.
 * Not thread safe.
 * 
 * Original code:
 * https://github.com/jiaguofang/b-plus-tree
 */
public abstract class BPlusTreeNode<K extends Comparable<? super K>, V>
{
	public abstract boolean containsKey(K key) throws Exception; 
	
	public abstract V getValue(K key) throws Exception;

	/** returns new root node or null if no changes were made */
	public abstract BPlusTreeNode<K,V> remove(BPlusTreeNode<K,V> root, K key, int branchingFactor) throws Exception;

	/** returns new root node or null if no changes were made */
	public abstract BPlusTreeNode<K,V> insertValue(BPlusTreeNode<K,V> root, K key, V value, int branchingFactor) throws Exception;

	public abstract K getFirstLeafKey() throws Exception;
	
	protected abstract void addChild(BPlusTreeNode<K,V> n);

	public abstract void merge(BPlusTreeNode<K,V> sibling) throws Exception;

	public abstract BPlusTreeNode<K,V> split() throws Exception;

	public abstract boolean isOverflow(int branchingFactor);

	public abstract boolean isUnderflow(int branchingFactor);
	
	public abstract boolean queryForward(K start, boolean includeStart, K end, boolean endPolicy, QueryClient<K,V> client) throws Exception;

	public abstract boolean queryBackward(K start, boolean includeStart, K end, boolean endPolicy, QueryClient<K,V> client) throws Exception;
	
	public abstract boolean isLeafNode();
	
	//

	protected final List<K> keys;
	
	
	public BPlusTreeNode()
	{
		this.keys = new ArrayList<K>();
	}
	

	public int size()
	{
		return keys.size();
	}
	
	
	public int indexOf(K key)
	{
		return Collections.binarySearch(keys, key);
	}
	
	
	public int insertIndex(K key)
	{
		int ix = Collections.binarySearch(keys, key);
		return ix >= 0 ? ix + 1 : -ix - 1;
	}


	public String toString()
	{
		return keys.toString();
	}
	
	
	/**
	 * Performs a search query with the keys specified by the range:
	 * {@code key1} and {@code key2}.
	 * 
	 * @param start the start key of the range
	 * @param includeStart whether to include the start key in the query
	 * @param end the end end of the range
	 * @param includeEnd whether to include end key in the query
	 * @param client handler accepts query results
	 */
	public void query(K start, boolean includeStart, K end, boolean includeEnd, QueryClient<K,V> client)
	{
		try
		{
			if(start.compareTo(end) <= 0)
			{
				queryForward(start, includeStart, end, includeEnd, client);
			}
			else
			{
				queryBackward(start, includeStart, end, includeEnd, client);
			}
		}
		catch(Throwable e)
		{
			client.onError(e);
		}
	}
	
	
	protected LeafNode<K,V> newLeafNode()
	{
		return new LeafNode<>();
	}
	
	
	/** dropping generics as they are getting in a way */
	protected InternalNode newInternalNode()
	{
		return new LocalInternalNode();
	}

	
	//
	

	public static class LeafNode<K extends Comparable<? super K>,V>
		extends BPlusTreeNode<K,V>
	{
		protected final List<V> values;


		public LeafNode()
		{
			this.values = new ArrayList<V>();
		}
		
		
		public boolean isLeafNode()
		{
			return true;
		}
		
		
		protected void addChild(BPlusTreeNode<K,V> n)
		{
			throw new Error();
		}
		
		
		@Override
		public boolean containsKey(K key)
		{
			int ix = indexOf(key);
			return (ix >= 0);
		}


		@Override
		public V getValue(K key)
		{
			int ix = indexOf(key);
			return ix >= 0 ? values.get(ix) : null;
		}
		

		@Override
		public BPlusTreeNode<K,V> remove(BPlusTreeNode<K,V> root, K key, int branchingFactor)
		{
			int ix = indexOf(key);
			if(ix >= 0)
			{
				keys.remove(ix);
				values.remove(ix);
				return root;
			}
			return null;
		}


		@Override
		public BPlusTreeNode<K,V> insertValue(BPlusTreeNode<K,V> root, K key, V value, int branchingFactor) throws Exception
		{
			int ix = indexOf(key);
			int valueIndex = ix >= 0 ? ix : -ix - 1;
			if(ix >= 0)
			{
				values.set(valueIndex, value);
			}
			else
			{
				keys.add(valueIndex, key);
				values.add(valueIndex, value);
			}
			
			if(root.isOverflow(branchingFactor))
			{
				BPlusTreeNode sibling = split();
				InternalNode newRoot = newInternalNode();
				newRoot.keys.add(sibling.getFirstLeafKey());
				newRoot.addChild(this);
				newRoot.addChild(sibling);
				return newRoot;
			}
			return root;
		}


		@Override
		public K getFirstLeafKey()
		{
			return keys.get(0);
		}

		
		public boolean queryForward(K start, boolean includeStart, K end, boolean includeEnd, QueryClient<K,V> client)
		{
			int sz = size();
			for(int i=0; i<sz; i++)
			{
				K key = keys.get(i);
				V val = values.get(i);
				int cms = key.compareTo(start);
				int cme = key.compareTo(end);
				
				if(((!includeStart && cms > 0) || (includeStart && cms >= 0)) && ((!includeEnd && cme < 0) || (includeEnd && cme <= 0)))
				{
					if(!client.acceptQueryResult(key, val))
					{
						return false;
					}
				}
				else if((!includeEnd && cme >= 0) || (includeEnd && cme > 0))
				{
					return false;
				}
			}
			return true;
		}


		public boolean queryBackward(K start, boolean includeStart, K end, boolean includeEnd, QueryClient<K,V> client)
		{
			for(int i=size()-1; i>=0; i--)
			{
				K key = keys.get(i);
				V val = values.get(i);
				int cms = key.compareTo(start);
				int cme = key.compareTo(end);
				
				if(((!includeStart && cms < 0) || (includeStart && cms <= 0)) && ((!includeEnd && cme > 0) || (includeEnd && cme >= 0)))
				{
					if(!client.acceptQueryResult(key, val))
					{
						return false;
					}
				}
				else if((!includeEnd && cme <= 0) || (includeEnd && cme < 0))
				{
					return false;
				}
			}
			return true;
		}


		@Override
		public void merge(BPlusTreeNode sibling)
		{
			@SuppressWarnings("unchecked")
			LeafNode node = (LeafNode)sibling;
			keys.addAll(node.keys);
			values.addAll(node.values);
		}


		@Override
		public BPlusTreeNode split()
		{
			int from = (size() + 1) / 2;
			int to = size();

			LeafNode sibling = newLeafNode();
			sibling.keys.addAll(keys.subList(from, to));
			sibling.values.addAll(values.subList(from, to));

			keys.subList(from, to).clear();
			values.subList(from, to).clear();

			return sibling;
		}


		@Override
		public boolean isOverflow(int branchingFactor)
		{
			return values.size() > branchingFactor - 1;
		}


		@Override
		public boolean isUnderflow(int branchingFactor)
		{
			return values.size() < branchingFactor / 2;
		}
	}
	
	
	//
	

	public abstract static class InternalNode<K extends Comparable<? super K>,V>
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
			
			if(child.isUnderflow(branchingFactor))
			{
				BPlusTreeNode<K,V> childLeftSibling = getChildLeftSibling(key);
				BPlusTreeNode<K,V> childRightSibling = getChildRightSibling(key);
				BPlusTreeNode<K,V> left = childLeftSibling != null ? childLeftSibling : child;
				BPlusTreeNode<K,V> right = childLeftSibling != null ? child : childRightSibling;
				left.merge(right);
				deleteChild(right.getFirstLeafKey());
				if(left.isOverflow(branchingFactor))
				{
					BPlusTreeNode<K,V> sibling = left.split();
					insertChild(sibling.getFirstLeafKey(), sibling);
				}
				
				if(root.size() == 0)
				{
					return left;
				}
			}
			return root;
		}


		@Override
		public BPlusTreeNode<K,V> insertValue(BPlusTreeNode<K,V> root, K key, V value, int branchingFactor) throws Exception
		{
			BPlusTreeNode<K,V> child = getChild(key);
			child.insertValue(root, key, value, branchingFactor);
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
			int ix = insertIndex(start);
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
			int ix = insertIndex(start);
			
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
		}


		@Override
		public BPlusTreeNode<K,V> split() throws Exception
		{
			int from = size() / 2 + 1;
			int to = size();
			InternalNode sibling = newInternalNode();
			
			sibling.keys.addAll(keys.subList(from, to));
			keys.subList(from - 1, to).clear();

			for(int i=from; i<=to; i++)
			{
				sibling.addChild(childAt(from));
				removeChildAt(from);
			}
			
//			sibling.keys.addAll(keys.subList(from, to));
//			sibling.children.addAll(children.subList(from, to + 1));
//
//			keys.subList(from - 1, to).clear();
//			children.subList(from, to + 1).clear();

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
			int ix = insertIndex(key);
			return childAt(ix);
		}


		public void deleteChild(K key)
		{
			int ix = indexOf(key);
			if(ix >= 0)
			{
				keys.remove(ix);
				removeChildAt(ix + 1);
			}
		}


		public void insertChild(K key, BPlusTreeNode<K,V> child)
		{
			int ix = indexOf(key);
			int childIndex = ix >= 0 ? ix + 1 : -ix - 1;
			if(ix >= 0)
			{
				setChild(childIndex, child);
			}
			else
			{
				keys.add(childIndex, key);
				addChild(childIndex + 1, child);
			}
		}


		public BPlusTreeNode<K,V> getChildLeftSibling(K key) throws Exception
		{
			int ix = insertIndex(key);
			if(ix > 0)
			{
				return childAt(ix - 1);
			}
			return null;
		}


		public BPlusTreeNode<K,V> getChildRightSibling(K key) throws Exception
		{
			int ix = insertIndex(key);
			if(ix < size())
			{
				return childAt(ix + 1);
			}
			return null;
		}
	}
	
	
	//
	
	
	/** internal node with locally stored children (implementation for testing) */
	public static class LocalInternalNode<K extends Comparable<? super K>,V> 
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
}
