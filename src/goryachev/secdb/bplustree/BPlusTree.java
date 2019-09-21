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
package goryachev.secdb.bplustree;
import java.util.ArrayList;
import java.util.Arrays;
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
	private final int branchingFactor;
	private BPlusTreeNode<K,V> root = new BPlusTreeNode.LeafNode();


	public BPlusTree(int branchingFactor)
	{
		if(branchingFactor <= 2)
		{
			throw new IllegalArgumentException("Illegal branching factor: " + branchingFactor);
		}
		this.branchingFactor = branchingFactor;
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
	public V get(K key) throws Exception
	{
		return root.getValue(key);
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
		root.query(start, includeStart, end, includeEnd, client);
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
	public void insert(K key, V value) throws Exception
	{
		setRoot(root.insertValue(root, key, value, branchingFactor));
	}


	/**
	 * Removes the association for the specified key from this tree if present.
	 * 
	 * @param key
	 *            the key whose association is to be removed from the tree
	 */
	public void remove(K key) throws Exception
	{
		setRoot(root.remove(root, key, branchingFactor));
	}
	
	
	protected void setRoot(BPlusTreeNode<K,V> n)
	{
		if(n != null)
		{
			root = n;
		}
	}
	

	public String toString()
	{
		return "BPlusTree";
	}
	
	
	public String dump() throws Exception
	{
		Queue<List<BPlusTreeNode>> queue = new LinkedList<List<BPlusTreeNode>>();
		queue.add(Arrays.asList(root));
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
					
					if(node instanceof BPlusTreeNode.InternalNode)
					{
						BPlusTreeNode.InternalNode n = (BPlusTreeNode.InternalNode)node;
						int sz = n.getChildCount();
						ArrayList<BPlusTreeNode> children = new ArrayList(sz);
						for(int i=0; i<sz; i++)
						{
							children.add(n.childAt(i));
						}
						nextQueue.add(children);
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
}
