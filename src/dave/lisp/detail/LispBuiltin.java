package dave.lisp.detail;

import dave.lisp.common.Environment;
import dave.lisp.common.Result;

public abstract class LispBuiltin extends LispObject implements LispCallable
{
	private final String mID;
	private final boolean mEvaluateArgs;

	protected LispBuiltin(String id, boolean e)
	{
		mID = id;
		mEvaluateArgs = e;
	}

	public String id() { return mID; }

	@Override
	public Result call(String name, LispObject a, Environment e)
	{
		if(mEvaluateArgs)
		{
			a = LispRuntime.eval_all(a, e);
		}

		return apply(a, e);
	}

	@Override
	public String serialize(boolean pretty)
	{
		return pretty ? mID : ("BUILTIN[" + mID + "]");
	}

	protected abstract Result apply(LispObject a, Environment e);
}

