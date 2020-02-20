package dave.lisp;

public interface LispCallable
{
	public abstract Result call(LispObject x, Environment e);
}

