package dave.lisp;

public abstract class LispIdentityObject extends LispObject
{
	@Override
	public Result evaluate(Environment e)
	{
		return new Result(this, e);
	}
}

