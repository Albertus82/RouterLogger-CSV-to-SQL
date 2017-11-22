package it.albertus.routerlogger.csv2sql.gui;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.SelectionEvent;

interface IGuiDirector {

	void sourceFileListKeyPressed(KeyEvent e);

	void sourceFileListSelected(SelectionEvent e);

	void addSourceFilesButtonSelected(SelectionEvent e);

	void removeSourceFilesButtonSelected(SelectionEvent e);

	void clearSourceFilesButtonSelected(SelectionEvent e);

	void sourceFileListContextMenuDetected(MenuDetectEvent e);

	void removeSourceFilesMenuItemSelected(SelectionEvent e);

	void selectAllSourceFilesMenuItemSelected(SelectionEvent e);

	void clearSourceFilesMenuItemSelected(SelectionEvent e);

	void browseDirectoryButtonSelected(SelectionEvent e);

	void processButtonSelected(SelectionEvent e);

	void closeButtonSelected(SelectionEvent e);

}
