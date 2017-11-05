package it.albertus.routerlogger.csv2sql.engine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.albertus.routerlogger.csv2sql.resources.Messages;
import it.albertus.util.logging.LoggerFactory;
import it.albertus.util.sql.SqlUtils;

public class CsvToSqlEngine {

	protected static final String CSV_FILE_EXTENSION = ".csv";
	protected static final String SQL_FILE_EXTENSION = ".sql";

	private final Logger logger = LoggerFactory.getLogger(CsvToSqlEngine.class);

	private final DateFormat ansiSqlTimestampFormat = new SimpleDateFormat("yyyy-M-dd HH:mm:ss.SSS"); // '1998-3-24 04:21:23.456'

	private final String sqlTableName;
	private final String sqlColumnNamesPrefix;
	private final String sqlTimestampColumnName;
	private final String sqlResponseTimeColumnName;
	private final int sqlMaxLengthColumnNames;
	private final String csvSeparator;
	private final DateFormat csvDateFormat;

	public CsvToSqlEngine(final String csvSeparator, final String csvTimestampPattern, final String sqlTableName, final String sqlColumnNamesPrefix, final String sqlTimestampColumnName, final String sqlResponseTimeColumnName, final int sqlMaxLengthColumnNames) {
		if (sqlTableName == null || sqlTableName.trim().isEmpty()) {
			throw new IllegalArgumentException("sqlTableName must not be blank");
		}
		if (csvSeparator == null || csvSeparator.isEmpty()) {
			throw new IllegalArgumentException("csvSeparator must not be empty");
		}
		csvDateFormat = new SimpleDateFormat(csvTimestampPattern);
		csvDateFormat.setLenient(false);
		this.sqlTableName = sqlTableName;
		this.sqlColumnNamesPrefix = sqlColumnNamesPrefix;
		this.sqlTimestampColumnName = sqlTimestampColumnName;
		this.sqlResponseTimeColumnName = sqlResponseTimeColumnName;
		this.sqlMaxLengthColumnNames = sqlMaxLengthColumnNames;
		this.csvSeparator = csvSeparator;
	}

	public void convert(final File csvFile, final String destDir, final CancellationStatus status) throws IOException, InterruptedException {
		boolean deleteIncompleteFile = false;
		final File destinationFile = getDestinationFile(csvFile, destDir);
		try (final FileReader fr = new FileReader(csvFile); final LineNumberReader lnr = new LineNumberReader(fr); final FileWriter fw = new FileWriter(destinationFile); final BufferedWriter bw = new BufferedWriter(fw)) {
			convert(csvFile.getPath(), lnr, bw, status);
			logger.log(Level.INFO, Messages.get("msg.csv2sql.conversion.success"), csvFile);
		}
		catch (final InterruptedException e) {
			deleteIncompleteFile = true;
			throw e;
		}
		finally {
			if (deleteIncompleteFile) {
				if (destinationFile.delete()) {
					logger.log(Level.INFO, Messages.get("msg.csv2sql.interrupted.delete.success"), destinationFile);
				}
				else {
					logger.log(Level.WARNING, Messages.get("msg.csv2sql.interrupted.delete.failure"), destinationFile);
				}
			}
		}
	}

	void convert(final String sourceFileName, final LineNumberReader reader, final BufferedWriter writer, final CancellationStatus status) throws IOException, InterruptedException {
		final String firstLine = reader.readLine();
		if (firstLine != null) {
			final String[] csvColumnNames = firstLine.trim().split(csvSeparator);
			final List<String> sqlColumnNames = getSqlColumnNames(csvColumnNames);
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty()) { // skip empty lines
					try {
						writeLine(line, writer, sqlColumnNames);
					}
					catch (final Exception e) {
						throw new IOException(Messages.get("err.csv2sql.runnable", sourceFileName, reader.getLineNumber()), e);
					}
				}
				if (status != null && status.isCanceled()) {
					throw new InterruptedException();
				}
			}
			writer.write("COMMIT;");
			writer.newLine();
		}
	}

	File getDestinationFile(final File csvFile, final String destDir) throws IOException {
		final String csvFileName = csvFile.getName();
		final String sqlFileName;
		if (csvFileName.toLowerCase().endsWith(CSV_FILE_EXTENSION)) {
			sqlFileName = csvFileName.substring(0, csvFileName.lastIndexOf('.')) + SQL_FILE_EXTENSION;
		}
		else {
			sqlFileName = csvFileName + SQL_FILE_EXTENSION;
		}
		final File sqlFile = new File(destDir + File.separator + sqlFileName);
		if (sqlFile.exists() || sqlFile.isDirectory()) {
			throw new IOException(Messages.get("err.csv2sql.destination.exists", sqlFile));
		}
		return sqlFile;
	}

	private List<String> getSqlColumnNames(final String[] csvColumnNames) {
		final List<String> sqlColumnNames = new ArrayList<>();
		sqlColumnNames.add(getSqlColumnName(sqlTimestampColumnName, sqlColumnNamesPrefix, sqlMaxLengthColumnNames));
		for (int i = 1; i < csvColumnNames.length; i++) {
			if (i == 1 && sqlResponseTimeColumnName != null) {
				sqlColumnNames.add(getSqlColumnName(sqlResponseTimeColumnName, sqlColumnNamesPrefix, sqlMaxLengthColumnNames));
			}
			else {
				sqlColumnNames.add(getSqlColumnName(csvColumnNames[i], sqlColumnNamesPrefix, sqlMaxLengthColumnNames));
			}
		}
		return sqlColumnNames;
	}

	private void writeLine(final String csv, final BufferedWriter sql, final List<? extends CharSequence> tableColumnNames) throws IOException, ParseException {
		sql.append("INSERT INTO ").append(sqlTableName).append(" (");
		final String[] values = csv.split(csvSeparator);
		for (int i = 0; i < values.length; i++) {
			sql.append(tableColumnNames.get(i));
			if (i != values.length - 1) {
				sql.write(',');
			}
		}
		sql.append(") VALUES (TIMESTAMP '").append(ansiSqlTimestampFormat.format(csvDateFormat.parse(values[0].trim()))).write('\'');
		for (int i = 1; i < values.length; i++) {
			sql.write(',');
			if (i == 1 && sqlResponseTimeColumnName != null) {
				sql.append(Integer.toString(Integer.parseInt(values[i].trim())));
			}
			else {
				sql.append('\'').append(values[i].replace("'", "''")).write('\'');
			}
		}
		sql.append(");");
		sql.newLine();
	}

	private String getSqlColumnName(final String name, final String prefix, final int maxLength) {
		String completeName = SqlUtils.sanitizeName(prefix + name);
		if (completeName.length() > maxLength) {
			completeName = completeName.substring(0, maxLength);
		}
		return completeName;
	}

}
