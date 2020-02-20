package dave.lisp;

public abstract class LispBuiltin extends LispIdentityObject implements LispCallable
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
	public Result call(LispObject a, Environment e)
	{
		if(mEvaluateArgs)
		{
			a = LispRuntime.eval_all(a, e);
		}

		return apply(a, e);
	}

	@Override
	public String serialize()
	{
		return "BUILTIN[" + mID + "]";
	}

	protected abstract Result apply(LispObject a, Environment e);
}

