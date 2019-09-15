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
			// TODO check for max size
			if(sz > 0)
			{
				// leaf node
				SecLeafNode n = new SecLeafNode(store);
				readKeys(rd, sz, n);
				readValues(store, rd, n);
				return n;
			}
			else
			{
				// internal node
				SecInternalNode n = new SecInternalNode(store);
				sz = -sz;
				readKeys(rd, sz, n);
				readNodeRefs(store, rd, n);
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
	
	
	private static void readValues(IStore store, DReader rd, SecLeafNode n) throws Exception
	{
		int sz = rd.readInt();
		// TODO check for max size
		for(int i=0; i<sz; i++)
		{
			DataHolder d = readDataHolder(store, rd);
			n.addValue(d);
		}
	}
	
	
	private static void readNodeRefs(IStore store, DReader rd, SecInternalNode n) throws Exception
	{
		int sz = rd.readInt();
		// TODO check for max size
		for(int i=0; i<sz; i++)
		{
			DataHolder d = readDataHolder(store, rd);
			n.addChild(d);
		}
	}


	// TODO remove?
	private static Ref readRef(DReader rd) throws Exception
	{
		String segment = rd.readString();
		long offset = rd.readLong();
		long length = rd.readLong();
		return new Ref(segment, offset, length);
	}
	
	
	private static DataHolder readDataHolder(IStore store, DReader rd) throws Exception
	{
		int sz = rd.readInt8();
		if(sz == 0)
		{
			String segment = rd.readString();
			long offset = rd.readLong();
			long length = rd.readLong();
			Ref ref = new Ref(segment, offset, length);
			return new DataHolder.REF(store, ref);
		}
		else
		{
			// inline value
			byte[] b = rd.readFully(sz);
			return new DataHolder.VAL(store, b);
		}
	}
}
