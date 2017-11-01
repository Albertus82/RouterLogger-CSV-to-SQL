package it.albertus.routerlogger.csv2sql.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;

public class CsvToSqlDialog extends Dialog {

	/**
	 * Creates a new <em>CSV to SQL converter</em> dialog instance using the
	 * provided parent shell.
	 * 
	 * @param parent the parent shell
	 * 
	 * @see #open()
	 */
	public CsvToSqlDialog(final Shell parent) {
		super(parent, SWT.SHEET | SWT.RESIZE);
	}

	/** Opens this <em>CSV to SQL converter</em> dialog. */
	public void open() {
		new CsvToSqlShellContent(new Shell(getParent(), getStyle())).open();
	}

}
