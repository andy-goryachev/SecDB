// Copyright © 2019-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;
import goryachev.common.util.Log;
import goryachev.common.util.SKey;
import goryachev.secdb.bplustree.BPlusTreeNode;
import goryachev.secdb.internal.DataHolder;
import goryachev.secdb.util.Utils;
import goryachev.secdb.internal.DBEngineIO;


/**
 * Transaction base class.
 */
public abstract class DBTransaction<R extends IRef>
{
	protected abstract void body() throws Exception;
	
	protected void onSuccess() { }
	
	protected void onError(Throwable e) { Log.ex(e); }
	
	protected void onFinish() { }
	
	//
	
	private IStore<R> store;
	private BPlusTreeNode<SKey,DataHolder<R>> root;
	private int branchingFactor;
	
	
	public DBTransaction()
	{
	}
	
	
	// TODO read bytes
	
	
	public DataHolder read(SKey key) throws Exception
	{
		return root.getValue(key);
	}
	
	
	public boolean containsKey(SKey key) throws Exception
	{
		return root.containsKey(key);
	}
	
	
//	public void insert(SKey key, byte[] bytes) throws Exception
//	{
//		// TODO maybe
//	}
	
	
	public void insert(SKey key, IStream in) throws Exception
	{
		DataHolder<R> h;
		
		if(in.getLength() < DBEngineIO.MAX_INLINE_SIZE)
		{
			// store value inline
			byte[] b = Utils.readBytes(in, DBEngineIO.MAX_INLINE_SIZE);
			h = new DataHolder.ValueHolder(store, b);
		}
		else
		{
			// store value in a separate segment
			R ref = store.store(in, false);
			h = new DataHolder.RefHolder<R>(store, ref);
		}
		
		BPlusTreeNode<SKey,DataHolder<R>> newRoot = root.insertValue(root, key, h, branchingFactor);
		root = newRoot;
	}
	
	
	public void remove(SKey key) throws Exception
	{
		BPlusTreeNode<SKey,DataHolder<R>> newRoot = root.remove(root, key, branchingFactor);
		root = newRoot;
	}
	
	
	protected void setRoot(IStore<R> store, BPlusTreeNode<SKey,DataHolder<R>> root, int branchingFactor)
	{
		if(this.store == null)
		{
			this.store = store;
			this.root = root;
			this.branchingFactor = branchingFactor;
		}
		else
		{
			throw new Error("transaction can be executed only once");
		}	
	}
	
	
	protected BPlusTreeNode<SKey,DataHolder<R>> getRoot()
	{
		return root;
	}
}
