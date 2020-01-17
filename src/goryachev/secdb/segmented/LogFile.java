// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import java.io.File;


/**
 * The purpose of Log File is to
 *   1. retain state between shutdowns
 *   2. assist in recovery
 */
public class LogFile
{
	protected final File dir;
	
	
	public LogFile(File dir)
	{
		this.dir = dir;
	}
}
