package dave.lisp.common;

import dave.lisp.detail.LispObject;

public interface Environment
{
	public abstract Result lookup(LispObject id);
}

