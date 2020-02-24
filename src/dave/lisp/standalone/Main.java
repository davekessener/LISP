package dave.lisp.standalone;

import java.util.Locale;
import java.util.function.Consumer;

import dave.arguments.Arguments;
import dave.arguments.Option;
import dave.arguments.ParseException;
import dave.arguments.Parser;
import dave.lisp.common.Library;
import dave.lisp.run.InteractiveEngine;
import dave.lisp.run.OneshotEngine;
import dave.lisp.utils.FileLineReader;
import dave.lisp.utils.UserInputLineReader;
import dave.util.SevereException;
import dave.util.ShutdownService;
import dave.util.ShutdownService.Priority;
import dave.util.log.Entry;
import dave.util.log.LogBase;
import dave.util.log.LogSink;
import dave.util.log.Logger;
import dave.util.log.Stderr;

public class Main
{
	private static final Option O_VERBOSITY = (new Option.OptionBuilder("verbosity")).setShortcut("v").build();
	private static final Option O_FIN = (new Option.OptionBuilder("file")).setShortcut("f").hasValue(true).build();
	
	public static void main(String[] args)
	{
		Locale.setDefault(Locale.US);
		
		Arguments a = parseArgs(args);
		
		LogBase.INSTANCE.registerSink(e -> e.severity.level() >= (a.hasArgument(O_VERBOSITY) ? 0 : 2), buildLogSink());
		
		LogBase.INSTANCE.start();
		
		ShutdownService.INSTANCE.register(Priority.LAST, LogBase.INSTANCE::stop);
		
		try
		{
			Logger.DEFAULT.info("%s v%d", Library.TITLE, Library.VERSION);
			
			run(a);
		}
		finally
		{
			ShutdownService.INSTANCE.shutdown();
		}
	}
	
	private static void run(Arguments args)
	{
		if(args.hasArgument(O_FIN))
		{
			(new OneshotEngine()).run(new FileLineReader(args.getArgument(O_FIN)));
		}
		else
		{
			(new InteractiveEngine()).run(new UserInputLineReader());
		}
	}
	
	private static Arguments parseArgs(String[] args)
	{
		Parser p = new Parser(O_VERBOSITY, O_FIN);
		
		try
		{
			return p.parse(args);
		}
		catch(ParseException e)
		{
			Stderr.printf("%s\n", p);
			
			throw new SevereException(e);
		}
	}
	
	private static Consumer<Entry> buildLogSink()
	{
		return (new LogSink.Builder()).setCallback(s -> Stderr.printf("# %s\n", s)).build();
	}
}

