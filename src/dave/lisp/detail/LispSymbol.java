package dave.lisp.detail;

import dave.lisp.utils.CharBuf;
import dave.lisp.common.Environment;
import dave.lisp.common.Result;
import dave.lisp.error.LispError;
import dave.lisp.error.ParseError;

public class LispSymbol extends LispObject
{
	private final String mValue;

	public LispSymbol(String v)
	{
		if(v.contains(" "))
			throw new LispError("ws in sym: %s", v);

		if(v.length() == 0)
			throw new LispError("empty symbol");

		mValue = v.toUpperCase();
	}

	public String value() { return mValue; }

	@Override
	public Result evaluate(Environment e)
	{
		Result r = e.lookup(this);

		if(r == null)
			throw new LispError("Unbound symbol %s!", mValue);

		return new Result(r.value, e);
	}

	@Override
	public String serialize()
	{
		return "'" + mValue;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof LispSymbol)
		{
			return mValue.equalsIgnoreCase(((LispSymbol) o).mValue);
		}
		
		return false;
	}

	public static LispSymbol deserialize(CharBuf s)
	{
		StringBuilder sb = new StringBuilder();

		do
		{
			sb.append(s.top());
			s.pop();
		}
		while(!s.empty() && is_sym(s.top()));

		String sym = sb.toString();

		if(sym.length() == 0)
			throw new ParseError("empty symbol @%s", s.rest());

		return new LispSymbol(sym);
	}

	private static boolean is_sym(char c)
	{
		return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9') || "!#$%^&*_+-=;:|,<.>/?~".contains("" + c);
	}
}

