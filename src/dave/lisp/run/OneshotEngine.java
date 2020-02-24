package dave.lisp.run;

import dave.lisp.common.LineReader;
import dave.lisp.common.LispEngine;
import dave.lisp.detail.LispRuntime;
import dave.lisp.error.EOIError;
import dave.lisp.error.LispError;

public class OneshotEngine implements LispEngine
{
	@Override
	public void run(LineReader in)
	{
		LispRuntime runtime = new LispRuntime();
		String input = "";
		int lines = 0;
		
		try
		{
			while(!in.done())
			{
				++lines;
				input += " " + in.readLine();
				
				try
				{
					runtime.eval(LispRuntime.parse(input.strip()));
					
					input = "";
				}
				catch(EOIError e)
				{
				}
			}
			
			if(!input.isBlank())
			{
				System.err.printf("EOI: %s\n", input);
			}
		}
		catch(LispError e)
		{
			System.err.printf("Error in line %d: %s |%s", lines, fmt(e), input);
		}
		catch(Throwable e)
		{
			System.err.printf("CRITICAL FAILURE in line %d: %s [%s]", lines, e.getMessage(), input);
			
			e.printStackTrace();
		}
	}
	
	private static final String fmt(Throwable e)
	{
		StackTraceElement f = e.getStackTrace()[0];
		
		String file = f.getFileName();
		String klass = getClassName(f);
		String method = f.getMethodName();
		int line = f.getLineNumber();
		String msg = e.getMessage();
		
		return String.format("'%s' [%s:%d, %s::%s]", msg, file, line, klass, method);
	}
	
	private static final String getClassName(StackTraceElement f)
	{
		String c = f.getClassName();
		int i = c.lastIndexOf('.');
		
		if(i >= 0)
		{
			c = c.substring(i + 1);
		}
		
		return c;
	}
}
