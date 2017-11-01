package it.albertus.routerlogger.csv2sql.gui.listener;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class CloseListener implements Listener, SelectionListener {

	private final IShellProvider provider;

	public CloseListener(final IShellProvider provider) {
		this.provider = provider;
	}

	/* Shell close command & OS X Menu */
	@Override
	public void handleEvent(final Event event) {
		disposeShellAndDisplay();
	}

	/* Menu */
	@Override
	public void widgetSelected(final SelectionEvent event) {
		disposeShellAndDisplay();
	}

	@Override
	public void widgetDefaultSelected(final SelectionEvent event) {/* Ignore */}

	private void disposeShellAndDisplay() {
		provider.getShell().dispose();
		final Display display = Display.getCurrent();
		if (display != null) {
			display.dispose(); // Fix close not working on Windows 10 when iconified
		}
	}

}
