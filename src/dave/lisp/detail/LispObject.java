package dave.lisp.detail;

import dave.lisp.utils.CharBuf;
import dave.lisp.common.Environment;
import dave.lisp.common.Library;
import dave.lisp.common.Result;
import dave.lisp.error.ParseError;

public abstract class LispObject
{
	public abstract Result evaluate(Environment e);
	public abstract String serialize( );

	@Override
	public String toString()
	{
		return serialize();
	}
	
	public static String serialize(LispObject o)
	{
		return (o == null ? "NIL" : o.serialize());
	}

	public static LispObject deserialize(CharBuf s)
	{
		char c = s.top();

		if('0' <= c && c <= '9')
		{
			return LispNumber.deserialize(s);
		}
		else switch(c)
		{
			case '\'':
				s.pop();
				return new LispCell(new LispSymbol("QUOTE"), deserialize(s));
				
			case '`':
				s.pop();
				return new LispCell(new LispSymbol(Library.Internals.MACRO_QUOTE), deserialize(s));
			
			case ',':
				s.pop();
				return new LispCell(new LispSymbol(Library.Internals.MACRO_UNQUOTE), deserialize(s));
				
			case '-':
			{
				LispSymbol sym = LispSymbol.deserialize(s);
				CharBuf tmp = new CharBuf(sym.value());

				tmp.pop();

				if(!tmp.empty() && ('0' <= tmp.top() && tmp.top() <= '9'))
				{
					LispNumber num = LispNumber.deserialize(tmp);

					if(tmp.empty())
					{
						return new LispNumber(-num.value());
					}
				}

				return sym;
			}

			case '(':
			case '[':
			case '{':
				return LispCell.deserialize(s);
			
			case ' ':
			case '\t':
			case '\n':
				throw new ParseError("Unexpected %s", s.rest());

			case '"':
				return LispString.deserialize(s);

			case '#':
				return LispBool.deserialize(s);

			default:
				return LispSymbol.deserialize(s);
		}
	}
}

