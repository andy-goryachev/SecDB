// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;
import goryachev.common.util.SKey;
import java.util.List;
import research.bplustree.BPlusTreeNode;


/**
 * SecDB B+ Tree Node implementation.
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
	protected BPlusTreeNode.InternalNode<SKey,IStored> newInternalNode()
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
	
	
	public static class SecInternalNode extends BPlusTreeNode.InternalNode<SKey,IStored>
	{
		public SecInternalNode()
		{
		}
		
		
		protected SecInternalNode(List<SKey> keys, List<BPlusTreeNode<SKey,IStored>> children)
		{
			super(keys, children);
		}
	}
}
