package dave.lisp.error;

import java.lang.RuntimeException;

public class LispError extends RuntimeException
{
	public static final long serialVersionUID = 221203499760768037l;

	public LispError(String f, Object ... a) { super(String.format(f, a)); }
}

