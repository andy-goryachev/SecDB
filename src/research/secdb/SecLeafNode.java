// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;
import goryachev.common.util.SKey;
import research.bplustree.BPlusTreeNode;


/**
 * SecDB LeafNode.
 */
public class SecLeafNode
	extends BPlusTreeNode.LeafNode<SKey,DataHolder>
{
	private final IStore store;

	
	public SecLeafNode(IStore store)
	{
		this.store = store;
	}
	
	
	protected LeafNode<SKey,DataHolder> newLeafNode()
	{
		return new SecLeafNode(store);
	}
	
	
	protected InternalNode newInternalNode()
	{
		return new SecInternalNode(store);
	}
}