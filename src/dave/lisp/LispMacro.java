package dave.lisp;

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
		e = new MultiplexEnvironment(build_closure(a), closure(), e);

		Result r = LispRuntime.eval(body(), e);

		return LispRuntime.eval(r.value, r.environment);
	}
}

