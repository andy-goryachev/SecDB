// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;
import goryachev.common.io.DReader;
import goryachev.common.util.CKit;
import goryachev.common.util.SKey;
import research.bplustree.BPlusTreeNode;


/**
 * SecDB serializer/deserializer.
 */
public class SecIO
{
	public static BPlusTreeNode<SKey,DataHolder> read(IStore store, byte[] buf) throws Exception
	{
		DReader rd = new DReader(buf);
		try
		{
			int sz = rd.readInt();
			if(sz > 0)
			{
				// leaf node
				SecLeafNode n = new SecLeafNode(store);
				readKeys(rd, sz, n);
				// value refs
				return n;
			}
			else
			{
				// internal node
				SecInternalNode n = new SecInternalNode(store);
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
}
