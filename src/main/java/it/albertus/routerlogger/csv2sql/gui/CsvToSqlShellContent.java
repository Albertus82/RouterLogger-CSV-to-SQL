package it.albertus.routerlogger.csv2sql.gui;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import it.albertus.jface.EnhancedErrorDialog;
import it.albertus.jface.Multilanguage;
import it.albertus.jface.SwtUtils;
import it.albertus.jface.decoration.ControlValidatorDecoration;
import it.albertus.jface.listener.ByteVerifyListener;
import it.albertus.jface.validation.ByteTextValidator;
import it.albertus.jface.validation.ControlValidator;
import it.albertus.jface.validation.StringTextValidator;
import it.albertus.jface.validation.Validator;
import it.albertus.routerlogger.csv2sql.engine.CsvToSqlConfig;
import it.albertus.routerlogger.csv2sql.engine.CsvToSqlEngine;
import it.albertus.routerlogger.csv2sql.resources.Messages;
import it.albertus.util.Configuration;
import it.albertus.util.logging.LoggerFactory;

public class CsvToSqlShellContent implements Multilanguage {

	public static final String TIMESTAMP_BASE_COLUMN_NAME = "timestamp";
	public static final String RESPONSE_TIME_BASE_COLUMN_NAME = "response_time_ms";

	public static class Defaults {
		public static final String CSV_FIELD_SEPARATOR = ";";
		public static final String CSV_DATE_PATTERN = "dd/MM/yyyy HH:mm:ss.SSS";
		public static final String DATABASE_TABLE_NAME = "router_log";
		public static final String DATABASE_COLUMN_NAME_PREFIX = "rl_";
		public static final int DATABASE_COLUMN_NAME_MAX_LENGTH = 30;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	private static final String LBL_CSV2SQL_SOURCE_MENU_DELETE_KEY = "lbl.csv2sql.source.menu.delete.key";

	private static final Logger logger = LoggerFactory.getLogger(CsvToSqlShellContent.class);

	private final Configuration configuration = CsvToSqlConfig.getInstance();

	private final Shell shell;

	private Group sourceGroup;
	private Label sourceFilesLabel;
	private List sourceFilesList;
	private MenuItem deleteMenuItem;
	private MenuItem selectAllMenuItem;
	private MenuItem clearMenuItem;
	private Button addSourceFileButton;
	private Button removeSourceFileButton;
	private Button clearSourceFilesButton;
	private Label csvSeparatorLabel;
	private Text csvSeparatorText;
	private Label csvTimestampPatternLabel;
	private Text csvTimestampPatternText;
	private Button csvResponseTimeFlag;

	private Group destinationGroup;
	private Label destinationDirectoryLabel;
	private Text destinationDirectoryText;
	private Button browseDirectoryButton;
	private Label sqlTableNameLabel;
	private Text sqlTableNameText;
	private Label sqlColumnNamesPrefixLabel;
	private Text sqlColumnNamesPrefixText;
	private Label sqlMaxLengthColumnNamesLabel;
	private Text sqlMaxLengthColumnNamesText;

	private Button processButton;
	private Button closeButton;

	private final Set<Validator> validators = new HashSet<>();

	private final ModifyListener textModifyListener = event -> updateProcessButtonStatus();

	/**
	 * Constructs a new instance of the <em>CSV to SQL converter</em> window,
	 * based on the provided shell.
	 * 
	 * @param shell the shell in which all the controls will be created (cannot
	 *        be null)
	 * 
	 * @see #open()
	 */
	public CsvToSqlShellContent(final Shell shell) {
		if (shell == null) {
			throw new NullPointerException("shell cannot be null");
		}
		else if (shell.isDisposed()) {
			throw new SWTException(SWT.ERROR_WIDGET_DISPOSED);
		}
		this.shell = shell;
	}

	@Override
	public Shell getShell() {
		return shell;
	}

	/** Opens this <em>CSV to SQL converter</em> window. */
	public void open() {
		shell.setData("lbl.csv2sql.title");
		shell.setText(Messages.get(shell.getData().toString()));
		shell.setImages(Images.getMainIcons());
		createContents(shell);
		constrainShellSize(shell);
		shell.open();
	}

