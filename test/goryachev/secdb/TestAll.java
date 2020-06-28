// Copyright © 2012-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;
import goryachev.common.test.TF;
import goryachev.secdb.bplustree.TestBPlusTree;
import goryachev.secdb.segmented.TestEAX;
import goryachev.secdb.segmented.TestEncryption;
import goryachev.secdb.segmented.TestLarge;
import goryachev.secdb.segmented.TOffsetIStream;
import goryachev.secdb.segmented.TestSecDB;
import goryachev.secdb.segmented.TestSegments;
import goryachev.secdb.segmented.TestStreams;


public class TestAll
{
	public static void main(String[] args)
	{
		TF.run
		(
			TestBPlusTree.class,
			TestEAX.class,
			TestEncryption.class,
			TestInMemoryStore.class,
//			TestLarge.class,
			TestSecDB.class,
			TestSegments.class,
			TestStreams.class
		);
	}
}
