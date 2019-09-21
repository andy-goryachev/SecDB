// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;
import goryachev.common.util.CKit;
import goryachev.common.util.CList;
import goryachev.common.util.CMap;
import goryachev.common.util.D;
import goryachev.common.util.Dump;
import goryachev.common.util.Hex;
import goryachev.common.util.SB;
import goryachev.secdb.IStore;
import goryachev.secdb.IStream;
import goryachev.secdb.Ref;
import goryachev.secdb.util.ByteArrayIStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;


/**
 * InMemory IStore.
 */
public class InMemoryStore
	implements IStore<Ref>
{
	private Ref rootRef;
	private long sequence;
	private final CMap<Ref,byte[]> objects = new CMap();
	
	
	public InMemoryStore()
	{
	}
	
	
	public String dump()
	{
		SB sb = new SB();
		
		sb.append("root=");
		sb.append(rootRef);
		
		CList<Ref> keys = objects.keys();
		Collections.sort(keys, new Comparator<Ref>()
		{
			public int compare(Ref a, Ref b)
			{
				int d = CKit.compare(a.getSegment(), b.getSegment());
				if(d == 0)
				{
					d = CKit.compare(a.getOffset(), b.getOffset());
				}
				return d;
			}
		});
		
		for(Ref k: keys)
		{
			byte[] b = objects.get(k);
			
			sb.nl();
			sb.append(k);
			sb.append("=");
			sb.append(Hex.toHexString(b));
		}
		return sb.toString();
	}


	public Ref getRootRef()
	{
		D.print("getRootRef", rootRef); // FIX
		return rootRef;
	}


	public void setRootRef(Ref ref) throws Exception
	{
		D.print("setRootRef", ref); // FIX
		rootRef = ref;
	}


	public synchronized Ref store(IStream in, boolean isTree) throws Exception
	{
		long seq = sequence++;
		long len = in.getLength(); 
		Ref ref = new Ref(null, seq, len);
		
		byte[] b = in.readBytes(Integer.MAX_VALUE);
		objects.put(ref, b);
		
		D.print("store", Dump.toHexString(b), ref); // FIX
		
		return ref;
	}


	public IStream load(Ref ref) throws Exception
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
}
