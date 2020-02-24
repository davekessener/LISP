package dave.lisp.common;

public final class Library
{
	public static final String TITLE = "LISP";
	public static final int VERSION = 1;
	
	public static class Internals
	{
		public static final String FILE_HANDLES = "__fds";
		public static final String MACRO_QUOTE = "__mquote";
		public static final String MACRO_UNQUOTE = "__munquote";
		
		private Internals( ) { }
	}
	
	private Library() { }
}
