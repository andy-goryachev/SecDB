// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;


/**
 * SecDB Error Code.
 */
public enum DBErrorCode
{
	/** create: directory is not empty */
	DIR_NOT_EMPTY,
	
	/** create: unable to create the base directory */
	DIR_UNABLE_TO_CREATE
}
