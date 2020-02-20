package dave.lisp.standalone;

import java.util.Scanner;

import java.io.IOException;

import dave.lisp.Result;
import dave.lisp.LispObject;
import dave.lisp.LispRuntime;

import dave.lisp.error.LispError;
import dave.lisp.error.ParseError;

public class Main
{
	public static void main(String[] args)
	{
		System.out.println("DLisp v1");

		try (Scanner in = new Scanner(System.in))
		{
			Result runtime = new Result(null, LispRuntime.DEFAULT_ENV);

			while(true)
			{
				System.out.print("> ");
				System.out.flush();

				String line = in.nextLine();

				if(line.equalsIgnoreCase("quit")) break;

				LispObject expr = null;
				
				try
				{
					expr = LispRuntime.parse(line);
					runtime = LispRuntime.eval(expr, runtime.environment);

					System.out.println(runtime.value == null ? "NIL" : runtime.value.serialize());
				}
				catch(ParseError e)
				{
					System.out.println("Parse-Error: " + e.getMessage());
				}
				catch(LispError e)
				{
					System.out.println("Evaluation-Error: " + e.getMessage());
				}
			}
		}
		finally
		{
		}
	}
}

