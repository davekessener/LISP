package dave.lisp.error;

public class EOIError extends ParseError
{
	private static final long serialVersionUID = -6603790721108145247L;

	public EOIError() { super(""); }
	public EOIError(String f, Object ... a) { super(f, a); }
}
