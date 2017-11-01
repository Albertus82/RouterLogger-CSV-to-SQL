package it.albertus.routerlogger.csv2sql.gui;

import org.eclipse.swt.widgets.Shell;

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
		super.updateLabels();
		menuBar.updateLabels();
	}

}
