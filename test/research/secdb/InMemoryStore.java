// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;
import goryachev.common.util.CMap;
import java.io.IOException;


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


	public Ref getRootRef()
	{
		return rootRef;
	}


	public void setRootRef(Ref ref) throws Exception
	{
		rootRef = ref;
	}


	public synchronized Ref store(IStream in) throws Exception
	{
		long seq = sequence++;
		long len = in.getLength(); 
		Ref ref = new Ref(null, seq, len);
		
		byte[] b = in.readBytes(Integer.MAX_VALUE);
		objects.put(ref, b);
		return ref;
	}


	public IStream load(Ref ref) throws Exception
	{
		byte[] b = objects.get(ref);
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
