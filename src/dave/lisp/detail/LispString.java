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
	
	public String value() { return mValue; }

	@Override
	public String serialize()
	{
		return String.format("\"%s\"", mValue);
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof LispString)
		{
			return mValue.equals(((LispString) o).mValue);
		}
		
		return false;
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
				
				switch(s.top())
				{
					case 'n':
						sb.append('\n');
						break;
						
					case 't':
						sb.append('\t');
						break;
						
					case '"':
						sb.append('"');
						break;
						
					case '0':
						sb.append('\0');
						break;
						
					default:
						throw new ParseError("Invalid escape sequence: \\%c", s.top());
				}
			}
			else
			{
				sb.append(s.top());
			}
			
			s.pop();
		}

		s.pop();

		return new LispString(sb.toString());
	}
}

