// Copyright © 2019-2026 Andy Goryachev <andy@goryachev.com>
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
{
	public abstract boolean hasValue();
	
	public abstract R getRef();
	
	public abstract boolean isRef();
	
	public abstract IStored getStoredValue();
	
	/** returns underlying byte array, internal method */
	protected abstract byte[] getBytes();
	
	//
	
	protected final IStore<R> store;
	
	
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
		protected final R ref;
		
		
		public RefHolder(IStore store, R ref)
		{
			super(store);
			this.ref = ref;
		}
		
		
		@Override
		public String toString()
		{
			return "RefHolder[ref=" + ref + "]";
		}


		@Override
		public boolean hasValue()
		{
			return false;
		}
		
		
		@Override
		public IStored getStoredValue()
		{
			return new IStored()
			{
				@Override
				public long getLength()
				{
					long len = ref.getLength();
					return store.convertLength(len, false);
				}


				@Override
				public IStream getIStream() throws Exception
				{
					return store.load(ref);
				}
			};
		}


		@Override
		public R getRef()
		{
			return ref;
		}


		@Override
		public boolean isRef()
		{
			return true;
		}


		@Override
		protected byte[] getBytes()
		{
			return null;
		}
	}
	
	
	//
	
	
	public static class ValueHolder<R extends IRef> extends DataHolder<R>
	{
		protected final byte[] bytes;
		
		
		public ValueHolder(IStore store, byte[] bytes)
		{
			super(store);
			this.bytes = bytes;
		}
		
		
		@Override
		public String toString()
		{
			return "ValueHolder[size=" + bytes.length + "]";
		}


		@Override
		public boolean hasValue()
		{
			return true;
		}
		
		
		@Override
		public IStored getStoredValue()
		{
			return new IStored()
			{
				@Override
				public long getLength()
				{
					return bytes.length;
				}


				@Override
				public IStream getIStream()
				{
					return new ByteArrayIStream(bytes);
				}
			};
		}

		
		@Override
		public R getRef()
		{
			return null;
		}
		
		
		@Override
		public boolean isRef()
		{
			return false;
		}
		
		
		@Override
		protected byte[] getBytes()
		{
			return bytes;
		}
	}
}
