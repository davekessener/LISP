package dave.lisp.common;

import dave.lisp.detail.LispObject;

public class Result
{
	public LispObject value = null;
	public Environment environment = null;

	public Result( ) { }
	public Result(LispObject value, Environment environment)
	{
		this.value = value;
		this.environment = environment;
	}
}

