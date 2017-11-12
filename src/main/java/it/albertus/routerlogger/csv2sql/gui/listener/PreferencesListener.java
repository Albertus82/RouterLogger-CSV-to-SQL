package it.albertus.routerlogger.csv2sql.gui.listener;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import it.albertus.jface.EnhancedErrorDialog;
import it.albertus.jface.Multilanguage;
import it.albertus.jface.preference.Preferences;
import it.albertus.routerlogger.csv2sql.engine.CsvToSqlConfig;
import it.albertus.routerlogger.csv2sql.gui.Images;
import it.albertus.routerlogger.csv2sql.gui.preference.Preference;
import it.albertus.routerlogger.csv2sql.gui.preference.page.PageDefinition;
import it.albertus.routerlogger.csv2sql.resources.Messages;
import it.albertus.routerlogger.csv2sql.resources.Messages.Language;
import it.albertus.util.logging.LoggerFactory;

public class PreferencesListener implements SelectionListener, Listener {

	private static final Logger logger = LoggerFactory.getLogger(PreferencesListener.class);

	private final Multilanguage gui;

	public PreferencesListener(final Multilanguage gui) {
		this.gui = gui;
	}

	private void execute() {
		final Language language = Messages.getLanguage();
		final Preferences preferences = new Preferences(PageDefinition.values(), Preference.values(), CsvToSqlConfig.getInstance(), Images.getMainIcons());
		try {
			preferences.openDialog(gui.getShell());
		}
		catch (final IOException ioe) {
			logger.log(Level.SEVERE, ioe.toString(), ioe);
			EnhancedErrorDialog.openError(gui.getShell(), Messages.get("lbl.window.title"), Messages.get("err.preferences.dialog.open"), IStatus.WARNING, ioe, Images.getMainIcons());
		}

		// Check if must update texts...
		if (!language.equals(Messages.getLanguage())) {
			gui.updateLabels();
		}
	}

	@Override
	public void handleEvent(final Event event) {
		execute();
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		execute();
	}

	@Override
	public void widgetDefaultSelected(final SelectionEvent e) {/* Ignore */}

}