	private void constrainShellSize(final Shell shell) {
		shell.pack();
		final Point minSize = shell.getSize();
		shell.setMinimumSize(minSize);
		shell.setSize(SwtUtils.convertHorizontalDLUsToPixels(sourceFilesList, 280), minSize.y);
	}

	protected void createContents(final Shell shell) {
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(shell);
		createSourceGroup(shell);
		createDestinationGroup(shell);
		createButtonBar(shell);
	}

	private void createSourceGroup(final Shell parent) {
		sourceGroup = new Group(parent, SWT.NONE);
		sourceGroup.setData("lbl.csv2sql.source");
		sourceGroup.setText(Messages.get(sourceGroup.getData().toString()));
		GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(sourceGroup);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(sourceGroup);
		createSourceFilesList(sourceGroup);
		createCsvSeparatorField(sourceGroup);
		createCsvDatePatternField(sourceGroup);
		createCsvResponseTimeFlag(sourceGroup);
	}

	private void createSourceFilesList(final Composite parent) {
		sourceFilesLabel = new Label(parent, SWT.NONE);
		sourceFilesLabel.setData("lbl.csv2sql.source.files");
		sourceFilesLabel.setText(Messages.get(sourceFilesLabel.getData().toString()));
		GridDataFactory.fillDefaults().span(2, 1).applyTo(sourceFilesLabel);

		sourceFilesList = new List(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		GridDataFactory.fillDefaults().span(1, 3).grab(true, true).applyTo(sourceFilesList);

		createSourceAddButton(parent);
		createSourceRemoveButton(parent);
		createSourceClearButton(parent);

		final ControlValidator<List> validator = new ControlValidator<List>(sourceFilesList) {
			@Override
			public boolean isValid() {
				return sourceFilesList != null && !sourceFilesList.isDisposed() && sourceFilesList.getItemCount() > 0;
			}
		};
		validators.add(validator);

		sourceFilesList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				if (e != null) {
					if (SWT.NONE == e.stateMask && SwtUtils.KEY_DELETE == e.keyCode && sourceFilesList.getSelectionCount() > 0) {
						removeSelectedItemsFromList();
					}
					else if (SWT.MOD1 == e.stateMask && SwtUtils.KEY_SELECT_ALL == e.keyCode) {
						sourceFilesList.selectAll();
					}
				}
			}
		});

