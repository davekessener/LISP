package dave.lisp.detail;

import java.util.List;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;

import dave.lisp.common.Environment;
import dave.lisp.common.IO;
import dave.lisp.common.Library;
import dave.lisp.common.Result;
import dave.lisp.error.LispError;

public class Builtins
{
	public static final LispBuiltin EVAL = new LispBuiltin("EVAL", true) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			LispCell c = (LispCell) x;
			
			if(c.cdr() != null)
				throw new LispError("Trailing CDR! [%s]", x);
			
			if(c.car() instanceof LispString)
			{
				return LispRuntime.eval(LispRuntime.parse(((LispString) c.car()).value()), e);
			}
			else
			{
				return LispRuntime.eval(c.car(), e);
			}
		}
	};
	
	public static final LispBuiltin QUOTE = new LispBuiltin("QUOTE", false) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			return new Result(x, e);
		}
	};

	public static final LispBuiltin MQUOTE = new LispBuiltin("MACRO-QUOTE", false) {
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

	public static final LispBuiltin DEFINE = new LispBuiltin("DEFINE", false) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			List<LispObject> a = new ArrayList<>();
			
			toList(x, a, 2, 2, false);
			
			Result v = LispRuntime.eval(a.get(1), e);
			
//			Stdout.printf("SETTING: %s -> %s\n", a.get(0).serialize(), LispObject.serialize(v.value));

			return new Result(a.get(0), new ExtendedEnvironment(a.get(0), v.value, e));
		}
	};

	public static final LispBuiltin BEGIN = new LispBuiltin("BEGIN", false) {
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
	
	public static final LispBuiltin CONS = new LispBuiltin("CONS", true) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			List<LispObject> a = new ArrayList<>();
			
			toList(x, a, 2, 2, false);
			
			return new Result(new LispCell(a.get(0), a.get(1)), e);
		}
	};
	
	public static final LispBuiltin CAR = new LispBuiltin("CAR", true) {
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
	
	public static final LispBuiltin CDR = new LispBuiltin("CDR", true) {
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

	public static final LispBuiltin LAMBDA = new CallableBuiltin("LAMBDA") {
		@Override
		protected LispObject create(LispObject args, LispObject body, Environment e)
		{
			return new LispLambda(args, body, e);
		}
	};

	public static final LispBuiltin MACRO = new CallableBuiltin("MACRO") {
		@Override
		protected LispObject create(LispObject args, LispObject body, Environment e)
		{
			return new LispMacro(args, body, e);
		}
	};

	public static final LispBuiltin MACRO_EXPAND = new LispBuiltin("MACRO-EXPAND", false) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			LispCell c = (LispCell) x;
			
			if(c.cdr() != null)
				throw new LispError("Trailing cdr! [%s]", x);
			
			return new Result(LispRuntime.expand(c.car(), e), e);
		}
	};
	
	private static abstract class IOBuiltin extends LispBuiltin
	{
		private IOBuiltin(String name)
		{
			super(name, true);
		}
		
		@SuppressWarnings("unchecked")
		protected IO getFile(LispObject lfd, Environment e)
		{
			if(!(lfd instanceof LispNumber))
				throw new LispError("Not a valid file-handle: %s", lfd);
			
			LispNumber dfd = (LispNumber) lfd;
			int fd = dfd.integer();
			
			if(fd < 0)
				throw new LispError("File-handle must be integral, non-negative values: %s", dfd);
			
			Result lfds = e.lookup(new LispSymbol(Library.Internals.FILE_HANDLES));
			
			if(lfds == null)
				throw new LispError("File-handles cannot be found!");
			
			if(!(lfds.value instanceof LispProxy))
				throw new LispError("File-handles invalid!");
			
			List<IO> fds = ((LispProxy<List<IO>>) lfds.value).content();
			
			if(fd >= fds.size())
				throw new LispError("Invalid file-handle %d!", fd);
			
			IO io = fds.get(fd);
			
			if(io == null)
				throw new LispError("File already closed! (%d)", fd);
			
			return io;
		}
	}
	
	public static final LispBuiltin WRITE = new IOBuiltin("WRITE") {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			List<LispObject> a = new ArrayList<>();
			
			toList(x, a, 1, Integer.MAX_VALUE, false);
			
			IO io = getFile(a.get(0), e);
			
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
	
	public static final LispBuiltin READ = new IOBuiltin("READ") {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			List<LispObject> a = new ArrayList<>();
			
			toList(x, a, 1, 2, false);
			
			IO io = getFile(a.get(0), e);
			
			int l = -1;
			
			if(a.size() > 1)
			{
				if(!(a.get(1) instanceof LispNumber))
					throw new LispError("Invalid amount of bytes to read: [%s]", x);
				
				l = ((LispNumber) a.get(1)).integer();
			}
			
			byte[] v = new byte[0];
			
			if(l > 0)
			{
				v = io.read(l);
			}
			else if(l == 0)
			{
				v = io.read(io.available());
			}
			else if(l == -1)
			{
				byte[] t1 = io.read(1);
				byte[] t2 = io.read(io.available());
				
				v = new byte[t1.length + t2.length];
				
				for(int i = 0 ; i < t1.length ; ++i)
				{
					v[i+0] = t1[i];
				}
				
				for(int i = 0 ; i < t2.length ; ++i)
				{
					v[i+t1.length] = t2[i];
				}
			}
			
			LispObject r = null;
			
			for(int i = v.length ; i > 0 ; --i)
			{
				r = new LispCell(new LispNumber(v[i-1]), r);
			}
			
			return new Result(r, e);
		}
	};
	
	private static abstract class StrHelperBuiltin extends LispBuiltin
	{
		private StrHelperBuiltin(String name)
		{
			super(name, true);
		}
		
		protected Charset getCharset(List<LispObject> a, int i)
		{
			Charset cs = Charset.defaultCharset();
			
			if(i < a.size())
			{
				if(!(a.get(i) instanceof LispString))
					throw new LispError("Expected string specifying charset! [%s]", a.get(i));
				
				try
				{
					cs = Charset.forName(((LispString) a.get(i)).value());
				}
				catch(IllegalCharsetNameException | UnsupportedCharsetException ex)
				{
					throw new LispError("No such charset: %s", ex.getMessage());
				}
			}
			
			return cs;
		}
	}
	
	public static final LispBuiltin STR_SER = new StrHelperBuiltin("STR-SERIALIZE") {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			List<LispObject> a = new ArrayList<>();
			
			toList(x, a, 1, 2, false);
			
			if(!(a.get(0) instanceof LispString))
				throw new LispError("Can only serialize string! [%s]", x);
			
			String s = ((LispString) a.get(0)).value();
			Charset cs = getCharset(a, 1);
			byte[] v = s.getBytes(cs);
			
			LispObject r = null;
			
			for(int i = v.length ; i > 0 ; --i)
			{
				r = new LispCell(new LispNumber(v[i-1]), r);
			}
			
			return new Result(r, e);
		}
	};
	
	public static final LispBuiltin STR_DES = new StrHelperBuiltin("STR-DESERIALIZE") {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			List<LispObject> a = new ArrayList<>();
			List<LispObject> v = new ArrayList<>();
			
			toList(x, a, 1, 2, false);
			toList(a.get(0), v, 0, Integer.MAX_VALUE, false);
			
			Charset cs = getCharset(a, 1);
			
			int[] lv = v.stream().mapToInt(o -> {
				if(!(o instanceof LispNumber))
					throw new LispError("Not a number: %s [%s]", o, x);
				
				return ((LispNumber) o).integer();
			}).toArray();
			
			byte[] bv = new byte[lv.length];
			
			for(int i = 0 ; i < lv.length ; ++i)
			{
				bv[i] = (byte) lv[i];
				
				if(bv[i] != lv[i])
					throw new LispError("Out of bounds: %02x [%s]", lv[i], x);
			}
			
			String s = new String(bv, cs);
			
			return new Result(new LispString(s), e);
		}
	};

	public static final LispBuiltin STR_LEN = new LispBuiltin("STR-LEN", true) {
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
	
	public static final LispBuiltin STR_APPEND = new LispBuiltin("STR-APPEND", true) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			List<LispObject> a = new ArrayList<>();
			
			toList(x, a, 0, Integer.MAX_VALUE, false);
			
			StringBuilder sb = new StringBuilder();
			
			for(LispObject o : a)
			{
				if(!(o instanceof LispString))
					throw new LispError("Not a string: %s [%s]", o, x);
				
				sb.append(((LispString) o).value());
			}
			
			return new Result(new LispString(sb.toString()), e);
		}
	};
	
	public static final LispBuiltin FORMAT = new LispBuiltin("FORMAT", true) {
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
				return LispObject.serialize(o, true);
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

	public static final LispBuiltin IF = new LispBuiltin("IF", false) {
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

	public static final LispBuiltin IS_NEGATIVE = new LispBuiltin("NEGATIVE?", true) {
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

	public static final LispBuiltin IS_LIST = new LispBuiltin("LIST?", true) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			LispCell c = (LispCell) x;
			
			if(c.cdr() != null)
				throw new LispError("Trailing arguments to LIST? [%s]", x);

			return new Result(from_bool(c.car() == null || c.car() instanceof LispCell), e);
		}
	};
	
	public static final LispBuiltin IS_EQ = new LispBuiltin("EQ?", true) {
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
	
	public static final LispBuiltin ORD = new LispBuiltin("ORD", true) {
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
	
	public static final LispBuiltin CHR = new LispBuiltin("CHR", true) {
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
	
	public static final LispBuiltin NOT = new LispBuiltin("NOT", true) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			List<LispObject> a = new ArrayList<>();
			
			toList(x, a, 1, 1, false);
			
			return new Result(from_bool(!to_bool(a.get(0))), e);
		}
	};
	
	public static final LispBuiltin AND = new LispBuiltin("AND", false) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			LispObject r = null;
			
			for(LispCell c = (LispCell) x ; true ; c = (LispCell) c.cdr())
			{
				r = LispRuntime.eval(c.car(), e).value;
				
				if(!to_bool(r))
					return new Result(LispBool.FALSE, e);
				
				if(c.cdr() == null)
					break;
				
				if(!(c.cdr() instanceof LispCell))
					throw new LispError("Trailing CDR! [%s]", x);
			}
			
			return new Result(r, e);
		}
	};
	
	public static final LispBuiltin OR = new LispBuiltin("OR", false) {
		@Override
		protected Result apply(LispObject x, Environment e)
		{
			LispObject r = null;
			
			for(LispCell c = (LispCell) x ; true ; c = (LispCell) c.cdr())
			{
				r = LispRuntime.eval(c.car(), e).value;
				
				if(to_bool(r))
					return new Result(r, e);
				
				if(c.cdr() == null)
					break;
				
				if(!(c.cdr() instanceof LispCell))
					throw new LispError("Trailing CDR: %s [%s]", c.cdr(), x);
			}
			
			return new Result(LispBool.FALSE, e);
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

	public static final boolean to_bool(LispObject o) { return !(o == null || o == LispBool.FALSE); }
	public static final LispBool from_bool(boolean f) { return f ? LispBool.TRUE : LispBool.FALSE; }
	
	private static final LispObject toList(LispObject x, List<LispObject> r, int min, int max, boolean cdr)
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

