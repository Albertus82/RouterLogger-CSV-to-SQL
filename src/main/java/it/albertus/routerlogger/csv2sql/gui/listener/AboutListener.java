package it.albertus.routerlogger.csv2sql.gui.listener;

import java.text.DateFormat;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import it.albertus.routerlogger.csv2sql.gui.AboutDialog;
import it.albertus.routerlogger.csv2sql.resources.Messages;
import it.albertus.util.Version;

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
		final AboutDialog aboutDialog = new AboutDialog(gui.getShell());
		aboutDialog.setText(Messages.get("lbl.about.title"));
		final Version version = Version.getInstance();
		aboutDialog.setMessage(Messages.get("lbl.csv2sql.title") + ' ' + Messages.get("msg.version", version.getNumber(), DateFormat.getDateInstance(DateFormat.MEDIUM, Messages.getLanguage().getLocale()).format(version.getDate())));
		aboutDialog.setApplicationUrl(Messages.get("msg.website"));
		aboutDialog.setIconUrl(Messages.get("msg.info.icon.site"));
		aboutDialog.open();
	}

}
