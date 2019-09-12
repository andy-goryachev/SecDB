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
	extends BPlusTreeNode<SKey,IStored>
{
	private final IStore store;


	protected SecNode(IStore store)
	{
		this.store = store;
	}
	
	
	public static SecNode read(IStore store, byte[] buf) throws Exception
	{
		DReader rd = new DReader(buf);
		try
		{
			int sz = rd.readInt();
			if(sz > 0)
			{
				// leaf node
				// keys
				// values/refs
			}
			else
			{
				// internal node
				// keys
				// children refs
				sz = -sz;
			}
			return null;
		}
		finally
		{
			CKit.close(rd);
		}
	}
	
	
	@Override
	protected BPlusTreeNode.LeafNode<SKey,IStored> newLeafNode()
	{
		return new SecLeafNode();
	}
	
	
	@Override
	protected BPlusTreeNode.InternalNode newInternalNode()
	{
		return new SecInternalNode();
	}
	
	
	//
	
	
	public static class SecLeafNode extends BPlusTreeNode.LeafNode<SKey,IStored>
	{
		public SecLeafNode()
		{
		}
	}
	
	
	//
	
	
	public static class SecInternalNode extends BPlusTreeNode.InternalNode<SKey,NodeHolder>
	{
		protected final List<BPlusTreeNode<SKey,NodeHolder>> children;
		
		
		public SecInternalNode()
		{
			children = new CList();
		}
		
		
		protected void addChild(BPlusTreeNode<SKey,NodeHolder> n)
		{
			children.add(n);
		}


		protected int getChildCount()
		{
			return children.size();
		}


		protected BPlusTreeNode<SKey,NodeHolder> childAt(int ix)
		{
			// TODO
			return children.get(ix);
		}


		protected void removeChildAt(int ix)
		{
			children.remove(ix);
		}


		protected void setChild(int ix, BPlusTreeNode<SKey,NodeHolder> n)
		{
			children.set(ix, n);
		}


		protected void addChild(int ix, BPlusTreeNode<SKey,NodeHolder> n)
		{
			children.add(ix, n);
		}
	}
}
