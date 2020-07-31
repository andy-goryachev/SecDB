// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.bplustree;
import goryachev.secdb.QueryClient;
import java.util.ArrayList;
import java.util.List;


/**
 * Leaf B+ Tree Node.
 */
public class LeafNode<K extends Comparable<? super K>,V>
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
			setModified();
			return root;
		}
		else
		{
			// not modified
			return null;
		}
	}


	@Override
	public BPlusTreeNode<K,V> insertValue(BPlusTreeNode<K,V> root, K key, V value, int branchingFactor) throws Exception
	{
		int ix = indexOf(key);
		if(ix >= 0)
		{
			values.set(ix, value);
		}
		else
		{
			ix = -ix - 1;
			keys.add(ix, key);
			values.add(ix, value);
		}
		
		setModified();
		
		if(root.isOverflow(branchingFactor))
		{
			BPlusTreeNode sibling = split();
			
			InternalNode newRoot = newInternalNode();
			newRoot.setModified();
			newRoot.keys.add(sibling.getFirstLeafKey());
			newRoot.addChild(this);
			newRoot.addChild(sibling);
			return newRoot;
		}
		return root;
	}


	@Override
	protected K getFirstLeafKey()
	{
		return keys.get(0);
	}
	
	
	/** 
	 * determines whether 'prefix' is the prefix of 'key'.
	 * throws UnsupportedOperationException if K type does not support such an operation.
	 */
	protected boolean isPrefix(K prefix, K key) throws Exception
	{
		throw new UnsupportedOperationException();
	}
	
	
	public boolean prefixQuery(K prefix, QueryClient<K,V> client) throws Exception
	{
		int sz = size();
		for(int i=0; i<sz; i++)
		{
			K key = keys.get(i);
			if(key.compareTo(prefix) >= 0)
			{
				if(isPrefix(prefix, key))
				{
					V val = values.get(i);
					if(!client.acceptQueryResult(key, val))
					{
						return false;
					}
				}
				else
				{
					return false;
				}
			}
		}
		return true;
	}


	public boolean queryForward(K start, boolean includeStart, K end, boolean includeEnd, QueryClient<K,V> client)
	{
		int sz = size();
		for(int i=0; i<sz; i++)
		{
			K key = keys.get(i);
			int cms = key.compareTo(start);
			int cme = key.compareTo(end);
			
			if(((!includeStart && cms > 0) || (includeStart && cms >= 0)) && ((!includeEnd && cme < 0) || (includeEnd && cme <= 0)))
			{
				V val = values.get(i);
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
	protected void merge(BPlusTreeNode<K,V> sibling) throws Exception
	{
		LeafNode<K,V> node = (LeafNode<K,V>)sibling;
		keys.addAll(node.keys);
		values.addAll(node.values);
		setModified();
	}
	

	@Override
	protected BPlusTreeNode split()
	{
		int to = size();
		int from = (to + 1) / 2;

		LeafNode sibling = newLeafNode();
		sibling.setModified();
		sibling.keys.addAll(keys.subList(from, to));
		sibling.values.addAll(values.subList(from, to));

		keys.subList(from, to).clear();
		values.subList(from, to).clear();
		setModified();

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


	public void dump(Appendable out, String indent, int level) throws Exception
	{
		int sz = keys.size();
		for(int i=0; i<sz; i++)
		{
			K key = keys.get(i);
			
			for(int j=0; j<level; j++)
			{
				out.append(indent);
			}
			
			out.append("val=");
			out.append(key.toString());
			out.append("\n");
		}
	}
	
	
	public void dumpKeys(Appendable out, String indent, int level) throws Exception
	{
		int sz = keys.size();
		for(int i=0; i<sz; i++)
		{
			K k = keyAt(i);
			
			for(int j=0; j<level; j++)
			{
				out.append(indent);
			}
			
			out.append(k.toString());
			out.append("\n");
		}
	}
}