package dave.lisp;

import java.util.List;
import java.util.ArrayList;

import dave.lisp.error.LispError;

public final class Builtins
{
	public static LispBuiltin DEFINE = new LispBuiltin("DEFINE", false) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			LispCell c1 = (LispCell) x;

			if(!(c1.cdr() instanceof LispCell))
				throw new LispError("Not a list: %s", c1.cdr());

			LispCell c2 = (LispCell) c1.cdr();

			if(c2.cdr() != null)
				throw new LispError("Trailing args in DEFINE: %x", c2.cdr());

			Result v = LispRuntime.eval(c2.car(), e);

			return new Result(v.value, new ExtendedEnvironment(c1.car(), v.value, e));
		}
	};

	public static LispBuiltin BEGIN = new LispBuiltin("BEGIN", false) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			Result r = new Result(null, e);

			while(x != null)
			{
				if(!(x instanceof LispCell))
					throw new LispError("Not a list! [%s]", x);

				LispCell c = (LispCell) x;

				r = LispRuntime.eval(c.car(), r.environment);
				x = c.cdr();
			}

			return r;
		}
	};

	public static LispBuiltin LAMBDA = new CallableBuiltin("LAMBDA") {
		@Override
		protected LispObject create(LispObject args, LispObject body, Environment e)
		{
			return new LispLambda(args, body, e);
		}
	};

	public static LispBuiltin MACRO = new CallableBuiltin("MACRO") {
		@Override
		protected LispObject create(LispObject args, LispObject body, Environment e)
		{
			return new LispMacro(args, body, e);
		}
	};

	public static abstract class CallableBuiltin extends LispBuiltin
	{
		public CallableBuiltin(String id)
		{
			super(id, false);
		}

		@Override
		protected Result apply(LispObject x, Environment e)
		{
			if(!(x instanceof LispCell))
				throw new LispError("No list! [%s]", x);

			LispCell c1 = (LispCell) x;

			if(!(c1.cdr() instanceof LispCell))
				throw new LispError("No list! [%s]", c1.cdr());

			LispCell c2 = (LispCell) c1.cdr();

			return new Result(create(c1.car(), c2.car(), e), e);
		}

		protected abstract LispObject create(LispObject args, LispObject body, Environment e);
	}

	public static LispBuiltin IF = new LispBuiltin("IF", false) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			LispCell c1 = (LispCell) x;

			if(!(c1.cdr() instanceof LispCell))
				throw new LispError("#T-branch missing in IF! [%s]", c1.cdr());

			LispCell c2 = (LispCell) c1.cdr();

			Result v = LispRuntime.eval(c1.car(), e);

			boolean r = !(v.value == null || v.value == LispBool.FALSE);

			if(r)
			{
				return LispRuntime.eval(c2.car(), e);
			}
			else if(c2.cdr() == null)
			{
				return new Result(null, e);
			}
			else if(c2.cdr() instanceof LispCell)
			{
				return LispRuntime.eval(((LispCell) c2.cdr()).car(), e);
			}
			else
			{
				throw new LispError("Malformed #F-branch! [%s]", c2.cdr());
			}
		}
	};

	public static LispBuiltin IS_NEGATIVE = new LispBuiltin("NEGATIVE?", true) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			LispCell c = (LispCell) x;

			if(!(c.car() instanceof LispNumber))
				throw new LispError("Not a number (NEGATIVE?) [%s]", c.car());

			if(c.cdr() != null)
				throw new LispError("Trailing arguments to NEGATIVE? [%s]", c.cdr());

			double v = ((LispNumber) c.car()).value();

			return new Result(from_bool(v < 0), e);
		}
	};

	public static LispBuiltin IS_LIST = new LispBuiltin("LIST?", true) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			LispCell c = (LispCell) x;

			return new Result(from_bool(c.car() instanceof LispCell), e);
		}
	};

	public static final LispBuiltin ADD = new AccumulativeNumericBuiltin("ADD", (a, b) -> (a + b));
	public static final LispBuiltin SUB = new MixedNumericBuiltin("SUB", (a, b) -> (a - b), v -> -v);
	public static final LispBuiltin MUL = new AccumulativeNumericBuiltin("MUL", (a, b) -> (a * b));
	public static final LispBuiltin DIV = new MixedNumericBuiltin("DIV", (a, b) -> (a / b), v -> (1.0 / v));

	public static abstract class NumericBuiltin extends LispBuiltin
	{
		public NumericBuiltin(String id) { super(id, true); }

		protected abstract double compute(double[] a);

		@Override
		protected Result apply(LispObject x, Environment e)
		{
			List<Double> v = new ArrayList<>();
			LispCell c = (LispCell) x;

			do
			{
				if(!(c.car() instanceof LispNumber))
					throw new LispError("Not a number! [%s]", c.car());

				LispNumber n = (LispNumber) c.car();

				c = (LispCell) c.cdr();

				v.add(n.value());
			}
			while(c != null);

			return new Result(new LispNumber(compute(toArray(v))), e);
		}
	}

	public static class AccumulativeNumericBuiltin extends NumericBuiltin
	{
		private final Accumulator mCallback;

		public AccumulativeNumericBuiltin(String id, Accumulator cb)
		{
			super(id);

			mCallback = cb;
		}

		@Override
		protected double compute(double[] a)
		{
			double v = a[0];

			for(int i = 1 ; i < a.length ; ++i)
			{
				v = mCallback.apply(v, a[i]);
			}

			return v;
		}
	}

	public static class MixedNumericBuiltin extends AccumulativeNumericBuiltin
	{
		private final Transformer mCallback;

		public MixedNumericBuiltin(String id, Accumulator f1, Transformer f2)
		{
			super(id, f1);

			mCallback = f2;
		}

		@Override
		protected double compute(double[] a)
		{
			if(a.length > 1)
			{
				return super.compute(a);
			}
			else
			{
				return mCallback.apply(a[0]);
			}
		}
	}

	public static interface Accumulator { double apply(double a, double b); }
	public static interface Transformer { double apply(double v); }

	public static boolean to_bool(LispObject o) { return !(o == null || o == LispBool.FALSE); }
	public static LispBool from_bool(boolean f) { return f ? LispBool.TRUE : LispBool.FALSE; }

	private static double[] toArray(List<Double> l)
	{
		double[] r = new double[l.size()];
		int i = 0;

		for(Double d : l)
		{
			r[i++] = d;
		}

		return r;
	}

	private Builtins( ) { }
}

