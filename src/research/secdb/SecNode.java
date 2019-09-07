// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;
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
	public static SecNode read(byte[] dec)
	{
		// TODO int size
		// positive: leaf node
		// negative: internal node
		// TODO keys
		// TODO IStored: inline, ref, length
		return null;
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
		
		
		protected SecLeafNode(List<SKey> keys, List<IStored> values)
		{
			super(keys, values);
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
		
		
		protected SecInternalNode(List<SKey> keys, List<BPlusTreeNode<SKey,NodeHolder>> children)
		{
			super(keys);
			this.children = children;
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
