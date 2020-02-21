package dave.lisp.detail;

import dave.lisp.utils.CharBuf;

import dave.lisp.error.ParseError;

public class LispString extends LispIdentityObject
{
	private final String mValue;

	public LispString(String v)
	{
		if(v == null) throw new NullPointerException();

		mValue = v;
	}

	@Override
	public String serialize()
	{
		return String.format("\"%s\"", mValue);
	}

	public static LispString deserialize(CharBuf s)
	{
		if(s.top() != '"')
			throw new ParseError("Invalid string: %s", s.rest());
		
		StringBuilder sb = new StringBuilder();

		s.pop();

		while(s.top() != '"')
		{
			if(s.top() == '\\')
			{
				s.pop();
			}

			sb.append(s.top());
			s.pop();
		}

		s.pop();

		return new LispString(sb.toString());
	}
}

