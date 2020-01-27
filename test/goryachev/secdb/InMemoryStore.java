// Copyright © 2019-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;
import goryachev.common.io.DReader;
import goryachev.common.io.DWriter;
import goryachev.common.util.CKit;
import goryachev.common.util.CList;
import goryachev.common.util.CMap;
import goryachev.common.util.D;
import goryachev.common.util.Dump;
import goryachev.common.util.Hex;
import goryachev.common.util.SB;
import goryachev.secdb.util.ByteArrayIStream;
import goryachev.secdb.util.Utils;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;


/**
 * InMemory IStore.
 */
public class InMemoryStore
	implements IStore<InMemoryRef>
{
	private InMemoryRef rootRef;
	private long sequence;
	private final CMap<InMemoryRef,byte[]> objects = new CMap();
	
	
	public InMemoryStore()
	{
	}
	
	
	public String dump()
	{
		SB sb = new SB();
		
		sb.append("root=");
		sb.append(rootRef);
		
		CList<InMemoryRef> keys = objects.keys();
		Collections.sort(keys, new Comparator<InMemoryRef>()
		{
			public int compare(InMemoryRef a, InMemoryRef b)
			{
				return CKit.compare(a.getSeq(), b.getSeq());
			}
		});
		
		for(InMemoryRef k: keys)
		{
			byte[] b = objects.get(k);
			
			sb.nl();
			sb.append(k);
			sb.append("=");
			sb.append(Hex.toHexString(b));
		}
		return sb.toString();
	}


	public InMemoryRef getRootRef()
	{
		D.print("getRootRef", rootRef); // FIX
		return rootRef;
	}


	public void setRootRef(InMemoryRef ref) throws Exception
	{
		D.print("setRootRef", ref); // FIX
		rootRef = ref;
	}


	public synchronized InMemoryRef store(IStream in, boolean isTree) throws Exception
	{
		long seq = sequence++;
		long len = in.getLength(); 
		InMemoryRef ref = new InMemoryRef(seq, len);
		
		byte[] b = Utils.readBytes(in, Integer.MAX_VALUE);
		objects.put(ref, b);
		
		D.print("store", Dump.toHexString(b), ref); // FIX
		
		return ref;
	}


	public IStream load(InMemoryRef ref) throws Exception
	{
		byte[] b = objects.get(ref);
		D.print("load", ref, Dump.toHexString(b)); // FIX
		if(b == null)
		{
			return null;
		}
		else
		{
			return new ByteArrayIStream(b);
		}
	}


	public void open() throws Exception
	{
	}


	public void close() throws IOException
	{
	}


	public void writeRef(InMemoryRef ref, DWriter wr) throws Exception
	{
		wr.writeLong(ref.getSeq());
		wr.writeLong(ref.getLength());
	}


	public InMemoryRef readRef(DReader rd) throws Exception
	{
		long seq = rd.readLong();
		long length = rd.readLong();
		return new InMemoryRef(seq, length);
	}
}
