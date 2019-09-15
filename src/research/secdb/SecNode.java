// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;
import goryachev.common.io.DReader;
import goryachev.common.util.CKit;
import goryachev.common.util.CList;
import goryachev.common.util.SKey;
import java.util.List;
import research.bplustree.BPlusTreeNode;


/**
 * SecDB B+ Tree Node implementation that uses underlying IStore.
 */
public abstract class SecNode
{
	private final IStore store;


	protected SecNode(IStore store)
	{
		this.store = store;
	}
	
	
	public static BPlusTreeNode<SKey,DataHolder> read(IStore store, byte[] buf) throws Exception
	{
		DReader rd = new DReader(buf);
		try
		{
			int sz = rd.readInt();
			if(sz > 0)
			{
				// leaf node
				SecLeafNode n = new SecLeafNode();
				readKeys(rd, sz, n);
				// value refs
				return n;
			}
			else
			{
				// internal node
				SecInternalNode n = new SecInternalNode();
				sz = -sz;
				readKeys(rd, sz, n);
				// child node refs
				return n;
			}
		}
		finally
		{
			CKit.close(rd);
		}
	}
	
	
	private static void readKeys(DReader rd, int sz, BPlusTreeNode<SKey,DataHolder> n) throws Exception
	{
		for(int i=0; i<sz; i++)
		{
			String s = rd.readString();
			SKey key = new SKey(s);
			n.insertIndex(key);
		}
	}
	
	
	protected BPlusTreeNode.LeafNode<SKey,DataHolder> newLeafNode()
	{
		return new SecLeafNode();
	}
	
	
	protected BPlusTreeNode.InternalNode<SKey,DataHolder> newInternalNode()
	{
		return new SecInternalNode();
	}
	
	
	//
	
	
	public static class SecLeafNode extends BPlusTreeNode.LeafNode<SKey,DataHolder>
	{
		public SecLeafNode()
		{
		}
	}
	
	
	//
	
	
	public static class SecInternalNode extends BPlusTreeNode.InternalNode<SKey,DataHolder>
	{
		protected final List<BPlusTreeNode<SKey,DataHolder>> children;
		
		
		public SecInternalNode()
		{
			children = new CList();
		}
		
		
		protected void addChild(BPlusTreeNode<SKey,DataHolder> n)
		{
			children.add(n);
		}


		protected int getChildCount()
		{
			return children.size();
		}


		protected BPlusTreeNode<SKey,DataHolder> childAt(int ix)
		{
			return children.get(ix);
		}


		protected void removeChildAt(int ix)
		{
			children.remove(ix);
		}


		protected void setChild(int ix, BPlusTreeNode<SKey,DataHolder> n)
		{
			children.set(ix, n);
		}


		protected void addChild(int ix, BPlusTreeNode<SKey,DataHolder> n)
		{
			children.add(ix, n);
		}
	}
}
