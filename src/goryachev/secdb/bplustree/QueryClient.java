// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.bplustree;


/**
 * Query Client.
 */
public interface QueryClient<K,V>
{
	/** accepts query results.  the query is aborted when this callback returns false */
	public boolean acceptQueryResult(K key, V value);
	
	
	/** invoked if an exception gets thrown during the query */
	public void onError(Throwable err);
}