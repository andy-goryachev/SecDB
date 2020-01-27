// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;


/**
 * SecDB Error Code.
 */
public enum DBErrorCode
{
	/** create: directory is not empty */
	DIR_NOT_EMPTY,
	
	/** open: database directory not found or not a directory */
	DIR_NOT_FOUND,
	
	/** create: unable to create the base directory */
	DIR_UNABLE_TO_CREATE,
	
	/** open: no log file(s) found */
	MISSING_LOG_FILE,
	
	/** open: database may need recovery */
	RECOVERY_REQUIRED,
	
	;
}
