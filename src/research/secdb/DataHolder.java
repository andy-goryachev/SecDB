// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;

/**
 * Data Holder: stores a reference to and, if possible, the cached value of
 * - short inline value
 * - BPlusTreeNode
 * - large object (reference only)
 */
public abstract class DataHolder
	implements IStored
{
	public abstract boolean hasValue();
	
	public abstract long getLength();
	
	public abstract IStream getIStream() throws Exception;
	
	//
	
	private final IStore<Ref> store;
	
	
	public DataHolder(IStore<Ref> store)
	{
		this.store = store;
	}
	
	
	public IStore<Ref> getIStore()
	{
		return store;
	}
	

	//
	
	
	public static class REF extends DataHolder
	{
		private final Ref ref;
		
		
		public REF(IStore store, Ref ref)
		{
			super(store);
			this.ref = ref;
		}


		public boolean hasValue()
		{
			return false;
		}


		public long getLength()
		{
			return ref.getLength();
		}


		public IStream getIStream() throws Exception
		{
			return getIStore().load(ref);
		}
	}
	
	
	//
	
	
	public static class VAL extends DataHolder
	{
		private final byte[] bytes;
		
		
		public VAL(IStore store, byte[] bytes)
		{
			super(store);
			this.bytes = bytes;
		}


		public boolean hasValue()
		{
			return true;
		}


		public long getLength()
		{
			return bytes.length;
		}


		public IStream getIStream()
		{
			return new ByteArrayIStream(bytes);
		}
	}
}
