package GUI

import PAL2.GUI.CoreApplication
import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.ListView
import javafx.scene.control.ProgressBar
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.stage.Stage
import javafx.stage.StageStyle
import mu.KotlinLogging
import java.net.URL
import java.util.*

private val logger = KotlinLogging.logger {}
/**
 *
 */
class DownloadsAnchor(val text: String)
{
    init
    {
        createAnchor()
        createImageView()
        createProgressBar()
        createAddonText()
        createSideImages()
    }

    private fun createSideImages()
    {
        upArrowImg = ImageView()
        hideImg = ImageView()
        downArrowImg = ImageView()

        anchorPane.children.addAll(upArrowImg, hideImg, downArrowImg)

        upArrowImg.layoutY = 2.5
        upArrowImg.layoutX = 2.0
        upArrowImg.fitHeight = 15.0
        upArrowImg.fitWidth = 15.0

        downArrowImg.layoutY = 37.5
        downArrowImg.layoutX = 2.0
        downArrowImg.fitHeight = 15.0
        downArrowImg.fitWidth = 15.0

        hideImg.layoutY = 20.0
        hideImg.layoutX = 2.0
        hideImg.fitHeight = 15.0
        hideImg.fitWidth = 15.0

        upArrowImg.image = Image(javaClass.getResource("/icons/upArrow.png").openStream())
        downArrowImg.image = Image(javaClass.getResource("/icons/downArrow.png").openStream())
        hideImg.image = Image(javaClass.getResource("/icons/minus.png").openStream())
    }

    lateinit var anchorPane: AnchorPane
    lateinit var imageView: ImageView
    lateinit var progressBar: ProgressBar
    lateinit var addonText: Text
    lateinit var upArrowImg: ImageView
    lateinit var downArrowImg: ImageView
    lateinit var hideImg: ImageView
    val template = "%n | %p"
    val font = Font("System Bold", 14.0)

    fun createAnchor()
    {
        anchorPane = AnchorPane()
    }

    fun createAddonText()
    {
        addonText = Text()
        addonText.style = "-fx-font-size: 14; -fx-font-weight: Bold;"
        anchorPane.children.add(addonText)
        addonText.text = text
        addonText.layoutX = 75.0
        addonText.layoutY = 22.0
        addonText.wrappingWidth = 520.0
        addonText.fill = Color.WHITE
    }

    fun createProgressBar()
    {
        progressBar = ProgressBar()
        anchorPane.children.add(progressBar)
        progressBar.layoutX = 75.0
        progressBar.layoutY = 32.0
        progressBar.prefWidth = 500.0
        progressBar.prefHeight = 18.0
    }

    fun createImageView()
    {
        imageView = ImageView()
        anchorPane.children.add(imageView)
        imageView.fitWidth = 50.0
        imageView.fitHeight = 50.0
        imageView.layoutX = 20.0
        imageView.layoutY = 2.5
    }

    fun setProgress(d: Double)
    {
        Platform.runLater { progressBar.progress = d }
    }

    fun addDownloadText(str: String)
    {
        Platform.runLater { addonText.text = template.replace("%n", text).replace("%p", str) }
    }

    fun setImg(image: Image)
    {
        Platform.runLater { imageView.image = image }
    }

    fun attachToListView()
    {
        CoreApplication.controller.addActiveDownload(this)
    }

    fun setName(str: String)
    {
        Platform.runLater { addonText.text = str }
    }
}

class DownloadTemplateController: Initializable
{
    override fun initialize(location: URL?, resources: ResourceBundle?)
    {

    }

    fun updateProgressbar(d: Double)
    {
        Platform.runLater { progressBarDownload.progress = d }
    }

    @FXML
    private lateinit var anchor: AnchorPane

    @FXML
    private lateinit var addonIcon: ImageView

    @FXML
    private lateinit var addonName: Text

    @FXML
    private lateinit var progressBarDownload: ProgressBar
}

class AnchorTest : Application()
{
    companion object
    {
        lateinit var stage: Stage
        lateinit var controller: DownloadTemplateController
        lateinit var listView: ListView<Parent>
    }
    override fun start(primaryStage: Stage?)
    {
        val fxmlLoader = FXMLLoader()
        controller = DownloadTemplateController()

        /*
        val arrayList = ArrayList<Parent>()
        fxmlLoader.setController(controller)
        for (c in 0..4)
        {
            arrayList.add(fxmlLoader.load<Parent>(javaClass.getResource("/downloadTemplate.fxml").openStream()))
        }*/

        var anchorPane = AnchorPane()
        listView = ListView<Parent>()

        anchorPane.children.add(listView)
        listView.prefWidth =  600.0

        stage = Stage()
        stage.initStyle(StageStyle.UNDECORATED)
        stage.icons.add(Image(javaClass.getResource("/witch.png").toString()))
        val scene = Scene(anchorPane, 600.0, 500.0)
        scene.stylesheets.add("layout_settings.css")
        stage.initStyle(StageStyle.TRANSPARENT)
        scene.fill = Color.TRANSPARENT
        stage.scene = scene
        stage.isAlwaysOnTop = true
        stage.x = 0.0
        stage.show()
    }

}