		sourceFilesList.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (sourceFilesList.getSelectionCount() > 0) {
					removeSourceFileButton.setEnabled(true);
				}
				else {
					removeSourceFileButton.setEnabled(false);
				}
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				widgetSelected(e);
			}
		});

		createSourceFilesListMenu(sourceFilesList);
	}

	private void createSourceAddButton(final Composite parent) {
		addSourceFileButton = new Button(parent, SWT.PUSH);
		addSourceFileButton.setData("lbl.csv2sql.source.add");
		addSourceFileButton.setText(Messages.get(addSourceFileButton.getData().toString()));
		final int addButtonWidth = SwtUtils.convertHorizontalDLUsToPixels(addSourceFileButton, IDialogConstants.BUTTON_WIDTH);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).hint(addButtonWidth, SWT.DEFAULT).applyTo(addSourceFileButton);

		addSourceFileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final Set<String> files = selectSourceFiles(parent.getShell());
				if (!files.isEmpty()) {
					files.addAll(Arrays.asList(sourceFilesList.getItems())); // merge
					sourceFilesList.setItems(files.toArray(new String[files.size()]));
					if (sourceFilesList.getItemCount() > 0) {
						clearSourceFilesButton.setEnabled(true);
					}
					if (destinationDirectoryText != null && !destinationDirectoryText.isDisposed() && destinationDirectoryText.getCharCount() == 0 && sourceFilesList.getItemCount() > 0) {
						final String lastItem = sourceFilesList.getItem(sourceFilesList.getItemCount() - 1);
						destinationDirectoryText.setText(new File(lastItem).getParent());
					}
				}
				updateProcessButtonStatus();
			}
		});
	}

	private void createSourceRemoveButton(final Composite parent) {
		removeSourceFileButton = new Button(parent, SWT.PUSH);
		removeSourceFileButton.setEnabled(false);
		removeSourceFileButton.setData("lbl.csv2sql.source.remove");
		removeSourceFileButton.setText(Messages.get(removeSourceFileButton.getData().toString()));
		final int removeButtonWidth = SwtUtils.convertHorizontalDLUsToPixels(removeSourceFileButton, IDialogConstants.BUTTON_WIDTH);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).hint(removeButtonWidth, SWT.DEFAULT).applyTo(removeSourceFileButton);

		removeSourceFileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				removeSelectedItemsFromList();
			}
		});
	}

	private void createSourceClearButton(final Composite parent) {
		clearSourceFilesButton = new Button(parent, SWT.PUSH);
		clearSourceFilesButton.setEnabled(false);
		clearSourceFilesButton.setData("lbl.csv2sql.source.clear");
		clearSourceFilesButton.setText(Messages.get(clearSourceFilesButton.getData().toString()));
		final int clearButtonWidth = SwtUtils.convertHorizontalDLUsToPixels(clearSourceFilesButton, IDialogConstants.BUTTON_WIDTH);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).hint(clearButtonWidth, SWT.DEFAULT).applyTo(clearSourceFilesButton);

		clearSourceFilesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				removeAllItemsFromList();
			}
		});
	}

	private void createSourceFilesListMenu(final List sourceFilesList) {
		final Menu contextMenu = new Menu(sourceFilesList);

		// Remove...
		deleteMenuItem = new MenuItem(contextMenu, SWT.PUSH);
		deleteMenuItem.setData("lbl.csv2sql.source.remove");
		deleteMenuItem.setText(Messages.get(deleteMenuItem.getData().toString()) + SwtUtils.getShortcutLabel(Messages.get(LBL_CSV2SQL_SOURCE_MENU_DELETE_KEY)));
		deleteMenuItem.setAccelerator(SwtUtils.KEY_DELETE); // dummy
		deleteMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				removeSelectedItemsFromList();
			}
		});

		new MenuItem(contextMenu, SWT.SEPARATOR);

		// Select all...
		selectAllMenuItem = new MenuItem(contextMenu, SWT.PUSH);
		selectAllMenuItem.setData("lbl.menu.item.select.all");
		selectAllMenuItem.setText(Messages.get(selectAllMenuItem.getData().toString()) + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_SELECT_ALL));
		selectAllMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_SELECT_ALL); // dummy
		selectAllMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				sourceFilesList.selectAll();
			}
		});

		new MenuItem(contextMenu, SWT.SEPARATOR);

		// Clear...
		clearMenuItem = new MenuItem(contextMenu, SWT.PUSH);
		clearMenuItem.setData("lbl.csv2sql.source.clear");
		clearMenuItem.setText(Messages.get(clearMenuItem.getData().toString()));
		clearMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				removeAllItemsFromList();
			}
		});

		sourceFilesList.addMenuDetectListener(event -> {
			deleteMenuItem.setEnabled(sourceFilesList.getSelectionCount() > 0);
			selectAllMenuItem.setEnabled(sourceFilesList.getItemCount() > 0);
			clearMenuItem.setEnabled(sourceFilesList.getItemCount() > 0);
			contextMenu.setVisible(true);
		});
	}

	private void createCsvSeparatorField(final Composite parent) {
		csvSeparatorLabel = new Label(parent, SWT.NONE);
		csvSeparatorLabel.setData("lbl.csv2sql.source.csv.separator");
		csvSeparatorLabel.setText(Messages.get(csvSeparatorLabel.getData().toString()));
		GridDataFactory.fillDefaults().span(2, 1).applyTo(csvSeparatorLabel);

		csvSeparatorText = new Text(parent, SWT.BORDER);
		csvSeparatorText.setText(configuration.getString("csv.field.separator", Defaults.CSV_FIELD_SEPARATOR));
		csvSeparatorText.setTextLimit(Byte.MAX_VALUE);
		csvSeparatorText.addModifyListener(textModifyListener);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(csvSeparatorText);

		final ControlValidator<Text> validator = new StringTextValidator(csvSeparatorText, false);
		new ControlValidatorDecoration(validator, () -> Messages.get("err.csv2sql.source.csv.separator"));
		validators.add(validator);
	}

	private void createCsvDatePatternField(final Composite parent) {
		csvTimestampPatternLabel = new Label(parent, SWT.NONE);
		csvTimestampPatternLabel.setData("lbl.csv2sql.source.csv.date.pattern");
		csvTimestampPatternLabel.setText(Messages.get(csvTimestampPatternLabel.getData().toString()));
		GridDataFactory.fillDefaults().span(2, 1).applyTo(csvTimestampPatternLabel);

		csvTimestampPatternText = new Text(parent, SWT.BORDER);
		csvTimestampPatternText.setText(Defaults.CSV_DATE_PATTERN);
		csvTimestampPatternText.setTextLimit(Byte.MAX_VALUE);
		csvTimestampPatternText.addModifyListener(textModifyListener);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(csvTimestampPatternText);

		final ControlValidator<Text> validator = new ControlValidator<Text>(csvTimestampPatternText) {
			@Override
			public boolean isValid() {
				if (getControl().getText().trim().isEmpty()) {
					return false;
				}
				try {
					new SimpleDateFormat(getControl().getText());
					return true;
				}
				catch (final Exception e) {
					return false;
				}
			}
		};
		new ControlValidatorDecoration(validator, () -> Messages.get("err.csv2sql.source.csv.date.pattern"));
		validators.add(validator);
	}

	private void createCsvResponseTimeFlag(final Composite parent) {
		csvResponseTimeFlag = new Button(parent, SWT.CHECK);
		csvResponseTimeFlag.setData("lbl.csv2sql.source.csv.responseTime");
		csvResponseTimeFlag.setText(Messages.get(csvResponseTimeFlag.getData().toString()));
		csvResponseTimeFlag.setSelection(true);
		GridDataFactory.swtDefaults().span(2, 1).applyTo(csvResponseTimeFlag);
	}

	private void createDestinationGroup(final Shell shell) {
		destinationGroup = new Group(shell, SWT.NONE);
		destinationGroup.setData("lbl.csv2sql.destination");
		destinationGroup.setText(Messages.get(destinationGroup.getData().toString()));
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(destinationGroup);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(destinationGroup);
		createDestinationDirectoryField(destinationGroup);
		createDatabaseTableNameField(destinationGroup);
		createDatabaseColumnNamePrefixField(destinationGroup);
		createDatabaseMaxLengthColumnNamesField(destinationGroup);
	}

	private void createDestinationDirectoryField(final Composite parent) {
		destinationDirectoryLabel = new Label(parent, SWT.NONE);
		destinationDirectoryLabel.setData("lbl.csv2sql.destination.directory");
		destinationDirectoryLabel.setText(Messages.get(destinationDirectoryLabel.getData().toString()));
		GridDataFactory.fillDefaults().span(2, 1).applyTo(destinationDirectoryLabel);

		destinationDirectoryText = new Text(parent, SWT.BORDER);
		destinationDirectoryText.setEditable(false);
		destinationDirectoryText.addModifyListener(textModifyListener);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(destinationDirectoryText);

		final ControlValidator<Text> validator = new StringTextValidator(destinationDirectoryText, false);
		validators.add(validator);

		browseDirectoryButton = new Button(parent, SWT.PUSH);
		browseDirectoryButton.setData("lbl.button.browse");
		browseDirectoryButton.setText(Messages.get(browseDirectoryButton.getData().toString()));
		final int buttonWidth = SwtUtils.convertHorizontalDLUsToPixels(browseDirectoryButton, IDialogConstants.BUTTON_WIDTH);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).hint(buttonWidth, SWT.DEFAULT).applyTo(browseDirectoryButton);
		browseDirectoryButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final String dir = selectDestinationPath(parent.getShell());
				if (dir != null) {
					destinationDirectoryText.setText(dir);
				}
			}
		});
	}

	private void createDatabaseTableNameField(final Composite parent) {
		sqlTableNameLabel = new Label(parent, SWT.NONE);
		sqlTableNameLabel.setData("lbl.csv2sql.destination.table.name");
		sqlTableNameLabel.setText(Messages.get(sqlTableNameLabel.getData().toString()));
		GridDataFactory.fillDefaults().span(2, 1).applyTo(sqlTableNameLabel);

		sqlTableNameText = new Text(parent, SWT.BORDER);
		sqlTableNameText.setTextLimit(Byte.MAX_VALUE);
		sqlTableNameText.setText(configuration.getString("database.table.name", Defaults.DATABASE_TABLE_NAME));
		sqlTableNameText.addModifyListener(textModifyListener);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(sqlTableNameText);

		final ControlValidator<Text> validator = new StringTextValidator(sqlTableNameText, false) {
			@Override
			public boolean isValid() {
				return super.isValid() && !sqlTableNameText.getText().trim().isEmpty();
			}
		};
		new ControlValidatorDecoration(validator, () -> Messages.get("err.csv2sql.destination.table.name"));
		validators.add(validator);
	}

	private void createDatabaseColumnNamePrefixField(final Composite parent) {
		sqlColumnNamesPrefixLabel = new Label(parent, SWT.NONE);
		sqlColumnNamesPrefixLabel.setData("lbl.csv2sql.destination.column.name.prefix");
		sqlColumnNamesPrefixLabel.setText(Messages.get(sqlColumnNamesPrefixLabel.getData().toString()));
		GridDataFactory.fillDefaults().span(2, 1).applyTo(sqlColumnNamesPrefixLabel);

		sqlColumnNamesPrefixText = new Text(parent, SWT.BORDER);
		sqlColumnNamesPrefixText.setTextLimit(Byte.MAX_VALUE);
		sqlColumnNamesPrefixText.setText(configuration.getString("database.column.name.prefix", Defaults.DATABASE_COLUMN_NAME_PREFIX));
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(sqlColumnNamesPrefixText);
	}

	private void createDatabaseMaxLengthColumnNamesField(final Composite parent) {
		sqlMaxLengthColumnNamesLabel = new Label(parent, SWT.NONE);
		sqlMaxLengthColumnNamesLabel.setData("lbl.csv2sql.destination.column.name.max.length");
		sqlMaxLengthColumnNamesLabel.setText(Messages.get(sqlMaxLengthColumnNamesLabel.getData().toString()));
		GridDataFactory.fillDefaults().span(2, 1).applyTo(sqlMaxLengthColumnNamesLabel);

		sqlMaxLengthColumnNamesText = new Text(parent, SWT.BORDER);
		sqlMaxLengthColumnNamesText.setTextLimit(String.valueOf(Byte.MAX_VALUE).length());
		sqlMaxLengthColumnNamesText.setText(Integer.toString(configuration.getInt("database.column.name.max.length", Defaults.DATABASE_COLUMN_NAME_MAX_LENGTH)));
		sqlMaxLengthColumnNamesText.addModifyListener(textModifyListener);
		sqlMaxLengthColumnNamesText.addVerifyListener(new ByteVerifyListener(false));
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(sqlMaxLengthColumnNamesText);

		final ControlValidator<Text> validator = new ByteTextValidator(sqlMaxLengthColumnNamesText, false, (byte) 8, Byte.MAX_VALUE);
		new ControlValidatorDecoration(validator, () -> Messages.get("err.preferences.integer.range", 8, Byte.MAX_VALUE));
		validators.add(validator);
	}

	private void createButtonBar(final Shell shell) {
		createProcessButton(shell);
		createCloseButton(shell);
	}

	private void createProcessButton(final Shell shell) {
		processButton = new Button(shell, SWT.PUSH);
		processButton.setEnabled(false);
		processButton.setData("lbl.csv2sql.button.convert");
		processButton.setText(Messages.get(processButton.getData().toString()));
		final int buttonWidth = SwtUtils.convertHorizontalDLUsToPixels(processButton, IDialogConstants.BUTTON_WIDTH);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).grab(true, false).minSize(buttonWidth, SWT.DEFAULT).applyTo(processButton);
		processButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				process(shell);
			}
		});
	}

	private void createCloseButton(final Shell shell) {
		closeButton = new Button(shell, SWT.PUSH);
		closeButton.setData("lbl.button.close");
		closeButton.setText(Messages.get(closeButton.getData().toString()));
		final int buttonWidth = SwtUtils.convertHorizontalDLUsToPixels(closeButton, IDialogConstants.BUTTON_WIDTH);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).grab(true, false).minSize(buttonWidth, SWT.DEFAULT).applyTo(closeButton);
		closeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				shell.close();
			}
		});
	}

	private void updateProcessButtonStatus() {
		if (processButton != null && !processButton.isDisposed()) {
			processButton.setEnabled(isValid());
		}
	}

	private boolean isValid() {
		for (final Validator validator : validators) {
			if (!validator.isValid()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Converts the selected CSV files to SQL scripts.
	 * 
	 * @param shell the parent shell, needed to open the progress monitor dialog
	 */
	private void process(final Shell shell) {
		try {
			final String sqlTableName = sqlTableNameText.getText().trim();
			final String sqlColumnNamesPrefix = sqlColumnNamesPrefixText.getText().trim();
			final String sqlTimestampColumnName = TIMESTAMP_BASE_COLUMN_NAME;
			final String sqlResponseTimeColumnName = csvResponseTimeFlag.getSelection() ? RESPONSE_TIME_BASE_COLUMN_NAME : null;
			final int sqlMaxLengthColumnNames = Integer.parseInt(sqlMaxLengthColumnNamesText.getText().trim());
			final String csvSeparator = csvSeparatorText.getText();
			final String csvTimestampPattern = csvTimestampPatternText.getText().trim();

			final CsvToSqlEngine converter = new CsvToSqlEngine(csvSeparator, csvTimestampPattern, sqlTableName, sqlColumnNamesPrefix, sqlTimestampColumnName, sqlResponseTimeColumnName, sqlMaxLengthColumnNames);

			final CsvToSqlRunnable runnable = new CsvToSqlRunnable(converter, sourceFilesList.getItems(), destinationDirectoryText.getText());

			ProgressMonitorDialog.setDefaultImages(shell.getImages());
			final ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell) {
				@Override
				protected void createCancelButton(final Composite parent) {
					super.createCancelButton(parent);
					cancel.setText(Messages.get("lbl.button.cancel"));
				}

				@Override
				protected void configureShell(final Shell shell) {
					super.configureShell(shell);
					shell.setText(Messages.get("lbl.csv2sql.progress.text"));
				}
			};

			dialog.run(true, true, runnable);

			final MessageBox box = new MessageBox(shell, SWT.ICON_INFORMATION);
			box.setText(shell.getText());
			box.setMessage(Messages.get("msg.csv2sql.dialog.result.message.success"));
			box.open();
		}
		catch (final InvocationTargetException e) {
			final String message = Messages.get("err.csv2sql.invocationTargetException");
			logger.log(Level.WARNING, message, e);
			EnhancedErrorDialog.openError(shell, shell.getText(), message, IStatus.WARNING, e.getCause() != null ? e.getCause() : e, shell.getDisplay().getSystemImage(SWT.ICON_WARNING));
		}
		catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.log(Level.FINE, e.toString(), e);
			final MessageBox box = new MessageBox(shell, SWT.ICON_INFORMATION);
			box.setText(shell.getText());
			box.setMessage(Messages.get("msg.csv2sql.dialog.result.message.cancelled"));
			box.open();
		}
		catch (final Exception e) {
			final String message = Messages.get("err.csv2sql.exception");
			logger.log(Level.SEVERE, message, e);
			EnhancedErrorDialog.openError(shell, shell.getText(), message, IStatus.ERROR, e, shell.getDisplay().getSystemImage(SWT.ICON_ERROR));
		}
	}

	/**
	 * Opens the file dialog to set the source files.
	 * 
	 * @param parent the parent shell
	 * @return the selected file names
	 */
	private Set<String> selectSourceFiles(final Shell parent) {
		final FileDialog openDialog = new FileDialog(parent, SWT.OPEN | SWT.MULTI);
		openDialog.setFilterExtensions(new String[] { "*.CSV;*.csv" });
		openDialog.open();
		final Set<String> fileNames = new TreeSet<>();
		for (final String fileName : openDialog.getFileNames()) {
			fileNames.add(openDialog.getFilterPath() + File.separator + fileName);
		}
		return fileNames;
	}

	/**
	 * Opens the directory dialog to set destination directory.
	 * 
	 * @param parent the parent shell
	 * @return the selected directory
	 */
	private String selectDestinationPath(final Shell parent) {
		final DirectoryDialog saveDialog = new DirectoryDialog(parent, SWT.NONE);
		saveDialog.setText(Messages.get("lbl.csv2sql.destination.dialog.text"));
		saveDialog.setMessage(Messages.get("lbl.csv2sql.destination.dialog.message"));
		return saveDialog.open();
	}

	private void removeSelectedItemsFromList() {
		removeSourceFileButton.setEnabled(false);
		if (sourceFilesList.getSelectionCount() > 0) {
			sourceFilesList.remove(sourceFilesList.getSelectionIndices());
			if (sourceFilesList.getItemCount() == 0) {
				clearSourceFilesButton.setEnabled(false);
			}
			updateProcessButtonStatus();
		}
	}

	private void removeAllItemsFromList() {
		clearSourceFilesButton.setEnabled(false);
		if (sourceFilesList.getItemCount() > 0) {
			sourceFilesList.removeAll();
			removeSourceFileButton.setEnabled(false);
			updateProcessButtonStatus();
		}
	}

	@Override
	public void updateLabels() {
		sourceGroup.setText(Messages.get(sourceGroup.getData().toString()));
		sourceFilesLabel.setText(Messages.get(sourceFilesLabel.getData().toString()));
		deleteMenuItem.setText(Messages.get(deleteMenuItem.getData().toString()) + SwtUtils.getShortcutLabel(Messages.get(LBL_CSV2SQL_SOURCE_MENU_DELETE_KEY)));
		selectAllMenuItem.setText(Messages.get(selectAllMenuItem.getData().toString()) + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_SELECT_ALL));
		clearMenuItem.setText(Messages.get(clearMenuItem.getData().toString()));
		addSourceFileButton.setText(Messages.get(addSourceFileButton.getData().toString()));
		removeSourceFileButton.setText(Messages.get(removeSourceFileButton.getData().toString()));
		clearSourceFilesButton.setText(Messages.get(clearSourceFilesButton.getData().toString()));
		csvSeparatorLabel.setText(Messages.get(csvSeparatorLabel.getData().toString()));
		csvTimestampPatternLabel.setText(Messages.get(csvTimestampPatternLabel.getData().toString()));
		csvResponseTimeFlag.setText(Messages.get(csvResponseTimeFlag.getData().toString()));

		destinationGroup.setText(Messages.get(destinationGroup.getData().toString()));
		destinationDirectoryLabel.setText(Messages.get(destinationDirectoryLabel.getData().toString()));
		browseDirectoryButton.setText(Messages.get(browseDirectoryButton.getData().toString()));
		sqlTableNameLabel.setText(Messages.get(sqlTableNameLabel.getData().toString()));
		sqlColumnNamesPrefixLabel.setText(Messages.get(sqlColumnNamesPrefixLabel.getData().toString()));
		sqlMaxLengthColumnNamesLabel.setText(Messages.get(sqlMaxLengthColumnNamesLabel.getData().toString()));

		processButton.setText(Messages.get(processButton.getData().toString()));
		closeButton.setText(Messages.get(closeButton.getData().toString()));
	}

}
