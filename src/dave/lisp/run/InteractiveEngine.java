package dave.lisp.run;

import dave.lisp.common.LineReader;
import dave.lisp.common.LispEngine;
import dave.lisp.detail.LispObject;
import dave.lisp.detail.LispRuntime;
import dave.lisp.error.EOIError;
import dave.lisp.error.LispError;
import dave.lisp.error.ParseError;
import dave.util.log.Stdout;

public class InteractiveEngine implements LispEngine
{
	@Override
	public void run(LineReader in)
	{
		LispRuntime runtime = new LispRuntime();
		String input = "";
		
		while(runtime.running())
		{
			Stdout.printf(input.isEmpty() ? "> " : "* ");
			
			if(in.done()) break;
			
			String line = in.readLine().strip();
			
			if(line.isEmpty())
			{
				input = "";
				
				continue;
			}
			
			String o = (input + " " + line).strip();
			
			input = "";
			
			try
			{
				LispObject r = runtime.eval(LispRuntime.parse(o));
				
				Stdout.printf("%s\n", LispObject.serialize(r));
			}
			catch(EOIError e)
			{
				input = o;
			}
			catch(ParseError e)
			{
				Stdout.printf("Parse-Error [%s:%d] %s\n", e.getStackTrace()[0].getFileName(), e.getStackTrace()[0].getLineNumber(), e.getMessage());
			}
			catch(LispError e)
			{
				Stdout.printf("Evaluation-Error [%s:%d] %s\n", e.getStackTrace()[0].getFileName(), e.getStackTrace()[0].getLineNumber(), e.getMessage());
			}
		}
	}
}
