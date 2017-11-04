package it.albertus.routerlogger.csv2sql.gui;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import it.albertus.jface.Multilanguage;
import it.albertus.jface.cocoa.CocoaEnhancerException;
import it.albertus.jface.cocoa.CocoaUIEnhancer;
import it.albertus.jface.sysinfo.SystemInformationDialog;
import it.albertus.routerlogger.csv2sql.gui.listener.AboutListener;
import it.albertus.routerlogger.csv2sql.gui.listener.ArmMenuListener;
import it.albertus.routerlogger.csv2sql.gui.listener.CloseListener;
import it.albertus.routerlogger.csv2sql.gui.listener.PreferencesListener;
import it.albertus.routerlogger.csv2sql.resources.Messages;
import it.albertus.util.logging.LoggerFactory;

/**
 * Solo i <tt>MenuItem</tt> che fanno parte di una barra dei men&ugrave; con
 * stile <tt>SWT.BAR</tt> hanno gli acceleratori funzionanti; negli altri casi
 * (ad es. <tt>SWT.POP_UP</tt>), bench&eacute; vengano visualizzate le
 * combinazioni di tasti, gli acceleratori non funzioneranno e le relative
 * combinazioni di tasti saranno ignorate.
 */
public class MenuBar implements Multilanguage {

	private static final String LBL_CSV2SQL_TITLE = "lbl.csv2sql.title";
	private static final String LBL_MENU_HEADER_FILE = "lbl.menu.header.file";
	private static final String LBL_MENU_ITEM_EXIT = "lbl.menu.item.exit";
	private static final String LBL_MENU_HEADER_TOOLS = "lbl.menu.header.tools";
	private static final String LBL_MENU_ITEM_PREFERENCES = "lbl.menu.item.preferences";
	private static final String LBL_MENU_HEADER_HELP = "lbl.menu.header.help";
	private static final String LBL_MENU_HEADER_HELP_WINDOWS = "lbl.menu.header.help.windows";
	private static final String LBL_MENU_ITEM_SYSTEM_INFO = "lbl.menu.item.system.info";
	private static final String LBL_MENU_ITEM_ABOUT = "lbl.menu.item.about";

	private static final Logger logger = LoggerFactory.getLogger(MenuBar.class);

	private final Shell shell;

	private MenuItem fileMenuHeader;
	private MenuItem fileExitItem;

	private MenuItem toolsMenuHeader;
	private MenuItem toolsPreferencesMenuItem;

	private final MenuItem helpMenuHeader;
	private final MenuItem helpSystemInfoItem;
	private MenuItem helpAboutItem;

	MenuBar(final CsvToSqlGui gui) {
		shell = gui.getShell();

		final CloseListener closeListener = new CloseListener(gui);
		final AboutListener aboutListener = new AboutListener(gui);
		final PreferencesListener preferencesListener = new PreferencesListener(gui);

		boolean cocoaMenuCreated = false;
		if (Util.isCocoa()) {
			try {
				new CocoaUIEnhancer(gui.getShell().getDisplay()).hookApplicationMenu(closeListener, aboutListener, preferencesListener);
				cocoaMenuCreated = true;
			}
			catch (final CocoaEnhancerException cce) {
				logger.log(Level.SEVERE, cce.toString(), cce);
			}
		}

		final Menu bar = new Menu(gui.getShell(), SWT.BAR); // Barra

		// File
		if (!cocoaMenuCreated) {
			final Menu fileMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
			fileMenuHeader = new MenuItem(bar, SWT.CASCADE);
			fileMenuHeader.setText(Messages.get(LBL_MENU_HEADER_FILE));
			fileMenuHeader.setMenu(fileMenu);

			fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
			fileExitItem.setText(Messages.get(LBL_MENU_ITEM_EXIT));
			fileExitItem.addSelectionListener(closeListener);

			final Menu toolsMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
			toolsMenuHeader = new MenuItem(bar, SWT.CASCADE);
			toolsMenuHeader.setText(Messages.get(LBL_MENU_HEADER_TOOLS));
			toolsMenuHeader.setMenu(toolsMenu);

			toolsPreferencesMenuItem = new MenuItem(toolsMenu, SWT.PUSH);
			toolsPreferencesMenuItem.setText(Messages.get(LBL_MENU_ITEM_PREFERENCES));
			toolsPreferencesMenuItem.addSelectionListener(preferencesListener);
		}

		// Help
		final Menu helpMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		helpMenuHeader = new MenuItem(bar, SWT.CASCADE);
		helpMenuHeader.setText(Messages.get(Util.isWindows() ? LBL_MENU_HEADER_HELP_WINDOWS : LBL_MENU_HEADER_HELP));
		helpMenuHeader.setMenu(helpMenu);

		helpSystemInfoItem = new MenuItem(helpMenu, SWT.PUSH);
		helpSystemInfoItem.setText(Messages.get(LBL_MENU_ITEM_SYSTEM_INFO));
		helpSystemInfoItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				SystemInformationDialog.open(gui.getShell());
			}
		});

		if (!cocoaMenuCreated) {
			new MenuItem(helpMenu, SWT.SEPARATOR);

			helpAboutItem = new MenuItem(helpMenu, SWT.PUSH);
			helpAboutItem.setText(Messages.get(LBL_MENU_ITEM_ABOUT, Messages.get(LBL_CSV2SQL_TITLE)));
			helpAboutItem.addSelectionListener(new AboutListener(gui));
		}

		final ArmMenuListener helpMenuListener = event -> helpSystemInfoItem.setEnabled(SystemInformationDialog.isAvailable());
		helpMenu.addMenuListener(helpMenuListener);
		helpMenuHeader.addArmListener(helpMenuListener);

		gui.getShell().setMenuBar(bar);
	}

	@Override
	public void updateLabels() {
		if (fileMenuHeader != null && !fileMenuHeader.isDisposed()) {
			fileMenuHeader.setText(Messages.get(LBL_MENU_HEADER_FILE));
		}
		if (fileExitItem != null && !fileExitItem.isDisposed()) {
			fileExitItem.setText(Messages.get(LBL_MENU_ITEM_EXIT));
		}
		if (toolsMenuHeader != null && !toolsMenuHeader.isDisposed()) {
			toolsMenuHeader.setText(Messages.get(LBL_MENU_HEADER_TOOLS));
		}
		if (toolsPreferencesMenuItem != null && !toolsPreferencesMenuItem.isDisposed()) {
			toolsPreferencesMenuItem.setText(Messages.get(LBL_MENU_ITEM_PREFERENCES));
		}
		helpMenuHeader.setText(Messages.get(Util.isWindows() ? LBL_MENU_HEADER_HELP_WINDOWS : LBL_MENU_HEADER_HELP));
		helpSystemInfoItem.setText(Messages.get(LBL_MENU_ITEM_SYSTEM_INFO));
		if (helpAboutItem != null && !helpAboutItem.isDisposed()) {
			helpAboutItem.setText(Messages.get(LBL_MENU_ITEM_ABOUT));
		}
	}

	@Override
	public Shell getShell() {
		return shell;
	}

}
