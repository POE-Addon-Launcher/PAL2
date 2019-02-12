package PAL2.GUI

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import javafx.stage.StageStyle

/**
 *
 */
class CoreApplication : Application()
{
    companion object
    {
        lateinit var stage: Stage
        lateinit var controller: CoreController
    }

    override fun start(primaryStage: Stage?)
    {
        stage = Stage()
        val fxmlLoader = FXMLLoader()
        stage.initStyle(StageStyle.UNDECORATED)
        val root = fxmlLoader.load<Parent>(javaClass.getResource("/CoreUI.fxml").openStream())

        controller = fxmlLoader.getController<CoreController>() as CoreController

        stage.title = "PAL: Core"
        stage.icons.add(Image(javaClass.getResource("/witch.png").toString()))
        val scene = Scene(root, 600.0, 500.0)
        scene.stylesheets.add("layout_settings.css")
        stage.scene = scene
        stage.show()
    }
}