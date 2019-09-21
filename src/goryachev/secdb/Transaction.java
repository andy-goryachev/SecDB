// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;
import goryachev.common.util.Log;
import goryachev.common.util.SKey;
import goryachev.secdb.bplustree.BPlusTreeNode;
import goryachev.secdb.impl.DataHolder;
import goryachev.secdb.impl.SecIO;


/**
 * Transaction.
 */
public abstract class Transaction
{
	protected abstract void body() throws Exception;
	
	protected void onSuccess() { }
	
	protected void onError(Throwable e) { Log.ex(e); }
	
	protected void onFinish() { }
	
	//
	
	private IStore<Ref> store;
	private BPlusTreeNode<SKey,DataHolder> root;
	private int branchingFactor;
	
	
	public Transaction()
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
	
	
	public void insert(SKey key, IStream in) throws Exception
	{
		DataHolder h;
		
		if(in.getLength() < SecIO.MAX_INLINE_SIZE)
		{
			// store value inline
			byte[] b = in.readBytes(SecIO.MAX_INLINE_SIZE);
			h = new DataHolder.VAL(store, b);
		}
		else
		{
			// store value in a separate segment
			Ref ref = store.store(in, false);
			h = new DataHolder.REF(store, ref);
		}
		
		BPlusTreeNode<SKey,DataHolder> newRoot = root.insertValue(root, key, h, branchingFactor);
		root = newRoot;
	}
	
	
	public void execute(SecDB db)
	{		
		db.execute(this);
	}
	
	
	protected void setRoot(IStore<Ref> store, BPlusTreeNode<SKey,DataHolder> root, int branchingFactor)
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
	
	
	protected BPlusTreeNode<SKey,DataHolder> getRoot()
	{
		return root;
	}
}
