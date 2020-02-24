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
	public Result call(LispObject a, Environment e)
	{
		Environment ex = new MultiplexEnvironment(build_closure(a), closure(), e);

		Result r = LispRuntime.eval(body(), ex);

		return LispRuntime.eval(r.value, e);
	}
}

