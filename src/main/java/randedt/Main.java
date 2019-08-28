package randedt;

import controllers.TextEditorController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TextEditor.fxml"));
        Parent root = loader.load();
        TextEditorController controller = loader.getController();

        String params;
        if (getParameters().getRaw() != null && getParameters().getRaw().size() == 1 &&
                (params = getParameters().getRaw().get(0)) != null) {
            controller.initController(params);
        }

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

    }
}
