// Copyright © 2020-2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.test.BeforeClass;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.CKit;
import goryachev.common.util.CList;
import goryachev.common.util.FileTools;
import goryachev.common.util.SKey;
import goryachev.crypto.OpaqueBytes;
import goryachev.crypto.OpaqueChars;
import goryachev.log.config.JsonLogConfig;
import goryachev.secdb.IStored;
import goryachev.secdb.IStream;
import goryachev.secdb.segmented.eax.EAXEncHelper;
import goryachev.secdb.segmented.xsalsa20poly1305.XSalsa20Poly1305EncHelper;
import java.io.File;
import java.security.SecureRandom;


/**
 * Test Encryption.
 */
public class TestEncryption
{
	protected static final File DIR = new File("user.home/encryption-test");
	protected static final String PASSPHRASE = "test";

	
	public static void main(String[] args)
	{
		TF.run();
	}
	
	
	@BeforeClass
	public static void initLog() throws Exception
	{
		JsonLogConfig.configure(CKit.readStringQuiet(TestLarge.class, "log-conf.json"));
		
		FileTools.deleteRecursively(DIR);
	}
	
	
	@Test
	public void test() throws Exception
	{
		boolean success = FileTools.deleteRecursively(DIR);
		if(!success)
		{
			throw new Exception("directory has not been removed: " + DIR);
		}
		
		SecureRandom random = new SecureRandom();
		
		byte[] clearKey = new byte[256/8];
		random.nextBytes(clearKey);
		
		OpaqueBytes key = new OpaqueBytes(clearKey);
		OpaqueChars passphrase = new OpaqueChars(PASSPHRASE.toCharArray());
		EncHelper helper = 
//			new XSalsa20Poly1305EncHelper(key);
			new EAXEncHelper(key);
				
		SecDB db;
		try
		{
			db = SecDB.open(DIR, helper, passphrase);
		}
		catch(SecException e)
		{
			switch(e.getErrorCode())
			{
			case DIR_NOT_FOUND:
				SecDB.create(DIR, key, passphrase, random);
				db = SecDB.open(DIR, helper, passphrase);
				break;
			default:
				throw e;
			}
		}
		
		CList<Throwable> errors = new CList();

		try
		{
			for(int i=0; i<3; i++)
			{
				String kv = "kv" + i;
				
				db.execute(new Transaction()
				{
					protected void body() throws Exception
					{
						insert(new SKey(kv), IStream.of(kv));
					}
	
					protected void onError(Throwable e)
					{
						e.printStackTrace();
						errors.add(e);
					}
				});
			}
		}
		finally
		{
			db.close();
		}
		
		if(errors.size() > 0)
		{
			for(Throwable e: errors)
			{
				TF.print(CKit.stackTrace(e));
			}
			
			throw new Error("encountered " + errors.size() + " errors");
		}
		
		// verify
		
		db = SecDB.open(DIR, helper, passphrase);
		
		try
		{
			for(int i=0; i<3; i++)
			{
				String kv = "kv" + i;
				
				IStored val = db.load(new SKey(kv));
				if(val == null)
				{
					throw new Exception("failed to load " + kv);
				}
				
				IStream in = val.getIStream();
				long len = in.getLength();
				if((len < 0) || (len > 100))
				{
					throw new Exception("invalid length: " + len);
				}
				
				byte[] buf = new byte[(int)len];
				CKit.readFully(in.getStream(), buf);
				String v = new String(buf, CKit.CHARSET_UTF8);
				TF.eq(kv, v);
			}
		}
		finally
		{
			db.close();
		}
	}
}
