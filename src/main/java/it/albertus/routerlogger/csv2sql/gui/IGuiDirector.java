package it.albertus.routerlogger.csv2sql.gui;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;

interface IGuiDirector {

	void addSourceFilesButtonSelected(SelectionEvent e);

	void browseDirectoryButtonSelected(SelectionEvent e);

	void clearSourceFilesButtonSelected(SelectionEvent e);

	void clearSourceFilesMenuItemSelected(SelectionEvent e);

	void closeButtonSelected(SelectionEvent e);

	void processButtonSelected(SelectionEvent e);

	void removeSourceFilesButtonSelected(SelectionEvent e);

	void removeSourceFilesMenuItemSelected(SelectionEvent e);

	void selectAllSourceFilesMenuItemSelected(SelectionEvent e);

	void sourceFilesListContextMenuDetected(MenuDetectEvent e);

	void sourceFilesListKeyPressed(KeyEvent e);

	void sourceFilesListSelected(SelectionEvent e);

	void textModified(ModifyEvent e);

}
