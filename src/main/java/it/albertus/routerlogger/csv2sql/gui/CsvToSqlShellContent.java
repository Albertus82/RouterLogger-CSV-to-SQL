package it.albertus.routerlogger.csv2sql.gui;

import static it.albertus.jface.decoration.ControlValidatorDecoration.DEFAULT_STYLE;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
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

	public static final byte DATABASE_COLUMN_NAME_MIN_LENGTH = 8;

	public static final String TIMESTAMP_BASE_COLUMN_NAME = "timestamp";
	public static final String RESPONSE_TIME_BASE_COLUMN_NAME = "response_time_ms";

	public static class Defaults {
		public static final String CSV_FIELD_SEPARATOR = ";";
		public static final String CSV_DATE_PATTERN = "dd/MM/yyyy HH:mm:ss.SSS";
		public static final boolean CSV_RESPONSE_TIME = true;
		public static final String DATABASE_TABLE_NAME = "router_log";
		public static final String DATABASE_COLUMN_NAME_PREFIX = "rl_";
		public static final byte DATABASE_COLUMN_NAME_MAX_LENGTH = 30;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	private static final String CSV_FIELD_SEPARATOR = "csv.field.separator";
	private static final String CSV_DATE_PATTERN = "csv.date.pattern";
	private static final String CSV_RESPONSE_TIME = "csv.response.time";
	private static final String DATABASE_DIRECTORY = "database.directory";
	private static final String DATABASE_TABLE_NAME = "database.table.name";
	private static final String DATABASE_COLUMN_NAME_PREFIX = "database.column.name.prefix";
	private static final String DATABASE_COLUMN_NAME_MAX_LENGTH = "database.column.name.max.length";

	public static final String LBL_CSV2SQL_TITLE = "lbl.csv2sql.title";

	private static final String LBL_CSV2SQL_SOURCE_MENU_DELETE_KEY = "lbl.csv2sql.source.menu.delete.key";

	private static final Logger logger = LoggerFactory.getLogger(CsvToSqlShellContent.class);

	private final Configuration configuration = CsvToSqlConfig.getInstance();

	private final Shell shell;

	// Source group
	private Group sourceGroup;
	private Label sourceFilesLabel;
	private List sourceFilesList;
	private Menu contextMenu;
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

	// Destination group
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

	// Button bar
	private Button processButton;
	private Button closeButton;

	private final Collection<Validator> validators = new HashSet<>();

	private final IGuiDirector director = new GuiDirector();

	private final ModifyListener textModifyListener = director::textModified;

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
		shell.setData(LBL_CSV2SQL_TITLE);
		shell.setText(Messages.get(shell.getData().toString()));
		shell.setImages(Images.getMainIcons());
		createContents(shell);
		constrainShellSize(shell);
		shell.open();
	}

	private void constrainShellSize(final Shell shell) {
		shell.pack();
		final Point minSize = shell.getSize();
		shell.setMinimumSize(minSize.x, shell.getMinimumSize().y);
		shell.setSize(SwtUtils.convertHorizontalDLUsToPixels(sourceFilesList, 280), minSize.y);
	}

	protected void createContents(final Shell shell) {
		shell.setLayout(new FillLayout());

		final ScrolledComposite scrollable = new ScrolledComposite(shell, SWT.V_SCROLL);
		final Composite composite = new Composite(scrollable, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(composite);

		createSourceGroup(composite);
		createDestinationGroup(composite);
		createButtonBar(composite);

		scrollable.setContent(composite);
		scrollable.setExpandVertical(true);
		scrollable.setExpandHorizontal(true);
		scrollable.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private void createSourceGroup(final Composite parent) {
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
				director.sourceFileListKeyPressed(e);
			}
		});

		sourceFilesList.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				director.sourceFileListSelected(e);
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				director.sourceFileListSelected(e);
			}
		});

		createSourceFilesListMenu(sourceFilesList);
	}

	private void createSourceAddButton(final Composite parent) {
		addSourceFileButton = new Button(parent, SWT.PUSH);
		addSourceFileButton.setData("lbl.csv2sql.source.add");
		addSourceFileButton.setText(Messages.get(addSourceFileButton.getData().toString()));
		final int addButtonWidth = SwtUtils.convertHorizontalDLUsToPixels(addSourceFileButton, IDialogConstants.BUTTON_WIDTH);
		GridDataFactory.swtDefaults().align(SWT.LEAD, SWT.TOP).hint(addButtonWidth, SWT.DEFAULT).applyTo(addSourceFileButton);

		addSourceFileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				director.addSourceFilesButtonSelected(e);
			}
		});
	}

	private void createSourceRemoveButton(final Composite parent) {
		removeSourceFileButton = new Button(parent, SWT.PUSH);
		removeSourceFileButton.setEnabled(false);
		removeSourceFileButton.setData("lbl.csv2sql.source.remove");
		removeSourceFileButton.setText(Messages.get(removeSourceFileButton.getData().toString()));
		final int removeButtonWidth = SwtUtils.convertHorizontalDLUsToPixels(removeSourceFileButton, IDialogConstants.BUTTON_WIDTH);
		GridDataFactory.swtDefaults().align(SWT.LEAD, SWT.TOP).hint(removeButtonWidth, SWT.DEFAULT).applyTo(removeSourceFileButton);

		removeSourceFileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				director.removeSourceFilesButtonSelected(e);
			}
		});
	}

	private void createSourceClearButton(final Composite parent) {
		clearSourceFilesButton = new Button(parent, SWT.PUSH);
		clearSourceFilesButton.setEnabled(false);
		clearSourceFilesButton.setData("lbl.csv2sql.source.clear");
		clearSourceFilesButton.setText(Messages.get(clearSourceFilesButton.getData().toString()));
		final int clearButtonWidth = SwtUtils.convertHorizontalDLUsToPixels(clearSourceFilesButton, IDialogConstants.BUTTON_WIDTH);
		GridDataFactory.swtDefaults().align(SWT.LEAD, SWT.TOP).hint(clearButtonWidth, SWT.DEFAULT).applyTo(clearSourceFilesButton);

		clearSourceFilesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				director.clearSourceFilesButtonSelected(e);
			}
		});
	}

	private void createSourceFilesListMenu(final List sourceFilesList) {
		contextMenu = new Menu(sourceFilesList);

		// Remove...
		deleteMenuItem = new MenuItem(contextMenu, SWT.PUSH);
		deleteMenuItem.setData("lbl.csv2sql.source.remove");
		deleteMenuItem.setText(Messages.get(deleteMenuItem.getData().toString()) + SwtUtils.getShortcutLabel(Messages.get(LBL_CSV2SQL_SOURCE_MENU_DELETE_KEY)));
		deleteMenuItem.setAccelerator(SwtUtils.KEY_DELETE); // dummy
		deleteMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				director.removeSourceFilesMenuItemSelected(e);
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
				director.selectAllSourceFilesMenuItemSelected(e);
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
				director.clearSourceFilesMenuItemSelected(e);
			}
		});

		sourceFilesList.addMenuDetectListener(director::sourceFileListContextMenuDetected);
	}

	private void createCsvSeparatorField(final Composite parent) {
		csvSeparatorLabel = new Label(parent, SWT.NONE);
		csvSeparatorLabel.setData("lbl.csv2sql.source.csv.separator");
		csvSeparatorLabel.setText(Messages.get(csvSeparatorLabel.getData().toString()));
		GridDataFactory.fillDefaults().span(2, 1).applyTo(csvSeparatorLabel);

		csvSeparatorText = new Text(parent, SWT.BORDER);
		csvSeparatorText.setTextLimit(Byte.MAX_VALUE);
		csvSeparatorText.addModifyListener(textModifyListener);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(csvSeparatorText);

		final ControlValidator<Text> validator = new StringTextValidator(csvSeparatorText, false);
		new ControlValidatorDecoration(validator, () -> Messages.get("err.csv2sql.source.csv.separator"), DEFAULT_STYLE, FieldDecorationRegistry.DEC_REQUIRED);
		validators.add(validator);
		csvSeparatorText.setText(configuration.getString(CSV_FIELD_SEPARATOR, Defaults.CSV_FIELD_SEPARATOR));
	}

	private void createCsvDatePatternField(final Composite parent) {
		csvTimestampPatternLabel = new Label(parent, SWT.NONE);
		csvTimestampPatternLabel.setData("lbl.csv2sql.source.csv.date.pattern");
		csvTimestampPatternLabel.setText(Messages.get(csvTimestampPatternLabel.getData().toString()));
		GridDataFactory.fillDefaults().span(2, 1).applyTo(csvTimestampPatternLabel);

		csvTimestampPatternText = new Text(parent, SWT.BORDER);
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
		csvTimestampPatternText.setText(configuration.getString(CSV_DATE_PATTERN, Defaults.CSV_DATE_PATTERN));
	}

	private void createCsvResponseTimeFlag(final Composite parent) {
		csvResponseTimeFlag = new Button(parent, SWT.CHECK);
		csvResponseTimeFlag.setData("lbl.csv2sql.source.csv.responseTime");
		csvResponseTimeFlag.setText(Messages.get(csvResponseTimeFlag.getData().toString()));
		csvResponseTimeFlag.setSelection(configuration.getBoolean(CSV_RESPONSE_TIME, Defaults.CSV_RESPONSE_TIME));
		GridDataFactory.swtDefaults().span(2, 1).applyTo(csvResponseTimeFlag);
	}

	private void createDestinationGroup(final Composite shell) {
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
		destinationDirectoryText.setText(configuration.getString(DATABASE_DIRECTORY, ""));
		destinationDirectoryText.addModifyListener(textModifyListener);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(destinationDirectoryText);

		final ControlValidator<Text> validator = new StringTextValidator(destinationDirectoryText, false) {
			@Override
			public boolean isValid() {
				return super.isValid() && new File(destinationDirectoryText.getText()).isDirectory();
			}
		};
		validators.add(validator);
		new ControlValidatorDecoration(validator, () -> Messages.get("err.csv2sql.destination.directory"));

		browseDirectoryButton = new Button(parent, SWT.PUSH);
		browseDirectoryButton.setData("lbl.button.browse");
		browseDirectoryButton.setText(Messages.get(browseDirectoryButton.getData().toString()));
		final int buttonWidth = SwtUtils.convertHorizontalDLUsToPixels(browseDirectoryButton, IDialogConstants.BUTTON_WIDTH);
		GridDataFactory.swtDefaults().align(SWT.LEAD, SWT.TOP).hint(buttonWidth, SWT.DEFAULT).applyTo(browseDirectoryButton);
		browseDirectoryButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				director.browseDirectoryButtonSelected(e);
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
		sqlTableNameText.addModifyListener(textModifyListener);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(sqlTableNameText);

		final ControlValidator<Text> validator = new StringTextValidator(sqlTableNameText, false) {
			@Override
			public boolean isValid() {
				return super.isValid() && !sqlTableNameText.getText().trim().isEmpty();
			}
		};
		new ControlValidatorDecoration(validator, () -> Messages.get("err.csv2sql.destination.table.name"), DEFAULT_STYLE, FieldDecorationRegistry.DEC_REQUIRED);
		validators.add(validator);
		sqlTableNameText.setText(configuration.getString(DATABASE_TABLE_NAME, Defaults.DATABASE_TABLE_NAME));
	}

	private void createDatabaseColumnNamePrefixField(final Composite parent) {
		sqlColumnNamesPrefixLabel = new Label(parent, SWT.NONE);
		sqlColumnNamesPrefixLabel.setData("lbl.csv2sql.destination.column.name.prefix");
		sqlColumnNamesPrefixLabel.setText(Messages.get(sqlColumnNamesPrefixLabel.getData().toString()));
		GridDataFactory.fillDefaults().span(2, 1).applyTo(sqlColumnNamesPrefixLabel);

		sqlColumnNamesPrefixText = new Text(parent, SWT.BORDER);
		sqlColumnNamesPrefixText.setTextLimit(Byte.MAX_VALUE);
		sqlColumnNamesPrefixText.setText(configuration.getString(DATABASE_COLUMN_NAME_PREFIX, Defaults.DATABASE_COLUMN_NAME_PREFIX));
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(sqlColumnNamesPrefixText);
	}

	private void createDatabaseMaxLengthColumnNamesField(final Composite parent) {
		sqlMaxLengthColumnNamesLabel = new Label(parent, SWT.NONE);
		sqlMaxLengthColumnNamesLabel.setData("lbl.csv2sql.destination.column.name.max.length");
		sqlMaxLengthColumnNamesLabel.setText(Messages.get(sqlMaxLengthColumnNamesLabel.getData().toString()));
		GridDataFactory.fillDefaults().span(2, 1).applyTo(sqlMaxLengthColumnNamesLabel);

		sqlMaxLengthColumnNamesText = new Text(parent, SWT.BORDER);
		sqlMaxLengthColumnNamesText.setTextLimit(2);
		sqlMaxLengthColumnNamesText.addModifyListener(textModifyListener);
		sqlMaxLengthColumnNamesText.addVerifyListener(new ByteVerifyListener(false));
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(sqlMaxLengthColumnNamesText);

		final ControlValidator<Text> validator = new ByteTextValidator(sqlMaxLengthColumnNamesText, false, DATABASE_COLUMN_NAME_MIN_LENGTH, null);
		new ControlValidatorDecoration(validator, () -> Messages.get("err.preferences.integer.range", 8, 99));
		validators.add(validator);
		sqlMaxLengthColumnNamesText.setText(Integer.toString(configuration.getInt(DATABASE_COLUMN_NAME_MAX_LENGTH, Defaults.DATABASE_COLUMN_NAME_MAX_LENGTH)));
	}

	private void createButtonBar(final Composite parent) {
		createProcessButton(parent);
		createCloseButton(parent);
	}

	private void createProcessButton(final Composite parent) {
		processButton = new Button(parent, SWT.PUSH);
		processButton.setEnabled(false);
		processButton.setData("lbl.csv2sql.button.convert");
		processButton.setText(Messages.get(processButton.getData().toString()));
		final int buttonWidth = SwtUtils.convertHorizontalDLUsToPixels(processButton, IDialogConstants.BUTTON_WIDTH);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).grab(true, false).minSize(buttonWidth, SWT.DEFAULT).applyTo(processButton);
		processButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				director.processButtonSelected(e);
			}
		});
	}

	private void createCloseButton(final Composite parent) {
		closeButton = new Button(parent, SWT.PUSH);
		closeButton.setData("lbl.button.close");
		closeButton.setText(Messages.get(closeButton.getData().toString()));
		final int buttonWidth = SwtUtils.convertHorizontalDLUsToPixels(closeButton, IDialogConstants.BUTTON_WIDTH);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).grab(true, false).minSize(buttonWidth, SWT.DEFAULT).applyTo(closeButton);
		closeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				director.closeButtonSelected(e);
			}
		});
	}

	@Override
	public void updateLabels() {
		shell.setText(Messages.get(shell.getData().toString()));
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

	/* Mediator */
	private class GuiDirector implements IGuiDirector {

		@Override
		public void sourceFileListKeyPressed(final KeyEvent e) {
			if (e != null) {
				if (SWT.NONE == e.stateMask && SwtUtils.KEY_DELETE == e.keyCode && sourceFilesList.getSelectionCount() > 0) {
					removeSelectedItemsFromList();
				}
				else if (SWT.MOD1 == e.stateMask && SwtUtils.KEY_SELECT_ALL == e.keyCode) {
					sourceFilesList.selectAll();
				}
			}
		}

		@Override
		public void sourceFileListSelected(final SelectionEvent e) {
			if (sourceFilesList.getSelectionCount() > 0) {
				removeSourceFileButton.setEnabled(true);
			}
			else {
				removeSourceFileButton.setEnabled(false);
			}
		}

		@Override
		public void addSourceFilesButtonSelected(final SelectionEvent e) {
			final Set<String> files = selectSourceFiles(shell);
			if (!files.isEmpty()) {
				files.addAll(Arrays.asList(sourceFilesList.getItems())); // merge
				sourceFilesList.setItems(files.toArray(new String[files.size()]));
				if (sourceFilesList.getItemCount() > 0) {
					clearSourceFilesButton.setEnabled(true);
				}
				if (destinationDirectoryText != null && !destinationDirectoryText.isDisposed() && (destinationDirectoryText.getCharCount() == 0 || !new File(destinationDirectoryText.getText()).isDirectory()) && sourceFilesList.getItemCount() > 0) {
					final String lastItem = sourceFilesList.getItem(sourceFilesList.getItemCount() - 1);
					destinationDirectoryText.setText(new File(lastItem).getParent());
				}
			}
			updateProcessButtonStatus();
		}

		@Override
		public void removeSourceFilesButtonSelected(final SelectionEvent e) {
			removeSelectedItemsFromList();
		}

		@Override
		public void clearSourceFilesButtonSelected(final SelectionEvent e) {
			removeAllItemsFromList();
		}

		@Override
		public void sourceFileListContextMenuDetected(final MenuDetectEvent e) {
			deleteMenuItem.setEnabled(sourceFilesList.getSelectionCount() > 0);
			selectAllMenuItem.setEnabled(sourceFilesList.getItemCount() > 0);
			clearMenuItem.setEnabled(sourceFilesList.getItemCount() > 0);
			contextMenu.setVisible(true);
		}

		@Override
		public void removeSourceFilesMenuItemSelected(SelectionEvent e) {
			removeSelectedItemsFromList();
		}

		@Override
		public void selectAllSourceFilesMenuItemSelected(SelectionEvent e) {
			sourceFilesList.selectAll();
		}

		@Override
		public void clearSourceFilesMenuItemSelected(SelectionEvent e) {
			removeAllItemsFromList();
		}

		@Override
		public void browseDirectoryButtonSelected(final SelectionEvent e) {
			final String dir = selectDestinationPath(shell);
			if (dir != null) {
				destinationDirectoryText.setText(dir);
			}
		}

		@Override
		public void processButtonSelected(final SelectionEvent e) {
			process(shell);
		}

		@Override
		public void closeButtonSelected(final SelectionEvent e) {
			shell.close();
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
		public void textModified(final ModifyEvent e) {
			updateProcessButtonStatus();
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
		 * @param shell the parent shell, needed to open the progress monitor
		 *        dialog
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

				final CsvToSqlRunnable runnable = new CsvToSqlRunnable(converter, sourceFilesList.getItems(), destinationDirectoryText.getText().trim());

				ProgressMonitorDialog.setDefaultImages(shell.getImages());
				final IRunnableContext dialog = new ProgressMonitorDialog(shell) {
					@Override
					protected void createCancelButton(final Composite parent) {
						super.createCancelButton(parent);
						cancel.setText(Messages.get("lbl.button.cancel")); // Improved localization
					}

					@Override
					protected void configureShell(final Shell shell) {
						super.configureShell(shell);
						shell.setText(Messages.get("lbl.csv2sql.progress.text")); // Improved localization
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
	}

}
