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
}
