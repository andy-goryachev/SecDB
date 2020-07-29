//	The MIT License (MIT)
//	
//	Copyright (c) 2014 Fang Jiaguo
//	Copyright © 2018-2020 Andy Goryachev <andy@goryachev.com>
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
import goryachev.secdb.QueryClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A Java implementation of B+ tree node for a key-value store.
 * Not thread safe.
 * 
 * This code is partially sourced from:
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
	
	/** 
	 * Finds all the entries where the key "starts with" the given prefix.  
	 * If the key type does not support such an operation, an UnsupportedOperationException is thrown.
	 * Returns true if no exceptions (Throwables) have been thrown, false otherwise
	 */ 
	public abstract boolean prefixQuery(K prefix, QueryClient<K,V> client) throws Exception;
	
	public abstract boolean isLeafNode();
	
	public abstract void dump(Appendable out, String indent, int level) throws Exception;
	
	//

	protected final List<K> keys;
	private boolean modified;
	
	
	public BPlusTreeNode()
	{
		this.keys = new ArrayList<K>();
	}
	

	public int size()
	{
		return keys.size();
	}
	
	
	public K keyAt(int ix)
	{
		return keys.get(ix);
	}
	
	
	public int indexOf(K key)
	{
		return Collections.binarySearch(keys, key);
	}
	
	
	public int findInsertIndex(K key)
	{
		int ix = Collections.binarySearch(keys, key);
		return ix >= 0 ? ix + 1 : -ix - 1;
	}
	
	
	/** deserialization */
	// TODO should be protected
	public void addKey(K k)
	{
		keys.add(k);
	}
	

	public final boolean isModified()
	{
		return modified;
	}
	
	
	public void setModified()
	{
		modified = true;
	}


	public String toString()
	{
		return keys.toString();
	}
	
	
	/**
	 * Performs a range query with the keys specified by the range:
	 * {@code start} and {@code end}.
	 * 
	 * @param start the start key of the range
	 * @param includeStart whether to include the start key in the query
	 * @param end the end end of the range
	 * @param includeEnd whether to include end key in the query
	 * @param client handler accepts query results
	 */
	public boolean rangeQuery(K start, boolean includeStart, K end, boolean includeEnd, QueryClient<K,V> client)
	{
		try
		{
			if(start.compareTo(end) <= 0)
			{
				return queryForward(start, includeStart, end, includeEnd, client);
			}
			else
			{
				return queryBackward(start, includeStart, end, includeEnd, client);
			}
		}
		catch(Throwable e)
		{
			client.onError(e);
		}
		return false;
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
}
