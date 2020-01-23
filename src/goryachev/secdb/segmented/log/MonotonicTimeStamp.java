// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented.log;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Monotonic Time Stamp.
 */
public class MonotonicTimeStamp
{
	private final AtomicLong lastTime = new AtomicLong();
	
	
	public MonotonicTimeStamp()
	{
	}

	
	public long monotonicTimeStamp()
	{
		long now = System.currentTimeMillis() * 1000;
		
		for(;;)
		{
			long last = lastTime.get();
			if(now < last)
			{
				now = last + 1;
			}
			
			if(lastTime.compareAndSet(last, now))
			{
				return now;
			}
		}
	}
	
	
	public void initialize(long ms)
	{
		lastTime.set(ms + 1);
	}
}
