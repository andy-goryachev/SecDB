// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;
import goryachev.common.util.Log;
import goryachev.common.util.SKey;
import research.bplustree.BPlusTreeNode;


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
	
	private BPlusTreeNode<SKey,IStored> root;
	
	
	public Transaction()
	{
	}
	
	
	// TODO
	// read, read bytes, write, contains
	
	
	public IStored read(SKey key) throws Exception
	{
		return root.getValue(key);
	}
	
	
	public boolean containsKey(SKey key)
	{
		return root.containsKey(key);
	}
	
	
	public void execute(SecDB db)
	{		
		db.execute(this);
	}
	
	
	protected void setRoot(BPlusTreeNode<SKey,IStored> root)
	{
		if(this.root == null)
		{
			this.root = root;
		}
		else
		{
			throw new Error("transaction can be executed only once");
		}	
	}
	
	
	protected BPlusTreeNode<SKey,IStored> getRoot()
	{
		return root;
	}
}
