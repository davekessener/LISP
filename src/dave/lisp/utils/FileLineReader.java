package dave.lisp.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dave.json.SevereIOException;
import dave.lisp.common.LineReader;

public class FileLineReader implements LineReader
{
	private final List<String> mContent;
	private final Iterator<String> mI;
	
	public FileLineReader(String fn)
	{
		try
		{
			mContent = new ArrayList<>();
			
			Files.readAllLines(Paths.get(fn)).forEach(s -> {
				s = s.strip();
				
				if(!s.isEmpty() && s.charAt(0) != ';')
				{
					mContent.add(s);
				}
				else
				{
					mContent.add("");
				}
			});
			
			mI = mContent.iterator();
		}
		catch(IOException e)
		{
			throw new SevereIOException(e);
		}
	}

	@Override
	public boolean done()
	{
		return !mI.hasNext();
	}

	@Override
	public String readLine()
	{
		return mI.next();
	}

	@Override
	public void close() throws Exception
	{
	}
}
