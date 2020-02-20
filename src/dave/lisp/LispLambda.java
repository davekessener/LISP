package dave.lisp;

import java.util.List;
import java.util.ArrayList;

import dave.lisp.error.LispError;

public class LispLambda extends LispIdentityObject implements LispCallable
{
	private final LispObject mArgNames;
	private final LispObject mBody;
	private final Environment mClosure;

	public LispLambda(LispObject args, LispObject body, Environment e)
	{
		mArgNames = args;
		mBody = body;
		mClosure = e;

		check();
	}

	protected LispObject arguments() { return mArgNames; }
	protected LispObject body() { return mBody; }
	protected Environment closure() { return mClosure; }

	@Override
	public Result call(LispObject a, Environment e)
	{
		a = LispRuntime.eval_all(a, e);
		e = new MultiplexEnvironment(build_closure(a), mClosure, e);

		return LispRuntime.eval(mBody, e);
	}

	@Override
	public String serialize()
	{
		return type() + "[" + mArgNames.serialize() + ", " + mBody.serialize() + ", " + mClosure + "]";
	}

	public String type()
	{
		return "LAMBDA";
	}

	private MapEnvironment build_closure_part(LispObject a, LispObject p)
	{
		if(a == null && p == null)
		{
			return new MapEnvironment();
		}
		else if(a == null || p == null)
		{
			throw new LispError("Mismatching arg lengths: %s vs %s!", a, p);
		}
		else if(!(p instanceof LispCell))
		{
			throw new LispError("LAMBDA args not a list: %s", p);
		}
		else if(!(a instanceof LispCell))
		{
			MapEnvironment e = new MapEnvironment();

			e.put(a, p);

			return e;
		}
		else
		{
			LispCell ac = (LispCell) a;
			LispCell pc = (LispCell) p;

			MapEnvironment e = build_closure_part(ac.cdr(), pc.cdr());

			e.put(ac.car(), pc.car());

			return e;
		}
	}

	protected Environment build_closure(LispObject args)
	{
		return build_closure_part(mArgNames, args);
	}

	private void checkNameBuf(List<String> names, String id)
	{
		if(names.contains(id))
			throw new LispError("Cannot have more than one '%s' as argument name in LAMBDA!", id);

		names.add(id);
	}

	private void checkArgs(int i, LispObject a, List<String> names)
	{
		if(a == null) return;

		String id = null;

		if(!(a instanceof LispCell))
		{
			if(!(a instanceof LispSymbol))
				throw new LispError("Malformed LAMBDA: arg %d is not a valid list! [%s]", i, a.serialize());

			id = ((LispSymbol) a).value();

			checkNameBuf(names, id);
		}
		else
		{
			LispCell cell = (LispCell) a;

			id = ((LispSymbol) cell.car()).value();

			checkNameBuf(names, id);

			if(!(cell.car() instanceof LispSymbol))
				throw new LispError("Malformed LAMBDA: arg %d is not a symbol! [%s]", i, cell.car().serialize());

			checkArgs(i + 1, cell.cdr(), names);
		}
	}

	private void check()
	{
		if(mArgNames == null)
			throw new LispError("LAMBDA arg-names cannot be NIL!");

		if(mBody == null)
			throw new LispError("LAMBDA body cannot be NIL!");

		if(mClosure == null)
			throw new LispError("LAMBDA closure cannot be NIL!");

		checkArgs(1, mArgNames, new ArrayList<>());
	}
}

