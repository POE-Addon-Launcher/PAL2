package GUI.PopUp;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class Updated_HTML_Popup_Controller
        implements Initializable
{
    @FXML
    private ImageView topbar_image_closeWindow;

    @FXML
    private WebView HTML_Viewer;


    private double xOffset = 0;
    private double yOffset = 0;

    public void onMouseDragged(MouseEvent mouseEvent)
    {
        Updated_HTML_Popup.stage.setX(mouseEvent.getScreenX() + xOffset);
        Updated_HTML_Popup.stage.setY(mouseEvent.getScreenY() + yOffset);
    }
    public void onMousePressed(MouseEvent mouseEvent)
    {
        xOffset = Updated_HTML_Popup.stage.getX() - mouseEvent.getScreenX();
        yOffset = Updated_HTML_Popup.stage.getY() - mouseEvent.getScreenY();
    }

    public void topbar_closeWIndow_onMouseClicked()
    {
        Updated_HTML_Popup.stage.close();
    }

    public void topbar_closeWindow_onMouseEntered()
    {
        changeImage(topbar_image_closeWindow, "/icons/cancel.png");
    }

    public void topbar_closeWindow_onMouseExited()
    {
        changeImage(topbar_image_closeWindow, "/icons/cancel0.png");
    }

    private void changeImage(ImageView imageView, String s)
    {
        Platform.runLater(() -> imageView.setImage(new Image(getClass().getResource(s).toString())));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        try
        {
            String folder = System.getenv("LOCALAPPDATA");

            File f = new File(folder + File.separator + "PAL" + File.separator + "update_notes.html");

            if (f.exists())
                f.delete();

            InputStream inputStream = new URL("https://raw.githubusercontent.com/POE-Addon-Launcher/Core/master/src/main/resources/update_notes.html").openStream();
            Files.copy(inputStream, Paths.get(f.getPath() ));
            Platform.runLater(() ->
            {
                try
                {
                    HTML_Viewer.getEngine().load(f.toURI().toURL().toString());
                }
                catch (MalformedURLException e)
                {
                    e.printStackTrace();
                }
            });

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
