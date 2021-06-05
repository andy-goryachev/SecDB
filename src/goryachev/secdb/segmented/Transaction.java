// Copyright © 2020-2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.secdb.DBTransaction;


/**
 * Transaction.
 */
public abstract class Transaction
	extends DBTransaction<Ref>
{
	protected abstract void body() throws Exception;
	
	protected void onSuccess() { }
	
	protected void onError(Throwable e) { log.error(e); }
	
	protected void onFinish() { }
}
