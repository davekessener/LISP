package dave.lisp.detail;

import dave.lisp.common.Environment;
import dave.lisp.common.Result;

public interface LispCallable
{
	public abstract Result call(String name, LispObject x, Environment e);
}

