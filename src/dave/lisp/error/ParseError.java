package dave.lisp.error;

public class ParseError extends LispError
{
	public static final long serialVersionUID = 1212304979670608377l;

	public ParseError(String f, Object ... a) { super(f, a); }
}

