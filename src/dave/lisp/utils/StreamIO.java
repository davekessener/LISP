package dave.lisp.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import dave.lisp.common.IO;
import dave.lisp.error.LispError;

public class StreamIO implements IO
{
	private final InputStream mIn;
	private final OutputStream mOut;
	
	public StreamIO(InputStream in, OutputStream out)
	{
		mIn = in;
		mOut = out;
	}

	@Override
	public void write(byte[] a)
	{
		try
		{
			mOut.write(a);
		}
		catch(IOException e)
		{
			throw new LispError("Cannot write! [%s]", e.getMessage());
		}
	}

	@Override
	public byte[] read(int n)
	{
		try
		{
			return mIn.readNBytes(n);
		}
		catch(IOException e)
		{
			throw new LispError("Cannot read! [%s]", e.getMessage());
		}
	}

	@Override
	public void close()
	{
		try
		{
			mIn.close();
			mOut.close();
		}
		catch(IOException e)
		{
			throw new LispError("Failed to close IO! [%s]", e.getMessage());
		}
	}
	
	@Override
	public int available()
	{
		try
		{
			return mIn.available();
		}
		catch(IOException e)
		{
			throw new LispError("Cannot check available! [%s]", e.getMessage());
		}
	}
}
