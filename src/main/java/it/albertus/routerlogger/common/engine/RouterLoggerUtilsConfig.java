package it.albertus.routerlogger.common.engine;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.albertus.util.Configuration;
import it.albertus.util.logging.CustomFormatter;
import it.albertus.util.logging.EnhancedFileHandler;
import it.albertus.util.logging.FileHandlerConfig;
import it.albertus.util.logging.LoggerFactory;
import it.albertus.util.logging.LoggingSupport;

public abstract class RouterLoggerUtilsConfig extends Configuration {

	protected static final String CFG_KEY_LANGUAGE = "language";
	protected static final String CFG_KEY_LOGGING_LEVEL = "logging.level";

	protected static final String DIRECTORY_NAME = "RouterLogger Utils";

	private static final String LOG_FORMAT_FILE = "%1$td/%1$tm/%1$tY %1$tH:%1$tM:%1$tS.%tL %4$s %3$s - %5$s%6$s%n";

	public static final Level DEFAULT_LOGGING_LEVEL = Level.INFO;
	public static final boolean DEFAULT_LOGGING_FILES_ENABLED = true;
	public static final String DEFAULT_LOGGING_FILES_PATH = getOsSpecificLocalAppDataDir() + File.separator + DIRECTORY_NAME;
	public static final int DEFAULT_LOGGING_FILES_LIMIT = 1024;
	public static final int DEFAULT_LOGGING_FILES_COUNT = 5;

	private static final Logger logger = LoggerFactory.getLogger(RouterLoggerUtilsConfig.class);

	private EnhancedFileHandler fileHandler;

	public RouterLoggerUtilsConfig(final String fileName, final boolean prependOsSpecificConfigurationDir) throws IOException {
		super(fileName, prependOsSpecificConfigurationDir);
	}

	protected abstract void updateLanguage();

	protected abstract String getLogFileName();

	protected void init() {
		updateLanguage();
		updateLogging();
	}

	@Override
	public void reload() throws IOException {
		super.reload();
		init();
	}

	private void updateLogging() {
		if (LoggingSupport.getInitialConfigurationProperty() == null) {
			updateLoggingLevel();

			if (getBoolean("logging.files.enabled", DEFAULT_LOGGING_FILES_ENABLED)) {
				enableLoggingFileHandler();
			}
			else {
				disableLoggingFileHandler();
			}
		}
	}

	private void enableLoggingFileHandler() {
		final String loggingPath = this.getString("logging.files.path", DEFAULT_LOGGING_FILES_PATH);
		if (loggingPath != null && !loggingPath.isEmpty()) {
			final FileHandlerConfig newConfig = new FileHandlerConfig();
			newConfig.setPattern(loggingPath + File.separator + getLogFileName());
			newConfig.setLimit(getInt("logging.files.limit", DEFAULT_LOGGING_FILES_LIMIT) * 1024);
			newConfig.setCount(getInt("logging.files.count", DEFAULT_LOGGING_FILES_COUNT));
			newConfig.setAppend(true);
			newConfig.setFormatter(new CustomFormatter(LOG_FORMAT_FILE));

			if (fileHandler != null) {
				final FileHandlerConfig oldConfig = FileHandlerConfig.fromHandler(fileHandler);
				if (!oldConfig.getPattern().equals(newConfig.getPattern()) || oldConfig.getLimit() != newConfig.getLimit() || oldConfig.getCount() != newConfig.getCount()) {
					logger.log(Level.FINE, "Logging configuration has changed; closing and removing old {0}...", fileHandler.getClass().getSimpleName());
					LoggingSupport.getRootLogger().removeHandler(fileHandler);
					fileHandler.close();
					fileHandler = null;
					logger.log(Level.FINE, "Old FileHandler closed and removed.");
				}
			}

			if (fileHandler == null) {
				logger.log(Level.FINE, "FileHandler not found; creating one...");
				try {
					new File(loggingPath).mkdirs();
					fileHandler = new EnhancedFileHandler(newConfig);
					LoggingSupport.getRootLogger().addHandler(fileHandler);
					logger.log(Level.FINE, "{0} created successfully.", fileHandler.getClass().getSimpleName());
				}
				catch (final IOException ioe) {
					logger.log(Level.SEVERE, ioe.toString(), ioe);
				}
			}
		}
	}

	private void disableLoggingFileHandler() {
		if (fileHandler != null) {
			LoggingSupport.getRootLogger().removeHandler(fileHandler);
			fileHandler.close();
			fileHandler = null;
			logger.log(Level.FINE, "FileHandler closed and removed.");
		}
	}

	private void updateLoggingLevel() {
		try {
			LoggingSupport.setLevel(LoggingSupport.getRootLogger().getName(), Level.parse(this.getString(CFG_KEY_LOGGING_LEVEL, DEFAULT_LOGGING_LEVEL.getName())));
		}
		catch (final IllegalArgumentException iae) {
			logger.log(Level.WARNING, iae.toString(), iae);
		}
	}

}
