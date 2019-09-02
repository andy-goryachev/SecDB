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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


/**
 * A Java implementation of B+ tree node for key-value store.
 * Not thread safe.
 * 
 * Original code:
 * https://github.com/jiaguofang/b-plus-tree
 */
public abstract class BPlusTreeNode<K extends Comparable<? super K>, V>
{
	// TODO accept exception to handle errors?
	@FunctionalInterface
	public interface QueryClient<K,V>
	{
		/** accepts query results.  the query is aborted when this callback returns false */
		public boolean acceptQueryResult(K key, V value);
	}
	
	//
	
	public abstract boolean containsKey(K key); 
	
	public abstract V getValue(K key);

	public abstract long countValues();

	/** returns new root node or null if no changes were made */
	public abstract BPlusTreeNode<K,V> remove(BPlusTreeNode<K,V> root, K key, int branchingFactor);

	/** returns new root node or null if no changes were made */
	public abstract BPlusTreeNode<K,V> insertValue(BPlusTreeNode<K,V> root, K key, V value, int branchingFactor);

	public abstract K getFirstLeafKey();

	public abstract void merge(BPlusTreeNode<K,V> sibling);

	public abstract BPlusTreeNode<K,V> split();

	public abstract boolean isOverflow(int branchingFactor);

	public abstract boolean isUnderflow(int branchingFactor);
	
	public abstract boolean queryForward(K start, boolean includeStart, K end, boolean endPolicy, QueryClient client);

	public abstract boolean queryBackward(K start, boolean includeStart, K end, boolean endPolicy, QueryClient client);
	
	//

	protected final List<K> keys;
	
	
	public BPlusTreeNode()
	{
		keys = new ArrayList<K>();
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
	public void query(K start, boolean includeStart, K end, boolean includeEnd, QueryClient client)
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
	
	
	protected LeafNode<K,V> newLeafNode()
	{
		return new LeafNode<>();
	}
	
	
	protected InternalNode<K,V> newInternalNode()
	{
		return new InternalNode();
	}

	
	//
	

	public static class InternalNode<K extends Comparable<? super K>,V>
		extends BPlusTreeNode<K,V>
	{
		protected final List<BPlusTreeNode<K,V>> children;


		public InternalNode()
		{
			this.children = new ArrayList<>();
		}
		
		
		@Override
		public boolean containsKey(K key)
		{
			return getChild(key).containsKey(key);
		}


		@Override
		public V getValue(K key)
		{
			return getChild(key).getValue(key);
		}
		
		
		@Override
		public long countValues()
		{
			long total = 0;
			for(BPlusTreeNode ch: children)
			{
				total += ch.countValues();
			}
			return total;
		}


		@Override
		public BPlusTreeNode<K,V> remove(BPlusTreeNode<K,V> root, K key, int branchingFactor)
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
		public BPlusTreeNode<K,V> insertValue(BPlusTreeNode<K,V> root, K key, V value, int branchingFactor)
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
				newRoot.children.add(this);
				newRoot.children.add(sibling);
				return newRoot;
			}
			return root;
		}


		@Override
		public K getFirstLeafKey()
		{
			return children.get(0).getFirstLeafKey();
		}

		
		public boolean queryForward(K start, boolean includeStart, K end, boolean includeEnd, QueryClient client)
		{
			int ix = insertIndex(start);
			int sz = children.size();
			
			for(int i=ix; i<sz; i++)
			{
				BPlusTreeNode n = children.get(i);
				if(!n.queryForward(start, includeStart, end, includeEnd, client))
				{
					return false;
				}
			}
			
			return true;
		}


		public boolean queryBackward(K start, boolean includeStart, K end, boolean includeEnd, QueryClient client)
		{
			int ix = insertIndex(start);
			
			for(int i=children.size()-1; i>=0; i--)
			{
				BPlusTreeNode n = children.get(i);
				if(!n.queryBackward(start, includeStart, end, includeEnd, client))
				{
					return false;
				}
			}
			
			return true;
		}


		@Override
		public void merge(BPlusTreeNode<K,V> sibling)
		{
			@SuppressWarnings("unchecked")
			InternalNode<K,V> node = (InternalNode)sibling;
			keys.add(node.getFirstLeafKey());
			keys.addAll(node.keys);
			children.addAll(node.children);

		}


		@Override
		public BPlusTreeNode<K,V> split()
		{
			int from = size() / 2 + 1;
			int to = size();
			InternalNode sibling = newInternalNode();
			sibling.keys.addAll(keys.subList(from, to));
			sibling.children.addAll(children.subList(from, to + 1));

			keys.subList(from - 1, to).clear();
			children.subList(from, to + 1).clear();

			return sibling;
		}


		@Override
		public boolean isOverflow(int branchingFactor)
		{
			return children.size() > branchingFactor;
		}


		@Override
		public boolean isUnderflow(int branchingFactor)
		{
			return children.size() < (branchingFactor + 1) / 2;
		}


		public BPlusTreeNode<K,V> getChild(K key)
		{
			int ix = insertIndex(key);
			return children.get(ix);
		}


		public void deleteChild(K key)
		{
			int ix = indexOf(key);
			if(ix >= 0)
			{
				keys.remove(ix);
				children.remove(ix + 1);
			}
		}


		public void insertChild(K key, BPlusTreeNode<K,V> child)
		{
			int ix = indexOf(key);
			int childIndex = ix >= 0 ? ix + 1 : -ix - 1;
			if(ix >= 0)
			{
				children.set(childIndex, child);
			}
			else
			{
				keys.add(childIndex, key);
				children.add(childIndex + 1, child);
			}
		}


		public BPlusTreeNode<K,V> getChildLeftSibling(K key)
		{
			int ix = insertIndex(key);
			if(ix > 0)
			{
				return children.get(ix - 1);
			}
			return null;
		}


		public BPlusTreeNode<K,V> getChildRightSibling(K key)
		{
			int ix = insertIndex(key);
			if(ix < size())
			{
				return children.get(ix + 1);
			}
			return null;
		}
	}
	
	
	//
	

	public static class LeafNode<K extends Comparable<? super K>,V>
		extends BPlusTreeNode<K,V>
	{
		protected final List<V> values;


		public LeafNode()
		{
			values = new ArrayList<V>();
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
		public long countValues()
		{
			return values.size();
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
		public BPlusTreeNode<K,V> insertValue(BPlusTreeNode<K,V> root, K key, V value, int branchingFactor)
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
				newRoot.children.add(this);
				newRoot.children.add(sibling);
				return newRoot;
			}
			return root;
		}


		@Override
		public K getFirstLeafKey()
		{
			return keys.get(0);
		}

		
		public boolean queryForward(K start, boolean includeStart, K end, boolean includeEnd, QueryClient client)
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


		public boolean queryBackward(K start, boolean includeStart, K end, boolean includeEnd, QueryClient client)
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
}
