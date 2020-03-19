package dave.lisp.detail;

import java.util.List;

import dave.lisp.common.Environment;
import dave.lisp.common.Result;

import java.util.Arrays;

public class MultiplexEnvironment extends BaseEnvironment
{
	private final List<Environment> mSubs;

	public MultiplexEnvironment(Environment ... e)
	{
		mSubs = Arrays.asList(e);
	}

	@Override
	public Result lookup(LispObject id)
	{
		for(Environment e : mSubs)
		{
			Result r = e.lookup(id);

			if(r != null)
			{
				return r;
			}
		}

		return null;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append('{');

		boolean first = true;
		for(Environment e : mSubs)
		{
			if(!first) sb.append(", ");
			sb.append(e.toString());
			first = false;
		}

		sb.append('}');

		return sb.toString();
	}
}

