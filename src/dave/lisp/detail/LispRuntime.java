package dave.lisp.detail;

import dave.lisp.utils.CharBuf;
import dave.lisp.common.Environment;
import dave.lisp.common.Result;
import dave.lisp.error.ParseError;

public class LispRuntime
{
	private Environment mState;
	
	public LispRuntime( ) { this(DEFAULT_ENV); }
	public LispRuntime(Environment e)
	{
		mState = e;
	}
	
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

	public static LispObject eval_all(LispObject x, Environment e)
	{
		if(x == null)
			return null;

		if(!(x instanceof LispCell))
			throw new IllegalArgumentException(x.getClass().getName());

		LispCell cell = (LispCell) x;
		Result r = eval(cell.car(), e);

		return new LispCell(r.value, eval_all(cell.cdr(), e));
	}

	public static LispObject parse(String s)
	{
		CharBuf cb = new CharBuf(s);
		LispObject o = LispObject.deserialize(cb);

		if(!cb.empty())
			throw new ParseError("trailing '%s'!", cb.rest());

		return o;
	}

	public static final Environment DEFAULT_ENV;

	static
	{
		MapEnvironment e = new MapEnvironment();

		e.put(new LispSymbol("DEFINE"), Builtins.DEFINE);
		e.put(new LispSymbol("BEGIN"), Builtins.BEGIN);
		e.put(new LispSymbol("LAMBDA"), Builtins.LAMBDA);
		e.put(new LispSymbol("MACRO"), Builtins.MACRO);
		e.put(new LispSymbol("IF"), Builtins.IF);
		e.put(new LispSymbol("NEGATIVE?"), Builtins.IS_NEGATIVE);
		e.put(new LispSymbol("LIST?"), Builtins.IS_LIST);
		e.put(new LispSymbol("+"), Builtins.ADD);
		e.put(new LispSymbol("-"), Builtins.SUB);
		e.put(new LispSymbol("*"), Builtins.MUL);
		e.put(new LispSymbol("/"), Builtins.DIV);

		DEFAULT_ENV = e;
	}
}

