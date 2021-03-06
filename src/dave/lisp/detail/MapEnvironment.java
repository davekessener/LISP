package dave.lisp.detail;

import java.util.Map;

import dave.lisp.common.Result;

import java.util.HashMap;

public class MapEnvironment extends BaseEnvironment
{
	private final Map<String, Result> mBindings;

	public MapEnvironment()
	{
		mBindings = new HashMap<>();
	}

	@Override
	public Result lookup(LispObject id)
	{
		return mBindings.get(getID(id).toUpperCase());
	}

	public void put(LispObject id, LispObject v) { put(getID(id), v); }
	public void put(String id, LispObject v)
	{
		mBindings.put(id.toUpperCase(), new Result(v, this));
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append('{');

		boolean first = true;
		for(Map.Entry<String, Result> e : mBindings.entrySet())
		{
			if(!first) sb.append(", ");
			sb.append(e.getKey()).append(" => ").append(LispObject.serialize(e.getValue().value));
			first = false;
		}

		sb.append('}');

		return sb.toString();
	}
}

