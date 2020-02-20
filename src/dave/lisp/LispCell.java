package dave.lisp;

import dave.lisp.utils.CharBuf;

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
		LispObject f = LispRuntime.eval(mCar, e).value;

		if(!(f instanceof LispCallable))
		{
			throw new LispError("%s cannot be called!", f);
		}

		return ((LispCallable) f).call(mCdr, e);
	}

	@Override
	public String serialize()
	{
		StringBuffer sb = new StringBuffer();

		serialize(sb, this, true);

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

	private static void serialize(StringBuffer sb, LispObject self, boolean first)
	{
		if(self == null)
		{
			sb.append(first ? "NIL" : ")");
		}
		else if(self instanceof LispCell)
		{
			LispCell cell = (LispCell) self;

			sb.append(first ? '(' : ' ');
			sb.append(cell.mCar.serialize());
			serialize(sb, cell.mCdr, false);
		}
		else
		{
			sb.append(" . ");
			sb.append(self.serialize());
			sb.append(")");
		}
	}

	private static LispCell deserialize(CharBuf s, char popen)
	{
		LispObject car = LispObject.deserialize(s);

		if(!is_pclose(s.top(), popen))
		{
			if(!is_ws(s.top()))
				throw new ParseError("Expected whitespace: %s", s.rest());

			while(is_ws(s.top()))
			{
				s.pop();
			}

			if(!is_pclose(s.top(), popen))
			{
				return new LispCell(car, deserialize(s, popen));
			}
		}

		s.pop();

		return new LispCell(car, null);
	}

	private static boolean is_pclose(char c, char popen)
	{
		return (popen == '(' && c == ')') || (popen == '[' && c == ']') || (popen == '{' && c == '}');
	}

	private static boolean is_ws(char c) { return c == ' ' || c == '\t' || c == '\n'; }
}

