package it.albertus.routerlogger.csv2sql.gui;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

import it.albertus.util.logging.LoggerFactory;

public class Images {

	private static final Logger logger = LoggerFactory.getLogger(Images.class);

	// Icona principale dell'applicazione (in vari formati)
	private static final List<Image> mainIcons = new ArrayList<>();

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
			logger.log(Level.SEVERE, e.toString(), e);
		}
	}

	public static Image[] getMainIcons() {
		return Collections.unmodifiableList(mainIcons).toArray(new Image[mainIcons.size()]);
	}

}
