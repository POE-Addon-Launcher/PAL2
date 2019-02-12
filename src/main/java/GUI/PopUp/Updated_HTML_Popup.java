package GUI.PopUp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 */
public class Updated_HTML_Popup
        extends Application
{
    public static Stage stage;

    public void start(Stage primaryStage) throws Exception
    {
        primaryStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader();
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setAlwaysOnTop(true);
        Parent root = fxmlLoader.load(getClass().getResource("/update_notes.fxml"));
        primaryStage.setTitle("PAL: NEW!");
        primaryStage.getIcons().add(new Image(getClass().getResource("/witch.png").toString()));
        Scene scene = new Scene(root, 499, 300);
        primaryStage.setScene(scene);
        stage = primaryStage;
        primaryStage.show();
    }

    public void activate(String[] args)
    {
        launch(args);
    }
}
