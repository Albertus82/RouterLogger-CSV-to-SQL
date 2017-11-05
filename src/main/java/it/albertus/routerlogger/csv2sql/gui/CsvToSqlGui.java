package it.albertus.routerlogger.csv2sql.gui;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import it.albertus.routerlogger.csv2sql.resources.Messages;

public class CsvToSqlGui extends CsvToSqlShellContent {

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
		getShell().layout(true,true);
	}

}
