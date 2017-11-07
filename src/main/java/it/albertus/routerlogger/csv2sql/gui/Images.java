package it.albertus.routerlogger.csv2sql.gui;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

public class Images {

	// Icona principale dell'applicazione (in vari formati)
	private static final Collection<Image> mainIcons = new LinkedHashSet<>();

	private Images() {
		throw new IllegalAccessError();
	}

	static {
		try (final InputStream is = Images.class.getResourceAsStream("main.ico")) {
			final ImageData[] images = new ImageLoader().load(is);
			for (final ImageData id : images) {
				mainIcons.add(new Image(Display.getCurrent(), id));
			}
		}
		catch (final IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public static Image[] getMainIcons() {
		return mainIcons.toArray(new Image[mainIcons.size()]);
	}

}
