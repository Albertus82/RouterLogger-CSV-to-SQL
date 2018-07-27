package it.albertus.routerlogger.csv2sql.gui.preference;

import static it.albertus.routerlogger.csv2sql.gui.preference.page.PageDefinition.DEFAULTS;
import static it.albertus.routerlogger.csv2sql.gui.preference.page.PageDefinition.GENERAL;
import static it.albertus.routerlogger.csv2sql.gui.preference.page.PageDefinition.LOGGING;

import java.util.Arrays;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.widgets.Composite;

import it.albertus.jface.preference.FieldEditorDetails;
import it.albertus.jface.preference.FieldEditorDetails.FieldEditorDetailsBuilder;
import it.albertus.jface.preference.FieldEditorFactory;
import it.albertus.jface.preference.IPreference;
import it.albertus.jface.preference.PreferenceDetails;
import it.albertus.jface.preference.PreferenceDetails.PreferenceDetailsBuilder;
import it.albertus.jface.preference.field.DefaultBooleanFieldEditor;
import it.albertus.jface.preference.field.DefaultComboFieldEditor;
import it.albertus.jface.preference.field.EnhancedDirectoryFieldEditor;
import it.albertus.jface.preference.field.EnhancedIntegerFieldEditor;
import it.albertus.jface.preference.field.EnhancedStringFieldEditor;
import it.albertus.jface.preference.field.ScaleIntegerFieldEditor;
import it.albertus.jface.preference.page.IPageDefinition;
import it.albertus.jface.preference.page.LoggingPreferencePage;
import it.albertus.routerlogger.csv2sql.engine.CsvToSqlConfig;
import it.albertus.routerlogger.csv2sql.gui.CsvToSqlShellContent;
import it.albertus.routerlogger.csv2sql.gui.preference.page.GeneralPreferencePage;
import it.albertus.routerlogger.csv2sql.resources.Messages;
import it.albertus.util.config.LoggingConfig;

public enum Preference implements IPreference {

	LANGUAGE(new PreferenceDetailsBuilder(GENERAL).defaultValue(Messages.DEFAULT_LANGUAGE).build(), new FieldEditorDetailsBuilder(DefaultComboFieldEditor.class).labelsAndValues(GeneralPreferencePage.getLanguageComboOptions()).build()),

