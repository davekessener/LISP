package dave.lisp.utils;

import java.util.Scanner;

import dave.lisp.common.LineReader;

public class UserInputLineReader implements LineReader
{
	private final Scanner mInput;
	
	public UserInputLineReader()
	{
		mInput = new Scanner(System.in);
	}

	@Override
	public boolean done()
	{
		return !mInput.hasNext();
	}

	@Override
	public String readLine()
	{
		return mInput.nextLine();
	}

	@Override
	public void close() throws Exception
	{
		mInput.close();
	}
}
