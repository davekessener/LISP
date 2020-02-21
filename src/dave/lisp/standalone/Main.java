package dave.lisp.standalone;

import dave.util.ShutdownService;
import dave.util.ShutdownService.Priority;
import dave.util.log.LogBase;
import dave.util.log.LogSink;

public class Main
{
	public static void main(String[] args)
	{
		LogBase.INSTANCE.registerSink(e -> true, LogSink.build());
		
		LogBase.INSTANCE.start();
		
		ShutdownService.INSTANCE.register(Priority.LAST, LogBase.INSTANCE::stop);
		
		try
		{
			run(args);
		}
		finally
		{
			ShutdownService.INSTANCE.shutdown();
		}
	}
	
	private static void run(String[] args)
	{
	}
}

