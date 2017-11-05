package it.albertus.routerlogger.csv2sql;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import it.albertus.jface.EnhancedErrorDialog;
import it.albertus.routerlogger.csv2sql.gui.CsvToSqlGui;
import it.albertus.routerlogger.csv2sql.gui.CsvToSqlShellContent;
import it.albertus.routerlogger.csv2sql.resources.Messages;
import it.albertus.util.Version;
import it.albertus.util.logging.LoggerFactory;

public class CsvToSqlConverter {

	private static final Logger logger = LoggerFactory.getLogger(CsvToSqlConverter.class);

	public static void main(final String... args) {
		Display display = null;
		Shell shell = null;
		try {
			Display.setAppName(Messages.get(CsvToSqlShellContent.LBL_CSV2SQL_TITLE));
			Display.setAppVersion(Version.getInstance().getNumber());
			display = Display.getDefault();
			shell = new Shell(display, SWT.RESIZE | SWT.MIN);
			new CsvToSqlGui(shell).open();
			while (!shell.isDisposed()) {
				if (!display.isDisposed() && !display.readAndDispatch()) {
					display.sleep();
				}
			}
		}
		catch (final Exception e) {
			logger.log(Level.SEVERE, e.toString(), e);
			if (display != null && shell != null && !display.isDisposed() && !shell.isDisposed()) {
				EnhancedErrorDialog.openError(shell, shell.getText(), e.toString(), IStatus.ERROR, e, shell.getDisplay().getSystemImage(SWT.ICON_ERROR));
			}
		}
		finally {
			if (display != null) {
				display.dispose();
			}
		}
	}

}
