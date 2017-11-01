package it.albertus.routerlogger.csv2sql.gui.preference.page;

import java.util.Locale;

import it.albertus.jface.preference.LocalizedLabelsAndValues;
import it.albertus.jface.preference.page.BasePreferencePage;
import it.albertus.routerlogger.csv2sql.resources.Messages;
import it.albertus.routerlogger.csv2sql.resources.Messages.Language;

public class GeneralPreferencePage extends BasePreferencePage {

	public static LocalizedLabelsAndValues getLanguageComboOptions() {
		final Language[] values = Messages.Language.values();
		final LocalizedLabelsAndValues options = new LocalizedLabelsAndValues(values.length);
		for (final Language language : values) {
			final Locale locale = language.getLocale();
			final String value = locale.getLanguage();
			options.add(() -> locale.getDisplayLanguage(locale), value);
		}
		return options;
	}

}
