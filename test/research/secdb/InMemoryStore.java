// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;
import java.io.IOException;


/**
 * InMemory IStore.
 */
public class InMemoryStore
	implements IStore<Ref>
{
	public InMemoryStore()
	{
	}


	public Ref getRootRef()
	{
		return null;
	}


	public void setRootRef(Ref ref) throws Exception
	{
	}


	public Ref store(IStream in) throws Exception
	{
		return null;
	}


	public IStream load(Ref ref) throws Exception
	{
		return null;
	}


	public void open() throws Exception
	{
	}


	public void close() throws IOException
	{
	}
}
