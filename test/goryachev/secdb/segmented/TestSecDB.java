// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import java.io.File;


/**
 * TestSecDB.
 */
public class TestSecDB
{
	public static void main(String[] args)
	{
		TF.run();
	}
	
	
	@Test
	public void test() throws Exception
	{
		File dir = new File("user.home/db-test");
		SecDB.create(dir, null);
	}
}
