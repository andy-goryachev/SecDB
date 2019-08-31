// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;
import goryachev.common.util.Log;


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
	
	private SecDB db;
	
	
	public Transaction()
	{
	}
	
	
	public void submit(SecDB db)
	{
		db.submit(this);
	}
	
	
	protected void setDB(SecDB db)
	{
		if(this.db == null)
		{
			this.db = db;
		}
		else
		{
			throw new Error("transaction can be executed only once");
		}
	}
}
