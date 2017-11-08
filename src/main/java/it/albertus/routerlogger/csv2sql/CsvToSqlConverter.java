package it.albertus.routerlogger.csv2sql;

import it.albertus.routerlogger.csv2sql.engine.CsvToSqlConfig;
import it.albertus.routerlogger.csv2sql.gui.CsvToSqlGui;
import it.albertus.util.InitializationException;
import it.albertus.util.logging.LoggingSupport;

public class CsvToSqlConverter {

	public static final String LOG_FORMAT = "%1$td/%1$tm/%1$tY %1$tH:%1$tM:%1$tS.%tL %4$s %3$s - %5$s%6$s%n";

	private static InitializationException initializationException;

	static {
		if (LoggingSupport.getFormat() == null) {
			LoggingSupport.setFormat(LOG_FORMAT);
		}
		try {
			CsvToSqlConfig.getInstance();
		}
		catch (final InitializationException e) {
			initializationException = e;
		}
		catch (final RuntimeException e) {
			initializationException = new InitializationException(e.getMessage(), e);
		}
	}

	private CsvToSqlConverter() {
		throw new IllegalAccessError();
	}

	public static void main(final String... args) {
		CsvToSqlGui.run(initializationException);
	}

	public static InitializationException getInitializationException() {
		return initializationException;
	}

}
