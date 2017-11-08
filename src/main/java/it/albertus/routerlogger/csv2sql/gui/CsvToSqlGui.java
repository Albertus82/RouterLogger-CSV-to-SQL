package it.albertus.routerlogger.csv2sql.gui;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import it.albertus.jface.EnhancedErrorDialog;
import it.albertus.routerlogger.csv2sql.resources.Messages;
import it.albertus.util.InitializationException;
import it.albertus.util.Version;
import it.albertus.util.logging.LoggerFactory;

public class CsvToSqlGui extends CsvToSqlShellContent {

	private static final Logger logger = LoggerFactory.getLogger(CsvToSqlGui.class);

	private MenuBar menuBar;

	public CsvToSqlGui(final Shell shell) {
		super(shell);
	}

	@Override
	protected void createContents(final Shell shell) {
		menuBar = new MenuBar(this);
		super.createContents(shell);
	}

	@Override
	public void updateLabels() {
		Display.setAppName(Messages.get(LBL_CSV2SQL_TITLE));
		super.updateLabels();
		menuBar.updateLabels();
		getShell().layout(true, true);
	}

	public static void run(final InitializationException ie) {
		Display display = null;
		Shell shell = null;
		try {
			Display.setAppName(Messages.get(LBL_CSV2SQL_TITLE));
			Display.setAppVersion(Version.getInstance().getNumber());
			display = Display.getDefault();
			if (ie != null) { // Display error dialog and exit.
				EnhancedErrorDialog.openError(null, Messages.get(LBL_CSV2SQL_TITLE), ie.getLocalizedMessage() != null ? ie.getLocalizedMessage() : ie.getMessage(), IStatus.ERROR, ie.getCause() != null ? ie.getCause() : ie, Images.getMainIcons());
			}
			else { // Open main window.
				shell = new Shell(display, SWT.RESIZE | SWT.MIN);
				new CsvToSqlGui(shell).open();
				while (!shell.isDisposed()) {
					if (!display.isDisposed() && !display.readAndDispatch()) {
						display.sleep();
					}
				}
			}
		}
		catch (final Exception ex) {
			logger.log(Level.SEVERE, ex.toString(), ex);
			if (display != null && shell != null && !display.isDisposed() && !shell.isDisposed()) {
				EnhancedErrorDialog.openError(shell, shell.getText(), ex.toString(), IStatus.ERROR, ex, shell.getDisplay().getSystemImage(SWT.ICON_ERROR));
			}
		}
		finally {
			if (display != null) {
				display.dispose();
			}
		}
	}

}
