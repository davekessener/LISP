package dave.lisp.detail;

import java.util.List;
import java.util.ArrayList;

import dave.lisp.common.Environment;
import dave.lisp.common.IO;
import dave.lisp.common.Library;
import dave.lisp.common.Result;
import dave.lisp.error.LispError;

public final class Builtins
{
	public static LispBuiltin QUOTE = new LispBuiltin("QUOTE", false) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			return new Result(x, e);
		}
	};

	public static LispBuiltin MQUOTE = new LispBuiltin("MACRO-QUOTE", false) {
		private Environment mEnv;
		
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			mEnv = e;
			
			return new Result(evl(x), e);
		}
		
		private LispObject evl(LispObject x)
		{
			if(x instanceof LispCell)
			{
				LispCell c = (LispCell) x;
				
				if((c.car() instanceof LispSymbol) && ((LispSymbol) c.car()).value().equalsIgnoreCase(Library.Internals.MACRO_UNQUOTE))
				{
					return LispRuntime.eval(c.cdr(), mEnv).value;
				}
				else
				{
					return evlCell(c);
				}
			}
			else
			{
				return x;
			}
		}
		
		private LispObject evlCell(LispCell c)
		{
			if(c == null)
				return null;
			
			LispObject car = evl(c.car());
			
			if(c.cdr() instanceof LispCell)
			{
				return new LispCell(car, evlCell((LispCell) c.cdr()));
			}
			else
			{
				return new LispCell(car, evl(c.cdr()));
			}
		}
	};

	public static LispBuiltin DEFINE = new LispBuiltin("DEFINE", false) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			List<LispObject> a = new ArrayList<>();
			
			toList(x, a, 2, 2, false);
			
			Result v = LispRuntime.eval(a.get(1), e);

			return new Result(a.get(0), new ExtendedEnvironment(a.get(0), v.value, e));
		}
	};

	public static LispBuiltin BEGIN = new LispBuiltin("BEGIN", false) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			Result r = new Result(null, e);
			List<LispObject> a = new ArrayList<>();
			
			toList(x, a, 0, Integer.MAX_VALUE, false);
			
			for(LispObject o : a)
			{
				r = LispRuntime.eval(o, r.environment);
			}

			return r;
		}
	};
	
	public static LispBuiltin CONS = new LispBuiltin("CONS", true) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			List<LispObject> a = new ArrayList<>();
			
			toList(x, a, 2, 2, false);
			
			return new Result(new LispCell(a.get(0), a.get(1)), e);
		}
	};
	
	public static LispBuiltin CAR = new LispBuiltin("CAR", true) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			LispCell c = (LispCell) x;
			
			if(!(c.car() instanceof LispCell))
				throw new LispError("Cannot take CAR of non-list object! [%s]", x);
			
			if(c.cdr() != null)
				throw new LispError("Trailing args! [%s]", x);
			
			c = (LispCell) c.car();
			
			return new Result(c.car(), e);
		}
	};
	
	public static LispBuiltin CDR = new LispBuiltin("CDR", true) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			LispCell c = (LispCell) x;
			
			if(!(c.car() instanceof LispCell))
				throw new LispError("Cannot take CDR of non-list object! [%s]", x);
			
			if(c.cdr() != null)
				throw new LispError("Trailing args! [%s]", x);
			
			c = (LispCell) c.car();
			
			return new Result(c.cdr(), e);
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
	
	public static LispBuiltin WRITE = new LispBuiltin("WRITE", true) {
		@SuppressWarnings("unchecked")
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			List<LispObject> a = new ArrayList<>();
			
			toList(x, a, 1, Integer.MAX_VALUE, false);
			
			if(!(a.get(0) instanceof LispNumber))
				throw new LispError("Not a valid file-handle: %s", a.get(0));
			
			LispNumber dfh = (LispNumber) a.get(0);
			int fh = (int) dfh.value();
			
			if(fh < 0 || dfh.value() != fh)
				throw new LispError("File-handle must be integral, non-negative values: %s", dfh);
			
			Result lfds = e.lookup(new LispSymbol(Library.Internals.FILE_HANDLES));
			
			if(lfds == null)
				throw new LispError("File-handles cannot be found!");
			
			if(!(lfds.value instanceof LispProxy))
				throw new LispError("File-handles invalid!");
			
			List<IO> fds = ((LispProxy<List<IO>>) lfds.value).content();
			
			if(fh >= fds.size())
				throw new LispError("Invalid file-handle %d!", fh);
			
			IO io = fds.get(fh);
			
			if(io == null)
				throw new LispError("File already closed! (%d)", fh);
			
			for(int j = 1 ; j < a.size() ; ++j)
			{
				LispObject o = a.get(j);
				
				if(o instanceof LispString)
				{
					io.write(((LispString) o).value().getBytes());
				}
				else if(o instanceof LispCell)
				{
					List<LispObject> l = new ArrayList<>();
					
					toList(o, l, 0, Integer.MAX_VALUE, false);
					
					byte[] bytes = new byte[l.size()];
					
					int i = 0;
					for(LispObject oo : l)
					{
						if(!(oo instanceof LispNumber))
							throw new LispError("Not a number! [%s]", oo);
						
						LispNumber num = (LispNumber) oo;
						int v = (int) num.value();
						
						if(v < 0 || v > 0xFF || num.value() != v)
							throw new LispError("Not a byte! [%s]", oo);
						
						bytes[i++] = (byte) v;
					}
					
					io.write(bytes);
				}
				else
				{
					throw new LispError("Cannot write this to file: [%s]", o);
				}
			}
			
			return new Result(null, e);
		}
	};
	
	public static LispBuiltin READ = new LispBuiltin("READ", true) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			throw new UnsupportedOperationException();
		}
	};
	
	public static LispBuiltin FORMAT = new LispBuiltin("FORMAT", true) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			List<LispObject> a = new ArrayList<>();
			
			toList(x, a, 1, Integer.MAX_VALUE, false);
			
			if(!(a.get(0) instanceof LispString))
				throw new LispError("Format string is not a string! [%s]", x);
			
			String f = ((LispString) a.get(0)).value();
			
			Object[] fa = new Object[a.size() - 1];
			
			for(int i = 0 ; i < fa.length ; ++i)
			{
				fa[i] = convert(a.get(i + 1));
			}
			
			return new Result(new LispString(String.format(f, fa)), e);
		}
		
		private Object convert(LispObject o)
		{
			if(o instanceof LispNumber)
			{
				double vd = ((LispNumber) o).value();
				int vi = (int) vd;
				
				return (vd == vi ? (Object) vi : (Object) vd);
			}
			else if(o instanceof LispString)
			{
				return ((LispString) o).value();
			}
			else
			{
				return LispObject.serialize(o);
			}
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
			List<LispObject> a = new ArrayList<>();
			
			toList(x, a, 2, 2, false);

			return new Result(create(a.get(0), a.get(1), e), e);
		}

		protected abstract LispObject create(LispObject args, LispObject body, Environment e);
	}

	public static LispBuiltin IF = new LispBuiltin("IF", false) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			List<LispObject> a = new ArrayList<>();
			
			toList(x, a, 2, 3, false);
			
			Result v = LispRuntime.eval(a.get(0), e);

			boolean r = !(v.value == null || v.value == LispBool.FALSE);

			if(r)
			{
				return LispRuntime.eval(a.get(1), e);
			}
			else if(a.size() == 2)
			{
				return new Result(null, e);
			}
			else
			{
				return LispRuntime.eval(a.get(2), e);
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
				throw new LispError("Trailing arguments to NEGATIVE? [%s]", x);

			double v = ((LispNumber) c.car()).value();

			return new Result(from_bool(v < 0), e);
		}
	};

	public static LispBuiltin IS_LIST = new LispBuiltin("LIST?", true) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			LispCell c = (LispCell) x;
			
			if(c.cdr() != null)
				throw new LispError("Trailing arguments to LIST? [%s]", x);

			return new Result(from_bool(c.car() == null || c.car() instanceof LispCell), e);
		}
	};
	
	public static LispBuiltin IS_EQ = new LispBuiltin("EQ?", true) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			List<LispObject> a = new ArrayList<>();
			
			toList(x, a, 2, 2, false);
			
			LispObject v1 = a.get(0);
			LispObject v2 = a.get(1);
			
			if(v1 == null && v2 == null)
			{
				return new Result(LispBool.TRUE, e);
			}
			else if(v1 == null || v2 == null)
			{
				return new Result(LispBool.FALSE, e);
			}
			else
			{
				return new Result(from_bool(v1.equals(v2)), e);
			}
		}
	};
	
	public static LispBuiltin ORD = new LispBuiltin("ORD", true) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			List<LispObject> a = new ArrayList<>();
			
			toList(x, a, 1, 2, false);
			
			if(!(a.get(0) instanceof LispString))
				throw new LispError("ORD can only be applied to strings! [%s]", x);
			
			if(a.size() > 1 && !(a.get(1) instanceof LispNumber))
				throw new LispError("ORD index mus be a number! [%s]", x);
			
			String v = ((LispString) a.get(0)).value();
			int i = ((LispNumber) a.get(1)).integer();
			
			if(i < 0 || i >= v.length())
				throw new LispError("ORD index out of bounds! (string length=%d, index=%d) [%s]", v.length(), i, x);
			
			return new Result(new LispNumber(v.charAt(i)), e);
		}
	};
	
	public static LispBuiltin CHR = new LispBuiltin("CHR", true) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			LispCell c = (LispCell) x;
			
			if(c.cdr() != null)
				throw new LispError("Trailing cdr [%s]", x);
			
			if(!(c.car() instanceof LispNumber))
				throw new LispError("CHR expects a number! [%s]", x);
			
			int v = ((LispNumber) c.car()).integer();
			
			return new Result(new LispString("" + (char) v), e);
		}
	};
	
	public static LispBuiltin STR_LEN = new LispBuiltin("STR-LEN", true) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			LispCell c = (LispCell) x;
			
			if(c.cdr() != null)
				throw new LispError("Trailing cdr [%s]", x);
			
			if(!(c.car() instanceof LispString))
				throw new LispError("Cannot get length of a non-string! [%s]", x);
			
			String s = ((LispString) c.car()).value();
			
			return new Result(new LispNumber(s.length()), e);
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
			List<LispObject> a = new ArrayList<>();
			
			toList(x, a, 1, Integer.MAX_VALUE, false);
			
			double[] v = new double[a.size()];
			
			for(int i = 0 ; i < v.length ; ++i)
			{
				LispObject o = a.get(i);
				
				if(!(o instanceof LispNumber))
					throw new LispError("Not a number! [%s]", o);
				
				v[i] = ((LispNumber) o).value();
			}

			return new Result(new LispNumber(compute(v)), e);
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
	
	private static LispObject toList(LispObject x, List<LispObject> r, int min, int max, boolean cdr)
	{
		LispObject a = x;
		
		while(x instanceof LispCell)
		{
			LispCell c = (LispCell) x;
			
			r.add(c.car());
			x = c.cdr();
		}
		
		if(r.size() < min || r.size() > max)
			throw new LispError("Expected arg list sized [%d, %d] not %d! [%s]", min, max, r.size(), a);
		
		if(!cdr && x != null)
			throw new LispError("Unexpected cdr trailing args: %s", a);
		
		return x;
	}

	private Builtins( ) { }
}

