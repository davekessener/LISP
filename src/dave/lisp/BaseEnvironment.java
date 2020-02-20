package dave.lisp;

import dave.lisp.error.LispError;

public abstract class BaseEnvironment implements Environment
{
	protected String getID(LispObject id)
	{
		if(!(id instanceof LispSymbol))
			throw new LispError("Must use symbol as map-key: %s!", id);

		return ((LispSymbol) id).value();
	}
}

