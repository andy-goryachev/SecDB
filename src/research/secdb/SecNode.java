// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;
import goryachev.common.util.SKey;
import research.bplustree.BPlusTreeNode;
import research.bplustree.BPlusTreeNode.InternalNode;
import research.bplustree.BPlusTreeNode.LeafNode;


/**
 * SecDB B+ Tree Node implementation.
 */
public abstract class SecNode
	extends BPlusTreeNode<SKey,IStored>
{
	public static SecNode read(byte[] dec)
	{
		// TODO
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
		
	}
	
	
	//
	
	
	public static class SecInternalNode extends BPlusTreeNode.InternalNode<SKey,IStored>
	{
		
	}
}
