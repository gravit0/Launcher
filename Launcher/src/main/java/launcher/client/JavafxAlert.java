package launcher.client;
import javafx.scene.control.Alert;

public class JavafxAlert {
    public static boolean info(String message,String title)
    {
        try {
            Class.forName("javafx.scene.control.Alert");
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            return true;
        } catch (ClassNotFoundException e)
        {
            return false;
        }

    }
}
