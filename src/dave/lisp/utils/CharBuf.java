package dave.lisp.utils;

import dave.lisp.error.EOIError;

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
			throw new EOIError();

		return mValue.charAt(mIdx);
	}

	public void pop()
	{
		if(empty())
			throw new EOIError();

		++mIdx;
	}
}

