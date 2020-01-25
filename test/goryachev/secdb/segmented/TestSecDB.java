// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import java.io.File;


/**
 * Test SecDB.
 */
public class TestSecDB
{
	private static final File DIR = new File("user.home/db-test");
	
	
	public static void main(String[] args)
	{
		TF.run();
	}
	
	
//	@Test
	public void testCreate() throws Exception
	{
		SecDB.create(DIR, null);
	}
	
	
	@Test
	public void testOpen() throws Exception
	{
		SecDB db = SecDB.open(DIR, null);
		db.close();
	}
}
