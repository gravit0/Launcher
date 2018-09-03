package launcher.choosebox;

import javafx.scene.control.Control;
import launcher.LauncherAPI;

abstract class ControlsFXControl extends Control {

	private String stylesheet;

	public ControlsFXControl() {

	}

	@LauncherAPI
	protected final String getUserAgentStylesheet(Class<?> clazz, String fileName) {

		if (stylesheet == null)
			stylesheet = clazz.getResource(fileName).toExternalForm();

		return stylesheet;
	}
}
