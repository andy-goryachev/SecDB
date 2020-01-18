// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import java.io.File;


/**
 * Utilities.
 */
public class Utils
{
	public static boolean isEmptyDir(File dir)
	{
		if(dir.isDirectory())
		{
			File[] fs = dir.listFiles();
			if(fs == null)
			{
				return true;
			}
			return fs.length == 0;
		}
		return false;
	}
}
