package PAL2.GUI.Loader

import PAL2.GUI.CoreController
import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.ProgressBar
import javafx.scene.image.Image
import javafx.scene.text.Text
import javafx.stage.Stage
import javafx.stage.StageStyle

/**
 *
 */
class Loader : Application()
{
    companion object
    {
        lateinit var stage: Stage
        lateinit var controller: LoaderController
    }

    override fun start(primaryStage: Stage?)
    {
        stage = Stage()
        val fxmlLoader = FXMLLoader()
        stage.initStyle(StageStyle.UNDECORATED)
        val root = fxmlLoader.load<Parent>(javaClass.getResource("/Loader.fxml").openStream())

        controller = fxmlLoader.getController<LoaderController>() as LoaderController

        stage.title = "PAL: Loader"
        stage.icons.add(Image(javaClass.getResource("/witch.png").toString()))
        val scene = Scene(root, 400.0, 80.0)
        scene.stylesheets.add("layout_settings.css")
        stage.scene = scene
        stage.show()
    }
}

class LoaderController
{
    @FXML
    lateinit var loggerText: Text

    @FXML
    lateinit var loadingProgress: ProgressBar

    fun updateProgressbar(d: Double)
    {
        Platform.runLater { loadingProgress.progress = d }
    }

    fun setText(str: String)
    {
        Platform.runLater { loggerText.text = str }
    }

    fun close()
    {
        Platform.runLater { Loader.stage.close() }
    }
}