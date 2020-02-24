package dave.lisp.common;

public interface IO
{
	public abstract void write(byte[] a);
	public abstract byte[] read(int n);
	public abstract void close( );
}
