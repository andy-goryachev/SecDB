// Copyright © 2019-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;


/**
 * Query Client.
 */
@FunctionalInterface
public interface QueryClient<K,V>
{
	/** accepts query results.  the query is aborted when this callback returns false */
	public boolean acceptQueryResult(K key, V value) throws Exception;
}