package it.albertus.routerlogger.csv2sql.engine;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.albertus.routerlogger.common.engine.InitializationException;
import it.albertus.routerlogger.common.engine.RouterLoggerUtilsConfig;
import it.albertus.routerlogger.csv2sql.resources.Messages;
import it.albertus.util.logging.LoggerFactory;

public class CsvToSqlConfig extends RouterLoggerUtilsConfig {

	private static final Logger logger = LoggerFactory.getLogger(CsvToSqlConfig.class);

	private static final String CFG_FILE_NAME = "csv2sql.cfg";
	private static final String LOG_FILE_NAME = "csv2sql.%g.log";

	private static CsvToSqlConfig instance;

	public static synchronized CsvToSqlConfig getInstance() {
		if (instance == null) {
			try {
				instance = new CsvToSqlConfig();
			}
			catch (final IOException e) {
				final String message = Messages.get("err.open.cfg", CFG_FILE_NAME);
				logger.log(Level.SEVERE, message, e);
				throw new InitializationException(message, e);
			}
		}
		return instance;
	}

	private CsvToSqlConfig() throws IOException {
		super(DIRECTORY_NAME + File.separator + CFG_FILE_NAME, true);
		init();
	}

	@Override
	protected void updateLanguage() {
		final String language = getString(CFG_KEY_LANGUAGE, Messages.DEFAULT_LANGUAGE);
		Messages.setLanguage(language);
	}

	@Override
	protected String getLogFileName() {
		return LOG_FILE_NAME;
	}

}
