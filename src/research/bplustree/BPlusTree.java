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
 * A Java implementation of B+ tree for key-value store.
 * Not thread safe.
 * 
 * Original code:
 * https://github.com/jiaguofang/b-plus-tree
 */
public class BPlusTree<K extends Comparable<? super K>, V>
{
	@FunctionalInterface
	public interface QueryClient<K,V>
	{
		/** accepts query results.  the query is aborted when this callback returns false */
		public boolean acceptQueryResult(K key, V value);
	}
	
	//

	private final int branchingFactor;
	protected BPlusTreeNode rootNode;


	public BPlusTree(int branchingFactor)
	{
		if(branchingFactor <= 2)
		{
			throw new IllegalArgumentException("Illegal branching factor: " + branchingFactor);
		}
		this.branchingFactor = branchingFactor;
	}
	
	
	protected void setRoot(BPlusTreeNode root)
	{
		this.rootNode = root;
	}
	
	
	protected BPlusTreeNode newRootNode()
	{
		return newLeafNode();
	}
	
	
	protected LeafNode newLeafNode()
	{
		return new LeafNode();
	}
	
	
	protected InternalNode newInternalNode()
	{
		return new InternalNode();
	}
	
	
	protected BPlusTreeNode root()
	{
		if(rootNode == null)
		{
			rootNode = newRootNode();
		}
		return rootNode;
	}


	/**
	 * Returns the value to which the specified key is associated, or
	 * {@code null} if this tree contains no association for the key.
	 *
	 * <p>
	 * A return value of {@code null} does not <i>necessarily</i> indicate that
	 * the tree contains no association for the key; it's also possible that the
	 * tree explicitly associates the key to {@code null}.
	 * 
	 * @param key
	 *            the key whose associated value is to be returned
	 * 
	 * @return the value to which the specified key is associated, or
	 *         {@code null} if this tree contains no association for the key
	 */
	public V get(K key)
	{
		return root().getValue(key);
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
		root().query(start, includeStart, end, includeEnd, client);
	}


	/**
	 * Associates the specified value with the specified key in this tree. If
	 * the tree previously contained a association for the key, the old value is
	 * replaced.
	 * 
	 * @param key
	 *            the key with which the specified value is to be associated
	 * @param value
	 *            the value to be associated with the specified key
	 */
	public void insert(K key, V value)
	{
		root().insertValue(key, value, branchingFactor);
	}


	/**
	 * Removes the association for the specified key from this tree if present.
	 * 
	 * @param key
	 *            the key whose association is to be removed from the tree
	 */
	public void remove(K key)
	{
		root().remove(key, branchingFactor);
	}
	
	
	/** returns the total number of values stored in a tree.  this is an expensive call */
	public long getTotalCount()
	{
		return root().countValues();
	}


	public String toString()
	{
		Queue<List<BPlusTreeNode>> queue = new LinkedList<List<BPlusTreeNode>>();
		queue.add(Arrays.asList(root()));
		StringBuilder sb = new StringBuilder();
		while(!queue.isEmpty())
		{
			Queue<List<BPlusTreeNode>> nextQueue = new LinkedList<List<BPlusTreeNode>>();
			while(!queue.isEmpty())
			{
				List<BPlusTreeNode> nodes = queue.remove();
				sb.append('{');
				Iterator<BPlusTreeNode> it = nodes.iterator();
				while(it.hasNext())
				{
					BPlusTreeNode node = it.next();
					sb.append(node.toString());
					if(it.hasNext())
					{
						sb.append(", ");
					}
					if(node instanceof BPlusTree.InternalNode)
					{
						nextQueue.add(((InternalNode)node).children);
					}
				}
				sb.append('}');
				if(!queue.isEmpty())
				{
					sb.append(", ");
				}
				else
				{
					sb.append('\n');
				}
			}
			queue = nextQueue;
		}

		return sb.toString();
	}
	
	
	//
	

	public abstract class BPlusTreeNode
	{
		public abstract V getValue(K key);

		public abstract long countValues();

		public abstract void remove(K key, int branchingFactor);

		public abstract void insertValue(K key, V value, int branchingFactor);

		public abstract K getFirstLeafKey();

		public abstract void merge(BPlusTreeNode sibling);

		public abstract BPlusTreeNode split();

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
	}
	
	
	//
	

	public class InternalNode
		extends BPlusTreeNode
	{
		protected final List<BPlusTreeNode> children;


		public InternalNode()
		{
			this.children = new ArrayList<BPlusTreeNode>();
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
		public void remove(K key, int branchingFactor)
		{
			BPlusTreeNode root = root();
			BPlusTreeNode child = getChild(key);
			child.remove(key, branchingFactor);
			if(child.isUnderflow(branchingFactor))
			{
				BPlusTreeNode childLeftSibling = getChildLeftSibling(key);
				BPlusTreeNode childRightSibling = getChildRightSibling(key);
				BPlusTreeNode left = childLeftSibling != null ? childLeftSibling : child;
				BPlusTreeNode right = childLeftSibling != null ? child : childRightSibling;
				left.merge(right);
				deleteChild(right.getFirstLeafKey());
				if(left.isOverflow(branchingFactor))
				{
					BPlusTreeNode sibling = left.split();
					insertChild(sibling.getFirstLeafKey(), sibling);
				}
				
				if(root.size() == 0)
				{
					setRoot(left);
				}
			}
		}


		@Override
		public void insertValue(K key, V value, int branchingFactor)
		{
			BPlusTreeNode root = root();
			BPlusTreeNode child = getChild(key);
			child.insertValue(key, value, branchingFactor);
			if(child.isOverflow(branchingFactor))
			{
				BPlusTreeNode sibling = child.split();
				insertChild(sibling.getFirstLeafKey(), sibling);
			}
			
			if(root.isOverflow(branchingFactor))
			{
				BPlusTreeNode sibling = split();
				InternalNode newRoot = newInternalNode();
				newRoot.keys.add(sibling.getFirstLeafKey());
				newRoot.children.add(this);
				newRoot.children.add(sibling);
				setRoot(newRoot);
			}
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
		public void merge(BPlusTreeNode sibling)
		{
			@SuppressWarnings("unchecked")
			InternalNode node = (InternalNode)sibling;
			keys.add(node.getFirstLeafKey());
			keys.addAll(node.keys);
			children.addAll(node.children);

		}


		@Override
		public BPlusTreeNode split()
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


		public BPlusTreeNode getChild(K key)
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


		public void insertChild(K key, BPlusTreeNode child)
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


		public BPlusTreeNode getChildLeftSibling(K key)
		{
			int ix = insertIndex(key);
			if(ix > 0)
			{
				return children.get(ix - 1);
			}
			return null;
		}


		public BPlusTreeNode getChildRightSibling(K key)
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
	

	public class LeafNode
		extends BPlusTreeNode
	{
		protected final List<V> values;


		public LeafNode()
		{
			values = new ArrayList<V>();
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
		public void remove(K key, int branchingFactor)
		{
			int ix = indexOf(key);
			if(ix >= 0)
			{
				keys.remove(ix);
				values.remove(ix);
			}
		}


		@Override
		public void insertValue(K key, V value, int branchingFactor)
		{
			BPlusTreeNode root = root();
			
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
				setRoot(newRoot);
			}
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
