package it.albertus.routerlogger.csv2sql.gui;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import it.albertus.routerlogger.csv2sql.engine.CsvToSqlEngine;
import it.albertus.routerlogger.csv2sql.resources.Messages;

public class CsvToSqlRunnable implements IRunnableWithProgress {

	private final CsvToSqlEngine converter;
	private final String[] sourceFiles;
	private final String destinationPath;

	CsvToSqlRunnable(final CsvToSqlEngine converter, final String[] sourceFiles, final String destinationPath) {
		this.converter = converter;
		this.sourceFiles = sourceFiles;
		this.destinationPath = destinationPath;
	}

	@Override
	public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		monitor.beginTask(Messages.get("lbl.csv2sql.runnable.task.name", sourceFiles.length), sourceFiles.length);

		for (int i = 0; i < sourceFiles.length; i++) {
			final String sourceFile = sourceFiles[i];
			monitor.subTask(Messages.get("lbl.csv2sql.runnable.subtask.name", i + 1, sourceFiles.length, sourceFile));
			try {
				converter.convert(new File(sourceFile), destinationPath, monitor::isCanceled);
			}
			catch (final IOException e) {
				throw new InvocationTargetException(e);
			}
			monitor.worked(1);

			if (monitor.isCanceled()) {
				throw new InterruptedException();
			}
		}

		monitor.done();
	}

}
