package it.albertus.routerlogger.csv2sql.gui.preference;

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
import it.albertus.jface.preference.field.ScaleIntegerFieldEditor;
import it.albertus.jface.preference.page.IPageDefinition;
import it.albertus.routerlogger.csv2sql.engine.CsvToSqlConfig;
import it.albertus.routerlogger.csv2sql.gui.preference.page.GeneralPreferencePage;
import it.albertus.routerlogger.csv2sql.gui.preference.page.LoggingPreferencePage;
import it.albertus.routerlogger.csv2sql.resources.Messages;
import it.albertus.util.LoggingConfig;

public enum Preference implements IPreference {

	LANGUAGE(new PreferenceDetailsBuilder(GENERAL).defaultValue(Messages.DEFAULT_LANGUAGE).build(), new FieldEditorDetailsBuilder(DefaultComboFieldEditor.class).labelsAndValues(GeneralPreferencePage.getLanguageComboOptions()).build()),

	LOGGING_LEVEL(new PreferenceDetailsBuilder(LOGGING).defaultValue(LoggingConfig.DEFAULT_LOGGING_LEVEL.getName()).build(), new FieldEditorDetailsBuilder(DefaultComboFieldEditor.class).labelsAndValues(LoggingPreferencePage.getLoggingComboOptions()).build()),
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

	public static IPreference forName(final String name) {
		if (name != null) {
			for (final IPreference preference : Preference.values()) {
				if (name.equals(preference.getName())) {
					return preference;
				}
			}
		}
		return null;
	}

}
