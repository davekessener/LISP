package dave.lisp.detail;

import dave.lisp.common.Environment;
import dave.lisp.common.Result;

public abstract class LispIdentityObject extends LispObject
{
	@Override
	public Result evaluate(Environment e)
	{
		return new Result(this, e);
	}
}

