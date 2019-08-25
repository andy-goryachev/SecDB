// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;
import java.io.IOException;


/**
 * Low Level Storage Interface.
 */
public interface IStore
{
	public void open() throws Exception;
	
	public void close() throws IOException;
}
