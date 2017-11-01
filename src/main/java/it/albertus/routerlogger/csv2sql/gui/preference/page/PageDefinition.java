package it.albertus.routerlogger.csv2sql.gui.preference.page;

import org.eclipse.jface.resource.ImageDescriptor;

import it.albertus.jface.preference.page.BasePreferencePage;
import it.albertus.jface.preference.page.IPageDefinition;
import it.albertus.jface.preference.page.LoggingPreferencePage;
import it.albertus.jface.preference.page.PageDefinitionDetails;
import it.albertus.jface.preference.page.PageDefinitionDetails.PageDefinitionDetailsBuilder;
import it.albertus.routerlogger.csv2sql.resources.Messages;

public enum PageDefinition implements IPageDefinition {

	GENERAL(new PageDefinitionDetailsBuilder().pageClass(GeneralPreferencePage.class).build()),
	DEFAULTS,
	LOGGING(new PageDefinitionDetailsBuilder().pageClass(LoggingPreferencePage.class).build());

	private static final String LABEL_KEY_PREFIX = "lbl.preferences.";

	private final PageDefinitionDetails pageDefinitionDetails;

	PageDefinition() {
		this(new PageDefinitionDetailsBuilder().build());
	}

	PageDefinition(final PageDefinitionDetails pageDefinitionDetails) {
		this.pageDefinitionDetails = pageDefinitionDetails;
		if (pageDefinitionDetails.getNodeId() == null) {
			pageDefinitionDetails.setNodeId(name().toLowerCase().replace('_', '.'));
		}
		if (pageDefinitionDetails.getLabel() == null) {
			pageDefinitionDetails.setLabel(() -> Messages.get(LABEL_KEY_PREFIX + pageDefinitionDetails.getNodeId()));
		}
	}

	@Override
	public String getNodeId() {
		return pageDefinitionDetails.getNodeId();
	}

	@Override
	public String getLabel() {
		return pageDefinitionDetails.getLabel().get();
	}

	@Override
	public Class<? extends BasePreferencePage> getPageClass() {
		return pageDefinitionDetails.getPageClass();
	}

	@Override
	public IPageDefinition getParent() {
		return pageDefinitionDetails.getParent();
	}

	@Override
	public ImageDescriptor getImage() {
		return pageDefinitionDetails.getImage();
	}

}
