package dave.lisp.detail;

import dave.lisp.error.LispError;
import dave.lisp.error.ParseError;

import dave.lisp.utils.CharBuf;

public class LispNumber extends LispIdentityObject
{
	private final double mValue;

	public LispNumber(double v)
	{
		mValue = v;
	}

	public double value() { return mValue; }
	
	public int integer()
	{
		int r = (int) mValue;
		
		if(mValue != r)
			throw new LispError("Not an integer! [%s]", this);
		
		return r;
	}

	@Override
	public String serialize()
	{
		int v = (int) mValue;
		
		return (v == mValue ? String.format("%d", v) : String.format("%.3f", mValue));
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof LispNumber)
		{
			return mValue == ((LispNumber) o).mValue;
		}
		
		return false;
	}

	public static LispNumber deserialize(CharBuf s)
	{
		double v = 0;
		double f = 0;

		if(!is_digit(s.top()))
			throw new ParseError("Not a number: %s", s.rest());

		while(!s.empty())
		{
			if(is_digit(s.top()))
			{
				int i = (s.top() - '0');

				if(f == 0)
				{
					v = v * 10 + i;
				}
				else
				{
					f *= 0.1;
					v += f * i;
				}
			}
			else if(s.top() == '.')
			{
				if(f == 0)
				{
					f = 1;
				}
				else
				{
					throw new ParseError("Double period in number @%s", s.rest());
				}
			}
			else
			{
				break;
			}

			s.pop();
		}

		return new LispNumber(v);
	}

	private static boolean is_digit(char c) { return '0' <= c && c <= '9'; }
}

