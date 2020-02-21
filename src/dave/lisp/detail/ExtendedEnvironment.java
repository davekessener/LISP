package dave.lisp.detail;

import dave.lisp.common.Environment;
import dave.lisp.common.Result;

public class ExtendedEnvironment extends BaseEnvironment
{
	private final String mKey;
	private final LispObject mValue;
	private final Environment mRest;

	public ExtendedEnvironment(LispObject k, LispObject v, Environment e)
	{
		mKey = getID(k);
		mValue = v;
		mRest = e;
	}

	@Override
	public Result lookup(LispObject k)
	{
		String id = getID(k);

		if(id.equalsIgnoreCase(mKey))
		{
			return new Result(mValue, this);
		}
		else
		{
			return mRest.lookup(k);
		}
	}

	@Override
	public String toString()
	{
		return "{" + mKey + " => " + mValue + ", " + mRest + "}";
	}
}

