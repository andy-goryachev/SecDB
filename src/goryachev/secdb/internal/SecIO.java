// Copyright © 2019-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.internal;
import goryachev.common.io.DReader;
import goryachev.common.io.DWriter;
import goryachev.common.io.DWriterBytes;
import goryachev.common.util.CKit;
import goryachev.common.util.SKey;
import goryachev.secdb.IRef;
import goryachev.secdb.IStore;
import goryachev.secdb.bplustree.BPlusTreeNode;
import goryachev.secdb.util.ByteArrayIStream;


/**
 * SecDB serializer/deserializer.
 */
public class SecIO
{
	/** marks DataHolder.REF instead of DataHolder.VAL */
	public static final int REF_MARKER = 255;
	/** size threshold below which small values are stored in the leaf node */
	public static final int MAX_INLINE_SIZE = REF_MARKER - 1;
	
	
	public static <R extends IRef> R store(IStore<R> store, BPlusTreeNode<SKey,DataHolder<R>> node) throws Exception
	{
		DWriterBytes wr = new DWriterBytes();
		try
		{
			if(node instanceof SecLeafNode)
			{
				SecLeafNode n = (SecLeafNode)node;
				int sz = n.size();
				wr.writeXInt8(sz);
				
				writeKeys(wr, sz, n);
				writeValues(store, wr, n);
			}
			else if(node instanceof SecInternalNode)
			{
				SecInternalNode n = (SecInternalNode)node;
				int sz = n.size();
				wr.writeXInt8(-sz);
				
				writeKeys(wr, sz, n);
				writeNodeRefs(store, wr, n);
			}
			else
			{
				throw new Error("?" + node);
			}
			
			byte[] b = wr.toByteArray();
			return store.store(new ByteArrayIStream(b), true);
		}
		finally
		{
			CKit.close(wr);
		}
	}
	
	
	public static <R extends IRef> BPlusTreeNode<SKey,DataHolder<R>> read(IStore<R> store, byte[] buf) throws Exception
	{
		DReader rd = new DReader(buf);
		try
		{
			// TODO perhaps add constructors that take arrays of (keys,refs/values)
			int sz = rd.readXInt8();
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
	
	
	private static void writeKeys(DWriter wr, int sz, BPlusTreeNode<SKey,DataHolder> n) throws Exception
	{
		for(int i=0; i<sz; i++)
		{
			SKey k = n.keyAt(i);
			String s = k.toString();
			wr.writeString(s);
		}
	}
	
	
	private static void readKeys(DReader rd, int sz, BPlusTreeNode<SKey,DataHolder> n) throws Exception
	{
		for(int i=0; i<sz; i++)
		{
			String s = rd.readString();
			SKey key = new SKey(s);
			n.addKey(key);
		}
	}
	
	
	private static void writeValues(IStore store, DWriter wr, SecLeafNode n) throws Exception
	{
		int sz = n.getValueCount();
		wr.writeUInt8(sz);
		
		for(int i=0; i<sz; i++)
		{
			DataHolder d = n.valueAt(i);
			writeDataHolder(store, d, wr);
		}
	}
	
	
	private static void readValues(IStore store, DReader rd, SecLeafNode n) throws Exception
	{
		int sz = rd.readUInt8();
		
		for(int i=0; i<sz; i++)
		{
			DataHolder d = readDataHolder(store, rd);
			n.addValue(d);
		}
	}
	
	
	private static <R extends IRef> void writeNodeRefs(IStore<R> store, DWriter wr, SecInternalNode n) throws Exception
	{
		int sz = n.getChildCount();
		wr.writeUInt8(sz);

		for(int i=0; i<sz; i++)
		{
			// data holder type = REF
			wr.writeUInt8(REF_MARKER);
			
			NodeHolder<R> h = n.nodeHolderAt(i);
			if(h.isModified())
			{
				// store node first
				R ref = store(store, h.getNode());
				store.writeRef(ref, wr);
			}
			else
			{
				// store ref
				R ref = h.getRef();
				store.writeRef(ref, wr);
			}
		}
	}
	
	
	private static void readNodeRefs(IStore store, DReader rd, SecInternalNode n) throws Exception
	{
		int sz = rd.readUInt8();
		for(int i=0; i<sz; i++)
		{
			DataHolder d = readDataHolder(store, rd);
			n.addChild(d);
		}
	}
	
	
	private static <R extends IRef> void writeDataHolder(IStore<R> store, DataHolder<R> d, DWriter wr) throws Exception
	{
		if(d.isRef())
		{
			wr.writeUInt8(REF_MARKER);
			store.writeRef(d.getRef(), wr);
		}
		else
		{
			byte[] b = d.getBytes();
			int len = b.length;
			if(len > MAX_INLINE_SIZE)
			{
				throw new Error("too long: " + len);
			}
			wr.writeUInt8(len);
			wr.write(b);
		}
	}
	
	
	private static <R extends IRef> DataHolder<R> readDataHolder(IStore<R> store, DReader rd) throws Exception
	{
		int sz = rd.readUInt8();
		if(sz == REF_MARKER)
		{
			R ref = store.readRef(rd);
			return new DataHolder.RefHolder(store, ref);
		}
		else
		{
			// inline value
			byte[] b = rd.readFully(sz);
			return new DataHolder.ValueHolder(store, b);
		}
	}
}
