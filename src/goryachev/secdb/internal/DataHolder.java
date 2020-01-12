// Copyright © 2019-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.internal;
import goryachev.secdb.IRef;
import goryachev.secdb.IStore;
import goryachev.secdb.IStored;
import goryachev.secdb.IStream;
import goryachev.secdb.util.ByteArrayIStream;


/**
 * Data Holder: stores a reference to and, if possible, the cached value of
 * - short inline value
 * - BPlusTreeNode
 * - large object (reference only)
 */
public abstract class DataHolder<R>
	implements IStored
{
	public abstract boolean hasValue();
	
	public abstract long getLength();
	
	public abstract IStream getIStream() throws Exception;
	
	public abstract R getRef();
	
	public abstract boolean isRef();
	
	/** returns underlying byte array, internal method */
	protected abstract byte[] getBytes();
	
	//
	
	private final IStore<R> store;
	
	
	public DataHolder(IStore<R> store)
	{
		this.store = store;
	}
	
	
	public IStore<R> getIStore()
	{
		return store;
	}
	

	//
	
	
	public static class RefHolder<R extends IRef> extends DataHolder<R>
	{
		private final R ref;
		
		
		public RefHolder(IStore store, R ref)
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


		public R getRef()
		{
			return ref;
		}


		public boolean isRef()
		{
			return true;
		}


		protected byte[] getBytes()
		{
			return null;
		}
	}
	
	
	//
	
	
	public static class ValueHolder<R extends IRef> extends DataHolder<R>
	{
		private final byte[] bytes;
		
		
		public ValueHolder(IStore store, byte[] bytes)
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
		
		
		public R getRef()
		{
			return null;
		}
		
		
		public boolean isRef()
		{
			return false;
		}
		
		
		protected byte[] getBytes()
		{
			return bytes;
		}
	}
}
