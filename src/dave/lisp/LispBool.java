package dave.lisp;

import dave.lisp.error.ParseError;

import dave.lisp.utils.CharBuf;

public class LispBool extends LispIdentityObject
{
	private final boolean mValue;

	private LispBool(boolean f)
	{
		mValue = f;
	}

	@Override
	public String serialize()
	{
		return String.format("#%c", mValue ? 'T' : 'F');
	}

	public static LispBool deserialize(CharBuf s)
	{
		if(s.top() != '#')
			throw new ParseError("Invalid bool %s", s.rest());

		s.pop();

		char c = s.top();

		s.pop();

		if(c == 'f' || c == 'F')
		{
			return FALSE;
		}
		else if(c == 't' || c == 'T')
		{
			return TRUE;
		}
		else
		{
			throw new ParseError("Invalid bool #%c%s", c, s.rest());
		}
	}

	public static final LispBool TRUE = new LispBool(true);
	public static final LispBool FALSE = new LispBool(false);
}

