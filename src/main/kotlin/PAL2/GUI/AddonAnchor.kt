package PAL2.GUI

import PAL_DataClasses.PAL_AddonFullData
import PAL2.SystemHandling.FileDownloader
import PAL2.SystemHandling.launchAddon
import SystemHandling.checkForUseableDownloads
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.io.File
import java.net.URL

/**
 *
 */
private val logger = KotlinLogging.logger {}

class AddonAnchor(var name: String, var version: String, var repo: String, var iconUrl: String, var addonID: Int)
{
    lateinit var anchorPane: AnchorPane
    lateinit var anchorButtonRight: AnchorPane
    lateinit var anchorButtonLeft: AnchorPane
    lateinit var icon: ImageView
    lateinit var versionText: Text
    lateinit var addonText: Text
    var rectButtonRight = Rectangle(75.0, 25.0)
    var buttonRightText = Text()
    var buttonLeftText = Text()
    var rectButtonLeft = Rectangle(75.0, 25.0)

    lateinit var repoText: Text

    var activeIcon = ""
    var isWebIcon = true


    init
    {
        createAnchor()
        initImage()
        initCreatorText()
        initAddonText()
        initVersionText()
        initButton("Download", anchorButtonLeft, buttonLeftText, rectButtonLeft)
        initButton("Info", anchorButtonRight, buttonRightText, rectButtonRight)

        // Attach Listeners
        setListners()


        rectButtonLeft.id = "buttonRect"
        buttonLeftText.id = "buttonText"
    }

    private fun initButton(arg: String, anchor: AnchorPane, text: Text, rect: Rectangle)
    {
        text.text = arg
        anchor.children.add(text)
        text.textAlignment = TextAlignment.CENTER
        text.layoutX = 0.0
        text.layoutY = 30.0
        text.wrappingWidth = 75.0
        text.fill = Color.WHITE

        anchor.children.add(rect)
        rect.layoutX = 0.0
        rect.layoutY = 12.5
        rect.stroke = Color.WHITE
        rect.fill = Color(1.0, 1.0, 1.0, 0.0)
    }

    private fun initVersionText()
    {
        versionText = Text()
        versionText.text = version
        anchorPane.children.add(versionText)
        versionText.wrappingWidth = 200.0
        versionText.fill = Color.WHITE
        versionText.layoutY = 38.5
        versionText.layoutX = 50.0
    }

    private fun initAddonText()
    {
        addonText = Text()
        addonText.text = name
        anchorPane.children.add(addonText)
        addonText.wrappingWidth = 200.0
        addonText.layoutX = 50.0
        addonText.layoutY = 22.5
        addonText.fill = Color.WHITE
        addonText.style = "-fx-font-weight: Bold"
    }

    private fun initCreatorText()
    {
        repoText = Text()
        repoText.text = repo
        anchorPane.children.add(repoText)
        repoText.wrappingWidth = 175.0
        repoText.layoutX = 325.0
        repoText.layoutY = 22.5
        repoText.textAlignment = TextAlignment.CENTER
        repoText.fill = Color.WHITE
    }

    private fun initImage()
    {
        icon = ImageView()
        anchorPane.children.add(icon)
        icon.fitWidth = 40.0
        icon.fitHeight = 40.0
        icon.layoutX = 4.0
        icon.layoutY = 7.5


        if (iconUrl == "")
        {
            setImage(icon, "/icons/NoIcon.png")
            isWebIcon = false
        }
        else
        {
            setURLImage(icon, iconUrl)
            isWebIcon = true
        }
    }

    private fun createAnchor()
    {
        anchorPane = AnchorPane()
        anchorPane.id = "$addonID"
        anchorPane.prefWidth = 575.0
        anchorButtonRight = AnchorPane()
        anchorButtonRight.prefWidth = 75.0
        anchorButtonRight.prefHeight = 50.0
        anchorButtonRight.layoutX = 500.0
        anchorButtonRight.layoutY = 0.0

        anchorButtonLeft = AnchorPane()
        anchorButtonLeft.prefHeight = 50.0
        anchorButtonLeft.prefWidth = 75.0
        anchorButtonLeft.layoutX = 250.0
        anchorButtonLeft.layoutY = 0.0

        anchorPane.children.add(anchorButtonLeft)
        anchorPane.children.add(anchorButtonRight)
    }

    private fun setImage(imageView: ImageView, s: String)
    {
        activeIcon = s
        imageView.image = Image(javaClass.getResource(s).toString())
    }

    private fun setURLImage(imageView: ImageView, s: String)
    {
        imageView.image = Image(s)
    }

    private fun setListners()
    {
        leftButtonListners()
        rightButtonListners()
    }



    fun leftButtonListners()
    {
        rectButtonLeft.onMouseClicked = EventHandler()
        {
            if(it.button == MouseButton.PRIMARY)
            {
                val a = GlobalData.getAddonByID(addonID)
                if (a != null)
                {
                    val download_urls = checkForUseableDownloads(a.download_urls, addonID)
                    if (download_urls.size == 1)
                    {
                        CoreApplication.controller.startDownload(download_urls[0], a.aid, icon.image)
                    }
                    else
                    {
                        var desc = "No description has been set, sorry."

                        if (a.description != null)
                        {
                            if (a.description is String)
                            {
                                desc = a.description!!
                            }

                        }

                        CoreApplication.controller.setDescInfo(icon.image, name, desc, addonID)
                        CoreApplication.controller.showDownloadsPage(download_urls)
                    }
                }
            }
        }

        rectButtonLeft.onMouseEntered = EventHandler()
        {
            rectButtonLeft.stroke = Color(1.0, 0.0, 1.0, 1.0)
        }

        rectButtonLeft.onMouseExited = EventHandler()
        {
            rectButtonLeft.stroke = Color.WHITE
        }
    }

    fun rightButtonListners()
    {
        rectButtonRight.onMouseClicked = EventHandler()
        {
            if (it.button == MouseButton.PRIMARY)
            {
                val a = GlobalData.getAddonByID(addonID)
                var desc = "No description has been set, sorry."
                if (a is PAL_AddonFullData)
                {
                    if (a.description != null)
                    {
                        if (a.description is String)
                        {
                            desc = a.description!!
                        }

                    }
                }
                CoreApplication.controller.setDescInfo(icon.image, name, desc, addonID)
            }
        }

        rectButtonRight.onMouseEntered = EventHandler()
        {
            rectButtonRight.stroke = Color(1.0, 0.0, 1.0, 1.0)
        }

        rectButtonRight.onMouseExited = EventHandler()
        {
            rectButtonRight.stroke = Color.WHITE
        }
    }
}