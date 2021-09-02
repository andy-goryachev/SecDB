// Copyright © 2020-2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;


/**
 * SecDB Error Codes.
 */
public enum SecErrorCode
{
	/** create: directory is not empty */
	DIR_NOT_EMPTY,
	
	/** open: database directory not found or not a directory */
	DIR_NOT_FOUND,
	
	/** create: unable to create the base directory */
	DIR_UNABLE_TO_CREATE,
	
	/** open: no log file(s) found */
	MISSING_LOG_FILE,
	
	/** missing data segment file */
	MISSING_SEGMENT_FILE,
	
	/** open: database may need recovery */
	RECOVERY_REQUIRED;
	
	//
	
	public static SecErrorCode getSecErrorCode(Throwable e)
	{
		if(e instanceof SecException)
		{
			return ((SecException)e).getErrorCode();
		}
		return null;
	}
}
