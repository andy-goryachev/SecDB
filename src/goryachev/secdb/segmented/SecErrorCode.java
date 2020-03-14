// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;


/**
 * SecDB Error Code.
 */
public enum SecErrorCode
{
	/** create: directory is not empty */
	DIR_NOT_EMPTY,
	
	/** open: database directory not found or not a directory */
	DIR_NOT_FOUND,
	
	/** create: unable to create the base directory */
	DIR_UNABLE_TO_CREATE,
	
	/** open: failed to read the main key file */
	FAILED_KEY_FILE_READ,
	
	/** create: failed to save the main key file */
	FAILED_KEY_FILE_WRITE,
	
	/** open: no log file(s) found */
	MISSING_LOG_FILE,
	
	/** missing data segment file */
	MISSING_SEGMENT_FILE,
	
	/** open: database may need recovery */
	RECOVERY_REQUIRED,
	
	;
}