	CSV_FIELD_SEPARATOR(new PreferenceDetailsBuilder(DEFAULTS).defaultValue(CsvToSqlShellContent.Defaults.CSV_FIELD_SEPARATOR).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).emptyStringAllowed(true).textLimit(Byte.MAX_VALUE).build()),
	CSV_DATE_PATTERN(new PreferenceDetailsBuilder(DEFAULTS).defaultValue(CsvToSqlShellContent.Defaults.CSV_DATE_PATTERN).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).emptyStringAllowed(true).textLimit(Byte.MAX_VALUE).build()),
	CSV_RESPONSE_TIME(new PreferenceDetailsBuilder(DEFAULTS).defaultValue(CsvToSqlShellContent.Defaults.CSV_RESPONSE_TIME).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),

	DATABASE_DIRECTORY(new PreferenceDetailsBuilder(DEFAULTS).separate().build(), new FieldEditorDetailsBuilder(EnhancedDirectoryFieldEditor.class).emptyStringAllowed(true).directoryDialogMessage(() -> Messages.get("lbl.preferences.database.directory")).build()),
	DATABASE_TABLE_NAME(new PreferenceDetailsBuilder(DEFAULTS).defaultValue(CsvToSqlShellContent.Defaults.DATABASE_TABLE_NAME).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).emptyStringAllowed(true).build()),
	DATABASE_COLUMN_NAME_PREFIX(new PreferenceDetailsBuilder(DEFAULTS).defaultValue(CsvToSqlShellContent.Defaults.DATABASE_COLUMN_NAME_PREFIX).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).emptyStringAllowed(true).textLimit(Byte.MAX_VALUE).build()),
	DATABASE_COLUMN_NAME_MAX_LENGTH(new PreferenceDetailsBuilder(DEFAULTS).defaultValue(CsvToSqlShellContent.Defaults.DATABASE_COLUMN_NAME_MAX_LENGTH).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).emptyStringAllowed(false).textLimit(2).numberMinimum(CsvToSqlShellContent.DATABASE_COLUMN_NAME_MIN_LENGTH).build()),

	LOGGING_LEVEL(new PreferenceDetailsBuilder(LOGGING).defaultValue(LoggingConfig.DEFAULT_LOGGING_LEVEL.getName()).build(), new FieldEditorDetailsBuilder(DefaultComboFieldEditor.class).labelsAndValues(LoggingPreferencePage.getLoggingLevelComboOptions()).build()),
	LOGGING_FILES_ENABLED(new PreferenceDetailsBuilder(LOGGING).separate().defaultValue(LoggingConfig.DEFAULT_LOGGING_FILES_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	LOGGING_FILES_PATH(new PreferenceDetailsBuilder(LOGGING).parent(LOGGING_FILES_ENABLED).defaultValue(CsvToSqlConfig.DEFAULT_LOGGING_FILES_PATH).build(), new FieldEditorDetailsBuilder(EnhancedDirectoryFieldEditor.class).emptyStringAllowed(false).directoryMustExist(false).directoryDialogMessage(() -> Messages.get("msg.preferences.directory.dialog.message.log")).build()),
	LOGGING_FILES_LIMIT(new PreferenceDetailsBuilder(LOGGING).parent(LOGGING_FILES_ENABLED).defaultValue(LoggingConfig.DEFAULT_LOGGING_FILES_LIMIT).build(), new FieldEditorDetailsBuilder(ScaleIntegerFieldEditor.class).scaleMinimum(512).scaleMaximum(8192).scalePageIncrement(512).build()),
	LOGGING_FILES_COUNT(new PreferenceDetailsBuilder(LOGGING).parent(LOGGING_FILES_ENABLED).defaultValue(LoggingConfig.DEFAULT_LOGGING_FILES_COUNT).build(), new FieldEditorDetailsBuilder(ScaleIntegerFieldEditor.class).scaleMinimum(1).scaleMaximum(9).scalePageIncrement(1).build());

	private static final String LABEL_KEY_PREFIX = "lbl.preferences.";

	private static final FieldEditorFactory fieldEditorFactory = new FieldEditorFactory();

	private final PreferenceDetails preferenceDetails;
	private final FieldEditorDetails fieldEditorDetails;

	Preference(final PreferenceDetails preferenceDetails, final FieldEditorDetails fieldEditorDetails) {
		this.preferenceDetails = preferenceDetails;
		this.fieldEditorDetails = fieldEditorDetails;
		if (preferenceDetails.getName() == null) {
			preferenceDetails.setName(name().toLowerCase().replace('_', '.'));
		}
		if (preferenceDetails.getLabel() == null) {
			preferenceDetails.setLabel(() -> Messages.get(LABEL_KEY_PREFIX + preferenceDetails.getName()));
		}
	}

	@Override
	public String getName() {
		return preferenceDetails.getName();
	}

	@Override
	public String getLabel() {
		return preferenceDetails.getLabel().get();
	}

	@Override
	public IPageDefinition getPageDefinition() {
		return preferenceDetails.getPageDefinition();
	}

	@Override
	public String getDefaultValue() {
		return preferenceDetails.getDefaultValue();
	}

	@Override
	public IPreference getParent() {
		return preferenceDetails.getParent();
	}

	@Override
	public boolean isRestartRequired() {
		return preferenceDetails.isRestartRequired();
	}

	@Override
	public boolean isSeparate() {
		return preferenceDetails.isSeparate();
	}

	@Override
	public Preference[] getChildren() {
		return Arrays.stream(values()).parallel().filter(item -> equals(item.getParent())).toArray(Preference[]::new);
	}

	@Override
	public FieldEditor createFieldEditor(final Composite parent) {
		return fieldEditorFactory.createFieldEditor(getName(), getLabel(), parent, fieldEditorDetails);
	}

}
