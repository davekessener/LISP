package dave.lisp.utils;

import dave.lisp.error.ParseError;

public class CharBuf
{
	private final String mValue;
	private int mIdx;

	public CharBuf(String s)
	{
		mValue = s;
		mIdx = 0;
	}

	public boolean empty() { return mIdx == mValue.length(); }
	public String rest() { return mValue.substring(mIdx); }

	public char top()
	{
		if(empty())
			throw new ParseError("Unexpected EOI!");

		return mValue.charAt(mIdx);
	}

	public void pop()
	{
		if(empty())
			throw new ParseError("Unexpected EOI!");

		++mIdx;
	}
}

