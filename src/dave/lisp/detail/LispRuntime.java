package dave.lisp.detail;

import dave.lisp.utils.CharBuf;
import dave.lisp.utils.StreamIO;

import java.util.ArrayList;
import java.util.List;

import dave.lisp.common.Environment;
import dave.lisp.common.IO;
import dave.lisp.common.Library;
import dave.lisp.common.Result;
import dave.lisp.error.ParseError;

public class LispRuntime
{
	private Environment mState;
	private boolean mRunning;
	
	public LispRuntime()
	{
		MapEnvironment e = new MapEnvironment();

		List<IO> fds = new ArrayList<>();
		
		fds.add(new StreamIO(System.in, System.out));

		e.put(new LispSymbol(Library.Internals.FILE_HANDLES), new LispProxy<>(fds));
		e.put(new LispSymbol("quit"), new QuitBuiltin());
		
		mState = new MultiplexEnvironment(e, DEFAULT_ENV);
		mRunning = true;
	}
	
	public boolean running() { return mRunning; }
	
	public LispObject eval(LispObject x)
	{
		Result r = eval(x, mState);
		
		mState = r.environment;
		
		return r.value;
	}
	
	public static Result eval(LispObject x, Environment e)
	{
		if(x == null)
		{
			return new Result(null, e);
		}
		else
		{
			return x.evaluate(e);
		}
	}
	
	public static LispObject expand(LispObject x, Environment e)
	{
		if(!(x instanceof LispCell))
		{
			return x;
		}
		else
		{
			LispCell c = (LispCell) x;
			LispObject f = c.car();
			String name = null;
			
			if(f instanceof LispSymbol)
			{
				Result r = e.lookup(f);
				
				name = ((LispSymbol) f).value();
				
				if(r == null)
					return x;
				
				f = r.value;
			}
			
			if(!(f instanceof LispMacro))
			{
				return new LispCell(expand(c.car(), e), expand_rest(c.cdr(), e));
			}
			
			LispObject m = ((LispMacro) f).expand(name, c.cdr(), e);
			
			return expand(m, e);
		}
	}
	
	private static LispObject expand_rest(LispObject x, Environment e)
	{
		if(x instanceof LispCell)
		{
			LispCell c = (LispCell) x;
			
			return new LispCell(expand(c.car(), e), expand_rest(c.cdr(), e));
		}
		else
		{
			return expand(x, e);
		}
	}

	public static LispObject eval_all(LispObject x, Environment e)
	{
		if(x == null)
			return null;

		if(!(x instanceof LispCell))
		{
			return eval(x, e).value;
		}
		else
		{
			LispCell cell = (LispCell) x;
			Result r = eval(cell.car(), e);
	
			return new LispCell(r.value, eval_all(cell.cdr(), e));
		}
	}

	public static LispObject parse(String s)
	{
		CharBuf cb = new CharBuf(s);
		LispObject o = LispObject.deserialize(cb);

		if(!cb.empty())
			throw new ParseError("trailing '%s'!", cb.rest());

		return o;
	}
	
	private class QuitBuiltin extends LispBuiltin
	{
		private QuitBuiltin() { super("QUIT", true); }
		
		@Override
		protected Result apply(LispObject a, Environment e)
		{
			LispRuntime.this.mRunning = false;
			
			return new Result(LispBool.TRUE, e);
		}
	}

	private static final Environment DEFAULT_ENV;

	static
	{
		MapEnvironment e = new MapEnvironment();

		e.put("NIL", null);
		e.put("*STDIO*", new LispNumber(0));
		
		e.put("eval", Builtins.EVAL);
		e.put("QUOTE", Builtins.QUOTE);
		e.put(Library.Internals.MACRO_QUOTE, Builtins.MQUOTE);
		e.put("DEFINE", Builtins.DEFINE);
		e.put("BEGIN", Builtins.BEGIN);
		e.put("CONS", Builtins.CONS);
		e.put("CAR", Builtins.CAR);
		e.put("CDR", Builtins.CDR);
		e.put("LAMBDA", Builtins.LAMBDA);
		e.put("MACRO", Builtins.MACRO);
		e.put("MACRO-EXPAND", Builtins.MACRO_EXPAND);
		e.put("IF", Builtins.IF);
		e.put("WRITE", Builtins.WRITE);
		e.put("READ", Builtins.READ);
		e.put("FORMAT", Builtins.FORMAT);
		e.put("NEGATIVE?", Builtins.IS_NEGATIVE);
		e.put("LIST?", Builtins.IS_LIST);
		e.put("EQ?", Builtins.IS_EQ);
		e.put("ORD", Builtins.ORD);
		e.put("CHR", Builtins.CHR);
		e.put("STR-LEN", Builtins.STR_LEN);
		e.put("STR-APPEND", Builtins.STR_APPEND);
		e.put("STR-SERIALIZE", Builtins.STR_SER);
		e.put("STR-DESERIALIZE", Builtins.STR_DES);
		e.put("NOT", Builtins.NOT);
		e.put("AND", Builtins.AND);
		e.put("OR", Builtins.OR);
		e.put("+", Builtins.ADD);
		e.put("-", Builtins.SUB);
		e.put("*", Builtins.MUL);
		e.put("/", Builtins.DIV);

		DEFAULT_ENV = e;
	}
}

