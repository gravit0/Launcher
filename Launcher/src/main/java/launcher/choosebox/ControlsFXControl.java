package launcher.choosebox;

import javafx.scene.control.Control;

abstract class ControlsFXControl extends Control {

    public ControlsFXControl() {
        
    }

    private String stylesheet;

    protected final String getUserAgentStylesheet(Class<?> clazz,
            String fileName) {

        if (stylesheet == null) {
            stylesheet = clazz.getResource(fileName).toExternalForm();
        }

        return stylesheet;
    }
}
