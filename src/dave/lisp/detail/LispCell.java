package dave.lisp.detail;

import dave.lisp.utils.CharBuf;
import dave.lisp.common.Environment;
import dave.lisp.common.Result;
import dave.lisp.error.LispError;
import dave.lisp.error.ParseError;

public class LispCell extends LispObject
{
	private final LispObject mCar, mCdr;

	public LispCell(LispObject car, LispObject cdr)
	{
		mCar = car;
		mCdr = cdr;
	}

	public LispObject car() { return mCar; }
	public LispObject cdr() { return mCdr; }

	@Override
	public Result evaluate(Environment e)
	{
		String name = null;
		LispObject f = LispRuntime.eval(mCar, e).value;

		if(!(f instanceof LispCallable))
		{
			throw new LispError("%s cannot be called!", f);
		}
		
		if(mCar instanceof LispSymbol)
		{
			name = ((LispSymbol) mCar).value();
		}

		return ((LispCallable) f).call(name, mCdr, e);
	}
	
	@Override
	public String serialize(boolean pretty)
	{
		StringBuffer sb = new StringBuffer();

		serialize(sb, pretty, this, true);

		return sb.toString();
	}

	public static LispCell deserialize(CharBuf s)
	{
		char popen = s.top();

		if(popen != '(' && popen != '[' && popen != '{')
			throw new ParseError("Invalid cell %s", s.rest());

		s.pop();

		return deserialize(s, popen);
	}

	private static void serialize(StringBuffer sb, boolean pretty, LispObject self, boolean first)
	{
		if(self == null)
		{
			sb.append(first ? "NIL" : ")");
		}
		else if(self instanceof LispCell)
		{
			LispCell cell = (LispCell) self;

			sb.append(first ? '(' : ' ');
			sb.append(LispObject.serialize(cell.mCar, pretty));
			serialize(sb, pretty, cell.mCdr, false);
		}
		else
		{
			sb.append(" . ");
			sb.append(self.serialize(pretty));
			sb.append(")");
		}
	}

	private static LispCell deserialize(CharBuf s, char popen)
	{
		skip_ws(s);
		
		if(is_pclose(s.top(), popen))
		{
			s.pop();
			
			return null;
		}
		
		LispObject car = LispObject.deserialize(s);
		LispObject cdr = null;

		if(!is_pclose(s.top(), popen))
		{
			if(!is_ws(s.top()))
				throw new ParseError("Expected whitespace: %s", s.rest());
			
			skip_ws(s);

			if(s.top() == '.')
			{
				s.pop();
				skip_ws(s);
				
				cdr = LispObject.deserialize(s);
				
				if(!is_pclose(s.top(), popen))
					throw new ParseError("Unexpected trailing chars after CDR: %s", s.rest());
			}
			else if(!is_pclose(s.top(), popen))
			{
				return new LispCell(car, deserialize(s, popen));
			}
		}

		s.pop();

		return new LispCell(car, cdr);
	}
	
	private static void skip_ws(CharBuf s) { while(!s.empty() && is_ws(s.top())) s.pop(); }

	private static boolean is_pclose(char c, char popen)
	{
		return (popen == '(' && c == ')') || (popen == '[' && c == ']') || (popen == '{' && c == '}');
	}

	private static boolean is_ws(char c) { return c == ' ' || c == '\t' || c == '\n'; }
}

