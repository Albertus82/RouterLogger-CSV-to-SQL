package it.albertus.routerlogger.csv2sql.gui.listener;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import it.albertus.routerlogger.csv2sql.gui.AboutDialog;

public class AboutListener implements Listener, SelectionListener {

	private final IShellProvider gui;

	public AboutListener(final IShellProvider gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		openDialog();
	}

	@Override
	public void handleEvent(final Event event) {
		openDialog();
	}

	@Override
	public void widgetDefaultSelected(final SelectionEvent e) {/* Ignore */}

	private void openDialog() {
		new AboutDialog(gui.getShell()).open();
	}

}
