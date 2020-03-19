package dave.lisp.detail;

import dave.lisp.common.Environment;
import dave.lisp.common.Result;
import dave.lisp.error.LispError;

public class LispProxy<T> extends LispObject
{
	private final T mContent;
	
	public LispProxy(T t)
	{
		mContent = t;
	}
	
	public T content() { return mContent; }
	
	@Override
	public Result evaluate(Environment e)
	{
		throw new LispError("Implementation detail cannot be evaluated!");
	}

	@Override
	public String serialize(boolean pretty)
	{
		return "***DETAIL***";
	}
}
