package it.albertus.routerlogger.csv2sql.gui;

import java.text.DateFormat;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

import it.albertus.jface.listener.LinkSelectionListener;
import it.albertus.routerlogger.csv2sql.resources.Messages;
import it.albertus.util.Version;

public class AboutDialog extends Dialog {

	public AboutDialog(final Shell parent) {
		this(parent, SWT.SHEET); // SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL
	}

	public AboutDialog(final Shell parent, final int style) {
		super(parent, style);
		this.setText(Messages.get("lbl.about.title", Messages.get("lbl.csv2sql.title")));
	}

	public void open() {
		final Shell shell = new Shell(getParent(), getStyle());
		shell.setText(getText());
		shell.setImage(shell.getDisplay().getSystemImage(SWT.ICON_INFORMATION));
		createContents(shell);
		constrainShellSize(shell);
		shell.open();
	}

	private void constrainShellSize(final Shell shell) {
		shell.pack();
	}

	private void createContents(final Shell shell) {
		shell.setLayout(new GridLayout(2, false));

		final Label icon = new Label(shell, SWT.NONE);
		icon.setImage(Images.getMainIcons()[5]);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).span(1, 2).applyTo(icon);

		final LinkSelectionListener linkSelectionListener = new LinkSelectionListener();

		final Link appInfo = new Link(shell, SWT.WRAP);
		final Version version = Version.getInstance();
		GridDataFactory.swtDefaults().align(SWT.LEAD, SWT.CENTER).grab(false, true).applyTo(appInfo);
		appInfo.setText(buildAnchor(Messages.get("lbl.about.app.url"), Messages.get("lbl.csv2sql.title")) + ' ' + Messages.get("lbl.about.app.version", version.getNumber(), DateFormat.getDateInstance(DateFormat.MEDIUM, Messages.getLanguage().getLocale()).format(version.getDate())));
		appInfo.addSelectionListener(linkSelectionListener);

		final Link iconInfo = new Link(shell, SWT.WRAP);
		GridDataFactory.swtDefaults().align(SWT.LEAD, SWT.CENTER).grab(false, true).applyTo(iconInfo);
		iconInfo.setText(Messages.get("lbl.about.icon", buildAnchor(Messages.get("lbl.about.icon.url"), Messages.get("lbl.about.icon.author"))));
		iconInfo.addSelectionListener(linkSelectionListener);

		final Button okButton = new Button(shell, SWT.PUSH);
		okButton.setText(Messages.get("lbl.button.ok"));
		final GC gc = new GC(okButton);
		gc.setFont(okButton.getFont());
		final int buttonWidth = org.eclipse.jface.dialogs.Dialog.convertHorizontalDLUsToPixels(gc.getFontMetrics(), IDialogConstants.BUTTON_WIDTH);
		gc.dispose();
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).span(2, 1).minSize(buttonWidth, SWT.DEFAULT).applyTo(okButton);
		okButton.setFocus();
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				shell.close();
			}
		});
		shell.setDefaultButton(okButton);
	}

	private static String buildAnchor(final String href, final String label) {
		return new StringBuilder("<a href=\"").append(href).append("\">").append(label).append("</a>").toString();
	}

}
