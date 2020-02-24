package dave.lisp.common;

public interface LineReader extends AutoCloseable
{
	public abstract boolean done( );
	public abstract String readLine( );
}
