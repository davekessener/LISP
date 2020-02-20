package dave.lisp;

public interface Environment
{
	public abstract Result lookup(LispObject id);
}

