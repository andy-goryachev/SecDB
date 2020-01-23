// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented.log;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Monotonic Time Stamp.
 */
public class MonotonicUniqueTimeStamp
{
	private final AtomicLong lastTime = new AtomicLong();
	
	
	public MonotonicUniqueTimeStamp()
	{
	}
	
	
	public MonotonicUniqueTimeStamp(long initialValue)
	{
		lastTime.set(initialValue);
	}

	
	public long nextTimeStamp()
	{
		long t = System.currentTimeMillis() * 1000;
		
		for(;;)
		{
			long last = lastTime.get();
			if(t < last)
			{
				t = last + 1;
			}
			
			if(lastTime.compareAndSet(last, t))
			{
				return t;
			}
		}
	}
}
