package dave.lisp.detail;

import dave.lisp.utils.CharBuf;
import dave.lisp.common.Environment;
import dave.lisp.common.Library;
import dave.lisp.common.Result;
import dave.lisp.error.ParseError;

public abstract class LispObject
{
	public Result evaluate(Environment e) { return new Result(this, e); }
	public abstract String serialize(boolean pretty);
	
	public final String serialize( ) { return serialize(false); }

	@Override
	public String toString()
	{
		return serialize();
	}
	
	public static String serialize(LispObject o) { return serialize(o, false); }
	public static String serialize(LispObject o, boolean pretty)
	{
		return (o == null ? "NIL" : o.serialize(pretty));
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
				return readChar(s);
				
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
	
	private static LispObject readChar(CharBuf s)
	{
		if(s.top() != '\'')
			throw new ParseError("Tried to read char! |%s", s.rest());
		
		s.pop();
		
		char c = s.top();
		
		s.pop();
		
		if(c == '\\')
		{
			char cc = s.top();
			
			s.pop();
			
			if(s.top() != '\'')
				throw new ParseError("Invalid escape sequence in char! '\\%c%s", cc, s.rest());
			
			switch(cc)
			{
				case 'n':
					c = '\n';
					break;
					
				case 'r':
					c = '\r';
					break;
					
				case 't':
					c = '\t';
					break;
					
				case '0':
					c = '\0';
					break;
					
				case '\'':
					c = '\'';
					break;
					
				default:
					throw new ParseError("Unknown escape sequence '\\%c%s", cc, s.rest());
			}
		}
		else if(s.top() != '\'')
		{
			s.unpop();
			
			return new LispCell(new LispSymbol("QUOTE"), deserialize(s));
		}
		
		s.pop();
		
		return new LispNumber(c);
	}
}

