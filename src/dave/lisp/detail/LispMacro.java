package dave.lisp.detail;

import dave.lisp.common.Environment;
import dave.lisp.common.Result;

public class LispMacro extends LispLambda
{
	public LispMacro(LispObject args, LispObject body, Environment e)
	{
		super(args, body, e);
	}

	@Override
	public String type()
	{
		return "MACRO";
	}

	@Override
	public Result call(String name, LispObject a, Environment e)
	{
		LispObject r = expand(name, a, e);
		
		return LispRuntime.eval(r, e);
	}
	
	public LispObject expand(String name, LispObject a, Environment e)
	{
		e = closure();
		
		if(name != null)
		{
			e = new ExtendedEnvironment(new LispSymbol(name), this, e);
		}
		
		e = new MultiplexEnvironment(build_closure(a), e);

		return LispRuntime.eval(body(), e).value;
	}
}

